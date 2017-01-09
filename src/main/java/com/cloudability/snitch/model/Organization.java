package com.cloudability.snitch.model;

import com.google.common.collect.ImmutableList;

public class Organization {
  public final ImmutableList<Account> account;

  public Organization(ImmutableList<Account> account) {
    this.account = account;
  }
}
