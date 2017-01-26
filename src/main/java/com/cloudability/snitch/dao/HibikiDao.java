package com.cloudability.snitch.dao;

import static com.cloudability.snitch.SnitchServer.MAPPER;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Account;
import com.cloudability.snitch.model.hibiki.HibikiComparisonResponse;
import com.cloudability.snitch.model.hibiki.HibikiPostRequest;
import com.cloudability.snitch.model.hibiki.HibikiResponse;
import com.cloudability.streams.Gullectors;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class HibikiDao {
  private static final Logger log = LogManager.getLogger();
  public final String baseUrl;

  public HibikiDao(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public double getCompare(ImmutableList<Account> accounts) {

    ImmutableList<String> accountIdentifiers = accounts.stream()
        .map(account -> account.accountIdentifier)
        .collect(Gullectors.toImmutableList());

    HibikiPostRequest post = new HibikiPostRequest(accountIdentifiers);
    Optional<HibikiComparisonResponse> response = Optional.empty();
    try {
      String postJson = MAPPER.writeValueAsString(post);
      StringEntity entity = new StringEntity(postJson, ContentType.APPLICATION_JSON);
      response = RestUtil.httpPostRequest(baseUrl + "/compare", entity, HibikiComparisonResponse.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to call Alexandria", e);
    }

    return response.get().result.products.ec2.costComparison.get(7).costs.totalEstimatedSavings;
  }

  public Optional<HibikiResponse> getPlan(ImmutableList<Account> accounts) {
    ImmutableList<String> accountIdentifiers = accounts.stream()
        .map(account -> account.accountIdentifier)
        .collect(Gullectors.toImmutableList());

    HibikiPostRequest post = new HibikiPostRequest(accountIdentifiers);
    Optional<HibikiResponse> response = Optional.empty();
    try {
      String postJson = MAPPER.writeValueAsString(post);
      StringEntity entity = new StringEntity(postJson, ContentType.APPLICATION_JSON);
      response = RestUtil.httpPostRequest(baseUrl, entity, HibikiResponse.class);
      return response;
    } catch (JsonProcessingException e) {
      log.error("Unable to call Alexandria", e);
    }
    return Optional.empty();
  }
}
