package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.chris_martin.jfie.JfieException.NoFactories.noFactories;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.JfieReport.nullReport;

class ClassToInstanceMap implements ClassToObjectFunction {

  final Map<Class, Object> map = new HashMap<Class, Object>();

  @Override
  public <T> JfieReport<T> apply(Class<T> type, FactoryList trace) {

    T instance = (T) map.get(type);

    if (instance != null)
      return newReport(instance, new ArrayList<JfieException.Problem>());

    return nullReport(Arrays.<JfieException.Problem>asList(noFactories(type)));
  }

}
