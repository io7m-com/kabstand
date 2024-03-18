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

/**
 * The type of mutable interval trees. Interval trees effectively act as
 * sorted sets, although they do not implement the full sorted set interface.
 *
 * @param <S> The type of scalar values
 */

interface IntervalTreeType<S : Comparable<S>> : Collection<IntervalType<S>> {

  /**
   * Set the change listener invoked when the tree is changed.
   *
   * @param listener The listener
   */

  fun setChangeListener(listener : (IntervalTreeChangeType<S>) -> Unit)

  /**
   * Insert an interval into the tree.
   *
   * @param value The interval
   *
   * @return `true` if the interval was not already present in the tree
   */

  fun insert(value : IntervalType<S>) : Boolean

  /**
   * Insert an interval into the tree.
   *
   * @param value The interval
   *
   * @return `true` if the interval was not already present in the tree
   */

  fun add(value : IntervalType<S>) : Boolean {
    return this.insert(value)
  }

  /**
   * Insert intervals into the tree.
   *
   * @param value The intervals
   *
   * @return `true` if at least one interval was not already present in the tree
   */

  fun addAll(value : Collection<IntervalType<S>>) : Boolean {
    var added = false
    for (v in value) {
      added = add(v) || added
    }
    return added
  }

  /**
   * Remove an interval from the tree.
   *
   * @param value The interval
   *
   * @return `true` if the interval was present in the tree
   */

  fun remove(value : IntervalType<S>) : Boolean

  /**
   * Remove intervals from the tree.
   *
   * @param c The intervals
   *
   * @return `true` if any interval was present in the tree
   */

  fun removeAll(c : Collection<IntervalType<S>>) : Boolean {
    var changed = false
    for (x in c) {
      changed = changed or this.remove(x)
    }
    return changed
  }

  /**
   * Remove all elements from the tree.
   */

  fun clear()

  /**
   * @param value The interval
   *
   * @return `true` if the exact interval is present in the tree
   */

  fun find(value : IntervalType<S>) : Boolean

  /**
   * @return The minimum interval in the set, if any
   */

  fun minimum() : IntervalType<S>?

  /**
   * @return The maximum interval in the set, if any
   */

  fun maximum() : IntervalType<S>?

  /**
   * @param interval The interval
   *
   * @return The set of intervals that overlap `interval`, if any
   */

  fun overlapping(interval : IntervalType<S>) : Collection<IntervalType<S>>

  override fun contains(element : IntervalType<S>) : Boolean {
    return this.find(element)
  }

  override fun containsAll(elements : Collection<IntervalType<S>>) : Boolean {
    var present = true
    for (x in elements) {
      present = present && this.contains(x)
    }
    return present
  }
}
