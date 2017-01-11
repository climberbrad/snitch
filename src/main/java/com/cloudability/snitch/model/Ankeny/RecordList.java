package com.cloudability.snitch.model.Ankeny;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonFormat(shape=JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({ "name", "entry" })
public class RecordList {
  public String name;
  public AggregateEntry entry;

  public RecordList() {
  }

}
