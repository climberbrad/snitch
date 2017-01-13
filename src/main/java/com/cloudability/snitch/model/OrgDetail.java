package com.cloudability.snitch.model;

public class OrgDetail {
  public final String id;
  public final String subscriptionStartsAt;
  public final int numReservations;
  public final int numAccounts;
  public final double savingsFromPlan;
  public final String lastLogin;
  public final int numRisExpiringNextMonth;

  public OrgDetail(
      String id,
      String subscriptionStartsAt,
      int numReservations,
      int numAccounts,
      double savingsFromPlan,
      String lastLogin,
      int numRisExpiringNextMonth
  ) {
    this.id = id;
    this.subscriptionStartsAt = subscriptionStartsAt;
    this.numReservations = numReservations;
    this.numAccounts = numAccounts;
    this.savingsFromPlan = savingsFromPlan;
    this.lastLogin = lastLogin;
    this.numRisExpiringNextMonth = numRisExpiringNextMonth;
  }
}
