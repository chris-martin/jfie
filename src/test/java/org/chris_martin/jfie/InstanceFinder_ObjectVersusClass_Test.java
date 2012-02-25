package org.chris_martin.jfie;

import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieState.jfieState;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

public class InstanceFinder_ObjectVersusClass_Test {

  @Test
  public void test1() {
    ArrayList x = new ArrayList();
    InstanceFinder instanceFinder = instanceFinder(x);
    Object a = instanceFinder.apply(Object.class, factoryList()).result;
    Object b = instanceFinder.apply(Object.class, factoryList()).result;
    assertSame(a, b);
    assertSame(a, x);
  }

  @Test
  public void test2() {
    ArrayList x = new ArrayList();
    InstanceFinder instanceFinder = instanceFinder(x);
    ArrayList a = instanceFinder.apply(ArrayList.class, factoryList()).result;
    ArrayList b = instanceFinder.apply(ArrayList.class, factoryList()).result;
    assertSame(a, b);
    assertSame(a, x);
  }

  @Test
  public void test3() {
    ArrayList x = new ArrayList();
    InstanceFinder instanceFinder = instanceFinder(x);
    Object a = instanceFinder.apply(Object.class, factoryList()).result;
    ArrayList b = instanceFinder.apply(ArrayList.class, factoryList()).result;
    assertSame(a, b);
    assertSame(a, x);
  }

  @Test
  public void test10() {
    InstanceFinder instanceFinder = instanceFinder(ArrayList.class);
    ArrayList a = instanceFinder.apply(ArrayList.class, factoryList()).result;
    ArrayList b = instanceFinder.apply(ArrayList.class, factoryList()).result;
    assertNotSame(a, b);
  }

  private InstanceFinder instanceFinder(Object ... args) {
    return new InstanceFinder(jfieState(args), mock(InstanceListener.class));
  }

}
