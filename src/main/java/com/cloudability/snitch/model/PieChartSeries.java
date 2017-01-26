package com.cloudability.snitch.model;

public class PieChartSeries {
  public final String name;
  public final boolean colorByPoint = true;
  public final DataPoint[] data;

  public PieChartSeries(String name, DataPoint[] data) {
    this.name = name;
    this.data = data;
  }
}
