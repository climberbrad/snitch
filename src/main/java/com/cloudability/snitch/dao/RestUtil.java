package com.cloudability.snitch.dao;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.PayerAccount;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class RestUtil {
  private static final Logger log = LogManager.getLogger();
  public static final int ANKENY_TIMEOUT = 39000;

  public static <T, U> Optional<T> genericPost(Client client, String url, U object, Class<T> expectedResultType) {
    Invocation.Builder request = client.target(url)
        .request(APPLICATION_JSON_TYPE)
        .property(READ_TIMEOUT, ANKENY_TIMEOUT);

    T response = request.post(Entity.json(object), expectedResultType);

    return response == null ? Optional.empty() : Optional.of(response);
  }

  public static ImmutableList<PayerAccount> getAccounts(Client client, String url) {
    Invocation.Builder request = client.target(url)
        .request(APPLICATION_JSON_TYPE)
        .property(READ_TIMEOUT, 5000);

    Response response = request.get();
    List<PayerAccount> payerAccounts =
        response.readEntity(new GenericType<List<PayerAccount>>() {});

    return ImmutableList.copyOf(payerAccounts);
  }
}
