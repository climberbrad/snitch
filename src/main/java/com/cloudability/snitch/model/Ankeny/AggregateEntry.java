package com.cloudability.snitch.model.Ankeny;

public class AggregateEntry {
  public String name;
  public String min;
  public String max;
  public String sum;
  public String count;

  public AggregateEntry() {
  }

  public AggregateEntry(String name, String min, String max, String sum, String count) {
    this.name = name;
    this.min = min;
    this.max = max;
    this.sum = sum;
    this.count = count;
  }
}
