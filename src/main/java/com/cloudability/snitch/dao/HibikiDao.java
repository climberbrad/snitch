package com.cloudability.snitch.dao;

import static com.cloudability.snitch.AccountUtil.getAllAccountIdentifiersWithDashes;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.PayerAccount;
import com.cloudability.snitch.model.hibiki.HibikiComparisonResponse;
import com.cloudability.snitch.model.hibiki.HibikiPostRequest;
import com.cloudability.snitch.model.hibiki.HibikiResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import javax.ws.rs.client.Client;

public class HibikiDao {
  private static final Logger log = LogManager.getLogger();
  public final String baseUrl;
  private final Client client;

  public HibikiDao(Client client, String baseUrl) {
    this.client = client;
    this.baseUrl = baseUrl;
  }

  public double getComparisonData(ImmutableList<PayerAccount> payerAccounts) {

    HibikiPostRequest post = new HibikiPostRequest(getAllAccountIdentifiersWithDashes(payerAccounts));
    Optional<HibikiComparisonResponse> response = Optional.empty();
    try {
      response = RestUtil.genericPost(client, baseUrl + "/compare", post, HibikiComparisonResponse.class);
    } catch (Exception e) {
      log.error("Unable to call Alexandria", e);
    }
    double totalEstimatedSavings = response.get().result.products.ec2.costComparison.get(7).costs.totalEstimatedSavings;
    return response.get().result != null ? totalEstimatedSavings : 0;
  }

  public Optional<HibikiResponse> getPlan(ImmutableList<PayerAccount> payerAccounts) {
    HibikiPostRequest post = new HibikiPostRequest(getAllAccountIdentifiersWithDashes(payerAccounts));
    Optional<HibikiResponse> response;
    try {
      response = RestUtil.genericPost(client, baseUrl, post, HibikiResponse.class);
      return response;
    } catch (Exception e) {
      log.error("Unable to call Alexandria", e);
    }
    return Optional.empty();
  }
}
