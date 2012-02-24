package org.chris_martin.jfie;

import org.testng.annotations.Test;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieState.jfieState;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class InstanceFinder1Test {

  @Test
  public void test1() {
    Object o = instanceFinder("abc").apply(Object.class, factoryList()).result;
    assertEquals(o, "abc");
  }

  @Test
  public void test2() {
    Object o = instanceFinder("abc").apply(String.class, factoryList()).result;
    assertEquals(o, "abc");
  }

  private InstanceFinder instanceFinder(Object ... args) {
    return new InstanceFinder(jfieState(args), mock(InstanceListener.class));
  }

}
