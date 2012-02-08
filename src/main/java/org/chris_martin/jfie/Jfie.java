package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.List;

import static org.chris_martin.jfie.BeMoreSpecific.beMoreSpecific;

@SuppressWarnings("unchecked")
public class Jfie {

  protected List<Class> types = new ArrayList();
  protected List<Jfie> jfies = new ArrayList();
  protected RefSet objects = new RefSet();

  public static Jfie jfie(Object ... args) {
    return new Jfie(args);
  }

  public <T> T get(Class<T> soughtType) {
    return (T) _get(soughtType);
  }

  protected Object _get(Class soughtType) {
    RefSet matches = new RefSet();
    matches.add(soughtType);
    for (Class type : types) {
      if (type != soughtType && soughtType.isAssignableFrom(type)) {
        matches.add(get(type));
      }
    }
    for (Object o : objects) {
      if (soughtType.isAssignableFrom(o.getClass())) {
        if (!matches.contains(o)) {
          matches.add(o);
        }
      }
    }
    for (Jfie jfie : jfies) {
      matches.add(jfie.get(soughtType));
    }
    RefSet matchesToRemove = new RefSet();
    for (Object a : matches) {
      Class typeA = a instanceof Class ? (Class) a : a.getClass();
      for (Object b : matches) {
        Class typeB = b instanceof Class ? (Class) b : b.getClass();
        if (typeA != typeB && typeA.isAssignableFrom(typeB)) {
          matchesToRemove.add(a);
        } else if (typeA != typeB && typeB.isAssignableFrom(typeA)) {
          matchesToRemove.add(b);
        } else if (typeA == typeB && a instanceof Class != b instanceof Class) {
          matchesToRemove.add(a instanceof Class ? a : b);
        }
      }
    }
    matches.removeAll(matchesToRemove);
    if (matches.size() == 0) return null;
    if (matches.size() > 1) throw beMoreSpecific();
    Object match = matches.iterator().next();
    if (match instanceof Class) {
      try {
        return soughtType.getConstructor().newInstance();
      } catch (Exception e) {
        return null;
      }
    } else {
      return match;
    }
  }

  /**
   * Creates a jfie, based on this one, which memoizes calls to {@link #get(Class)}.
   * In other words, use this to turn a bean-instantiating jfie into a set of
   * lazy-instantiating singletons.
   */
  public Jfie memoize() {
    Objects objects = new Objects();
    objects.types.addAll(types);
    objects.jfies.addAll(jfies);
    return objects;
  }

  protected Jfie(Object ... args) {
    for (Object arg : args) {
      if (arg instanceof Jfie) {
        jfies.add((Jfie) arg);
      } else if (arg instanceof Class) {
        types.add((Class) arg);
      } else {
        objects.add(arg);
      }
    }
  }

  private static class Objects extends Jfie {

    protected Objects() { }

    @Override
    public <T> T get(Class<T> soughtType) {
      T object = super.get(soughtType);
      objects.add(object);
      return object;
    }

    @Override
    public Jfie memoize() {
      return this;
    }

  }

}
