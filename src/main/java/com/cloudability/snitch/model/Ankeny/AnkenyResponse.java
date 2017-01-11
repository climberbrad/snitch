package com.cloudability.snitch.model.Ankeny;

import java.util.List;

public class AnkenyResponse {
  public String start_at;
  public String end_at;
  public List<String> fields;
  public List<RecordList> records;
  public List<AggregateEntry> aggregates;
  public List<String> global_errors;
  public int generationTime;
  public int count;

  public AnkenyResponse() {
  }
}
