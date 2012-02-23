package org.chris_martin.jfie;

import java.util.ArrayList;
import java.util.List;

import static org.chris_martin.jfie.JfieException.Problem;

public final class JfieReport<T> {

  final T result;
  final List<Problem> problems = new ArrayList<Problem>();

  private JfieReport(T result, List<Problem> problems) {
    this.result = result;
  }

  static <T> JfieReport<T> nullReport(List<Problem> problems) {
    JfieReport<T> x = new JfieReport<T>(null, problems);
    x.problems.addAll(problems);
    return x;
  }

  static <T> JfieReport<T> newReport(T result, List<Problem> problems) {
    JfieReport<T> x = new JfieReport<T>(result, problems);
    x.problems.addAll(problems);
    return x;
  }

}
