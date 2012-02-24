package org.chris_martin.jfie;

import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.FactoryCycle;
import static org.chris_martin.jfie.JfieException.FactoryFailure;
import static org.chris_martin.jfie.JfieState.jfieState;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class InstanceFinder2Test {

  @Test
  public void test1() {
    JfieReport<Int> report = instanceFinder("abc").apply(Int.class, factoryList());
    assertNull(report.result);
    assertEquals(report.problems.size(), 2, report.problems.toString());

    Set<Class> problemTypes = new HashSet<Class>();
    problemTypes.add(report.problems.get(0).getClass());
    problemTypes.add(report.problems.get(1).getClass());

    Set<Class> expectedProblemTypes = new HashSet<Class>();
    expectedProblemTypes.add(FactoryFailure.class);
    expectedProblemTypes.add(FactoryCycle.class);

    assertEquals(problemTypes, expectedProblemTypes);
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
