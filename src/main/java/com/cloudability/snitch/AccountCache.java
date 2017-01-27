package com.cloudability.snitch;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.dao.PipelineDao;
import com.cloudability.snitch.model.Account;
import com.cloudability.streams.Gullectors;

import java.util.HashMap;

public class AccountCache {
  private final HashMap<String, ImmutableList<Account>> accountCache = new HashMap<>();
  private PipelineDao pipelineDao;

  public AccountCache(PipelineDao pipelineDao) {
    this.pipelineDao = pipelineDao;
  }

  public ImmutableList<Account> getAllAccounts(String orgId) {
    if (accountCache.get(orgId) == null) {
      addAccount(orgId);
    }
    return accountCache.get(orgId);
  }

  public ImmutableList<String> getPayerAccounts(String orgId) {
    if (accountCache.get(orgId) == null) {
      addAccount(orgId);
    }

    return accountCache.get(orgId).stream().filter(account -> account.isPayer)
        .map(acct -> acct.accountIdentifier)
        .collect(Gullectors.toImmutableList());
  }

  public ImmutableList<String> getLinkedAccounts(String orgId) {
    if (accountCache.get(orgId) == null) {
      addAccount(orgId);
    }

    return accountCache.get(orgId).stream().filter(account -> !account.isPayer)
        .map(acct -> acct.accountIdentifier)
        .collect(Gullectors.toImmutableList());
  }

  private void addAccount(String orgId) {
    accountCache.put(orgId, pipelineDao.getAccounts(orgId));
  }
}
