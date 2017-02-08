package com.cloudability.snitch.dao;

import static com.cloudability.snitch.AccountUtil.getLinkedAccountIdentifiers;
import static com.cloudability.snitch.AccountUtil.getPayerAccountIdentifiers;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Ankeny.AnkenyCostPerServiceResponse;
import com.cloudability.snitch.model.Ankeny.AnkenyPostData;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;
import com.cloudability.snitch.model.PayerAccount;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.ws.rs.client.Client;

public class AnkenyDao {
  private static final Logger log = LogManager.getLogger();
  public final String ankenyBaseUrl;
  public final Client client;
  public final DateTimeFormatter ANKENY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

  public AnkenyDao(Client client, String ankenyBaseUrl) {
    this.client = client;
    this.ankenyBaseUrl = ankenyBaseUrl;
  }

  public Optional<AnkenyResponse> getTotalSpend(
      String orgId,
      ImmutableList<PayerAccount> payerAccounts,
      int groupId,
      Instant startDate,
      Instant endDate) {
    AnkenyPostData data = AnkenyPostData.newBuilder()
        .withAccount_identifiers(getPayerAccountIdentifiers(payerAccounts))
        .withDimensions(ImmutableList.of("month"))
        .withMetrics(ImmutableList.of("invoiced_cost"))
        .withOrganization_id(Integer.valueOf(orgId))
        .withGroup_id(groupId)
        .withLinked_account_identifiers(getLinkedAccountIdentifiers(payerAccounts))
        .withStart_at(ANKENY_DATE_FORMATTER.format(startDate))
        .withEnd_at(ANKENY_DATE_FORMATTER.format(endDate))
        .build();

    try {
      return RestUtil.genericPost(client, ankenyBaseUrl, data, AnkenyResponse.class);
    } catch (Exception ex) {
      log.error("Unable to get Ankeny data", ex);
    }
    return Optional.empty();
  }

  public Optional<AnkenyCostPerServiceResponse> getCostPerServicePerMonth(
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
        .withStart_at(ANKENY_DATE_FORMATTER.format(startDate))
        .withEnd_at(ANKENY_DATE_FORMATTER.format(endDate))
        .build();

    return RestUtil.genericPost(client, ankenyBaseUrl, data, AnkenyCostPerServiceResponse.class);
  }
}
