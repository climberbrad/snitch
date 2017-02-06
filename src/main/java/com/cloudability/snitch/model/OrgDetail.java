package com.cloudability.snitch.model;

public class OrgDetail {
  public final String id;
  public final String subscriptionStartsAt;
  public final int numReservations;
  public final double savingsFromPlan;
  public final String lastLogin;
  public final int numRisExpiringNextMonth;
  public String dateOfLastRiPurchase;
  public String planLastExecuted;
  public int numLoginsLastMonth;
  public int numLoginsLastTwoMonth;
  public String numTotalPageLoads;
  public String numPlannerPageLoads;
  public int numCustomWidgetsCreated;
  public String lastDataSyncDate;
  public int numAwsServices;
  public long underutilized;

  public OrgDetail(
      String id,
      String subscriptionStartsAt,
      int numReservations,
      double savingsFromPlan,
      String lastLogin,
      int numRisExpiringNextMonth,
      String dateOfLastRiPurchase,
      String planLastExecuted,
      int numLoginsLastMonth,
      int numLoginsLastTwoMonth,
      String numTotalPageLoads,
      String numPlannerPageLoads,
      int numCustomWidgetsCreated,
      String lastDataSyncDate,
      int numAwsServices,
      long underutilized
  ) {
    this.id = id;
    this.subscriptionStartsAt = subscriptionStartsAt;
    this.numReservations = numReservations;
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
    this.lastDataSyncDate = lastDataSyncDate;
    this.numAwsServices = numAwsServices;
    this.underutilized = underutilized;
  }
}
