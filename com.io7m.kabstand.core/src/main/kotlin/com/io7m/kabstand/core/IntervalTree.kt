/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.kabstand.core

import com.io7m.kabstand.core.IntervalTree.BalanceFactor.*
import com.io7m.kabstand.core.IntervalTreeChangeType.Deleted
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.max

/**
 * An interval tree. The tree is an AVL tree storing intervals and the maximum
 * upper bounds that contain their subtrees.
 *
 * @param <S> The type of scalar values in intervals
 */

class IntervalTree<S : Comparable<S>> private constructor(
  private var root : Node<S>?,
  private var listener : (IntervalTreeChangeType<S>) -> Unit,
  private var validation : Boolean
) : IntervalTreeDebuggableType<S> {

  private class Node<S : Comparable<S>>(
    var interval : IntervalType<S>,
    var left : Node<S>?,
    var parent : Node<S>?,
    var right : Node<S>?,
    var maximum : IntervalType<S>,
    var height : Int
  ) {

    fun updateMaximum() : IntervalType<S> {
      var newMaximum = this.maximum
      if (this.left != null) {
        newMaximum = this.maximum.upperMaximum(this.left!!.updateMaximum())
      }
      if (this.right != null) {
        newMaximum = this.maximum.upperMaximum(this.right!!.updateMaximum())
      }
      this.maximum = newMaximum
      return newMaximum
    }

    fun leftHeight() : Int {
      return this.left?.height ?: 0
    }

    fun rightHeight() : Int {
      return this.right?.height ?: 0
    }

    /**
     * Determine the balance factor of the given node.
     *
     * @return A balance factor
     */

    fun balanceFactor() : BalanceFactor {
      val delta : Int = this.balanceFactorRaw()
      if (delta > 1) {
        return LEFT_HEAVY
      }
      if (delta > 0) {
        return BALANCED_LEANING_LEFT
      }
      if (delta < -1) {
        return RIGHT_HEAVY
      }
      return if (delta < 0) {
        BALANCED_LEANING_RIGHT
      } else BALANCED
    }

    fun balanceFactorRaw() : Int {
      val heightL = this.leftHeight()
      val heightR = this.rightHeight()
      return heightL - heightR
    }

    fun updateHeight() {
      this.height = max(this.leftHeight(), this.rightHeight()) + 1
    }

    fun takeOwnershipLeft(node : Node<S>?) {
      this.left = node
      node?.setNewParent(this)
      this.checkInvariants()
    }

    fun checkInvariants() {
      val leftST = this.left
      val rightST = this.right

      check(this != leftST) { "A node must not be equal to its own left child." }
      check(this != rightST) { "A node must not be equal to its own right child." }
      check(this != this.parent) { "A node must not be equal to its own parent." }

      if (leftST != null) {
        val cmp = leftST.interval.compare(this.interval)
        check(cmp == IntervalComparison.LESS_THAN) {
          "Left value node ${leftST.interval} must be < current node value ${this.interval} but is $cmp"
        }
      }
      if (rightST != null) {
        val cmp = rightST.interval.compare(this.interval)
        check(cmp == IntervalComparison.MORE_THAN) {
          "Right value node ${rightST.interval} must be > current node value ${this.interval} but is $cmp"
        }
      }
    }

    fun setNewParent(node : Node<S>?) {
      check(node != this) { "A node's parent must not be equal to the node." }
      if (node != null) {
        check(node != this.left) { "A node's parent must not be equal to its own left child." }
        check(node != this.right) { "A node's parent must not be equal to its own right child." }
      }
      this.parent = node
      this.checkInvariants()
    }

    fun takeOwnershipRight(node : Node<S>?) {
      this.right = node
      node?.setNewParent(this)
      this.checkInvariants()
    }
  }

  private fun publish(change : IntervalTreeChangeType<S>) {
    try {
      this.listener(change)
    } catch (e : Throwable) {
      // Nothing we can do about it.
    }
  }

  private fun sizeTraverse(node : Node<S>?) : Int {
    if (node == null) {
      return 0
    }
    return 1 + this.sizeTraverse(node.left) + this.sizeTraverse(node.right)
  }

  private fun balance(current : Node<S>) : Node<S> {
    return when (current.balanceFactor()) {
      BALANCED,
      BALANCED_LEANING_LEFT,
      BALANCED_LEANING_RIGHT -> {
        current
      }

      LEFT_HEAVY             -> {
        when (current.left!!.balanceFactor()) {
          RIGHT_HEAVY,
          BALANCED_LEANING_RIGHT -> {
            this.publish(IntervalTreeChangeType.Balanced("RL", current.interval))
            this.rotateRL(current)
          }

          LEFT_HEAVY,
          BALANCED,
          BALANCED_LEANING_LEFT  -> {
            this.publish(IntervalTreeChangeType.Balanced("RR", current.interval))
            this.rotateRR(current)
          }
        }
      }

      RIGHT_HEAVY            -> {
        when (current.right!!.balanceFactor()) {
          LEFT_HEAVY,
          BALANCED_LEANING_LEFT  -> {
            this.publish(IntervalTreeChangeType.Balanced("LR", current.interval))
            this.rotateLR(current)
          }

          RIGHT_HEAVY,
          BALANCED,
          BALANCED_LEANING_RIGHT -> {
            this.publish(IntervalTreeChangeType.Balanced("LL", current.interval))
            this.rotateLL(current)
          }
        }
      }
    }
  }

  /**
   * Perform an RR ("Single Right") rotation.
   *
   * @param c The current node
   *
   * @return The new root node of the subtree
   */

  private fun rotateRR(c : Node<S>) : Node<S> {
    // B becomes the new root of the subtree.
    val b : Node<S> = c.left!!
    val oldParent : Node<S>? = c.parent

    // C takes ownership of B's right child as its own left child.
    c.takeOwnershipLeft(b.right)

    // B takes ownership of C as its right child.
    b.takeOwnershipRight(c)

    // B's parent is now what C's _used_ to be.
    b.setNewParent(oldParent)
    c.updateHeight()
    b.updateHeight()
    return b
  }

  /**
   * Perform an LL ("Single Left") rotation.
   *
   * @param a The current node
   *
   * @return The new root node of the subtree
   */

  private fun rotateLL(a : Node<S>) : Node<S> {
    // B becomes the new root of the subtree.
    val b : Node<S> = a.right!!
    val oldParent : Node<S>? = a.parent

    // A takes ownership of B's left child as its own right child.
    a.takeOwnershipRight(b.left)

    // B takes ownership of A as its own left child.
    b.takeOwnershipLeft(a)

    // B's parent is now what A's _used_ to be.
    b.setNewParent(oldParent)
    a.updateHeight()
    b.updateHeight()
    return b
  }

  /**
   * Perform an RL ("Double Right") rotation. Yes, this naming scheme is
   * utterly misleading.
   *
   * @param current The current node
   *
   * @return The new root node of the subtree
   */

  private fun rotateRL(current : Node<S>) : Node<S> {
    current.takeOwnershipLeft(this.rotateLL(current.left!!))
    check(current.balanceFactor() == LEFT_HEAVY) {
      "Node must be LEFT_HEAVY after rotation."
    }
    return this.rotateRR(current)
  }

  /**
   * Perform an LR ("Double Left") rotation. Yes, this naming scheme is
   * utterly misleading.
   *
   * @param current The current node
   *
   * @return The new root node of the subtree
   */

  private fun rotateLR(current : Node<S>) : Node<S> {
    current.takeOwnershipRight(this.rotateRR(current.right!!))
    check(current.balanceFactor() == RIGHT_HEAVY) {
      "Node must be RIGHT_HEAVY after rotation."
    }
    return this.rotateLL(current)
  }

  private fun validateAt(current : Node<S>?) {
    if (current == null) {
      return
    }

    current.checkInvariants()

    check(current.balanceFactor().isBalanced)
    { "Balance factor of node $current is ${current.balanceFactor()}" }

    this.validateAt(current.left)
    this.validateAt(current.right)
  }

  private fun validate() {
    if (this.validation) {
      this.validateAt(this.root)
    }
  }

  @Throws(DuplicateIntervalException::class)
  private fun create(
    parent : Node<S>?,
    current : Node<S>?,
    interval : IntervalType<S>
  ) : Node<S> {

    /*
     * If the current node is null, we're creating a leaf of some kind.
     */

    if (current == null) {
      this.publish(IntervalTreeChangeType.Created(interval))
      val newNode : Node<S> = Node(
        interval = interval,
        left = null,
        parent = null,
        right = null,
        maximum = interval,
        height = 0
      )
      newNode.setNewParent(parent)
      newNode.updateMaximum()
      return newNode
    }

    when (interval.compare(current.interval)) {
      IntervalComparison.EQUAL     -> {
        throw DuplicateIntervalException()
      }

      IntervalComparison.LESS_THAN -> {
        current.takeOwnershipLeft(this.create(current, current.left, interval))
      }

      IntervalComparison.MORE_THAN -> {
        current.takeOwnershipRight(this.create(current, current.right, interval))
      }
    }

    current.updateMaximum()
    current.updateHeight()
    return this.balance(current)
  }

  companion object {

    @JvmStatic
    fun <S : Comparable<S>> empty() : IntervalTreeDebuggableType<S> {
      return IntervalTree(
        root = null,
        listener = { },
        validation = false
      )
    }

  }

  override fun setChangeListener(
    listener : (IntervalTreeChangeType<S>) -> Unit
  ) {
    this.listener = listener
  }

  override fun insert(value : IntervalType<S>) : Boolean {
    try {
      this.root = this.create(null, this.root, value)
    } catch (e : DuplicateIntervalException) {
      return false
    }

    this.validate()
    return true
  }

  override fun remove(value : IntervalType<S>) : Boolean {
    try {
      this.root = this.removeAt(this.root, value)
    } catch (e : NonexistentIntervalException) {
      return false
    }

    this.validate()
    return true
  }

  @Throws(NonexistentIntervalException::class)
  private fun removeAt(
    current : Node<S>?,
    interval : IntervalType<S>
  ) : Node<S>? {
    if (current == null) {
      throw NonexistentIntervalException()
    }

    when (interval.compare(current.interval)) {
      IntervalComparison.EQUAL     -> {

        /*
         * If the current node has no children, then it is replaced with
         * nothing.
         */

        if (current.left == null && current.right == null) {
          this.publish(Deleted("Leaf", interval))
          return null
        }

        /*
         * If the current node has only a single child, then the node is
         * replaced by its own child.
         */

        if ((current.left != null) xor (current.right != null)) {
          return if (current.left != null) {
            this.publish(Deleted("SingleParentL", interval))
            current.left
          } else {
            this.publish(Deleted("SingleParentR", interval))
            current.right
          }
        }

        /*
         * The current node must have two children. The current node is
         * effectively replaced by the successor (the node with the smallest
         * value greater than the current node). This is handled by simply
         * setting the value of the current node to that of the successor,
         * and then removing the original successor from the right subtree.
         */

        val successor : Node<S> = this.findMinimum(current.right!!)
        current.interval = successor.interval
        current.takeOwnershipRight(
          this.removeAt(current.right, successor.interval)
        )
        current.updateMaximum()
        current.updateHeight()
        this.publish(Deleted("Branch", interval))
        return this.balance(current)
      }

      IntervalComparison.LESS_THAN -> {
        current.takeOwnershipLeft(this.removeAt(current.left, interval))
        current.updateMaximum()
        current.updateHeight()
        return this.balance(current)
      }

      IntervalComparison.MORE_THAN -> {
        current.takeOwnershipRight(this.removeAt(current.right, interval))
        current.updateMaximum()
        current.updateHeight()
        return this.balance(current)
      }
    }
  }

  private fun findMinimum(current : Node<S>) : Node<S> {
    return if (current.left != null) {
      this.findMinimum(current.left!!)
    } else {
      current
    }
  }

  override fun find(value : IntervalType<S>) : Boolean {
    return this.findAt(this.root, value)
  }

  private fun findAt(
    current : Node<S>?,
    interval : IntervalType<S>
  ) : Boolean {
    return if (current == null) {
      false
    } else when (interval.compare(current.interval)) {
      IntervalComparison.EQUAL     -> {
        true
      }

      IntervalComparison.LESS_THAN -> {
        this.findAt(current.left, interval)
      }

      IntervalComparison.MORE_THAN -> {
        this.findAt(current.right, interval)
      }
    }
  }

  override fun enableInternalValidation(enabled : Boolean) {
    this.validation = enabled
  }

  override fun clear() {
    this.publish(IntervalTreeChangeType.Cleared())
    this.root = null
  }

  override fun minimum() : IntervalType<S>? {
    return this.minimumAt(this.root)
  }

  private fun minimumAt(current : Node<S>?) : IntervalType<S>? {
    if (current == null) {
      return null
    }
    return if (current.left == null) {
      current.interval
    } else {
      this.minimumAt(current.left)
    }
  }

  override fun maximum() : IntervalType<S>? {
    return this.maximumAt(this.root)
  }

  private fun maximumAt(current : Node<S>?) : IntervalType<S>? {
    if (current == null) {
      return null
    }
    return if (current.right == null) {
      current.interval
    } else {
      this.maximumAt(current.right)
    }
  }

  override val size : Int
    get() = this.sizeTraverse(this.root)

  override fun isEmpty() : Boolean {
    return this.root == null
  }

  private fun all(current : Node<S>?) : Stream<Node<S>> {
    if (current == null) {
      return Stream.empty()
    }

    val currentStream : Stream<Node<S>> =
      Stream.of(current)
    val leftStream : Stream<Node<S>> =
      this.all(current.left)
    val rightStream : Stream<Node<S>> =
      this.all(current.right)

    return Stream.of(leftStream, currentStream, rightStream)
      .flatMap(Function.identity())
  }

  override fun iterator() : Iterator<IntervalType<S>> {
    val stream : Stream<Node<S>> = this.all(this.root)
    return stream.map { x -> x.interval }.iterator()
  }

  override fun overlapping(
    interval : IntervalType<S>
  ) : Collection<IntervalType<S>> {
    return if (this.root == null) {
      listOf()
    } else {
      this.overlappingAt(this.root!!, interval)
        .collect(Collectors.toList())
    }
  }

  private fun overlappingAt(
    current : Node<S>,
    interval : IntervalType<S>
  ) : Stream<IntervalType<S>> {

    /*
     * Test the current node's interval against the requested interval. The
     * current node's interval is only returned if it overlaps the requested
     * interval. Note that we don't check against the maximum: The maximum is
     * used to determine if recursion should proceed into child nodes.
     */

    val currentStream : Stream<IntervalType<S>> =
      if (interval.overlaps(current.interval)) {
      Stream.of(current.interval)
    } else {
      Stream.empty()
    }

    val lst = current.left
    val leftStream : Stream<IntervalType<S>> =
      if (lst != null && lst.maximum.overlaps(interval)) {
        this.overlappingAt(lst, interval)
    } else {
      Stream.empty()
    }

    val rst = current.right
    val rightStream : Stream<IntervalType<S>> =
      if (rst != null && rst.maximum.overlaps(interval)) {
        this.overlappingAt(rst, interval)
    } else {
      Stream.empty()
    }

    return Stream.of(leftStream, currentStream, rightStream)
      .flatMap { x -> x }
  }

  private class DuplicateIntervalException : Exception()

  private class NonexistentIntervalException : Exception()

  internal enum class BalanceFactor {

    /*
     * The node is completely balanced; both subtrees have the same height.
     */

    BALANCED,

    /*
     * The node is below the threshold that requires balancing, but the left
     * subtree has a greater height than the right.
     */

    BALANCED_LEANING_LEFT,

    /*
     * The node is below the threshold that requires balancing, but the right
     * subtree has a greater height than the left.
     */

    BALANCED_LEANING_RIGHT,

    /*
     * The node is unbalanced and needs balancing. The left subtree has a
     * greater height than the right.
     */

    LEFT_HEAVY,

    /*
     * The node is unbalanced and needs balancing. The right subtree has a
     * greater height than the left.
     */

    RIGHT_HEAVY;

    /**
     * @return `true` if this status indicates a balanced node
     */

    val isBalanced : Boolean
      get() = when (this) {
        BALANCED, BALANCED_LEANING_LEFT, BALANCED_LEANING_RIGHT -> true
        LEFT_HEAVY, RIGHT_HEAVY                                 -> false
      }
  }
}
