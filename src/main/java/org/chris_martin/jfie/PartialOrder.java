package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.chris_martin.jfie.PartialOrder.Relation.*;

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

    EQUAL,

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

      for (Node node : u)
        if (node == null)
          throw new NullPointerException();

      Set<Node> bad = new HashSet<Node>();

      List<Node> uList = new ArrayList<Node>();
      uList.addAll(u);

      for (int i = 0; i < uList.size(); i++) for (int j = i + 1; j < uList.size(); j++) {

        Node a = uList.get(i), b = uList.get(j);

        Relation relation = definition.relation(a, b);

        switch (relation) {
          case HIGHER: case LOWER:
            bad.add(relation == Relation.HIGHER ? a : b);
        }

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

  static PartialOrder<Ref> refHierarchyPartialOrder() {
    return RefHierarchy.PARTIAL_ORDER;
  }

  private static class RefHierarchy implements PartialOrder.ComparisonDefinition<Ref> {

    private static final RefHierarchy INSTANCE = new RefHierarchy();
    private static final PartialOrder<Ref> PARTIAL_ORDER = PartialOrders.partialOrder(INSTANCE);

    @Override
    public PartialOrder.Relation relation(Ref a, Ref b) {

      if (a.type() != b.type())
        return TypeHierarchy.INSTANCE.relation(a.type(), b.type());

      if (a.isObject() != b.isObject())
        return a.isObject() ? LOWER : HIGHER;

      return EQUAL;
    }

  }

  static PartialOrder<Class> typeHierarchyPartialOrder() {
    return TypeHierarchy.PARTIAL_ORDER;
  }

  private static class TypeHierarchy implements PartialOrder.ComparisonDefinition<Class> {

    private static final TypeHierarchy INSTANCE = new TypeHierarchy();
    private static final PartialOrder<Class> PARTIAL_ORDER = PartialOrders.partialOrder(INSTANCE);

    @Override
    public PartialOrder.Relation relation(Class a, Class b) {

      if (a == b)
        return EQUAL;

      if (a.isAssignableFrom(b))
        return HIGHER;

      if (b.isAssignableFrom(a))
        return LOWER;

      return UNDEFINED;
    }

  }

}
