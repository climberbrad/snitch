package com.cloudability.snitch.model.Ankeny;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

  @JsonFormat(shape=JsonFormat.Shape.ARRAY)
  @JsonPropertyOrder({ "serviceName", "index", "entry" })
public class MultiRecordList {
    public String serviceName;
    public String index;
    public AggregateEntry entry;

    public MultiRecordList() {
    }
}
