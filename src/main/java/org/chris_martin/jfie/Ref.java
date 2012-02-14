package org.chris_martin.jfie;

interface Ref {

  boolean isType();
  boolean isObject();
  Class type();
  Object object();

  class Factory {

    static Ref ref(Object x) {
      return x == null ? null : new Impl(x);
    }

    private static class Impl implements Ref {

      final Class type;
      final Object object;

      Impl(Object x) {

        if (x instanceof Ref)
          throw new IllegalArgumentException();

        if (x instanceof Class) {
          type = (Class) x;
          object = null;
        } else {
          type = x.getClass();
          object = x;
        }
      }

      @Override
      public boolean isType() {
        return object == null;
      }

      @Override
      public boolean isObject() {
        return object != null;
      }

      @Override
      public Class type() {
        return type;
      }

      @Override
      public Object object() {
        return object;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Ref)) return false;
        Ref that = (Ref) o;
        return isType() == that.isType() && (isType() ? type == that.type() : object == that.object());
      }

      @Override
      public int hashCode() {
        return type != null ? type.hashCode() : object.hashCode();
      }

      @Override
      public String toString() {
        String x = type.getName();
        if (isObject()) x += ": " + object;
        return x;
      }

    }

  }

}
