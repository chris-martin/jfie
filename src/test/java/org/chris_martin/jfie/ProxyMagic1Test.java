package org.chris_martin.jfie;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.NoFactories.noFactories;
import static org.chris_martin.jfie.JfieException.Problem;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.JfieReport.nullReport;
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

  private <T> T magic(Class<T> type) {
    return new ProxyMagic(instanceFinder()).apply(type, factoryList()).result;
  }

  private ClassToObjectFunction instanceFinder() {
    return new ClassToObjectFunction() {
      @Override
      public <T> JfieReport apply(Class<T> type, FactoryList trace) {

        if (type == Parent.class)
          return newReport(new ParentImpl(), new ArrayList<Problem>());

        return nullReport(Arrays.<Problem>asList(noFactories(type)));
      }
    };
  }

}
