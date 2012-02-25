package org.chris_martin.jfie;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieState.jfieState;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class InstanceFinder_BeMoreSpecific_Test {

  @Test
  public void test1() {
    InstanceFinder instanceFinder = instanceFinder(HashSet.class, ArrayList.class);
    JfieReport<Collection> report = instanceFinder.apply(Collection.class, factoryList());
    assertNull(report.result);
    assertEquals(report.problems.size(), 1);
    assertEquals(report.problems.get(0).getClass(), JfieException.BeMoreSpecific.class);
  }

  @Test
  public void test2() {
    InstanceFinder instanceFinder = instanceFinder("abc", "def");
    JfieReport<Object> report = instanceFinder.apply(Object.class, factoryList());
    assertNull(report.result);
    assertEquals(report.problems.size(), 1);
    assertEquals(report.problems.get(0).getClass(), JfieException.BeMoreSpecific.class);
  }

  @Test
  public void test3() {
    InstanceFinder instanceFinder = instanceFinder("abc", "def");
    JfieReport<String> report = instanceFinder.apply(String.class, factoryList());
    assertNull(report.result);
    assertEquals(report.problems.size(), 1);
    assertEquals(report.problems.get(0).getClass(), JfieException.BeMoreSpecific.class);
  }

  private InstanceFinder instanceFinder(Object ... args) {
    return new InstanceFinder(jfieState(args), mock(InstanceListener.class));
  }

}
