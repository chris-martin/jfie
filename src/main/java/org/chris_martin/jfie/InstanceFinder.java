package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.chris_martin.jfie.JfieException.BeMoreSpecific.beMoreSpecific;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.JfieReport.nullReport;
import static org.chris_martin.jfie.PartialOrders.refHierarchyPartialOrder;
import static org.chris_martin.jfie.Refs.objectRef;
import static org.chris_martin.jfie.Refs.typeRef;

class InstanceFinder implements ClassToObjectFunction {

  private final JfieStateView config;
  private final ClassToObjectFunction instantiator;
  private final ClassToObjectFunction proxyMagic;

  InstanceFinder(JfieStateView config, InstanceListener instanceListener) {
    this.config = config;
    instantiator = new Instantiator(this, instanceListener);
    proxyMagic = new ProxyMagic(this);
  }

  @Override
  public <T> JfieReport<T> apply(Class<T> soughtType, FactoryList trace) {

    List<JfieException.Problem> log = new ArrayList<JfieException.Problem>();

    Ref<T> match;
    {

      Set<Ref<T>> matches;
      {
        JfieReport<Set<Ref<T>>> matchesReport = findAllMatches(soughtType, trace);
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

      JfieReport<T> magic = (JfieReport) proxyMagic.apply(soughtType, trace);
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

    JfieReport<? extends T> x = instantiator.apply(match.type(), trace);
    log.addAll(x.problems);
    return newReport(x.result, log);
  }


  private <T> JfieReport<Set<Ref<T>>> findAllMatches(Class<T> soughtType, FactoryList trace) {

    Set<Ref<T>> matches = new HashSet<Ref<T>>();

    matches.add(typeRef(soughtType));

    List<JfieException.Problem> log = new ArrayList<JfieException.Problem>();

    for (Ref ref : config.refs()) {
      Class type = ref.type();
      if (ref.isType()) {
        if (type != soughtType && soughtType.isAssignableFrom(type)) {
          JfieReport<T> report = apply(type, trace);
          log.addAll(report.problems);

          if (report.result != null)
            matches.add(objectRef(report.result));

        }
      } else if (soughtType.isAssignableFrom(ref.type())) {
        matches.add(ref);
      }
    }

    for (Jfie jfie : config.jfies()) {
      JfieReport<T> report = jfie.instanceFinder.apply(soughtType, trace);
      log.addAll(report.problems);

      if (report.result != null)
        matches.add(objectRef(report.result));

    }

    return JfieReport.newReport(matches, log);
  }

}
