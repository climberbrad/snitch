package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Organization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OrgDao {
  private static final Logger log = LogManager.getLogger();
  private final SnitchDbConnectionManager connectionManager;

  private static final String SELECT_ACTIVE_ORGS = "SELECT "
      + "id,name,created_at,subscription_state,updated_at,company_size,company_sector,latest_daily_mail_sendable_date "
      + "FROM organizations "
      + "WHERE subscription_state='active'";

  public OrgDao(SnitchDbConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  public ImmutableList<Organization> getActiveOrgs() {
    ImmutableList.Builder<Organization> organizationBuilder = ImmutableList.builder();

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_ORGS)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int orgId = rs.getInt(1);
          String orgName = rs.getString(2);
          organizationBuilder.add(new Organization(orgId, orgName));
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return organizationBuilder.build();
  }
}
