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
 * The base type of intervals.
 *
 * @param <S> The scalar type
 */

interface IntervalType<S : Comparable<S>> : Comparable<IntervalType<S>> {

  /**
   * @param other The other interval
   *
   * @return `true` if this interval overlaps `other`
   */

  fun overlaps(other : IntervalType<S>) : Boolean

  /**
   * The interval size (1 + (upper - lower))
   */

  fun size() : S

  /**
   * @return The inclusive upper bound
   */

  fun upper() : S

  /**
   * @return The inclusive lower bound
   */

  fun lower() : S

  /**
   * @param other The other interval
   *
   * @return The interval with an upper bound equal to the maximum of this and the other interval's upper bounds
   */

  fun upperMaximum(other : IntervalType<S>) : IntervalType<S>

  /**
   * Compare two intervals. Analogous to [Comparable.compareTo]
   * but with an enum result.
   *
   * @param other The other interval
   *
   * @return The comparison result
   */

  fun compare(other : IntervalType<S>) : IntervalComparison {
    val lowerC = lower().compareTo(other.lower())
    if (lowerC < 0) {
      return IntervalComparison.LESS_THAN
    }
    if (lowerC == 0) {
      val upperC = upper().compareTo(other.upper())
      if (upperC < 0) {
        return IntervalComparison.LESS_THAN
      }
      return if (upperC == 0) {
        IntervalComparison.EQUAL
      } else IntervalComparison.MORE_THAN
    }
    return IntervalComparison.MORE_THAN
  }

  override fun compareTo(other : IntervalType<S>) : Int {
    return when (compare(other)) {
      IntervalComparison.LESS_THAN -> -1
      IntervalComparison.EQUAL     -> 0
      IntervalComparison.MORE_THAN -> 1
    }
  }
}
