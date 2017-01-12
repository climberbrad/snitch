package com.cloudability.snitch.dao;

import static com.cloudability.snitch.SnitchServer.MAPPER;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Account;
import com.cloudability.snitch.model.alexandria.AlexandriaPostRequest;
import com.cloudability.snitch.model.alexandria.AlexandriaResponse;
import com.cloudability.streams.Gullectors;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AlexandriaDao {
  private static final Logger log = LogManager.getLogger();
  public final String baseUrl;

  public AlexandriaDao(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public int getActiveRiCount(ImmutableList<Account> accounts) {
    ImmutableList<String> filters = ImmutableList.of("state==active");

    ImmutableList<String> accountIdentifiers = accounts.stream()
        .map(account -> account.accountIdentifier.replace("-", ""))
        .collect(Gullectors.toImmutableList());

    AlexandriaPostRequest post = new AlexandriaPostRequest(accountIdentifiers, filters);
    Optional<AlexandriaResponse> response = Optional.empty();
    try {
      StringEntity entity = new StringEntity(MAPPER.writeValueAsString(post), ContentType.APPLICATION_JSON);
      response = RestUtil.httpPostRequest(baseUrl, entity, AlexandriaResponse.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to call Alexandria", e);
    }

    return response.isPresent() ? response.get().aggregate.quantity : 0;
  }
}
