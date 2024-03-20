/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com\> https://www.io7m.com
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

import com.io7m.kabstand.core.IntervalL;
import com.io7m.kabstand.core.IntervalTree;
import com.io7m.kabstand.core.IntervalTreeDebuggableType;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for interval trees.
 */

public final class IntervalTreeLTest
  extends IntervalTreeContract<IntervalL, Long>
{
  @Override
  protected IntervalL interval(
    final long lower,
    final long upper)
  {
    return new IntervalL(lower, upper);
  }

  @Provide
  public Arbitrary<List<IntervalL>> intervals()
  {
    return Arbitraries.defaultFor(IntervalL.class)
      .list();
  }

  @Override
  protected IntervalTreeDebuggableType<Long> create()
  {
    final var t = IntervalTree.<Long>empty();
    t.enableInternalValidation(true);
    return t;
  }
}
