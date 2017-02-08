package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.LinkedAccount;
import com.cloudability.snitch.model.PayerAccount;

import javax.ws.rs.client.Client;

public class PipelineDao {
  private final String baseUrl;
  private final Client client;

  public PipelineDao(Client client, String baseUrl) {
    this.client = client;
    this.baseUrl = baseUrl;
  }

  public ImmutableList<PayerAccount> getAccounts(String orgId) {
    ImmutableList<PayerAccount> payerAccounts = RestUtil.getAccounts(client, baseUrl + "/" + orgId);
    for (PayerAccount payerAccount : payerAccounts) {
      payerAccount.payer_account_id = hyphenate(payerAccount.payer_account_id);
      for (LinkedAccount linkedAccount : payerAccount.accounts) {
        linkedAccount.account_id = hyphenate(linkedAccount.account_id);
      }
    }
    return payerAccounts;
  }

  private String hyphenate(String accountId) {
    return accountId.substring(0, 4) + "-" + accountId.substring(4, 8) + "-" + accountId.substring(8, 12);
  }
}
