package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.chris_martin.jfie.Factories.constructorFactoriesByDescendingArity;
import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.BeMoreSpecific.beMoreSpecific;
import static org.chris_martin.jfie.JfieException.FactoryFailure.factoryFailure;
import static org.chris_martin.jfie.JfieException.NoOptions.noOptions;
import static org.chris_martin.jfie.JfieException.Problem;
import static org.chris_martin.jfie.JfieException.newJfieException;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.PartialOrders.partialOrder;
import static org.chris_martin.jfie.RefHierarchy.refHierarchy;
import static org.chris_martin.jfie.Refs.ref;

/**

<p style="font-weight: bold;">Classes with multiple constructors</p>

<p>JFIE does not fail when constructor choice is ambiguous; it just fucking
injects something. Prefer higher-arity constructors over fewer args. Our job
is to inject, so damn it we're going to inject as many things as possible.</p>

<p>JFIE will never call a constructor with a {@code null} argument.
If it can't find a constructor for which instances can be provided
for all arguments, it will fail.</p>

<p style="font-weight: bold;">Injecting the same type into a class repeatedly</p>

<p>This is allowed. If you have multiple constructor args, public fields,
and setter methods that all take the same type, then you're going to end up
getting injected with a lot of instances.</p>

<p style="font-weight: bold;">Ambiguous types</p>

<p>If you give JFIE two classes and then ask it for a supertype of those classes,
JFIE will tell you to {@link JfieException.BeMoreSpecific}. Look, I know this is
supposed to be a really permissive tool and all, but there are limits. I'm not
going to start guessing which classes you want.</p>

<p style="font-weight: bold;">Generic types</p>

<p>Fuck you, this tool doesn't support generic types. Java generics suck anyway.</p>

<p style="font-weight: bold;">Circular dependencies</p>

<p>Circular dependencies in fields and setter methods are absolutely okay.
In constructors, they are absolutely <em>not</em> okay. JFIE will throw up
if it notices even the possibility of any of that shit going on.</p>

 */
public final class Jfie {

  private List<Jfie> jfies = new ArrayList<Jfie>();
  private Set<Ref> refs = new HashSet<Ref>();
  private boolean memoize;

  /**
   * @throws NullPointerException If any of the {@code args} are null.
   */
  public static Jfie jfie(Object ... args) {
    return new Jfie(args);
  }

  /**
   * @param soughtType The type of object you want
   * @return An instance of the requested type. Never null.
   * @throws JfieException If an instance could not be provided.
   * @throws NullPointerException If {@code soughtType} is null.
   */
  public <T> T get(Class<T> soughtType) {
    if (soughtType == null) throw new NullPointerException();
    JfieReport<T> report = report(soughtType);
    if (report.result == null)
      throw newJfieException(report.exceptions);
    return report.result;
  }

  public <T> JfieReport<T> report(Class<T> soughtType) {
    JfieReport<T> report = _get(soughtType, factoryList());
    return newReport(report.result, report.exceptions);
  }

  /**
   * @param trace A stack trace of constructors used to detect constructor dependency cycles
   */
  protected <T> JfieReport<T> _get(Class<T> soughtType, FactoryList trace) {

    List<Problem> log = new ArrayList<Problem>();

    Ref<? extends T> match;
    {

      Set<Ref<? extends T>> matches;
      {
        JfieReport<Set<Ref<? extends T>>> matchesReport = findAllMatches(soughtType, trace);
        matches = matchesReport.result;
        log.addAll(matchesReport.exceptions);
      }

      matches = (Set) partialOrder(refHierarchy()).lowest(matches);

      if (matches.size() == 0) {
        log.add(noOptions(soughtType));
        return newReport(null, log);
      }

      if (matches.size() > 1) {
        log.add(beMoreSpecific(soughtType, matches));
        return newReport(null, log);
      }

      match = matches.iterator().next();
    }

    if (match.isObject())
      return newReport(match.object(), log);

    {
      Problem constructorCycle = trace.checkForCycle(match.type());
      if (constructorCycle != null) {
        log.add(constructorCycle);
        return newReport(null, log);
      }
    }

    JfieReport<? extends T> x = instantiate((Class<? extends T>) match.type(), trace);
    log.addAll(x.exceptions);
    return newReport(x.result, log);
  }

  private <T> JfieReport<Set<Ref<? extends T>>> findAllMatches(Class<T> soughtType, FactoryList trace) {

    Set<Ref<? extends T>> matches = new HashSet<Ref<? extends T>>();

    matches.add(ref(soughtType));

    List<Problem> log = new ArrayList<Problem>();

    for (Ref ref : refs) {
      Class type = ref.type();
      if (ref.isType()) {
        if (type != soughtType && soughtType.isAssignableFrom(type)) {
          JfieReport<T> report = _get(type, trace);
          matches.add(ref(report.result));
          log.addAll(report.exceptions);
        }
      } else {
        if (soughtType.isAssignableFrom(ref.type())) {
          matches.add(ref);
        }
      }
    }

    for (Jfie jfie : jfies) {
      JfieReport<T> report = jfie._get(soughtType, trace);
      matches.add(ref(report.result));
      log.addAll(report.exceptions);
    }

    return JfieReport.newReport(matches, log);
  }

  private <T> JfieReport<T> instantiate(Class<T> type, FactoryList trace) {

    T x = null;

    List<Problem> log = new ArrayList<Problem>();

    constructors: for (Factory<T> factory : constructorFactoriesByDescendingArity(type)) {

      FactoryList trace2 = trace.add(factory);

      List<Object> instances = new ArrayList<Object>();
      for (Class arg : factory.parameterTypes()) {

        JfieReport instance = _get(arg, trace2);
        log.addAll(instance.exceptions);

        if (instance.result != null)
          instances.add(instance.result);
        else
          continue constructors;

      }
      x = factory.newInstance(instances);
      if (x == null) {
        log.add(factoryFailure(factory));
        continue constructors;
      }
      break;

    }

    if (memoize && x != null) {
      refs.remove(ref(x.getClass()));
      refs.add(ref(x));
    }

    return newReport(x, log);
  }

  /**
   * Creates a jfie, based on this one, which memoizes calls to {@link #get(Class)}.
   * In other words, use this to turn a bean-instantiating jfie into a set of
   * lazy-instantiating singletons.
   */
  public Jfie memoize() {
    Jfie x = new Jfie();
    x.jfies.addAll(jfies);
    x.refs.addAll(refs);
    x.memoize = true;
    return x;
  }

  protected Jfie(Object ... args) {
    for (Object arg : args) {

      if (arg == null)
        throw new NullPointerException();

      if (arg instanceof Jfie)
        jfies.add((Jfie) arg);
      else
        refs.add(ref(arg));

    }
  }

}
