package com.cloudability.snitch;

import com.cloudability.platform.server.CloudabilityApp;
import com.cloudability.platform.server.CloudabilityService;
import com.cloudability.snitch.api.SnitchResource;
import com.cloudability.snitch.config.DatabaseConfig;
import com.cloudability.snitch.dao.OrgDao;
import com.cloudability.snitch.dao.SnitchDbConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.configuration.Configuration;

/**
 * Snitch give out org info
 */
public class SnitchServer extends CloudabilityApp {
  public static final ObjectMapper MAPPER = new ObjectMapper();

  protected void configure(CloudabilityService.Builder cldyServiceBuilder, Configuration configuration) {

    DatabaseConfig databaseConfig = new DatabaseConfig(configuration);
    SnitchDbConnectionManager connectionManager = new SnitchDbConnectionManager(databaseConfig);

    cldyServiceBuilder
        .withAppName("Snitch")
        .withJsonMapper(MAPPER)
        .withServices(connectionManager)
        .withResource(new SnitchResource(new OrgDao(connectionManager)));
  }

  public static void main(String... args) throws Exception {
    new SnitchServer().run(args);
  }

}
