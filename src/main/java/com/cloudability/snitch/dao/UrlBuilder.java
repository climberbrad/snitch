package com.cloudability.snitch.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import org.glassfish.jersey.uri.UriComponent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UrlBuilder {
  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneId.of("UTC"));

  public final String baseUrl;

  private List<String> params = new ArrayList<>();
  private Map<String, List<String>> postParams = new HashMap<>();

  public UrlBuilder(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public UrlBuilder add(String name, String value) {
    if (value != null) {
      List<String> values = postParams.get(name);
      if (values == null) {
        values = new ArrayList<>();
        postParams.put(name, values);
      }
      values.add(value);
      params.add(encode(name) + "=" + encode(value));
    }
    return this;
  }

  public UrlBuilder addRawKvPair(String kvPair) {
    if (kvPair != null) {
      parseParams(kvPair).entrySet().stream()
          .forEach(
              entry -> entry.getValue().stream()
                  .forEach(value -> add(entry.getKey(), value))
          );
    }
    return this;
  }

  public static Map<String, List<String>> parseParams(String urlParams) {
    Map<String, List<String>> map = new HashMap<>();
    String[] kvPairs = urlParams.split("&");
    for (String kvPair : kvPairs) {
      String[] kv = kvPair.split("=", -1);
      List<String> values = map.get(kv[0]);
      if (values == null) {
        values = new ArrayList<>();
        map.put(kv[0], values);
      }
      values.add(kv[1]);
    }
    return map;
  }

  public UrlBuilder addHour(String name, Instant instant) {
    if (instant != null) {
      add(name, DATE_TIME_FORMATTER.format(instant.truncatedTo(ChronoUnit.HOURS)));
    }
    return this;
  }

  public UrlBuilder add(String name, ImmutableList<String> list) {
    if (list != null) {
      return add(name, list.stream().collect(Collectors.joining(",")));
    }
    return this;
  }

  public UrlBuilder add(String name, Double value) {
    if (value != null) {
      return add(name, String.format("%.2f", value));
    }
    return this;
  }

  public UrlBuilder add(String name, Boolean value) {
    if (value != null) {
      return add(name, value.toString());
    }
    return this;
  }

  public String build() {
    if (params.isEmpty()) {
      return baseUrl;
    }
    return baseUrl + "?" + params.stream().collect(Collectors.joining("&"));
  }

  public static String encode(String unsafe) {
    return UriComponent.contextualEncode(unsafe, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED, false);
  }

  public Map<String, List<String>> getPostParams() {
    return postParams;
  }

  public Map<String, String> getPostJson() {
    Map<String, String> json = new HashMap<>();
    for(Map.Entry<String, List<String>> entry : postParams.entrySet()) {
      json.put(entry.getKey(), Joiner.on(",").join(entry.getValue()));
    }
    return json;
  }
}
