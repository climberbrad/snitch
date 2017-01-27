package com.cloudability.snitch.model;

import com.google.common.collect.ImmutableList;

public class Organization {
  public final String id;
  public final String name;
  public final ImmutableList<Account> accounts;

  public Organization(String id, String name, ImmutableList<Account> accounts) {
    this.id = id;
    this.name = name;
    this.accounts = accounts;
  }
}
