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
 * A change event that has occurred to an interval tree.
 *
 * @param <S> The type of scalar values
 */

sealed interface IntervalTreeChangeType<S : Comparable<S>> {

  /**
   * The tree was rebalanced.
   *
   * @param type     The type of rebalance operation
   * @param interval The interval in the rebalanced node
   * @param <S>      The type of scalar values
   */

  data class Balanced<S : Comparable<S>>(
    val type : String,
    val interval : IntervalType<S>
  ) : IntervalTreeChangeType<S> {
    override fun toString() : String {
      return String.format("[Balanced %s %s]", type, interval)
    }
  }

  /**
   * A new node was created in the tree.
   *
   * @param interval The interval added
   * @param <S>      The type of scalar values
   */

  data class Created<S : Comparable<S>>(
    val interval : IntervalType<S>
  ) : IntervalTreeChangeType<S> {
    override fun toString() : String {
      return String.format("[Created %s]", interval)
    }
  }

  /**
   * A node was deleted from the tree.
   *
   * @param type     The type of deletion operation
   * @param interval The interval in the deleted node
   * @param <S>      The type of scalar values
   */

  data class Deleted<S : Comparable<S>>(
    val type : String,
    val interval : IntervalType<S>
  ) : IntervalTreeChangeType<S> {
    override fun toString() : String {
      return String.format("[Deleted %s %s]", type, interval)
    }
  }

  /**
   * All nodes were deleted from the tree.
   *
   * @param <S> The type of scalar values
   */

  class Cleared<S : Comparable<S>> : IntervalTreeChangeType<S> {
    override fun toString() : String {
      return "[Cleared]"
    }

    override fun equals(other : Any?) : Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      return true
    }

    override fun hashCode() : Int {
      return javaClass.hashCode()
    }
  }
}