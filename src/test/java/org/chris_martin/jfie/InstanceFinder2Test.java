package org.chris_martin.jfie;

import org.testng.annotations.Test;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.*;
import static org.chris_martin.jfie.JfieState.jfieState;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

public class InstanceFinder2Test {

  @Test
  public void test1() {
    JfieReport<Int> report = instanceFinder("abc").apply(Int.class, factoryList());
    assertNull(report.result);
    assertEquals(report.problems.size(), 2, report.problems.toString());
    Problem a = report.problems.get(0), b = report.problems.get(1);
    assertTrue(a instanceof FactoryFailure || b instanceof FactoryFailure);
    assertTrue(a instanceof FactoryCycle || b instanceof FactoryCycle);
  }

  @Test
  public void test2() {
    Int x = instanceFinder("42").apply(Int.class, factoryList()).result;
    assertEquals(x, new Int(42));
  }

  private static class Int {

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

    @Override
    public boolean equals(Object o) {
      return o.getClass() == Int.class && ((Int) o).i == i;
    }

    @Override
    public int hashCode() {
      return i;
    }

  }

  private InstanceFinder instanceFinder(Object ... args) {
    return new InstanceFinder(jfieState(args), mock(InstanceListener.class));
  }

}
