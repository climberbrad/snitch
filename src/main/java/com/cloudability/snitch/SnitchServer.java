package com.cloudability.snitch;

import com.cloudability.platform.server.CloudabilityApp;
import com.cloudability.platform.server.CloudabilityService;
import com.cloudability.snitch.api.SnitchResource;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.configuration.Configuration;

/**
 * Snitch give out org info
 */
public class SnitchServer extends CloudabilityApp {
  public static final ObjectMapper MAPPER = new ObjectMapper();

  protected void configure(CloudabilityService.Builder cldyServiceBuilder, Configuration configuration) {
    cldyServiceBuilder
        .withAppName("Snitch")
        .withJsonMapper(MAPPER)
        .withResource(new SnitchResource());
  }

  public static void main(String... args) throws Exception {
    new SnitchServer().run(args);
  }

}
