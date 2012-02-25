package org.chris_martin.jfie;

import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.chris_martin.jfie.Jfie.jfie;
import static org.testng.Assert.*;

public class JfieTest {

  @Test
  public void test1() {
    Jfie jfie = jfie(ArrayList.class).memoize();
    assertSame(jfie.get(ArrayList.class), jfie.get(Object.class));
  }

  @Test
  public void test2() {
    Jfie jfie = jfie(StringWrapper.class, "abc");
    StringWrapper x = jfie.get(StringWrapper.class);
    assertNotNull(x);
    assertEquals(x.x, "abc");
  }

  @Test
  public void test3() {
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
  public void test4() {
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
  public void test5() {
    Jfie jfie = jfie(A.class, B.class);
    assertNull(jfie.get(A.class));
  }

  @Test(expectedExceptions = JfieException.class)
  public void test6() {
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
