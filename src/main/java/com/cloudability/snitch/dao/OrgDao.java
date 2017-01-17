package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Account;
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

  private static final String SELECT_ACCOUNTS = "SELECT subscription_starts_at, orgs.group_id, ca.account_identifier, ca.is_primary "
      + "FROM organizations AS orgs  "
      + "JOIN credentials AS creds ON orgs.id = creds.organization_id "
      + "JOIN credential_accounts AS ca ON ca.credential_id = creds.id "
      + "JOIN service_accounts AS sa ON sa.account_identifier = ca.account_identifier "
      + "WHERE orgs.id =? "
      + "group by ca.account_identifier";

  private static final String SELECT_LAST_SYNC_DATE = "SELECT date(max(last_bill_fetched_at)) "
      + "FROM credentials "
      + "WHERE account_identifier in ";

  public OrgDao(SnitchDbConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  public ImmutableList<Account> getAccounts(String orgId) {
    ImmutableList.Builder<Account> accountBuilder = ImmutableList.builder();

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_ACCOUNTS)) {
      stmt.setString(1, orgId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String subscriptionStartsAt = rs.getString(1);
          int groupId = rs.getInt(2);
          String accountIdentifier = rs.getString(3);
          Boolean isPrimary = rs.getBoolean(4);
          if(accountIdentifier.contains("-") && accountIdentifier.length() == 14) {
            accountBuilder.add(new Account(subscriptionStartsAt, groupId, accountIdentifier, isPrimary));
          }
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return accountBuilder.build();
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

  public String getLastDataSyncDate(ImmutableList<String> accountIdentifiers) {
    String lastSyncDate = "";
    StringBuilder sqlBuilder = new StringBuilder(SELECT_LAST_SYNC_DATE);

    sqlBuilder.append("(");
    for(int i=0;i<accountIdentifiers.size();i++) {
      if(i > 0) {
        sqlBuilder.append(",");
      }
      sqlBuilder.append("?");
    }
    sqlBuilder.append(")");



    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
      for(int i=0;i<accountIdentifiers.size();i++) {
        stmt.setString(i+1, accountIdentifiers.get(i));
      }

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          lastSyncDate = rs.getString(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return lastSyncDate;
  }
}
