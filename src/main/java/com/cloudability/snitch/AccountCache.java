package com.cloudability.snitch;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.dao.PipelineDao;
import com.cloudability.snitch.model.PayerAccount;
import com.cloudability.streams.Gullectors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class AccountCache {
  private static final HashMap<String, ImmutableList<PayerAccount>> CACHE = new HashMap<>();
  private static PipelineDao pipelineDao;

  public AccountCache(PipelineDao pipelineDao) {
    this.pipelineDao = pipelineDao;
  }

  public static ImmutableList<PayerAccount> getPayerAccounts(String orgId) {
    if (CACHE.get(orgId) == null) {
      addAccount(orgId);
    }
    return CACHE.get(orgId);
  }

  public static ImmutableList<String> getPayerAccountIdentifiers(String orgId) {
    if (CACHE.get(orgId) == null) {
      addAccount(orgId);
    }

    return CACHE.get(orgId).stream()
        .map(payerAccount -> payerAccount.payer_account_id)
        .collect(Gullectors.toImmutableList());
  }

  public static ImmutableList<String> getLinkedAccountIdentifiers(String orgId) {
    if (CACHE.get(orgId) == null) {
      addAccount(orgId);
    }

    return CACHE.get(orgId).stream()
        .flatMap(payerAccount -> payerAccount.accounts.stream())
        .map(account -> account.account_id)
        .collect(Gullectors.toImmutableList());
  }

  public static ImmutableList<String> getAllAccountIdentifiersNoDashes(String orgId) {
    Set<String> accountIdentifierSet = new HashSet<>();
    accountIdentifierSet.addAll(getPayerAccountIdentifiers(orgId));
    accountIdentifierSet.addAll(getLinkedAccountIdentifiers(orgId));

    return getAllAccountIdentifiersWithDashes(orgId).stream()
        .map(account -> account.replace("-", ""))
        .collect(Gullectors.toImmutableList());
  }

  public static ImmutableList<String> getAllAccountIdentifiersWithDashes(String orgId) {
    Set<String> accountIdentifierSet = new HashSet<>();
    accountIdentifierSet.addAll(getPayerAccountIdentifiers(orgId));
    accountIdentifierSet.addAll(getLinkedAccountIdentifiers(orgId));

    return ImmutableList.copyOf(accountIdentifierSet);
  }

  private static void addAccount(String orgId) {
    CACHE.put(orgId, pipelineDao.getAccounts(orgId));
  }
}
