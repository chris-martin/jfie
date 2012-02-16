package org.chris_martin.jfie;

interface Ref<T> {

  boolean isType();

  boolean isObject();

  Class<? extends T> type();

  /**
   * @return If this {@link #isObject is an object} ref, the
   *  object represented by this ref. Otherwise, {@code null}.
   */
  T object();

  String describe();

}

final class Refs {

  private Refs() { }

  static Ref<?> ref(Object o) {

    if (o instanceof Class)
      return typeRef((Class<?>) o);

    return objectRef(o);
  }

  static <T> Ref<T> objectRef(T x) {

    if (x == null)
      return null;

    // Check for a common programmer error
    // (you shouldn't be nesting refs).
    if (x instanceof Ref)
      throw new IllegalArgumentException();

    return new ObjectImpl<T>(x);
  }

  static <T> Ref<T> typeRef(Class<? extends T> x) {

    if (x == null)
      return null;

    return new TypeImpl<T>(x);
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<? extends T> getClassOf(T t) {
    return (Class) t.getClass();
  }

  private static abstract class BaseImpl<T> implements Ref<T> {

    @Override
    public boolean equals(Object o) {

      if (this == o)
        return true;

      if (o == null || !(o instanceof Ref))
        return false;

      Ref that = (Ref) o;

      if (isType() != that.isType())
        return false;

      if (isType())
        return type() == that.type();
      else
        return object() == that.object();
    }

    @Override
    public String toString() {
      return "<" + Ref.class.getName() + ": " + describe() + ">";
    }

  }

  private static class TypeImpl<T> extends BaseImpl<T> {

    final Class<? extends T> type;

    TypeImpl(Class<? extends T> type) {
      this.type = type;
    }

    @Override
    public boolean isType() {
      return true;
    }

    @Override
    public boolean isObject() {
      return false;
    }

    @Override
    public Class<? extends T> type() {
      return type;
    }

    @Override
    public T object() {
      return null;
    }

    @Override
    public int hashCode() {
      return type.hashCode();
    }

    @Override
    public String describe() {
      return type.getName();
    }

  }

  private static class ObjectImpl<T> extends BaseImpl<T> {

    final T object;

    ObjectImpl(T object) {
      this.object = object;
    }

    @Override
    public boolean isType() {
      return false;
    }

    @Override
    public boolean isObject() {
      return true;
    }

    @Override
    public Class<? extends T> type() {
      return getClassOf(object);
    }

    @Override
    public T object() {
      return object;
    }

    @Override
    public int hashCode() {
      return object.hashCode();
    }

    @Override
    public String describe() {
      return object.toString();
    }

  }

}
