package com.cloudability.snitch.model;

public class OrgDetail {
  public final String id;
  public final String subscriptionStartsAt;
  public final int numReservations;
  public final String savingsFromPlan;
  public final String lastLogin;
  public final int numRisExpiringNextMonth;
  public final String dateOfLastRiPurchase;
  public final String planLastExecuted;
  public final int numLoginsLastMonth;
  public final int numLoginsLastTwoMonth;
  public final String numTotalPageLoads;
  public final String numPlannerPageLoads;
  public final int numCustomWidgetsCreated;
  public final String lastDataSyncDate;
  public int numAwsServices = 0;
  public final long underutilized;
  public final String costLastMonth;

  public OrgDetail(
      String id,
      String subscriptionStartsAt,
      int numReservations,
      String savingsFromPlan,
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
      long underutilized,
      String costLastMonth
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
    this.costLastMonth = costLastMonth;
  }
}
