package org.chris_martin.jfie;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.chris_martin.jfie.JfieException.ConstructorCycle.constructorCycle;

final class JfieConstructor implements Iterable<Class> {

  final Constructor constructor;

  private JfieConstructor(Constructor constructor) {
    this.constructor = constructor;
  }

  static JfieConstructor newConstructor(Constructor constructor) {
    return new JfieConstructor(constructor);
  }

  int arity() {
    return constructor.getParameterTypes().length;
  }

  Object newInstance(List args) {
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
  public Iterator<Class> iterator() {
    return Arrays.asList(constructor.getParameterTypes()).iterator();
  }

  String comparisonString() {

    StringBuilder x = new StringBuilder();

    for (Class arg : constructor.getParameterTypes())
      x.append(arg.getName());

    return x.toString();
  }

  @Override
  public String toString() {
    return constructor.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JfieConstructor that = (JfieConstructor) o;
    return constructor.equals(that.constructor);
  }

  @Override
  public int hashCode() {
    return constructor.hashCode();
  }

  static Iterable<JfieConstructor> constructorsByDescendingArity(Class type) {
    List<JfieConstructor> constructors = new ArrayList<JfieConstructor>();
    for (Constructor constructor : type.getConstructors())
      constructors.add(newConstructor(constructor));
    Collections.sort(constructors, ARITY_COMPARATOR);
    return constructors;
  }

  private static final ArityComparator ARITY_COMPARATOR = new ArityComparator();
  private static class ArityComparator implements Comparator<JfieConstructor> {
    @Override
    public int compare(JfieConstructor a, JfieConstructor b) {

      int x = a.arity() - b.arity();
      if (x != 0) return x;

      // secondary sort just to ensure determinism
      return a.comparisonString().compareTo(b.comparisonString());

    }
  }

  static ConstructorList newConstructorList(JfieConstructor ... constructors) {
    ConstructorList x = new ConstructorList();
    for (JfieConstructor constructor : constructors) x.add(constructor);
    return x;
  }

  static class ConstructorList {

    private final ArrayList<Constructor> list = new ArrayList<Constructor>();

    private ConstructorList() { }

    void add(JfieConstructor constructor) {
      add(constructor.constructor);
    }

    void add(Constructor constructor) {
      list.add(constructor);
    }

    ConstructorList copy() {
      ConstructorList x = new ConstructorList();
      for (Constructor constructor : list) x.add(constructor);
      return x;
    }

    JfieException.ConstructorCycle checkForCycle(Class type) {

      for (Constructor constructor : list)
        if (constructor.getDeclaringClass() == type)
          return constructorCycle(list.subList(list.indexOf(constructor), list.size()));

      return null;
    }

  }

}
