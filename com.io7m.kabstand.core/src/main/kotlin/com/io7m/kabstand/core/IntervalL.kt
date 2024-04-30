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

data class IntervalL(
  val lower : Long,
  val upper : Long
) : IntervalType<Long> {

  init {
    check(this.upper >= this.lower) { "Upper ${this.upper} must be >= lower ${this.lower} " }
  }

  override fun overlaps(
    other : IntervalType<Long>
  ) : Boolean {
    return (this.lower <= other.upper() && other.lower() <= this.upper)
  }

  override fun upperMaximum(other : IntervalType<Long>) : IntervalType<Long> {
    return IntervalL(
      this.lower,
      Math.max(this.upper, other.upper())
    )
  }

  override fun size() : Long {
    return 1L + (this.upper - this.lower)
  }

  override fun upper() : Long {
    return this.upper
  }

  override fun lower() : Long {
    return this.lower
  }
}
