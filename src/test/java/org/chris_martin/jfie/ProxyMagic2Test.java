package org.chris_martin.jfie;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.NoFactories.noFactories;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.JfieReport.nullReport;
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
    return new ClassToObjectFunction() {
      @Override
      public <T> JfieReport apply(Class<T> type, FactoryList trace) {

        if (type == IX.class)
          return newReport(new X(), new ArrayList<JfieException.Problem>());

        if (type == IY.class)
          return newReport(new Y(), new ArrayList<JfieException.Problem>());

        return nullReport(Arrays.<JfieException.Problem>asList(noFactories(type)));
      }
    };
  }

}
