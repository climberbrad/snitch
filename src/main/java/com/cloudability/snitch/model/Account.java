package com.cloudability.snitch.model;

public class Account {
  public final String subscriptionStartsAt;
  public final int groupId;
  public final boolean isPrimary;
  public final String accountIdentifier;

  public Account(String subscriptionStartsAt, int groupId, String accountIdentifier, boolean isPrimary) {
    this.subscriptionStartsAt = subscriptionStartsAt;
    this.groupId = groupId;
    this.isPrimary = isPrimary;
    this.accountIdentifier = accountIdentifier;
  }
}
