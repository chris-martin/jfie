package org.chris_martin.jfie;

import org.testng.annotations.Test;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.*;
import static org.chris_martin.jfie.JfieState.jfieState;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class InstanceFinderTest {

  private InstanceFinder instanceFinder(Object ... args) {
    return new InstanceFinder(jfieState(args), mock(InstanceListener.class));
  }

  private <T> JfieReport<T> invoke(ClassToObjectFunction function, Class<T> type) {
    return function.apply(type, factoryList());
  }

  @Test
  public void test1() {
    assertEquals(invoke(instanceFinder("abc"), Object.class).result, "abc");
  }

  @Test
  public void test2() {
    assertEquals(invoke(instanceFinder("abc"), String.class).result, "abc");
  }

  @Test
  public void test3() {
    JfieReport<Int> report = invoke(instanceFinder("abc"), Int.class);
    assertNull(report.result);
    assertEquals(report.problems.size(), 2, report.problems.toString());
    Problem a = report.problems.get(0), b = report.problems.get(1);
    assertTrue(a instanceof FactoryFailure || b instanceof FactoryFailure);
    assertTrue(a instanceof FactoryCycle || b instanceof FactoryCycle);
  }

  static class Int {

    private int i;

    private Int(int i) {
      this.i = i;
    }

    public Int(Int i) {
      this.i = i.i;
    }

    public Int(String s) {
      this(Integer.parseInt(s));
    }

  }

}
