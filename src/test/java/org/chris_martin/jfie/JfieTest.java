package org.chris_martin.jfie;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.chris_martin.jfie.Jfie.jfie;
import static org.testng.Assert.*;

public class JfieTest {

  @Test
  public void test1() {
    Jfie jfie = jfie(ArrayList.class);
    List a = jfie.get(List.class);
    assertNotNull(a);
    assertTrue(a.getClass() == ArrayList.class);
  }

  @Test(expectedExceptions = JfieException.class)
  public void test2() {
    Jfie jfie = jfie(HashSet.class, ArrayList.class);
    jfie.get(Collection.class);
  }

  @Test
  public void test3() {
    Jfie jfie = jfie("abc", new Object());
    Assert.assertEquals(jfie.get(Object.class), "abc");
  }

  @Test
  public void test4() {
    Jfie jfie = jfie("abc", new Object());
    Assert.assertEquals(jfie.get(String.class), "abc");
  }

  @Test(expectedExceptions = JfieException.class)
  public void test5() {
    Jfie jfie = jfie("abc", "def");
    assertNull(jfie.get(Object.class));
  }

  @Test(expectedExceptions = JfieException.class)
  public void test6() {
    Jfie jfie = jfie("abc", "def");
    assertNull(jfie.get(String.class));
  }

  @Test
  public void test7() {
    ArrayList a = new ArrayList();
    Jfie jfie = jfie(a);
    assertSame(jfie.get(Object.class), jfie.get(Object.class));
  }

  @Test
  public void test8() {
    ArrayList a = new ArrayList();
    Jfie jfie = jfie(a);
    assertSame(jfie.get(ArrayList.class), jfie.get(ArrayList.class));
  }

  @Test
  public void test9() {
    ArrayList a = new ArrayList();
    Jfie jfie = jfie(a);
    assertSame(jfie.get(Object.class), jfie.get(ArrayList.class));
  }

  @Test
  public void test10() {
    Jfie jfie = jfie(ArrayList.class);
    assertNotSame(jfie.get(ArrayList.class), jfie.get(ArrayList.class));
  }

  @Test
  public void test11() {
    Jfie jfie = jfie(ArrayList.class).memoize();
    assertSame(jfie.get(ArrayList.class), jfie.get(Object.class));
  }

  @Test
  public void test12() {
    Jfie jfie = jfie(StringWrapper.class, "abc");
    StringWrapper x = jfie.get(StringWrapper.class);
    assertNotNull(x);
    assertEquals(x.x, "abc");
  }

  @Test
  public void test13() {
    Jfie jfie = jfie(DoubleStringWrapper.class, "xyz");
    DoubleStringWrapper a = jfie.get(DoubleStringWrapper.class);
    DoubleStringWrapper b = jfie.get(DoubleStringWrapper.class);
    assertNotSame(a, b);
    assertEquals(a.x, "xyz");
    assertEquals(a.y, "xyz");
    assertEquals(b.x, "xyz");
    assertEquals(b.y, "xyz");
  }

  @Test
  public void test14() {
    Jfie jfie = jfie(DoubleStringWrapper.class, "xyz").memoize();
    DoubleStringWrapper a = jfie.get(DoubleStringWrapper.class);
    DoubleStringWrapper b = jfie.get(DoubleStringWrapper.class);
    assertSame(a, b);
    assertEquals(a.x, "xyz");
    assertEquals(a.y, "xyz");
    assertEquals(b.x, "xyz");
    assertEquals(b.y, "xyz");
  }

  @Test(expectedExceptions = JfieException.class)
  public void test15() {
    Jfie jfie = jfie(A.class, B.class);
    assertNull(jfie.get(A.class));
  }

  @Test(expectedExceptions = JfieException.class)
  public void test16() {
    jfie().get(R.class);
  }

  private static interface R { String r(); }

  private static class A {
    private B b;
    public A(B b) {
      this.b = b;
    }
  }
  private static class B {
    private A a;
    public B(A a) {
      this.a = a;
    }
  }

  private static class StringWrapper {
    private String x;
    public StringWrapper(String x) {
      this.x = x;
    }
  }

  private static class DoubleStringWrapper {
    private String x, y;
    public DoubleStringWrapper(String x, String y) {
      this.x = x;
      this.y = y;
    }
  }

}
