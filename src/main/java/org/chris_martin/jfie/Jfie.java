package org.chris_martin.jfie;

import static org.chris_martin.jfie.FactoryLists.factoryList;
import static org.chris_martin.jfie.JfieException.newJfieException;

/**
 * <p style="font-weight: bold;">Classes with multiple constructors</p>
 *
 * <p>JFIE does not fail when constructor choice is ambiguous; it just fucking
 * injects something. Prefer higher-arity constructors over fewer args. Our job
 * is to inject, so damn it we're going to inject as many things as possible.</p>
 *
 * <p>JFIE will never call a constructor with a {@code null} argument.
 * If it can't find a constructor for which instances can be provided
 * for all arguments, it will fail.</p>
 *
 * <p style="font-weight: bold;">Injecting the same type into a class repeatedly</p>
 *
 * <p>This is allowed. If you have multiple constructor args, public fields,
 * and setter methods that all take the same type, then you're going to end up
 * getting injected with a lot of instances.</p>
 *
 * <p style="font-weight: bold;">Ambiguous types</p>
 *
 * <p>If you give JFIE two classes and then ask it for a supertype of those classes,
 * JFIE will tell you to {@link JfieException.BeMoreSpecific}. Look, I know this is
 * supposed to be a really permissive tool and all, but there are limits. I'm not
 * going to start guessing which classes you want.</p>
 *
 * <p style="font-weight: bold;">Generic types</p>
 *
 * <p>Fuck you, this tool doesn't support generic types. Java generics suck anyway.</p>
 *
 * <p style="font-weight: bold;">Circular dependencies</p>
 *
 * <p>Circular dependencies in fields and setter methods are absolutely okay.
 * In constructors, they are absolutely <em>not</em> okay. JFIE will throw up
 * if it notices even the possibility of any of that shit going on.</p>
 */
public final class Jfie {

  final JfieState state;
  final ClassToObjectFunction instanceFinder;

  private Jfie(JfieState state) {
    this.state = state;
    instanceFinder = new InstanceFinder(state, state);
  }

  /**
   * @throws NullPointerException If any of the {@code args} are null.
   */
  public static Jfie jfie(Object ... args) {
    return new Jfie(new JfieState(args));
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

  public JfieReport report(Class soughtType) {
    return instanceFinder.apply(soughtType, factoryList());
  }

  /**
   * Creates a jfie, based on this one, which memoizes calls to {@link #get(Class)}.
   * In other words, use this to turn a bean-instantiating jfie into a set of
   * lazy-instantiating singletons.
   */
  public Jfie memoize() {
    return new Jfie(state.memoize());
  }

}
