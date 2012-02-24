package org.chris_martin.jfie;

interface ClassToObjectFunction {

  /**
   * @param trace A stack trace of constructors used to detect constructor dependency cycles
   */
  <T> JfieReport<T> apply(Class<T> type, FactoryList trace);

}
