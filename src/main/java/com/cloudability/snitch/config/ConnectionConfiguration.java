package com.cloudability.snitch.config;

public interface ConnectionConfiguration {

  String getHostname();

  Integer getPort();

  String getUser();

  String getPassword();

  String getDbName();

  int getConnectionPoolSize();

  int getConnectionTimeout();

  String getDriver();

  public String getUrl();

}
