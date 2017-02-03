package com.cloudability.snitch;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.dao.PipelineDao;
import com.cloudability.snitch.model.PayerAccount;
import com.cloudability.streams.Gullectors;

import java.util.HashSet;
import java.util.Set;

public final class AccountUtil {
  private static PipelineDao pipelineDao;

  public AccountUtil(PipelineDao pipelineDao) {
    this.pipelineDao = pipelineDao;
  }

  public static ImmutableList<PayerAccount> getAccounts(String orgId) {
    return pipelineDao.getAccounts(orgId);
  }

  public static ImmutableList<String> getPayerAccountIdentifiers(ImmutableList<PayerAccount> payerAccounts) {
    return payerAccounts.stream()
        .map(payerAccount -> payerAccount.payer_account_id)
        .collect(Gullectors.toImmutableList());
  }

  public static ImmutableList<String> getLinkedAccountIdentifiers(ImmutableList<PayerAccount> payerAccounts) {
    return payerAccounts.stream()
        .flatMap(payerAccount -> payerAccount.accounts.stream())
        .map(account -> account.account_id)
        .collect(Gullectors.toImmutableList());
  }

  public static ImmutableList<String> getAllAccountIdentifiersNoDashes(ImmutableList<PayerAccount> payerAccounts) {
    Set<String> accountIdentifierSet = new HashSet<>();
    accountIdentifierSet.addAll(getPayerAccountIdentifiers(payerAccounts));
    accountIdentifierSet.addAll(getLinkedAccountIdentifiers(payerAccounts));

    return getAllAccountIdentifiersWithDashes(payerAccounts).stream()
        .map(account -> account.replace("-", ""))
        .collect(Gullectors.toImmutableList());
  }

  public static ImmutableList<String> getAllAccountIdentifiersWithDashes(ImmutableList<PayerAccount> payerAccounts) {
    Set<String> accountIdentifierSet = new HashSet<>();
    accountIdentifierSet.addAll(getPayerAccountIdentifiers(payerAccounts));
    accountIdentifierSet.addAll(getLinkedAccountIdentifiers(payerAccounts));

    return ImmutableList.copyOf(accountIdentifierSet);
  }
}
