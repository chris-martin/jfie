package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.chris_martin.jfie.Refs.objectRef;
import static org.chris_martin.jfie.Refs.ref;
import static org.chris_martin.jfie.Refs.typeRef;

class JfieState implements JfieStateView, InstanceListener {

  private final List<Jfie> jfies = new ArrayList<Jfie>();
  private final Set<Ref> refs = new HashSet<Ref>();
  private boolean memoize;

  JfieState(Object ... args) {

    for (Object arg : args)
      add(arg);

  }

  private void add(Object arg) {

    if (arg == null)
      throw new NullPointerException();

    if (arg instanceof Jfie)
      jfies.add((Jfie) arg);
    else
      refs.add(ref(arg));

  }

  @Override
  public Iterable<Jfie> jfies() {
    return jfies;
  }

  @Override
  public Iterable<Ref> refs() {
    return refs;
  }

  @Override
  public void onInstantiate(Object x) {

    if (!memoize)
      return;

    refs.remove(typeRef(x.getClass()));
    refs.add(objectRef(x));

  }

  JfieState memoize() {

    JfieState x = new JfieState();

    x.jfies.addAll(jfies);
    x.refs.addAll(refs);
    x.memoize = true;

    return x;
  }

}
