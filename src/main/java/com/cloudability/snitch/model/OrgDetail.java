package com.cloudability.snitch.model;

public class OrgDetail {
  public final String id;
  public final String subscriptionStartsAt;
  public final int numReservations;
  public final int numAccounts;
  public final double savingsFromPlan;

  public OrgDetail(String id, String subscriptionStartsAt, int numReservations, int numAccounts, double savingsFromPlan) {
    this.id = id;
    this.subscriptionStartsAt = subscriptionStartsAt;
    this.numReservations = numReservations;
    this.numAccounts = numAccounts;
    this.savingsFromPlan = savingsFromPlan;
  }
}
