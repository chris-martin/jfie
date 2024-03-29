package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.List;

import static org.chris_martin.jfie.Factories.constructorFactoriesByDescendingArity;
import static org.chris_martin.jfie.JfieException.FactoryFailure.factoryFailure;
import static org.chris_martin.jfie.JfieException.NoFactories.noFactories;
import static org.chris_martin.jfie.JfieReport.newReport;

class Instantiator implements ClassToObjectFunction {

  private final ClassToObjectFunction instanceFinder;
  private final InstanceListener instanceListener;

  Instantiator(ClassToObjectFunction instanceFinder, InstanceListener instanceListener) {
    this.instanceFinder = instanceFinder;
    this.instanceListener = instanceListener;
  }

  @Override
  public <T> JfieReport<T> apply(Class<T> type, FactoryList trace) {

    T x = null;

    List<JfieException.Problem> log = new ArrayList<JfieException.Problem>();

    List<? extends Factory<T>> factoryList =
      constructorFactoriesByDescendingArity(type);

    if (factoryList.size() == 0)
      log.add(noFactories(type));

    factories: for (Factory<T> factory : factoryList) {

      FactoryList trace2 = trace.add(factory);

      List<Object> instances = new ArrayList<Object>();
      for (Class<?> arg : factory.parameterTypes()) {

        JfieReport<?> instance = instanceFinder.apply(arg, trace2);
        log.addAll(instance.problems);

        if (instance.result != null)
          instances.add(instance.result);
        else
          continue factories;

      }
      x = factory.newInstance(instances);
      if (x == null) {
        log.add(factoryFailure(factory));
        continue factories;
      }
      break;

    }

    if (x == null && log.size() == 0)
      throw new AssertionError();

    if (x != null)
      instanceListener.onInstantiate(x);

    return newReport(x, log);
  }

}
