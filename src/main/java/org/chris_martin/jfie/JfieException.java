package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.List;

public final class JfieException extends RuntimeException {

  private final List<Problem> exceptions = new ArrayList<Problem>();

  private JfieException() { }

  static JfieException newJfieException(Iterable<Problem> exceptions) {

    JfieException x = new JfieException();
    for (Problem e : exceptions)
      x.exceptions.add(e);

    throw x;
  }

  @Override
  public String getMessage() {

    StringBuilder x = new StringBuilder();

    for (Problem e : exceptions)
      x.append(e).append("\n");

    x.setLength(x.length() - 1);

    return x.toString();
  }

  @Override
  public String toString() {
    return "JfieException:\n" + getMessage();
  }

  static class Problem { }

  public static final class NoOptions extends Problem {

    private final Class type;

    private NoOptions(Class type) {
      this.type = type;
    }

    static NoOptions noOptions(Class type) {
      return new NoOptions(type);
    }

    @Override
    public String toString() {
      return "No options for " + type.getName();
    }

  }

  public static final class BeMoreSpecific extends Problem {

    private final Class type;
    private final List<Ref> possibilities;

    private BeMoreSpecific(Class type, List<Ref> possibilities) {
      this.type = type;
      this.possibilities = possibilities;
    }

    static BeMoreSpecific beMoreSpecific(Class type, Iterable<? extends Ref> possibilities) {

      List<Ref> list = new ArrayList<Ref>();

      for (Ref ref : possibilities)
        list.add(ref);

      return new BeMoreSpecific(type, list);
    }

    @Override
    public String toString() {

      StringBuilder x = new StringBuilder();

      x.append("Too many options for ").append(type.getName()).append(":");

      for (Ref ref : possibilities)
        x.append("\n  ").append(ref.describe());

      return x.toString();
    }

  }

  public static final class FactoryCycle extends Problem {

    private final List<Factory> factories = new ArrayList<Factory>();

    private FactoryCycle(List<Factory> factories) {
      this.factories.addAll(factories);
    }

    static FactoryCycle factoryCycle(List<Factory> factories) {

      if (factories.size() == 0)
        throw new NullPointerException();

      return new FactoryCycle(factories);
    }

    @Override
    public String toString() {
      StringBuilder x = new StringBuilder();
      x.append("Factory cycle: ");
      for (Factory factory : factories) {
        x.append(factory).append(", ");
      }
      x.setLength(x.length() - 2);
      return x.toString();
    }

  }

  public static final class FactoryFailure extends Problem {

    private final Factory factory;

    private FactoryFailure(Factory factory) {
      this.factory = factory;
    }

    static FactoryFailure factoryFailure(Factory factory) {
      return new FactoryFailure(factory);
    }

    @Override
    public String toString() {
      return "Factory failure: " + factory;
    }

  }

  public static final class NoFactories extends Problem {

    private Class type;

    private NoFactories(Class type) {
      this.type = type;
    }

    static NoFactories noFactories(Class type) {
      return new NoFactories(type);
    }

    @Override
    public String toString() {
      return "No factories: " + type.getName();
    }

  }

}
