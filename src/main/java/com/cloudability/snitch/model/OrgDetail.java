package com.cloudability.snitch.model;

public class OrgDetail {
  public final String id;
  public final String subscriptionStartsAt;
  public final int numReservations;
  public final int numAccounts;
  public final double savingsFromPlan;
  public final String lastLogin;
  public final int numRisExpiringNextMonth;
  public String dateOfLastRiPurchase;
  public String planLastExecuted;
  public String numLoginsLastMonth;
  public String numLoginsLastTwoMonth;
  public String numTotalPageLoads;
  public String numPlannerPageLoads;
  public int numCustomWidgetsCreated;

  public OrgDetail(
      String id,
      String subscriptionStartsAt,
      int numReservations,
      int numAccounts,
      double savingsFromPlan,
      String lastLogin,
      int numRisExpiringNextMonth,
      String dateOfLastRiPurchase,
      String planLastExecuted,
      String numLoginsLastMonth,
      String numLoginsLastTwoMonth,
      String numTotalPageLoads,
      String numPlannerPageLoads,
      int numCustomWidgetsCreated
  ) {
    this.id = id;
    this.subscriptionStartsAt = subscriptionStartsAt;
    this.numReservations = numReservations;
    this.numAccounts = numAccounts;
    this.savingsFromPlan = savingsFromPlan;
    this.lastLogin = lastLogin;
    this.numRisExpiringNextMonth = numRisExpiringNextMonth;
    this.dateOfLastRiPurchase = dateOfLastRiPurchase;
    this.planLastExecuted = planLastExecuted;
    this.numLoginsLastMonth = numLoginsLastMonth;
    this.numLoginsLastTwoMonth = numLoginsLastTwoMonth;
    this.numTotalPageLoads = numTotalPageLoads;
    this.numPlannerPageLoads = numPlannerPageLoads;
    this.numCustomWidgetsCreated = numCustomWidgetsCreated;
  }
}
