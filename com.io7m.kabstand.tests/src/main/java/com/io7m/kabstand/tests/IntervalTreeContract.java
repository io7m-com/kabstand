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

import com.io7m.kabstand.core.IntervalTreeChangeType;
import com.io7m.kabstand.core.IntervalTreeDebuggableType;
import com.io7m.kabstand.core.IntervalTreeType;
import com.io7m.kabstand.core.IntervalType;
import com.io7m.kabstand.generation.IntervalArbTreeChange;
import kotlin.Unit;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for interval trees.
 */

public abstract class IntervalTreeContract<
  I extends IntervalType<S>,
  S extends Comparable<S>>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(IntervalTreeContract.class);

  private ArrayList<IntervalTreeChangeType<S>> changes;
  private IntervalTreeType<S> tree;

  protected abstract I interval(
    long lower,
    long upper);

  @Provide("intervals")
  protected abstract Arbitrary<List<I>> intervals();

  protected abstract IntervalTreeDebuggableType<S> create();

  @BeforeEach
  public final void beforeContract()
  {
    this.changes = new ArrayList<>();
  }

  private Unit logChange(
    final IntervalTreeChangeType<S> change)
  {
    LOG.debug("Change: {}", change);
    this.changes.add(change);
    return Unit.INSTANCE;
  }

  /**
   * The size of an empty tree is zero.
   */

  @Test
  public void testSizeEmpty()
  {
    this.tree = this.create();
    assertEquals(0, this.tree.size());
    assertTrue(this.tree.isEmpty());
  }

  /**
   * For every element x inserted into a tree, tree size must grow by 1.
   */

  @Test
  public void testSizeInsertOne()
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    assertEquals(0, this.tree.size());
    final var i = this.interval(20L, 30L);
    assertTrue(this.tree.insert(i));
    assertEquals(1, this.tree.size());
  }

  /**
   * For every element x inserted into a tree, tree size must grow by 1.
   *
   * @param xs The elements
   */

  @Property
  public void testSizeInsertMany(
    final @ForAll("intervals") List<I> xs)
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    assertEquals(0, this.tree.size());

    var c = 0;
    for (final var x : xs) {
      if (this.tree.insert(x)) {
        c += 1;
      }
      assertTrue(this.tree.find(x));
    }

    assertEquals(c, this.tree.size());
  }

  /**
   * For every element x inserted into a tree, the element is in the tree.
   * Elements that are not inserted are not in the tree.
   *
   * @param xs The elements
   * @param ys The other elements
   */

  @Property
  public void testInsertFind(
    final @ForAll("intervals") List<I> xs,
    final @ForAll("intervals") List<I> ys)
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    for (final var x : xs) {
      this.tree.insert(x);
      assertTrue(this.tree.find(x));
    }

    for (final var y : ys) {
      if (!xs.contains(y)) {
        assertFalse(this.tree.find(y));
      }
    }
  }

  /**
   * For every element x inserted into a tree, the intervals returned by
   * overlapping(x) must overlap x.
   */

  @Test
  public void testOverlapsSpecific()
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    assertEquals(0, this.tree.size());

    final var i0 = this.interval(0L, 9L);
    final var i1 = this.interval(10L, 19L);
    final var i2 = this.interval(20L, 29L);
    final var i3 = this.interval(5L, 14L);

    assertTrue(this.tree.insert(i0));
    assertTrue(this.tree.insert(i1));
    assertTrue(this.tree.insert(i2));
    assertTrue(this.tree.insert(i3));
    assertEquals(4, this.tree.size());

    {
      final var e = List.of(i0);
      final var r = List.copyOf(this.tree.overlapping(i0));
      assertAll(
        r.stream()
          .map(s -> (Executable) () -> {
            assertTrue(s.overlaps(i0), String.format("%s overlaps %s", s, i0));
          })
          .collect(Collectors.toList())
      );
      assertEquals(e, r, "i0 overlaps");
    }

    {
      final var e = List.of(i3, i1);
      final var r = List.copyOf(this.tree.overlapping(i1));
      assertAll(
        r.stream()
          .map(s -> (Executable) () -> {
            assertTrue(s.overlaps(i1), String.format("%s overlaps %s", s, i1));
          })
          .collect(Collectors.toList())
      );
      assertEquals(e, r, "i1 overlaps");
    }

    {
      final var e = List.of(i2);
      final var r = List.copyOf(this.tree.overlapping(i2));
      assertAll(
        r.stream()
          .map(s -> (Executable) () -> {
            assertTrue(s.overlaps(i2), String.format("%s overlaps %s", s, i2));
          })
          .collect(Collectors.toList())
      );
      assertEquals(e, r, "i2 overlaps");
    }

    {
      final var e = List.of(i0, i3, i1);
      final var r = List.copyOf(this.tree.overlapping(i3));
      assertAll(
        r.stream()
          .map(s -> (Executable) () -> {
            assertTrue(s.overlaps(i3), String.format("%s overlaps %s", s, i3));
          })
          .collect(Collectors.toList())
      );
      assertEquals(e, r, "i3 overlaps");
    }
  }

  /**
   * For every element x inserted into a tree, the intervals returned by
   * overlapping(x) must overlap x.
   *
   * @param xs The elements
   */

  @Property
  public void testOverlapsForall(
    final @ForAll("intervals") List<I> xs)
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    final var inserted = new HashSet<I>(xs.size());
    for (final var x : xs) {
      if (this.tree.insert(x)) {
        inserted.add(x);
      }
    }

    for (final var x : inserted) {
      final var r = this.tree.overlapping(x);
      assertAll(
        r.stream()
          .map(s -> (Executable) () -> {
            assertTrue(s.overlaps(x), String.format("%s overlaps %s", s, x));
          })
          .collect(Collectors.toList())
      );
    }
  }

  /**
   * The empty tree never contains an interval that overlaps.
   *
   * @param xs The elements
   */

  @Property
  public void testOverlapsEmpty(
    final @ForAll("intervals") List<I> xs)
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    for (final var x : xs) {
      assertEquals(List.of(), List.copyOf(this.tree.overlapping(x)));
    }
  }

  /**
   * For every element x inserted into a tree, removing x keeps other elements
   * present.
   *
   * @param xs The elements
   */

  @Property
  public void testRemoveMany(
    final @ForAll("intervals") List<I> xs)
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    assertEquals(0, this.tree.size());

    final var inserted = new HashSet<I>();
    for (final var x : xs) {
      if (this.tree.insert(x)) {
        inserted.add(x);
      }
    }

    final var removed = new HashSet<I>();
    for (final var x : inserted) {
      if (this.tree.remove(x)) {
        removed.add(x);
      }
    }

    /*
     * Elements that were removed are not in the tree.
     */

    for (final var x : removed) {
      assertFalse(this.tree.find(x));
    }
  }

  /**
   * Removal works.
   */

  @Test
  public void testRemoveSpecific0()
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    assertEquals(0, this.tree.size());

    final var i0 = this.interval(0L, 9L);
    assertTrue(this.tree.insert(i0));
    assertEquals(1, this.tree.size());
    assertTrue(this.tree.remove(i0));
    assertEquals(0, this.tree.size());
  }

  /**
   * Removal works.
   */

  @Test
  public void testRemoveSpecific1()
  {
    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    assertEquals(0, this.tree.size());

    final var i0 = this.interval(0L, 9L);
    final var i1 = this.interval(10L, 19L);

    assertTrue(this.tree.insert(i0));
    assertEquals(1, this.tree.size());
    assertTrue(this.tree.insert(i1));
    assertEquals(2, this.tree.size());

    assertTrue(this.tree.remove(i0));
    assertEquals(1, this.tree.size());
  }

  /**
   * For every element x inserted into a tree, removing x keeps other elements
   * present.
   */

  @Test
  public void testRemoveSpecific2()
  {
    final var i0 =
      this.interval(0L, 0L);
    final var i1 =
      this.interval(0L, 1L);

    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    assertEquals(0, this.tree.size());
    assertTrue(this.tree.insert(i0));
    assertTrue(this.tree.find(i0));
    assertTrue(this.tree.insert(i1));
    assertTrue(this.tree.find(i1));
    assertEquals(2, this.tree.size());

    assertTrue(this.tree.remove(i0));
    assertFalse(this.tree.find(i0));
    assertFalse(this.tree.remove(i0));
    assertEquals(1, this.tree.size());

    assertTrue(this.tree.remove(i1));
    assertFalse(this.tree.find(i1));
    assertFalse(this.tree.remove(i1));
    assertEquals(0, this.tree.size());
  }

  /**
   * The collection view of the tree is an ordered set.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionOrdered(
    final @ForAll("intervals") List<I> xs)
  {
    final var unique = new TreeSet<>(xs);

    this.tree = this.create();
    this.tree.addAll(unique);
    assertEquals(unique.size(), this.tree.size());

    assertEquals(
      List.copyOf(unique),
      List.copyOf(this.tree)
    );
  }

  /**
   * containsAll() is correct.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionContainsAll(
    final @ForAll("intervals") List<I> xs)
  {
    final var unique = new TreeSet<>(xs);

    this.tree = this.create();
    this.tree.addAll(unique);
    assertEquals(unique.size(), this.tree.size());
    assertTrue(this.tree.containsAll(unique));
  }

  /**
   * removeAll() is correct.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionAddAllContainsAll(
    final @ForAll("intervals") List<I> xs)
  {
    Assumptions.assumeTrue(!xs.isEmpty());

    final var unique = new TreeSet<>(xs);

    this.tree = this.create();
    this.tree.addAll(unique);
    assertEquals(unique.size(), this.tree.size());
    assertTrue(this.tree.containsAll(unique));
    assertTrue(this.tree.removeAll(unique));
    assertTrue(this.tree.isEmpty());
  }

  /**
   * clear() is correct.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionClear(
    final @ForAll("intervals") List<I> xs)
  {
    final var unique = new TreeSet<>(xs);

    this.tree = this.create();
    this.tree.addAll(unique);
    this.tree.clear();
    assertTrue(this.tree.isEmpty());
  }

  /**
   * toArray() is correct.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionToArray0(
    final @ForAll("intervals") List<I> xs)
  {
    final var unique = new TreeSet<>(xs);

    this.tree = this.create();
    this.tree.addAll(unique);

    final var a0 = unique.toArray();
    final var a1 = this.tree.toArray();
    assertArrayEquals(a0, a1);
  }

  /**
   * toArray() is correct.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionToArray1(
    final @ForAll("intervals") List<I> xs)
  {
    final var unique = new TreeSet<>(xs);

    this.tree = this.create();
    this.tree.addAll(unique);

    final I[] ta0 =
      (I[]) Array.newInstance(IntervalType.class, unique.size());
    final I[] ta1 =
      (I[]) Array.newInstance(IntervalType.class, unique.size());

    final var a0 = unique.toArray(ta0);
    final var a1 = this.tree.toArray(ta1);
    assertArrayEquals(a0, a1);
  }

  /**
   * toArray() is correct.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionToArray2(
    final @ForAll("intervals") List<I> xs)
  {
    final var unique = new TreeSet<>(xs);
    Assumptions.assumeTrue(unique.size() >= 2);

    this.tree = this.create();
    this.tree.addAll(unique);

    final I[] ta0 =
      (I[]) Array.newInstance(IntervalType.class, unique.size());
    final I[] ta1 =
      (I[]) Array.newInstance(IntervalType.class, unique.size());

    final var ta0s =
      Arrays.copyOf(ta0, ta0.length / 2);
    final var ta1s =
      Arrays.copyOf(ta1, ta1.length / 2);

    final var a0 = unique.toArray(ta0s);
    final var a1 = this.tree.toArray(ta1s);
    assertArrayEquals(a0, a1);
  }

  /**
   * toArray() is correct.
   *
   * @param xs The elements
   */

  @Property
  public void testCollectionToArray3(
    final @ForAll("intervals") List<I> xs)
  {
    final var unique = new TreeSet<>(xs);
    Assumptions.assumeTrue(unique.size() >= 2);

    this.tree = this.create();
    this.tree.addAll(unique);

    final I[] ta0 =
      (I[]) Array.newInstance(IntervalType.class, unique.size());
    final I[] ta1 =
      (I[]) Array.newInstance(IntervalType.class, unique.size());

    final var ta0s =
      Arrays.copyOf(ta0, ta0.length * 2);
    final var ta1s =
      Arrays.copyOf(ta1, ta1.length * 2);

    final var a0 = unique.toArray(ta0s);
    final var a1 = this.tree.toArray(ta1s);
    assertArrayEquals(a0, a1);
  }

  @Provide("changes")
  private static Arbitrary<IntervalTreeChangeType> changes()
  {
    return (Arbitrary<IntervalTreeChangeType>) new IntervalArbTreeChange()
      .provideFor(
        TypeUsage.of(IntervalTreeChangeType.class),
        new ArbitraryProvider.SubtypeProvider()
        {
          @Override
          public Set<Arbitrary<?>> apply(TypeUsage typeUsage)
          {
            return Set.of();
          }
        }
      ).iterator().next();
  }

  @Property
  public void testChanges(
    final @ForAll("changes") IntervalTreeChangeType c0,
    final @ForAll("changes") IntervalTreeChangeType c1)
  {
    if (Objects.equals(c0, c1)) {
      assertEquals(c0.toString(), c1.toString());
    } else {
      assertNotEquals(c0.toString(), c1.toString());
    }
  }

  /**
   * The non-empty tree always has a minimum element.
   *
   * @param xs The elements
   */

  @Property
  public void testMinimumNonEmpty(
    final @ForAll("intervals") List<I> xs)
  {
    Assumptions.assumeFalse(xs.isEmpty());

    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    final var ordered = new TreeSet<>(xs);
    this.tree.addAll(ordered);

    assertEquals(
      ordered.first(),
      this.tree.minimum()
    );
  }

  /**
   * The non-empty tree always has a maximum element.
   *
   * @param xs The elements
   */

  @Property
  public void testMaximumNonEmpty(
    final @ForAll("intervals") List<I> xs)
  {
    Assumptions.assumeFalse(xs.isEmpty());

    this.tree = this.create();
    this.tree.setChangeListener(this::logChange);

    final var ordered = new TreeSet<>(xs);
    this.tree.addAll(ordered);

    assertEquals(
      ordered.last(),
      this.tree.maximum()
    );
  }

  /**
   * The empty tree never has a minimum element.
   */

  @Test
  public void testMinimumEmpty()
  {
    this.tree = this.create();

    assertEquals(
      null,
      this.tree.minimum()
    );
  }

  /**
   * The empty tree never has a maximum element.
   */

  @Test
  public void testMaximumEmpty()
  {
    this.tree = this.create();

    assertEquals(
      null,
      this.tree.maximum()
    );
  }
}
