package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Ankeny.AnkenyPostData;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;

import java.util.Optional;

public class AnkenyDao {
  public static final String ANKENY_BASE_URL = "";

  public Optional<AnkenyResponse> getMontlyCostData(
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

    return RestUtil.httpPostRequest(ANKENY_BASE_URL, data);
  }
}
