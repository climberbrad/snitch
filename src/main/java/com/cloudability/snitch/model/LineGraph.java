package com.cloudability.snitch.model;

import com.google.common.collect.ImmutableList;

public class LineGraph {
  public final Chart chart;
  public final Title title;
  public final XAxis xAxis;
  public final ImmutableList<SeriesData> series;

  public LineGraph(Chart chart, Title title, XAxis xAxis, ImmutableList<SeriesData> series) {
    this.chart = chart;
    this.title = title;
    this.xAxis = xAxis;
    this.series = series;
  }
}
