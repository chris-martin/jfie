package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.List;

import static org.chris_martin.jfie.JfieConstructor.*;
import static org.chris_martin.jfie.JfieException.ConstructorFailure.constructorFailure;
import static org.chris_martin.jfie.JfieException.Problem;
import static org.chris_martin.jfie.JfieException.BeMoreSpecific.beMoreSpecific;
import static org.chris_martin.jfie.JfieException.NoOptions.noOptions;
import static org.chris_martin.jfie.JfieException.newJfieException;
import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.RefSet.newRefSet;

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
@SuppressWarnings("unchecked")
public final class Jfie {

  private List<Class> types = new ArrayList();
  private List<Jfie> jfies = new ArrayList();
  private RefSet objects = newRefSet();
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
    return _get(soughtType, newConstructorList());
  }

  /**
   * @param trace A stack trace of constructors used to detect constructor dependency cycles
   */
  protected JfieReport _get(Class soughtType, ConstructorList trace) {

    List<Problem> log = new ArrayList<Problem>();

    JfieReport<RefSet> matchesReport = findAllMatches(soughtType, trace);
    RefSet matches = matchesReport.result;
    log.addAll(matchesReport.exceptions);

    matches.removeAll(uselessMatches(matches));

    if (matchesReport.result.size() == 0) {
      log.add(noOptions(soughtType));
      return newReport(null, log);
    }

    if (matchesReport.result.size() > 1) {
      log.add(beMoreSpecific(soughtType, matches));
      return newReport(null, log);
    }

    Object match = matches.iterator().next();

    if (!(match instanceof Class))
      return newReport(match, log);

    Class type = (Class) match;

    Problem constructorCycle = trace.checkForCycle(type);
    if (constructorCycle != null) {
      log.add(constructorCycle);
      return newReport(match, log);
    }

    JfieReport<Object> x = instantiate(type, trace);
    log.addAll(x.exceptions);
    return newReport(x.result, log);
  }

  private RefSet uselessMatches(RefSet matches) {

    RefSet matchesToRemove = newRefSet();

    for (Object a : matches) {
      Class typeA = a instanceof Class ? (Class) a : a.getClass();

      for (Object b : matches) {
        Class typeB = b instanceof Class ? (Class) b : b.getClass();

        if (typeA != typeB && typeA.isAssignableFrom(typeB))
          matchesToRemove.add(a);
        else if (typeA != typeB && typeB.isAssignableFrom(typeA))
          matchesToRemove.add(b);
        else if (typeA == typeB && (a instanceof Class != b instanceof Class))
          matchesToRemove.add(a instanceof Class ? a : b);
      }
    }

    return matchesToRemove;
  }

  private JfieReport<RefSet> findAllMatches(Class soughtType, ConstructorList trace) {

    RefSet matches = newRefSet();

    matches.add(soughtType);

    List<Problem> log = new ArrayList<Problem>();

    for (Class type : types)
      if (type != soughtType && soughtType.isAssignableFrom(type)) {
        JfieReport report = _get(type, trace);
        matches.add(report.result);
        log.addAll(report.exceptions);
      }

    for (Object o : objects)
      if (soughtType.isAssignableFrom(o.getClass()))
        if (!matches.contains(o))
          matches.add(o);

    for (Jfie jfie : jfies) {
      JfieReport report = jfie._get(soughtType, trace);
      matches.add(report.result);
      log.addAll(report.exceptions);
    }

    return JfieReport.newReport(matches, log);
  }

  private JfieReport instantiate(Class type, ConstructorList trace) {

    Object x = null;

    List<Problem> log = new ArrayList<Problem>();

    constructors: for (JfieConstructor constructor : constructorsByDescendingArity(type)) {

      ConstructorList trace2 = trace.copy();
      trace2.add(constructor);

      List instances = new ArrayList();
      for (Class arg : constructor) {
        JfieReport instance = _get(arg, trace2);
        log.addAll(instance.exceptions);
        if (instance.result != null) {
          instances.add(instance.result);
        } else {
          continue constructors;
        }
      }
      x = constructor.newInstance(instances);
      if (x == null) {
        log.add(constructorFailure(constructor.constructor));
        continue constructors;
      }
      break;

    }

    if (memoize && x != null)
      objects.add(x);

    return newReport(x, log);
  }



  /**
   * Creates a jfie, based on this one, which memoizes calls to {@link #get(Class)}.
   * In other words, use this to turn a bean-instantiating jfie into a set of
   * lazy-instantiating singletons.
   */
  public Jfie memoize() {
    Jfie x = new Jfie();
    x.types.addAll(types);
    x.jfies.addAll(jfies);
    x.objects.addAll(objects);
    x.memoize = true;
    return x;
  }

  protected Jfie(Object ... args) {
    for (Object arg : args) {

      if (arg == null)
        throw new NullPointerException();

      if (arg instanceof Jfie)
        jfies.add((Jfie) arg);
      else if (arg instanceof Class)
        types.add((Class) arg);
      else
        objects.add(arg);

    }
  }

}
