package org.chris_martin.jfie;

import java.util.HashMap;
import java.util.Map;

class Primitives {

  private static final Map<String, Class> boxes = new HashMap<String, Class>();
  static {
    boxes.put("int", Integer.class);
    boxes.put("long", Long.class);
    boxes.put("double", Double.class);
    boxes.put("float", Float.class);
    boxes.put("bool", Boolean.class);
    boxes.put("char", Character.class);
    boxes.put("byte", Byte.class);
    boxes.put("void", Void.class);
    boxes.put("short", Short.class);
  }

  static Class box(Class type) {

    if (!type.isPrimitive())
      return type;

    Class box = boxes.get(type.getName());

    if (box == null)
      throw new AssertionError();

    return box;
  }

}
