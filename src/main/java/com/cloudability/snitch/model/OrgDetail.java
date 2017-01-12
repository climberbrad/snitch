package com.cloudability.snitch.model;

public class OrgDetail {
  public final String id;
  public final int numReservations;
  public final int numAccounts;

  public OrgDetail(String id, int numReservations, int numAccounts) {
    this.id = id;
    this.numReservations = numReservations;
    this.numAccounts = numAccounts;
  }
}
