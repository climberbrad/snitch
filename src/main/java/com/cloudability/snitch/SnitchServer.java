package com.cloudability.snitch;

import com.cloudability.platform.server.CloudabilityApp;
import com.cloudability.platform.server.CloudabilityService;
import com.cloudability.snitch.api.SnitchHealthCheck;
import com.cloudability.snitch.api.SnitchResource;
import com.cloudability.snitch.config.JdbcConfig;
import com.cloudability.snitch.config.RedshiftConfig;
import com.cloudability.snitch.dao.AlexandriaDao;
import com.cloudability.snitch.dao.AnkenyDao;
import com.cloudability.snitch.dao.GuiDao;
import com.cloudability.snitch.dao.HibikiDao;
import com.cloudability.snitch.dao.PipelineDao;
import com.cloudability.snitch.dao.RedshiftDao;
import com.cloudability.snitch.dao.SnitchDbConnectionManager;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.configuration.Configuration;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Snitch give out org info
 */
public class SnitchServer extends CloudabilityApp {
  public static final ObjectMapper MAPPER = new ObjectMapper()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  protected void configure(CloudabilityService.Builder cldyServiceBuilder, Configuration configuration) {

    JdbcConfig jdbcConfig = new JdbcConfig(configuration);
    RedshiftConfig redshiftConfig = new RedshiftConfig(configuration);
    SnitchDbConnectionManager redshiftConnectionManager = new SnitchDbConnectionManager(redshiftConfig);
    SnitchDbConnectionManager connectionManager = new SnitchDbConnectionManager(jdbcConfig);

    String ankenyBaseUrl = configuration.getString("ankeny.baseUrl");
    String alexandriaBaseUrl = configuration.getString("alexandria.baseUrl");
    String hibikiBaseUrl = configuration.getString("hibiki.baseUrl");
    String pipelineAccountUrl = configuration.getString("account.baseUrl");

    PipelineDao pipelineDao = new PipelineDao(pipelineAccountUrl);
    GuiDao guiDao = new GuiDao(connectionManager);
    cldyServiceBuilder
        .withAppName("Snitch")
        .withHealthCheck(new SnitchHealthCheck())
        .withJsonMapper(MAPPER)
        .withServices(connectionManager)
        .withStaticContent("/tamtool", Resource.newClassPathResource("webapp", true, false))
        .withResource(new AccountUtil(new PipelineDao(pipelineAccountUrl)))
        .withResource(new SnitchResource(
            new OrgDataBroker(
                guiDao,
                new AnkenyDao(ankenyBaseUrl),
                new RedshiftDao(redshiftConnectionManager),
                new AlexandriaDao(alexandriaBaseUrl),
                new HibikiDao(hibikiBaseUrl))
        ));
  }

  public static void main(String... args) throws Exception {
    new SnitchServer().run(args);
  }

}
