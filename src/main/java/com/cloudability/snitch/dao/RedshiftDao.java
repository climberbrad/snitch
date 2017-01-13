package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.UserLogins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

// import com.amazon.redshift.jdbc41.DataSource;

public class RedshiftDao {
  private static final Logger log = LogManager.getLogger();
  private final SnitchDbConnectionManager connectionManager;

  private static final String SELECT_LOGINS = "SELECT name, count(name), date_part('month', sent_at) as month "
      + "FROM success.dashboard_index "
      + "where env = 'production' "
      + "and org_id =  ? "
      + "and sent_at > '2016-01-01' "
      + "and sent_at < '2016-12-31' "
      + "group by month, name";

  public static final String COUNT_LOGINS = "SELECT count(*) "
      + "FROM success.dashboard_index "
      + "where env = 'production' "
      + "and org_id =  ? "
      + "and sent_at > ? "
      + "and sent_at < ?";

  private static final String SELECT_LATEST_LOGIN = "SELECT name, date(sent_at) "
      + "FROM success.dashboard_index "
      + "where env = 'production' "
      + "and org_id =  ? "
      + "order by sent_at desc "
      + "limit 1";

  private static final String SELECT_LAST_RI_PLAN_RUN = "SELECT date(max(p.received_at)) "
      + "FROM success.pages as p "
      + "JOIN success.users_login as l ON p.user_id = l.user_id "
      + "where p.received_at > '2016-12-01' "
      + "and l.received_at > '2016-12-01' "
      + "and path = '/reserved_instance_planner' "
      + "and l.org_id = ? "
      + "limit 1";

  public RedshiftDao(SnitchDbConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  public ImmutableList<UserLogins> getLoginData(String orgId) {
    ImmutableList.Builder<UserLogins> loginStatBuilder = ImmutableList.builder();
    Map<String, UserLogins> loginsMap = new HashMap<>();

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_LOGINS)) {
      stmt.setString(1, orgId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String userName = rs.getString(1);
          int numLogins = rs.getInt(2);
          int month = rs.getInt(3);

          UserLogins loginStat = loginsMap.get(userName);
          if (loginStat == null) {
            loginStat = new UserLogins(userName);
            loginsMap.put(userName, loginStat);
          }
          loginStat.addMontlyCount(month, numLogins);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }

    for (String userId : loginsMap.keySet()) {
      loginStatBuilder.add(loginsMap.get(userId));
    }
    return loginStatBuilder.build();
  }

  public String getLatestLogin(String orgId) {
    String loginDate = "";
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_LATEST_LOGIN)) {
      stmt.setString(1, orgId);


      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String userName = rs.getString(1);
          loginDate = rs.getString(2);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return loginDate;
  }

  public String getLastRiPlanDate(String orgId) {
    String planLastExecutedDate = "";
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_LAST_RI_PLAN_RUN)) {
      stmt.setString(1, orgId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          planLastExecutedDate = rs.getString(1).trim();
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return planLastExecutedDate;
  }

  public String getLoginCount(String orgId, String startDate, String endDate) {
    String numLoginsLastMonth = "";
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(COUNT_LOGINS)) {
      stmt.setString(1, orgId);
      stmt.setString(2, startDate);
      stmt.setString(3, endDate);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          numLoginsLastMonth = rs.getString(1).trim();
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return numLoginsLastMonth;
  }
}
