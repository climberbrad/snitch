package com.cloudability.snitch.dao;

import static com.cloudability.snitch.AccountUtil.getAllAccountIdentifiersNoDashes;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.PayerAccount;
import com.cloudability.snitch.model.alexandria.AlexandriaPostRequest;
import com.cloudability.snitch.model.alexandria.AlexandriaResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import javax.ws.rs.client.Client;

public class AlexandriaDao {
  private static final Logger log = LogManager.getLogger();
  public final String baseUrl;
  private final Client client;

  public AlexandriaDao(Client client, String baseUrl) {
    this.client = client;
    this.baseUrl = baseUrl;
  }

  /**
   * Number of current RIs
   *
   * @param accountIdentifiers
   * @return
   */
  public int getActiveRiCount(ImmutableList<PayerAccount> accountIdentifiers) {
    ImmutableList<String> filters = ImmutableList.of("state==active");

    AlexandriaPostRequest post = new AlexandriaPostRequest(getAllAccountIdentifiersNoDashes(accountIdentifiers), filters, 0);
    Optional<AlexandriaResponse> response = Optional.empty();
    try {
      response = RestUtil.genericPost(client, baseUrl, post,AlexandriaResponse.class);
    } catch (Exception e) {
      log.error("Unable to call Alexandria", e);
    }

    return (response.isPresent() && response.get().aggregate != null) ? response.get().aggregate.quantity : 0;
  }

  /**
   *
   * @param payerAccounts
   * @return
   */
  public int getNumRisExpiringNextMonth(ImmutableList<PayerAccount> payerAccounts) {

    long nextMonth = System.currentTimeMillis() + 2592000000l;

    ImmutableList<String> filters = ImmutableList.of(
        "state==active",
        "end > " + System.currentTimeMillis(),
        "end < " + nextMonth
    );

    AlexandriaPostRequest post = new AlexandriaPostRequest(getAllAccountIdentifiersNoDashes(payerAccounts), filters, 0);
    Optional<AlexandriaResponse> response = Optional.empty();
    try {
      response = RestUtil.genericPost(client, baseUrl, post, AlexandriaResponse.class);
    } catch (Exception e) {
      log.error("Unable to call Alexandria", e);
    }

    return (response.isPresent() && response.get().aggregate != null) ? response.get().aggregate.quantity : 0;
  }

  /**
   *
   * @param payerAccounts
   * @return
   */
  public long getDateOfLastRiPurchase(ImmutableList<PayerAccount> payerAccounts) {
    ImmutableList<String> filters = ImmutableList.of(
        "state==active"
    );

    AlexandriaPostRequest post = new AlexandriaPostRequest(getAllAccountIdentifiersNoDashes(payerAccounts), filters, 1);
    Optional<AlexandriaResponse> response = Optional.empty();
    try {
      response = RestUtil.genericPost(client, baseUrl, post, AlexandriaResponse.class);
    } catch (Exception e) {
      log.error("Unable to call Alexandria", e);
    }

    return (response.isPresent() && response.get().result != null && response.get().result.size() > 0) ? Long.valueOf(response.get().result.get(0).start).longValue() : 0;
  }

}
