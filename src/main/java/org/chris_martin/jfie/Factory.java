package org.chris_martin.jfie;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

interface Factory<T> {

  Class<T> manufacturedType();

  Iterable<Class<?>> parameterTypes();

  T newInstance(List args);

}

final class Factories {

  private Factories() { }

  static <T> Factory<T> constructorFactory(Constructor<T> constructor) {
    return new ConstructorFactory<T>(constructor);
  }

  static <T> List<? extends Factory<T>>
      constructorFactoriesByDescendingArity(Class<T> type) {

    boolean explicitNullaryConstructor = false;

    List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
    for (Constructor constructor : type.getConstructors()) {

      constructors.add(constructor);

      if (constructor.getParameterTypes().length == 0)
        explicitNullaryConstructor = true;

    }

    if (!explicitNullaryConstructor) {
      try {
        Constructor constructor = type.getConstructor();
        constructors.add(constructor);
      } catch (NoSuchMethodException e) { }
    }

    Collections.sort(constructors, new Comparator<Constructor>() {

      @Override
      public int compare(Constructor a, Constructor b) {

        int x = arity(a) - arity(b);
        if (x != 0) return x;

        // secondary sort just to ensure determinism
        return str(a).compareTo(str(b));
      }

      int arity(Constructor constructor) {
        return constructor.getParameterTypes().length;
      }

      String str(Constructor constructor) {

        StringBuilder x = new StringBuilder();

        for (Class arg : constructor.getParameterTypes())
          x.append(arg.getName());

        return x.toString();
      }

    });

    List<Factory<T>> factories = new ArrayList<Factory<T>>();
    for (Constructor<T> constructor : constructors)
      factories.add(constructorFactory(constructor));

    return factories;
  }

  private static class ConstructorFactory<T> implements Factory<T> {

    final Constructor<T> constructor;

    private ConstructorFactory(Constructor<T> constructor) {
      this.constructor = constructor;
    }

    @Override
    public Class<T> manufacturedType() {
      return constructor.getDeclaringClass();
    }

    @Override
    public T newInstance(List args) {
      try {
        return constructor.newInstance(args.toArray());
      } catch (IllegalArgumentException e) {
      } catch (InstantiationException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
      } catch (ExceptionInInitializerError e) {
      }
      return null;
    }

    @Override
    public Iterable<Class<?>> parameterTypes() {
      return Arrays.asList(constructor.getParameterTypes());
    }

    @Override
    public String toString() {
      return constructor.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ConstructorFactory that = (ConstructorFactory) o;
      return constructor.equals(that.constructor);
    }

    @Override
    public int hashCode() {
      return constructor.hashCode();
    }

  }

}
