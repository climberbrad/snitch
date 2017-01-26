package com.cloudability.snitch.model;

import com.google.common.collect.ImmutableList;

public class PieChart {
  public final Chart chart;
  public final Title title;
  public final PlotOptions plotOptions = new PlotOptions();
  public final ImmutableList<PieChartSeries> series;

  public PieChart(Chart chart, Title title, ImmutableList<PieChartSeries> series) {
    this.chart = chart;
    this.title = title;
    this.series = series;
  }
}
