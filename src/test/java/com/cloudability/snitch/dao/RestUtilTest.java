package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Ankeny.AnkenyPostData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

public class RestUtilTest {
  private final String ANKENY_URL = " http://ankeny.s.cloudability.org/cost/report";
  public static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void postToAnkeny() throws JsonProcessingException {
    AnkenyPostData data = AnkenyPostData.newBuilder()
        .withAccount_identifiers(ImmutableList.of("4253-5587-2505"))
        .withDimensions(ImmutableList.of("month"))
        .withMetrics(ImmutableList.of("invoiced_cost"))
        .withOrganization_id(4857)
        .withGroup_id(1858)
        .withLinked_account_identifiers(ImmutableList.of(
            "1269-7970-4565",
            "3382-7344-4847",
            "9887-1179-8652",
            "3382-7344-4847",
            "9887-1179-8652",
            "4253-5587-2505",
            "3382-7344-4847",
            "1269-7970-4565",
            "9887-1179-8652",
            "1269-7970-4565",
            "9887-1179-8652",
            "3382-7344-4847",
            "b2e5e975e1b2ed30c011be51acb9f0995d90566f9390a8ea35c6a64254a5fc8b",
            "mat@cloudability.com",
            "matellis",
            "tecnh",
            "teccura",
            "cloudability",
            "newrelic-platform",
            "CloudabilityArchive",
            "589575",
            "ceb987fb8dcbb5336e47ab59b",
            "1f0202262cba3ed6bfbcb3b709e3f6abf5138bf018f14d628a7d56469978e48f",
            "52438",
            "e0d7ac226308ca2878cbe60d5d1a95c59fd01ef8976d4d20063ce0bc4e1ec809",
            "b91dccfa3b40d7b8c0fea09f12abb0b08dba196d6b44ef59bec3ce854a353beb",
            "3449cc1e1a029f3ed4dea66e5bbd429d036d7ff1380c170fb8e014ffc3b41533",
            "61268799130651",
            "e0d7ac226308ca2878cbe60d5d1a95c59fd01ef8976d4d20063ce0bc4e1ec809"
        ))
        .withStart_at("2016-01-01")
        .withEnd_at("2016-01-31")
        .build();

//    Optional<AnkenyResponse> response = RestUtil.httpPostRequest(AnkenyDao.ANKENY_BASE_URL, data);
//    System.out.println(MAPPER.writeValueAsString(response.get()));

  }

}
