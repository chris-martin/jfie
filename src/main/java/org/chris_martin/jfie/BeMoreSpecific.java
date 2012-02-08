package org.chris_martin.jfie;

public class BeMoreSpecific extends RuntimeException {

  static BeMoreSpecific beMoreSpecific() {
    throw new BeMoreSpecific();
  }

}
