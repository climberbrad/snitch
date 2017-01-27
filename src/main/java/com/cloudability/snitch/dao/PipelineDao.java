package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Account;
import com.cloudability.snitch.model.AccountGETResult;
import com.cloudability.snitch.model.PipelineAccount;

public class PipelineDao {
  private final String baseUrl;

  public PipelineDao(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public ImmutableList<Account> getAccounts(String orgId) {
    ImmutableList.Builder<Account> accountBuilder = ImmutableList.builder();
    ImmutableList<AccountGETResult> pipelineAccounts = RestUtil.httpGetAccountsRequest(baseUrl + "/" + orgId);
    for(AccountGETResult payerAccount : pipelineAccounts) {

      accountBuilder.add(new Account(hyphenate(payerAccount.payer_account_id), true));
      for(PipelineAccount linkedAccount : payerAccount.accounts) {
        accountBuilder.add(new Account(hyphenate(linkedAccount.account_id), false));
      }
    }
    return accountBuilder.build();
  }

  private String hyphenate(String accountId) {
    return accountId.substring(0, 4) + "-" + accountId.substring(4, 8) + "-" + accountId.substring(8, 12);
  }
}
