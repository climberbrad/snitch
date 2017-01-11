package com.cloudability.snitch.model;

public class Account {
  public final int groupId;
  public final boolean isPrimary;
  public final String accountIdentifier;

  public Account(int groupId, String accountIdentifier, boolean isPrimary) {
    this.groupId = groupId;
    this.isPrimary = isPrimary;
    this.accountIdentifier = accountIdentifier;
  }
}
