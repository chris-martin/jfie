package org.chris_martin.jfie;

import java.util.HashSet;
import java.util.Set;

interface PartialOrder<Node> {

  /**
   * <p>{{@code x} ∈ {@code u} : ∄ {@code y} ∈ {@code u} : {@code x} &gt; {@code y} }</p>
   *
   * In other words, return a subset of {@code u} formed by removing an element
   * from {@code u} if it is greater than any other element of {@code u}.
   */
  Set<Node> lowest(Set<? extends Node> u);

  /**
   * Defines a partial order by providing the {@link Relation}s
   * between pairs of {@link Node}s.
   */
  interface ComparisonDefinition<Node> {

    Relation relation(Node a, Node b);

  }

  /**
   * The relation (or lack of relation) between a pair of nodes.
   */
  enum Relation {

    /** {@code a} &gt; {@code b} */
    HIGHER,

    /** {@code a} &lt; {@code b} */
    LOWER,

    /** {@code a} and {@code b} are unrelated */
    UNDEFINED,

  }

}

final class PartialOrders {

  private PartialOrders() { }

  static <Node> PartialOrder<Node> partialOrder(
      PartialOrder.ComparisonDefinition<Node> definition) {

    return new ComparisonImpl<Node>(definition);
  }

  private static class ComparisonImpl<Node> implements PartialOrder<Node> {

    @Override
    public Set<Node> lowest(Set<? extends Node> u) {

      Set<Node> bad = new HashSet<Node>();

      for (Node a : u) for (Node b : u) {

        Relation relation = definition.relation(a, b);

        if (relation != Relation.UNDEFINED)
          bad.add(relation == Relation.HIGHER ? a : b);

      }

      Set<Node> lowest = new HashSet<Node>(u);
      lowest.removeAll(bad);
      return lowest;
    }

    private final ComparisonDefinition<Node> definition;

    private ComparisonImpl(ComparisonDefinition<Node> definition) {
      this.definition = definition;
    }

  }

}

