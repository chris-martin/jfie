package org.chris_martin.jfie;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieState.jfieState;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class InstanceFinder_String_Test {

  @Test
  public void test1() {
    InstanceFinder instanceFinder = instanceFinder("abc");
    Object o = instanceFinder.apply(Object.class, factoryList()).result;
    assertEquals(o, "abc");
  }

  @Test
  public void test2() {
    InstanceFinder instanceFinder = instanceFinder("abc");
    Object o = instanceFinder.apply(String.class, factoryList()).result;
    assertEquals(o, "abc");
  }

  @Test
  public void test3() {
    InstanceFinder instanceFinder = instanceFinder("abc", new Object());
    Object o = instanceFinder.apply(Object.class, factoryList()).result;
    Assert.assertEquals(o, "abc");
  }

  @Test
  public void test4() {
    InstanceFinder instanceFinder = instanceFinder("abc", new Object());
    Object o = instanceFinder.apply(String.class, factoryList()).result;
    Assert.assertEquals(o, "abc");
  }

  private InstanceFinder instanceFinder(Object ... args) {
    return new InstanceFinder(jfieState(args), mock(InstanceListener.class));
  }

}
