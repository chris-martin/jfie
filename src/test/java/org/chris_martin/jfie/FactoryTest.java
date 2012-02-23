package org.chris_martin.jfie;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class FactoryTest {

  @Test
  public void test1() {
    List<? extends Factory<X>> factories =
      Factories.constructorFactoriesByDescendingArity(X.class);
    assertEquals(factories.size(), 1);
  }

  @Test
  public void test2() {
    List<? extends Factory<Y>> factories =
      Factories.constructorFactoriesByDescendingArity(Y.class);
    assertEquals(factories.size(), 1);
  }

  public static class X { }

  static class Y {
    public Y() { }
  }

}
