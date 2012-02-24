package org.chris_martin.jfie;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.chris_martin.jfie.JfieReport.newReport;
import static org.chris_martin.jfie.JfieReport.nullReport;
import static org.chris_martin.jfie.PartialOrders.typeHierarchyPartialOrder;

final class ProxyMagic implements ClassToObjectFunction {

  private final ClassToObjectFunction instanceFinder;

  ProxyMagic(ClassToObjectFunction instanceFinder) {
    this.instanceFinder = instanceFinder;
  }

  @Override
  public <T> JfieReport<T> apply(Class<T> soughtType, FactoryList trace) {

    Set<Class> parentTypes = typeHierarchyPartialOrder().lowest(
      new HashSet<Class>(Arrays.asList(soughtType.getInterfaces())));

    List<JfieException.Problem> log = new ArrayList<JfieException.Problem>();

    List<Object> instances = new ArrayList<Object>();
    for (Class parentType : parentTypes) {
      JfieReport parent = instanceFinder.apply(parentType, trace);
      log.addAll(parent.problems);

      if (parent.result == null)
        return nullReport(log);

      instances.add(parent.result);
    }

    final Map<Method, ObjectMethod> handlers = new HashMap<Method, ObjectMethod>();
    for (Method method : soughtType.getMethods()) {

      List<ObjectMethod> matches = findEquivalentMethods(instances, method);

      if (matches.size() != 1)
        return nullReport(log);

      handlers.put(method, matches.get(0));

    }

    InvocationHandler invocationHandler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return handlers.get(method).invoke(args);
      }
    };

    ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    T proxy = (T) Proxy.newProxyInstance(
      classLoader, new Class[]{soughtType}, invocationHandler);

    return newReport(proxy, log);
  }

  private static class ObjectMethod {

    final Object object;
    final Method method;

    private ObjectMethod(Object object, Method method) {
      this.object = object;
      this.method = method;
    }

    Object invoke(Object ... args) throws InvocationTargetException, IllegalAccessException {
      return method.invoke(object, args);
    }

  }

  private static List<ObjectMethod> findEquivalentMethods(Iterable<Object> objects, Method method) {
    List<ObjectMethod> equivalentMethods = new ArrayList<ObjectMethod>();
    for (Object object : objects) {
      Method equivalentMethod = findEquivalentMethod(object.getClass(), method);
      if (equivalentMethod != null) {
        equivalentMethods.add(new ObjectMethod(object, equivalentMethod));
      }
    }
    return equivalentMethods;
  }

  private static Method findEquivalentMethod(Class type, Method method) {

    for (Method x : type.getMethods())
      if (methodEquality(method, x))
        return x;

    return null;
  }

  private static boolean methodEquality(Method a, Method b) {

    if (!a.getName().equals(b.getName()))
      return false;

    if (!a.getReturnType().equals(b.getReturnType()))
      return false;

    Class<?>[] params1 = a.getParameterTypes();
    Class<?>[] params2 = b.getParameterTypes();

    if (params1.length != params2.length)
      return false;

    for (int i = 0; i < params1.length; i++)
      if (params1[i] != params2[i])
        return false;

    return true;
  }

}
