package com.cloudability.snitch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PayerAccount {
  public String payer_account_id;
  public String nick_name;
  public List<LinkedAccount> accounts;

  public PayerAccount(
      @JsonProperty("payer_account_id") String payer_account_id,
      @JsonProperty("nick_name") String nick_name,
      @JsonProperty("accounts") List<LinkedAccount> accounts)
  {
    this.payer_account_id = payer_account_id;
    this.nick_name = nick_name;
    this.accounts = accounts;
  }
}
