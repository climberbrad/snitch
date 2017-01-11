package com.cloudability.snitch.dao;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.UserLogins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import com.amazon.redshift.jdbc41.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class RedshiftDao {
  private static final Logger log = LogManager.getLogger();
  private final SnitchDbConnectionManager connectionManager;

  private static final String SELECT_LOGINS = "SELECT "
      + "user_id, sum(sign_in_count) as logins, date_part('month', timestamp) as month "
      + "FROM success.users_login "
      + "where env = 'production' "
      + "and org_id = ? "
      + "and timestamp > '2016-01-01' "
      + "and timestamp < '2016-12-31' "
      + "group by month, user_id";

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
          String userId = rs.getString(1);
          int numLogins = rs.getInt(2);
          int month = rs.getInt(3);

          UserLogins loginStat = loginsMap.get(userId);
          if(loginStat == null) {
            loginStat = new UserLogins(userId);
            loginsMap.put(userId, loginStat);
          }
          loginStat.addMontlyCount(month, numLogins);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }

    for(String userId : loginsMap.keySet()) {
      loginStatBuilder.add(loginsMap.get(userId));
    }
    return loginStatBuilder.build();
  }


}
