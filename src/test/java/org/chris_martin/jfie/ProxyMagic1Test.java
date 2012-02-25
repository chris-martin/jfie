package org.chris_martin.jfie;

import org.testng.annotations.Test;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ProxyMagic1Test {

  @Test
  public void test1() {
    ChildA a = new ProxyMagic(instanceFinder())
      .apply(ChildA.class, factoryList()).result;
    assertNotNull(a);
    assertEquals(a.xyz(), ";p;");
  }

  @Test
  public void test2() {
    ChildB b = new ProxyMagic(instanceFinder())
      .apply(ChildB.class, factoryList()).result;
    assertNotNull(b);
    assertEquals(b.xyz(), ";p;");
  }

  static interface Parent { String xyz(); }

  static interface ChildA extends Parent { }

  static interface ChildB extends Parent { String xyz(); }

  static class ParentImpl implements Parent {

    public ParentImpl() { }

    @Override
    public String xyz() {
      return ";p;";
    }

  }

  private ClassToObjectFunction instanceFinder() {
    ClassToInstanceMap x = new ClassToInstanceMap();
    x.map.put(Parent.class, new ParentImpl());
    return x;
  }

}
