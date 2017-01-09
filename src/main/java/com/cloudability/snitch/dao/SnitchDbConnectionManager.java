package com.cloudability.snitch.dao;

import com.google.common.util.concurrent.AbstractIdleService;

import com.cloudability.snitch.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class SnitchDbConnectionManager extends AbstractIdleService {

  private static final Logger log = LogManager.getLogger();
  private final HikariDataSource dataSource;

  public SnitchDbConnectionManager(final DatabaseConfig databaseConfig) {
  HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setMaximumPoolSize(databaseConfig.connectionPoolSize);
    hikariConfig.setDataSourceClassName(databaseConfig.driver);
    hikariConfig.addDataSourceProperty("serverName", databaseConfig.hostname);
    hikariConfig.addDataSourceProperty("port", databaseConfig.port);
    hikariConfig.addDataSourceProperty("databaseName", databaseConfig.dbName);
    hikariConfig.addDataSourceProperty("user", databaseConfig.user);
    hikariConfig.addDataSourceProperty("password", databaseConfig.password);
    hikariConfig.addDataSourceProperty("serverTimezone", "UTC");
    hikariConfig.addDataSourceProperty("useLegacyDatetimeCode", false);
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
    hikariConfig.setIdleTimeout(180000);
    hikariConfig.setMaxLifetime(1800000);
    hikariConfig.setInitializationFailFast(true);
    hikariConfig.setConnectionTestQuery("SELECT current_timestamp");

  HikariDataSource dataSource = new HikariDataSource(hikariConfig);
    dataSource.setConnectionTimeout(databaseConfig.connectionTimeout);
    this.dataSource = dataSource;
    log.debug("Datasource Created for: {}", databaseConfig.dbName);
}

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Override
  protected void startUp() throws Exception {
    // do nothing
  }

  @Override
  protected void shutDown() throws Exception {
    dataSource.close();
  }
}
