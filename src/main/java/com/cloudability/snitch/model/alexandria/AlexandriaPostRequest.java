package com.cloudability.snitch.model.alexandria;

import com.google.common.collect.ImmutableList;

public class AlexandriaPostRequest {
  public final ImmutableList<String> accounts;
  public final int limit = 0;
  public final int offset = 0;
  public final ImmutableList<String> filter;

  public AlexandriaPostRequest(ImmutableList<String> accounts, ImmutableList<String> filters) {
    this.accounts = accounts;
    this.filter = filters;
  }
}
