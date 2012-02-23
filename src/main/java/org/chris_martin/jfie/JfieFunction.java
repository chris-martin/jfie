package org.chris_martin.jfie;

interface JfieFunction<A, B> {

  /**
   * @param trace A stack trace of constructors used to detect constructor dependency cycles
   */
  JfieReport<B> apply(A arg, FactoryList trace);

}
