package com.cloudability.snitch.model;

public class SeriesData {
  public final String name;
  public final double[] data;

  public SeriesData(String name, double[] data) {
    this.name = name;
    this.data = data;
  }
}
