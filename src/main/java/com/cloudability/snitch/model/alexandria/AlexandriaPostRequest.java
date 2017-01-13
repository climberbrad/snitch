package com.cloudability.snitch.model.alexandria;

import com.google.common.collect.ImmutableList;

public class AlexandriaPostRequest {
  public final ImmutableList<String> accounts;
  public int limit;
  public final int offset = 0;
  public final String sortField = "start";
  public final ImmutableList<String> filter;

  public AlexandriaPostRequest(ImmutableList<String> accounts, ImmutableList<String> filters, int limit) {
    this.accounts = accounts;
    this.filter = filters;
    this.limit = limit;
  }
}
