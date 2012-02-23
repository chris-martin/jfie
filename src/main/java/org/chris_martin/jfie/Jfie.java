package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.chris_martin.jfie.Factories.constructorFactoriesByDescendingArity;
import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.BeMoreSpecific.beMoreSpecific;
import static org.chris_martin.jfie.JfieException.FactoryFailure.factoryFailure;
import static org.chris_martin.jfie.JfieException.NoFactories.noFactories;
import static org.chris_martin.jfie.JfieException.Problem;
import static org.chris_martin.jfie.JfieException.newJfieException;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.JfieReport.nullReport;
import static org.chris_martin.jfie.PartialOrders.refHierarchyPartialOrder;
import static org.chris_martin.jfie.Refs.*;

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

  private final List<Jfie> jfies = new ArrayList<Jfie>();
  private final Set<Ref> refs = new HashSet<Ref>();
  private boolean memoize;

  private final JfieFunction<Class, Object> instanceFinder = new JfieFunction<Class, Object>() {
    @Override
    public JfieReport<Object> apply(Class soughtType, FactoryList trace) {
      return _get(soughtType, trace);
    }
  };
  private final JfieFunction<Class, Object> proxyMagic = new ProxyMagic(instanceFinder);

  private Jfie() { }

  /**
   * @throws NullPointerException If any of the {@code args} are null.
   */
  public static Jfie jfie(Object ... args) {
    Jfie x = new Jfie();
    for (Object arg : args) x.addArg(arg);
    return x;
  }

  private void addArg(Object arg) {

    if (arg == null)
      throw new NullPointerException();

    if (arg instanceof Jfie)
      jfies.add((Jfie) arg);
    else
      refs.add(ref(arg));

  }

  /**
   * @param soughtType The type of object you want
   * @return An instance of the requested type. Never null.
   * @throws JfieException If an instance could not be provided.
   * @throws NullPointerException If {@code soughtType} is null.
   */
  public <T> T get(Class<T> soughtType) {

    if (soughtType == null)
      throw new NullPointerException();

    JfieReport<T> report = report(soughtType);

    if (report.result == null) {

      if (report.problems.size() == 0)
        throw new AssertionError();

      throw newJfieException(report.problems);
    }

    return report.result;
  }

  public <T> JfieReport<T> report(Class<T> soughtType) {
    return _get(soughtType, factoryList());
  }

  /**
   * @param trace A stack trace of constructors used to detect constructor dependency cycles
   */
  protected <T> JfieReport<T> _get(Class<T> soughtType, FactoryList trace) {

    List<Problem> log = new ArrayList<Problem>();

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
      Problem constructorCycle = trace.checkForCycle(match.type());
      if (constructorCycle != null) {
        log.add(constructorCycle);
        return nullReport(log);
      }
    }

    JfieReport<? extends T> x = instantiate((Class<? extends T>) match.type(), trace);
    log.addAll(x.problems);
    return newReport(x.result, log);
  }

  private <T> JfieReport<Set<Ref<T>>> findAllMatches(Class<T> soughtType, FactoryList trace) {

    Set<Ref<T>> matches = new HashSet<Ref<T>>();

    matches.add(typeRef(soughtType));

    List<Problem> log = new ArrayList<Problem>();

    for (Ref ref : refs) {
      Class type = ref.type();
      if (ref.isType()) {
        if (type != soughtType && soughtType.isAssignableFrom(type)) {
          JfieReport<T> report = _get(type, trace);
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

    for (Jfie jfie : jfies) {
      JfieReport<T> report = jfie._get(soughtType, trace);
      log.addAll(report.problems);

      if (report.result != null)
        matches.add(objectRef(report.result));

    }

    return JfieReport.newReport(matches, log);
  }

  private <T> JfieReport<T> instantiate(Class<T> type, FactoryList trace) {

    T x = null;

    List<Problem> log = new ArrayList<Problem>();

    List<? extends Factory<T>> factoryList = constructorFactoriesByDescendingArity(type);

    if (factoryList.size() == 0)
      log.add(noFactories(type));

    factories: for (Factory<T> factory : factoryList) {

      FactoryList trace2 = trace.add(factory);

      List<Object> instances = new ArrayList<Object>();
      for (Class arg : factory.parameterTypes()) {

        JfieReport instance = _get(arg, trace2);
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

    if (x != null)
      instantiated(x);

    if (x == null && log.size() == 0)
      throw new AssertionError();

    return newReport(x, log);
  }

  private void instantiated(Object x) {
    if (memoize) {
      refs.remove(typeRef(x.getClass()));
      refs.add(objectRef(x));
    }
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

}
