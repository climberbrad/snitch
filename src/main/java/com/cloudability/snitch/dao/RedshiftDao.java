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

  private static final String SELECT_LATEST_LOGIN = "SELECT name, date(sent_at) "
      + "FROM success.dashboard_index "
      + "where env = 'production' "
      + "and org_id =  ? "
      + "order by sent_at desc "
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
}
