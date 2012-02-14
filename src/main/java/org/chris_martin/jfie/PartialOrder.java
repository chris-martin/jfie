package org.chris_martin.jfie;

import java.util.HashSet;
import java.util.Set;

class PartialOrder<Node> {

  Set<Node> lowest(Set<? extends Node> universe) {

    Set<Node> bad = new HashSet<Node>();

    for (Node a : universe) for (Node b : universe) {

      Relation relation = definition.relation(a, b);

      if (relation != Relation.UNDEFINED)
        bad.add(relation == Relation.HIGHER ? a : b);

    }

    Set<Node> lowest = new HashSet<Node>(universe);
    lowest.removeAll(bad);
    return lowest;
  }

  private final Definition<Node> definition;

  private PartialOrder(Definition<Node> definition) {
    this.definition = definition;
  }

  static <Node> PartialOrder<Node> partialOrder(Definition<Node> definition) {
    return new PartialOrder<Node>(definition);
  }

  enum Relation {

    /** {@code a} &gt; {@code b} */
    HIGHER,

    /** {@code a} &lt; {@code b} */
    LOWER,

    /** {@code a} and {@code b} are unrelated */
    UNDEFINED,

  }

  /**
   * A {@link PartialOrder.Definition} defines a partial order
   * by providing the {@link Relation}s between pairs of {@link Node}s.
   */
  interface Definition<Node> {

    Relation relation(Node a, Node b);

  }

}
