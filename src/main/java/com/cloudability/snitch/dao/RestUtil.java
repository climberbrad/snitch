package com.cloudability.snitch.dao;

import static com.cloudability.snitch.SnitchServer.MAPPER;

import com.google.common.io.CharStreams;

import com.cloudability.snitch.model.Ankeny.AnkenyPostData;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

public class RestUtil {
  private static final Logger log = LogManager.getLogger();

  public static Optional<AnkenyResponse> httpPostRequest(String url, AnkenyPostData post) {
    try {
      StringEntity entity = new StringEntity(MAPPER.writeValueAsString(post), ContentType.APPLICATION_JSON);
      return httpPostRequest(url, entity, AnkenyResponse.class);
    } catch (Exception ex) {
      log.error("Unable to make Ankeny request", ex);
    }
    return Optional.empty();
  }

  private static <T> Optional<T> httpPostRequest(
      final String url,
      final HttpEntity entity,
      final Class<T> expectedResultType) {
    CloseableHttpResponse response = null;
    String body;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(url);
      httpPost.addHeader(HttpHeaders.ACCEPT, "application/json");
      httpPost.setEntity(entity);
      response = httpClient.execute(httpPost);
      body = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
    } catch (IOException ex) {
      throw new RuntimeException("Hibiki Server not available", ex);
    } finally {
      if (response != null) {
        try {
          response.close();
        } catch (IOException ex) {
          log.error(ex);
        }
      }
    }
    JsonNode json = null;
    try {
      json = MAPPER.readTree(body);
    } catch (IOException e) {
      log.error("Failed to parse as JSON.", response, e);
    }

    try {
      T result = MAPPER.treeToValue(json, expectedResultType);
      return Optional.ofNullable(result);
    } catch (Exception ex) {
      log.error(body, response, ex);
    }
    throw new IllegalStateException("Unable to get parse response");
  }
}
