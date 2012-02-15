package org.chris_martin.jfie;

import java.util.ArrayList;

import static org.chris_martin.jfie.JfieException.FactoryCycle.factoryCycle;

interface FactoryList {

  FactoryList add(Factory factory);

  JfieException.FactoryCycle checkForCycle(Class type);

}

final class FactoryLists {

  private FactoryLists() { }

  static FactoryList factoryList() {
    return new Impl();
  }

  static FactoryList factoryList(Iterable<Factory> factories) {
    Impl x = new Impl();
    for (Factory factory : factories) x.add(factory);
    return x;
  }

  private static class Impl implements FactoryList {

    private final ArrayList<Factory> list = new ArrayList<Factory>();

    @Override
    public Impl add(Factory factory) {
      Impl x = new Impl();
      x.list.addAll(list);
      x.list.add(factory);
      return x;
    }

    @Override
    public JfieException.FactoryCycle checkForCycle(Class type) {

      for (Factory factory : list)
        if (factory.manufacturedType() == type)
          return factoryCycle(list.subList(list.indexOf(factory), list.size()));

      return null;
    }

  }

}
