package com.cloudability.snitch;

import com.cloudability.facts.FactsWebContextFilter;
import com.cloudability.platform.server.CloudabilityApp;
import com.cloudability.platform.server.CloudabilityService;
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
import com.cloudability.snitch.healthcheck.SnitchHealthCheck;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import org.apache.commons.configuration.Configuration;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;



/**
 * Snitch give out org info
 */
public class SnitchServer extends CloudabilityApp {
  public static final ObjectMapper MAPPER = new ObjectMapper()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);


  protected void configure(CloudabilityService.Builder cldyServiceBuilder, Configuration configuration) {

    JdbcConfig jdbcConfig = new JdbcConfig(configuration);
    RedshiftConfig redshiftConfig = new RedshiftConfig(configuration);
    SnitchDbConnectionManager redshiftConnectionManager = new SnitchDbConnectionManager(redshiftConfig);
    SnitchDbConnectionManager connectionManager = new SnitchDbConnectionManager(jdbcConfig);

    String ankenyBaseUrl = configuration.getString("ankeny.baseUrl");
    String alexandriaBaseUrl = configuration.getString("alexandria.baseUrl");
    String hibikiBaseUrl = configuration.getString("hibiki.baseUrl");
    String pipelineAccountUrl = configuration.getString("account.baseUrl");

    GuiDao guiDao = new GuiDao(connectionManager);
    Client jerseyClient = createHttpClient(MAPPER);

    cldyServiceBuilder
        .withAppName("Snitch")
        .withHealthCheck(new SnitchHealthCheck())
        .withJsonMapper(MAPPER)
        .withServices(connectionManager)
        .withStaticContent("", Resource.newClassPathResource("webapp", true, false))
        .withResource(new AccountUtil(new PipelineDao(jerseyClient, pipelineAccountUrl)))
        .withResource(new SnitchResource(
            new SnitchRequestBroker(
                guiDao,
                new AnkenyDao(jerseyClient, ankenyBaseUrl),
                new RedshiftDao(redshiftConnectionManager),
                new AlexandriaDao(jerseyClient, alexandriaBaseUrl),
                new HibikiDao(jerseyClient, hibikiBaseUrl))
        ));
  }

  public static void main(String... args) throws Exception {
    new SnitchServer().run(args);
  }

  private static synchronized Client createHttpClient(ObjectMapper mapper) {
    Client client = ClientBuilder.newClient()
        .register(FactsWebContextFilter.buildClientFilter())
        .register(new JacksonJsonProvider(mapper))
        .register(GZipEncoder.class)
        .register(EncodingFilter.class)
        .property(org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT, 1000)
        .property(org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT, 10000)
        .property(org.glassfish.jersey.client.ClientProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);

    if (!(client instanceof JerseyClient)) {
      throw new IllegalStateException("Unexpected client library found!");
    }
    return client;
  }

}
