package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.AccountCache;
import com.cloudability.snitch.model.OrgSearchResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GuiDao {
  private static final Logger log = LogManager.getLogger();
  private final SnitchDbConnectionManager connectionManager;

  private static final String SELECT_SUBSCRIPTION_START_DATE = "SELECT "
      + "date(created_at) "
      + "FROM organizations "
      + "WHERE id = ?";

  private static final String SELECT_ACTIVE_ORGS = "SELECT "
      + "id,name "
      + "FROM organizations "
      + "WHERE subscription_state='active'";

  private static final String SELECT_LAST_SYNC_DATE = "SELECT date(max(last_bill_fetched_at)) "
      + "FROM credentials "
      + "WHERE account_identifier in ";

  private static final String SELECT_GROUP_ID = "SELECT group_id "
      + "FROM organizations where id = ?";

  public GuiDao(SnitchDbConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  public ImmutableList<OrgSearchResult> getActiveOrgList() {
    ImmutableList.Builder<OrgSearchResult> organizationBuilder = ImmutableList.builder();

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_ORGS)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int orgId = rs.getInt(1);
          String orgName = rs.getString(2);
          organizationBuilder.add(new OrgSearchResult(orgId, orgName));
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return organizationBuilder.build();
  }

  public String getLastDataSyncDate(String orgId) {
    String lastSyncDate = "";
    StringBuilder sqlBuilder = new StringBuilder(SELECT_LAST_SYNC_DATE);

    ImmutableList<String> accountIdentifiers = AccountCache.getAllAccountIdentifiersWithDashes(orgId);

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

  public String getSubscriptionStartDate(String orgId) {
    String subscriptionStartDate = "";
    try (Connection conn = connectionManager.getConnection();
    PreparedStatement stmt = conn.prepareStatement(SELECT_SUBSCRIPTION_START_DATE)) {
      stmt.setString(1, orgId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          subscriptionStartDate = rs.getString(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return subscriptionStartDate;
  }

  public int getGroupId(String orgId) {
    int groupId = 0;
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_GROUP_ID)) {
      stmt.setString(1, orgId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          groupId = rs.getInt(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return groupId;
  }
}
