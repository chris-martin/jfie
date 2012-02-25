package org.chris_martin.jfie;

import org.testng.annotations.Test;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ProxyMagic2Test {

  @Test
  public void test() {
    IZ z = new ProxyMagic(instanceFinder())
      .apply(IZ.class, factoryList()).result;
    assertNotNull(z);
    assertEquals(z.x(), "xxx");
    assertEquals(z.y(), "yyy");
  }

  private static interface IX { String x(); }

  private static interface IY { String y(); }

  private static interface IZ extends IX, IY { }

  private static class X implements IX {
    public X() { }
    @Override
    public String x() {
      return "xxx";
    }
  }

  private static class Y implements IY {
    public Y() { }
    @Override
    public String y() {
      return "yyy";
    }
  }

  private ClassToObjectFunction instanceFinder() {
    ClassToInstanceMap x = new ClassToInstanceMap();
    x.map.put(IX.class, new X());
    x.map.put(IY.class, new Y());
    return x;
  }

}
