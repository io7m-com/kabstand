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


package com.io7m.kabstand.tests;

import com.io7m.kabstand.core.IntervalD;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.math.BigInteger;
import java.util.Objects;

import static com.io7m.kabstand.core.IntervalComparison.EQUAL;
import static com.io7m.kabstand.core.IntervalComparison.LESS_THAN;
import static com.io7m.kabstand.core.IntervalComparison.MORE_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class IntervalDTest
{
  @Property
  public void testCompareReflexive(
    final @ForAll IntervalD x)
  {
    assertEquals(EQUAL, x.compare(x));
  }

  @Property
  public void testCompareAntiSymmetric(
    final @ForAll IntervalD x,
    final @ForAll IntervalD y)
  {
    switch (x.compare(y)) {
      case LESS_THAN: {
        assertEquals(MORE_THAN, y.compare(x));
        break;
      }
      case EQUAL: {
        assertEquals(EQUAL, y.compare(x));
        break;
      }
      case MORE_THAN: {
        assertEquals(LESS_THAN, y.compare(x));
        break;
      }
    }
  }

  @Property
  public void testToString(
    final @ForAll IntervalD x,
    final @ForAll IntervalD y)
  {
    if (Objects.equals(x, y)) {
      assertEquals(x.toString(), y.toString());
    } else {
      assertNotEquals(x.toString(), y.toString());
    }
  }

  @Provide("values")
  private static Arbitrary<Double> values()
  {
    return Arbitraries.doubles();
  }

  @Property
  public void testInvalid(
    final @ForAll("values") Double x,
    final @ForAll("values") Double y)
  {
    if (x.compareTo(y) < 0) {
      assertThrows(IllegalStateException.class, () -> {
        new IntervalD(y, x);
      });
    }
  }
}
