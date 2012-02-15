package org.chris_martin.jfie;

import static org.chris_martin.jfie.PartialOrder.Relation;
import static org.chris_martin.jfie.PartialOrder.Relation.*;

class RefHierarchy implements PartialOrder.ComparisonDefinition<Ref> {

  private static final RefHierarchy INSTANCE = new RefHierarchy();

  static RefHierarchy refHierarchy() {
    return INSTANCE;
  }

  @Override
  public Relation relation(Ref a, Ref b) {

    if (a.type() != b.type()) {

      if (a.type().isAssignableFrom(b.type()))
        return HIGHER;

      if (b.type().isAssignableFrom(a.type()))
        return LOWER;

    } else {

      if (a.isObject() != b.isObject())
        return a.isObject() ? LOWER : HIGHER;

    }

    return UNDEFINED;
  }

}
