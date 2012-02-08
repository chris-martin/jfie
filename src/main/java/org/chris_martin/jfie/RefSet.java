package org.chris_martin.jfie;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class RefSet<T> implements Iterable<T> {

  final Set<Wrapper<T>> set = new HashSet<Wrapper<T>>();

  void add(T t) {
    if (t != null) {
      set.add(wrap(t));
    }
  }

  boolean contains(T t) {
    return set.contains(wrap(t));
  }

  int size() {
    return set.size();
  }

  @Override
  public Iterator<T> iterator() {
    final Iterator<Wrapper<T>> it = set.iterator();
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return it.hasNext();
      }
      @Override
      public T next() {
        return it.next().t;
      }
      @Override
      public void remove() {
        it.remove();
      }
    };
  }

  private static <T> Wrapper<T> wrap(T t) {
    return new Wrapper<T>(t);
  }

  public void removeAll(RefSet toRemove) {
    set.removeAll(toRemove.set);
  }

  private static class Wrapper<T> {

    T t;

    Wrapper(T t) {
      this.t = t;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Wrapper && t == ((Wrapper) o).t;
    }

    @Override
    public int hashCode() {
      return t.hashCode();
    }

  }

}
