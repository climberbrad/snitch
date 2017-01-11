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

  private static final String SELECT_ACCOUNTS = "SELECT "
      + "orgs.name, orgs.id AS org_id, sa.id AS account_id, ca.account_identifier "
      + "FROM organizations AS orgs  "
      + "JOIN credentials AS creds ON orgs.id = creds.organization_id "
      + "JOIN credential_accounts AS ca ON ca.credential_id = creds.id "
      + "JOIN service_accounts AS sa ON sa.account_identifier = ca.account_identifier "
      + "WHERE orgs.id = ?";

  public OrgDao(SnitchDbConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  public ImmutableList<String> getLinkedAccounts(String orgId) {
    ImmutableList.Builder<String> linkedAccountBuilder = ImmutableList.builder();

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_ACCOUNTS)) {
      stmt.setString(1, orgId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String accountIdentifier = rs.getString(3);
          linkedAccountBuilder.add(accountIdentifier);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return linkedAccountBuilder.build();
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
