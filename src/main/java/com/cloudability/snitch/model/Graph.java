package com.cloudability.snitch.model;

import com.google.common.collect.ImmutableList;

public class Graph {
  public final GraphType graphType;
  public final String title;
  public final String subTitle;
  public final String yAxisTitle;
  public final String xAxisTitle;
  public final String[] xAxisData;
  public final ImmutableList<SeriesData> seriesData;

  private Graph(Builder builder) {
    graphType = builder.graphType;
    title = builder.title;
    subTitle = builder.subTitle;
    yAxisTitle = builder.yAxisTitle;
    xAxisTitle = builder.xAxisTitle;
    xAxisData = builder.xAxisData;
    seriesData = builder.dataPoints;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private GraphType graphType;
    private String title;
    private String subTitle;
    private String yAxisTitle;
    private String xAxisTitle;
    private String[] xAxisData;
    private ImmutableList<SeriesData> dataPoints;

    private Builder() {
    }

    public Builder withGraphType(GraphType graphType) {
      this.graphType = graphType;
      return this;
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withSubTitle(String subTitle) {
      this.subTitle = subTitle;
      return this;
    }

    public Builder withYAxisTitle(String yAxisTitle) {
      this.yAxisTitle = yAxisTitle;
      return this;
    }

    public Builder withXAxisTitle(String xAxisTitle) {
      this.xAxisTitle = xAxisTitle;
      return this;
    }

    public Builder withXAxisData(String[] xAxisData) {
      this.xAxisData = xAxisData;
      return this;
    }

    public Builder withDataPoints(ImmutableList<SeriesData> dataPoints) {
      this.dataPoints = dataPoints;
      return this;
    }

    public Graph build() {
      return new Graph(this);
    }
  }
}
