package com.cloudability.snitch.model;

public class Account {
  public final String accountIdentifier;
  public final boolean isPayer;

  public Account(String accountIdentifier, boolean isPayer) {
    this.accountIdentifier = accountIdentifier;
    this.isPayer = isPayer;
  }
}
