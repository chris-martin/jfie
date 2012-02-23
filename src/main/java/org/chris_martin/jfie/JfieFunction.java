package org.chris_martin.jfie;

interface JfieFunction<A, B> {

  JfieReport<B> apply(A arg, FactoryList trace);

}
