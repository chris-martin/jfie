package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.List;

import static org.chris_martin.jfie.JfieException.Problem;

public final class JfieReport<T> {

  final T result;
  final List<Problem> exceptions = new ArrayList<Problem>();

  private JfieReport(T result, List<Problem> exceptions) {
    this.result = result;
  }

  static <T> JfieReport<T> newReport(T result, List<Problem> exceptions) {
    JfieReport<T> x = new JfieReport<T>(result, exceptions);
    x.exceptions.addAll(exceptions);
    return x;
  }

  public T getResult() {
    return result;
  }

  public List<Problem> getExceptions() {
    return exceptions;
  }

}
