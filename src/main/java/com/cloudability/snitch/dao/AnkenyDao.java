package com.cloudability.snitch.dao;

import static com.cloudability.snitch.AccountUtil.getLinkedAccountIdentifiers;
import static com.cloudability.snitch.AccountUtil.getPayerAccountIdentifiers;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Ankeny.AnkenyCostPerServiceResponse;
import com.cloudability.snitch.model.Ankeny.AnkenyPostData;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;
import com.cloudability.snitch.model.PayerAccount;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AnkenyDao {
  public final String ankenyBaseUrl;
  public final DateTimeFormatter ANKENY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

  public AnkenyDao(String ankenyBaseUrl) {
    this.ankenyBaseUrl = ankenyBaseUrl;
  }

  public Optional<AnkenyResponse> getTotalMontlyCostData(
      String orgId,
      ImmutableList<PayerAccount> payerAccounts,
      int groupId,
      Instant startDate,
      Instant endDate)
  {
    AnkenyPostData data = AnkenyPostData.newBuilder()
        .withAccount_identifiers(getPayerAccountIdentifiers(payerAccounts))
        .withDimensions(ImmutableList.of("month"))
        .withMetrics(ImmutableList.of("invoiced_cost"))
        .withOrganization_id(Integer.valueOf(orgId))
        .withGroup_id(groupId)
        .withLinked_account_identifiers(getLinkedAccountIdentifiers(payerAccounts))
        .withBackend("olap")
        .withStart_at(ANKENY_DATE_FORMATTER.format(startDate))
        .withEnd_at(ANKENY_DATE_FORMATTER.format(endDate))
        .build();
    return RestUtil.ankenyTotalSpendRequest(ankenyBaseUrl, data);
  }

  public Optional<AnkenyCostPerServiceResponse> getCostPerService(
      String orgId,
      ImmutableList<PayerAccount> payerAccounts,
      int groupId,
      Instant startDate,
      Instant endDate) {

    AnkenyPostData data = AnkenyPostData.newBuilder()
        .withAccount_identifiers(getPayerAccountIdentifiers(payerAccounts))
        .withDimensions(ImmutableList.of("service_name", "month"))
        .withMetrics(ImmutableList.of("invoiced_cost"))
        .withOrganization_id(Integer.valueOf(orgId))
        .withGroup_id(groupId)
        .withLinked_account_identifiers(getLinkedAccountIdentifiers(payerAccounts))
        .withBackend("olap")
        .withStart_at(ANKENY_DATE_FORMATTER.format(startDate))
        .withEnd_at(ANKENY_DATE_FORMATTER.format(endDate))
        .build();
    return RestUtil.multiServiceAnkenyPostRequest(ankenyBaseUrl, data);
  }
}
