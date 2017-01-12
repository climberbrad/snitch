package com.cloudability.snitch.model.hibiki;

import com.google.common.collect.ImmutableList;

public class HibikiPostRequest {
  public final String accounts;
  public final String startDate = "2016-11-01";
  public final String endDate = "2016-11-10";
  public final String term = "one-year";
  public final String excludePricingOptions = "no_upfront, partial_upfront";
  public final String tenancy = "shared,dedicated";
  public final String usageThreshold = "0.0";
  public final String savingsThreshold = "0.0";
  public final String optimizations = "all";
  public final String combineAccounts = "true";
  public final String products = "ec2";
  public final String offeringClass = "standard";

  public HibikiPostRequest(ImmutableList<String> accounts) {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i<accounts.size();i++) {
      if(i>0) {
        sb.append(",");
      }
      sb.append(accounts.get(i));
    }

    this.accounts = sb.toString();
  }
}
