package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Ankeny.AnkenyCostPerServiceResponse;
import com.cloudability.snitch.model.Ankeny.AnkenyPostData;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;

import java.util.Optional;

public class AnkenyDao {
  public final String ankenyBaseUrl;

  public AnkenyDao(String ankenyBaseUrl) {
    this.ankenyBaseUrl = ankenyBaseUrl;
  }

  public Optional<AnkenyResponse> getTotalMontlyCostData(
      int orgId,
      int groupId,
      String payerAccountId,
      ImmutableList<String> acccountIdentifiers)
  {
    AnkenyPostData data = AnkenyPostData.newBuilder()
        .withAccount_identifiers(ImmutableList.of(payerAccountId))
        .withDimensions(ImmutableList.of("month"))
        .withMetrics(ImmutableList.of("invoiced_cost"))
        .withOrganization_id(orgId)
        .withGroup_id(groupId)
        .withLinked_account_identifiers(acccountIdentifiers)
        .withBackend("olap")
        .withStart_at("2016-01-01")
        .withEnd_at("2016-12-31")
        .build();
    return RestUtil.ankenyTotalSpendRequest(ankenyBaseUrl, data);
  }

  public Optional<AnkenyCostPerServiceResponse> getMontlyCostPerService(
      int orgId,
      int groupId,
      String payerAccountId,
      ImmutableList<String> acccountIdentifiers) {

    AnkenyPostData data = AnkenyPostData.newBuilder()
        .withAccount_identifiers(ImmutableList.of(payerAccountId))
        .withDimensions(ImmutableList.of("service_name", "month"))
        .withMetrics(ImmutableList.of("invoiced_cost"))
        .withOrganization_id(orgId)
        .withGroup_id(groupId)
        .withLinked_account_identifiers(acccountIdentifiers)
        .withBackend("olap")
        .withStart_at("2016-01-01")
        .withEnd_at("2016-12-31")
        .build();
    return RestUtil.multiServiceAnkenyPostRequest(ankenyBaseUrl, data);
  }
}
