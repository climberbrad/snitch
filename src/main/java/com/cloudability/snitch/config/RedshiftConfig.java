package com.cloudability.snitch.config;

import org.apache.commons.configuration.Configuration;

public class RedshiftConfig implements ConnectionConfiguration {
  public static final String DB_HOST = "redshift.host";
  public static final String DB_PORT = "redshift.port";
  public static final String DB_USER = "redshift.user";
  public static final String DB_PASSWORD = "redshift.password";
  public static final String DB_NAME = "redshift.name";
  public static final String DB_CONNECTION_POOL_SIZE = "redshift.connection.pool.size";
  public static final String DB_CONNECTION_TIMEOUT = "redshift.connection.timeout";
  public static final String DB_DRIVER = "redshift.driver";
  public static final String URL = "redshift.url";

  public final String hostname;
  public final Integer port;
  public final String user;
  public final String password;
  public final String dbName;
  public final int connectionPoolSize;
  public final int connectionTimeout;
  public final String driver;
  public final String url;

  public RedshiftConfig(final Configuration configuration) {
    this.hostname = configuration.getString(DB_HOST);
    this.port = configuration.getInt(DB_PORT);
    this.user = configuration.getString(DB_USER);
    this.password = configuration.getString(DB_PASSWORD);
    this.dbName = configuration.getString(DB_NAME);
    this.connectionPoolSize = configuration.getInt(DB_CONNECTION_POOL_SIZE);
    this.connectionTimeout = configuration.getInt(DB_CONNECTION_TIMEOUT);
    this.driver = configuration.getString(DB_DRIVER);
    this.url = configuration.getString(URL);
  }

  @Override
  public String getHostname() {
    return hostname;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getDbName() {
    return dbName;
  }

  @Override
  public int getConnectionPoolSize() {
    return connectionPoolSize;
  }

  @Override
  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  @Override
  public String getDriver() {
    return driver;
  }

  @Override
  public String getUrl() {
    return url;
  }
}
