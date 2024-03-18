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

package com.io7m.kabstand.generation;

import com.io7m.kabstand.core.IntervalD;
import com.io7m.kabstand.core.IntervalTreeChangeType;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

/**
 * Arbitrary provider.
 */

public final class IntervalArbTreeChange
  extends IntervalArbAbstract<IntervalTreeChangeType>
{
  /**
   * Arbitrary provider.
   */

  public IntervalArbTreeChange()
  {
    super(
      IntervalTreeChangeType.class,
      () -> {
        return Arbitraries.oneOf(
          balanced(),
          created(),
          deleted(),
          cleared()
        );
      });
  }

  private static Arbitrary<IntervalTreeChangeType<?>> cleared()
  {
    return Arbitraries.create(IntervalTreeChangeType.Cleared::new);
  }

  private static Arbitrary<IntervalTreeChangeType<?>> deleted()
  {
    return Combinators.combine(
      Arbitraries.strings(),
      Arbitraries.defaultFor(IntervalD.class)
    ).as(IntervalTreeChangeType.Deleted::new);
  }

  private static Arbitrary<IntervalTreeChangeType<?>> created()
  {
    return Arbitraries.defaultFor(IntervalD.class)
      .map(IntervalTreeChangeType.Created::new);
  }

  private static Arbitrary<IntervalTreeChangeType<?>> balanced()
  {
    return Combinators.combine(
      Arbitraries.strings(),
      Arbitraries.defaultFor(IntervalD.class)
    ).as(IntervalTreeChangeType.Balanced::new);
  }
}
