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
    Jfie jfie = jfie("abc");
    assertEquals(jfie.get(Object.class), "abc");
  }

  @Test
  public void test2() {
    Jfie jfie = jfie("abc");
    assertEquals(jfie.get(String.class), "abc");
  }

  @Test
  public void test3() {
    Jfie jfie = jfie("abc");
    assertNull(jfie.get(Integer.class));
  }

  @Test
  public void test4() {
    Jfie jfie = jfie(ArrayList.class);
    List a = jfie.get(List.class);
    assertNotNull(a);
    assertTrue(a.getClass() == ArrayList.class);
  }

  @Test(expectedExceptions = BeMoreSpecific.class)
  public void test5() {
    Jfie jfie = jfie(HashSet.class, ArrayList.class);
    jfie.get(Collection.class);
  }

  @Test
  public void test6() {
    Jfie jfie = jfie("abc", new Object());
    Assert.assertEquals(jfie.get(Object.class), "abc");
  }

  @Test
  public void test7() {
    Jfie jfie = jfie("abc", new Object());
    Assert.assertEquals(jfie.get(String.class), "abc");
  }

  @Test(expectedExceptions = BeMoreSpecific.class)
  public void test8() {
    Jfie jfie = jfie("abc", "def");
    jfie.get(Object.class);
  }

  @Test(expectedExceptions = BeMoreSpecific.class)
  public void test9() {
    Jfie jfie = jfie("abc", "def");
    jfie.get(String.class);
  }

  @Test
  public void test10() {
    ArrayList a = new ArrayList();
    Jfie jfie = jfie(a);
    assertSame(jfie.get(Object.class), jfie.get(Object.class));
  }

  @Test
  public void test11() {
    ArrayList a = new ArrayList();
    Jfie jfie = jfie(a);
    assertSame(jfie.get(ArrayList.class), jfie.get(ArrayList.class));
  }

  @Test
  public void test12() {
    ArrayList a = new ArrayList();
    Jfie jfie = jfie(a);
    assertSame(jfie.get(Object.class), jfie.get(ArrayList.class));
  }

  @Test
  public void test13() {
    Jfie jfie = jfie(ArrayList.class);
    assertNotSame(jfie.get(ArrayList.class), jfie.get(ArrayList.class));
  }

  @Test
  public void test14() {
    Jfie jfie = jfie(ArrayList.class).memoize();
    assertSame(jfie.get(ArrayList.class), jfie.get(Object.class));
  }

}
