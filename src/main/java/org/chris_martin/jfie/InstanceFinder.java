package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.chris_martin.jfie.Factories.constructorFactoriesByDescendingArity;
import static org.chris_martin.jfie.JfieException.BeMoreSpecific.beMoreSpecific;
import static org.chris_martin.jfie.JfieException.FactoryFailure.factoryFailure;
import static org.chris_martin.jfie.JfieException.NoFactories.noFactories;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.JfieReport.nullReport;
import static org.chris_martin.jfie.PartialOrders.refHierarchyPartialOrder;
import static org.chris_martin.jfie.Refs.objectRef;
import static org.chris_martin.jfie.Refs.typeRef;

class InstanceFinder implements JfieFunction<Class, Object> {

  private final JfieFunction<Class, Object> proxyMagic = new ProxyMagic(this);

  private final JfieStateView config;
  private final InstanceListener instanceListener;

  InstanceFinder(JfieStateView config, InstanceListener instanceListener) {
    this.config = config;
    this.instanceListener = instanceListener;
  }

  @Override
  public JfieReport<Object> apply(Class soughtType, FactoryList trace) {

    List<JfieException.Problem> log = new ArrayList<JfieException.Problem>();

    Ref match;
    {

      Set<Ref> matches;
      {
        JfieReport<Set<Ref>> matchesReport = findAllMatches(soughtType, trace);
        matches = matchesReport.result;
        log.addAll(matchesReport.problems);
      }

      matches = (Set) refHierarchyPartialOrder().lowest(matches);

      if (matches.size() == 0)
        throw new AssertionError();

      if (matches.size() > 1) {
        log.add(beMoreSpecific(soughtType, matches));
        return nullReport(log);
      }

      match = matches.iterator().next();
    }

    if (match.isObject())
      return newReport(match.object(), log);

    if (match.type().isInterface()) {

      JfieReport magic = (JfieReport) proxyMagic.apply(soughtType, trace);
      log.addAll(magic.problems);

      if (magic.result != null)
        return newReport(magic.result, log);

    }

    {
      JfieException.Problem constructorCycle = trace.checkForCycle(match.type());
      if (constructorCycle != null) {
        log.add(constructorCycle);
        return nullReport(log);
      }
    }

    JfieReport x = instantiate(match.type(), trace);
    log.addAll(x.problems);
    return newReport(x.result, log);
  }


  private JfieReport<Set<Ref>> findAllMatches(Class soughtType, FactoryList trace) {

    Set<Ref> matches = new HashSet<Ref>();

    matches.add(typeRef(soughtType));

    List<JfieException.Problem> log = new ArrayList<JfieException.Problem>();

    for (Ref ref : config.refs()) {
      Class type = ref.type();
      if (ref.isType()) {
        if (type != soughtType && soughtType.isAssignableFrom(type)) {
          JfieReport report = apply(type, trace);
          log.addAll(report.problems);

          if (report.result != null)
            matches.add(objectRef(report.result));

        }
      } else {
        if (soughtType.isAssignableFrom(ref.type())) {
          matches.add(ref);
        }
      }
    }

    for (Jfie jfie : config.jfies()) {
      JfieReport report = jfie.instanceFinder.apply(soughtType, trace);
      log.addAll(report.problems);

      if (report.result != null)
        matches.add(objectRef(report.result));

    }

    return JfieReport.newReport(matches, log);
  }

  private <T> JfieReport<T> instantiate(Class<T> type, FactoryList trace) {

    T x = null;

    List<JfieException.Problem> log = new ArrayList<JfieException.Problem>();

    List<? extends Factory<T>> factoryList = constructorFactoriesByDescendingArity(type);

    if (factoryList.size() == 0)
      log.add(noFactories(type));

    factories: for (Factory<T> factory : factoryList) {

      FactoryList trace2 = trace.add(factory);

      List<Object> instances = new ArrayList<Object>();
      for (Class arg : factory.parameterTypes()) {

        JfieReport instance = apply(arg, trace2);
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
