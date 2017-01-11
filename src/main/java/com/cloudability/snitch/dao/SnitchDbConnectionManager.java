package com.cloudability.snitch.dao;

import com.google.common.util.concurrent.AbstractIdleService;

import com.cloudability.snitch.config.ConnectionConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class SnitchDbConnectionManager extends AbstractIdleService {

  private static final Logger log = LogManager.getLogger();
  private final HikariDataSource dataSource;

  public SnitchDbConnectionManager(final ConnectionConfiguration jdbcConfig) {
  HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setMaximumPoolSize(jdbcConfig.getConnectionPoolSize());
//    hikariConfig.setDataSourceClassName(jdbcConfig.getDriver());
    hikariConfig.setJdbcUrl(jdbcConfig.getUrl() );
    hikariConfig.addDataSourceProperty("serverName", jdbcConfig.getHostname());
    hikariConfig.addDataSourceProperty("port", jdbcConfig.getPort());
    hikariConfig.addDataSourceProperty("databaseName", jdbcConfig.getDbName());
    hikariConfig.addDataSourceProperty("user", jdbcConfig.getUser());
    hikariConfig.addDataSourceProperty("password", jdbcConfig.getPassword());
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
    dataSource.setConnectionTimeout(jdbcConfig.getConnectionTimeout());
    this.dataSource = dataSource;
    log.debug("Datasource Created for: {}", jdbcConfig.getDbName());
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
