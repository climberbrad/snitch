package com.cloudability.snitch.dao;

import static com.cloudability.snitch.dao.RedshiftDao.LOGIN_STAT_WINDOW.DAILY_INCREMENT;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.cloudability.snitch.model.UserLogins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// import com.amazon.redshift.jdbc41.DataSource;

public class RedshiftDao {
  private static final Logger log = LogManager.getLogger();
  private final SnitchDbConnectionManager connectionManager;
  public final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

  private static final String SELECT_MONTHLY_LOGINS = "SELECT name, count(name), date_part('month', sent_at) as month "
      + "FROM success.dashboard_index "
      + "where env = 'production' "
      + "and org_id =  ? "
      + "and sent_at >= ? "
      + "and sent_at <= ? "
      + "group by month, name";

  private static final String SELECT_DAILY_LOGINS = "SELECT name, count(name), date_part('day', sent_at) as day "
      + "FROM success.dashboard_index "
      + "where env = 'production' "
      + "and org_id = ?"
      + "and sent_at >= ?"
      + "and sent_at <= ?"
      + "group by day, name";

  public static final String COUNT_LOGINS = "SELECT count(*) "
      + "FROM success.dashboard_index "
      + "where env = 'production' "
      + "and org_id =  ? "
      + "and sent_at >= ? "
      + "and sent_at <= ?";

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

  private static final String COUNT_PAGE_LOADS = "SELECT count(*) "
      + "FROM success.pages as p "
      + "JOIN success.users_login as l ON p.user_id = l.user_id  "
      + "where l.org_id = ? "
      + "and l.received_at >= ? "
      + "and l.received_at <= ? "
      + "and p.received_at >= ? "
      + "and p.received_at <= ? "
      + "limit 1";

  private static final String PAGE_LOAD_STATS = "SELECT context_page_title, count(*) "
      + "FROM success.pages as p  "
      + "JOIN success.users_login as l ON p.user_id = l.user_id   "
      + "where l.org_id = ? "
      + "and p.received_at >= ?"
      + "and p.received_at <= ? "
      + "and l.received_at >= ? "
      + "and l.received_at <= ? "
      + "group by context_page_title";

  public static final String COUNT_PLANNER_PAGE_LOADS = "SELECT count(*) "
      + "FROM success.pages as p "
      + "JOIN success.users_login as l ON p.user_id = l.user_id  "
      + "where p.received_at > '2016-12-01'  "
      + "and l.received_at > '2016-12-01'  "
      + "and l.org_id = ? "
      + "and path = '/reserved_instance_planner' "
      + "limit 1";

  public static final String COUNT_CUSTOM_COST_REPORTS = "SELECT count(*) "
      + "FROM success.api_reports_costcube_create "
      + "where org_id = ? "
      + "limit 1";

  public static final String COUNT_CUSTOM_USAGE_REPORTS = "SELECT count(*) "
      + "FROM success.api_reports_usagecube_create "
      + "where org_id = ? "
      + "limit 1";

  public RedshiftDao(SnitchDbConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  public ImmutableList<UserLogins> getLoginData(String orgId, Instant startDate, Instant endDate, LOGIN_STAT_WINDOW window) {
    ImmutableList.Builder<UserLogins> loginStatBuilder = ImmutableList.builder();
    Map<String, UserLogins> loginsMap = new HashMap<>();

    String sql = window == DAILY_INCREMENT ? SELECT_DAILY_LOGINS : SELECT_MONTHLY_LOGINS;

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, orgId);
      stmt.setString(2, DB_DATE_FORMATTER.format(startDate));
      stmt.setString(3, DB_DATE_FORMATTER.format(endDate));

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
          planLastExecutedDate = rs.getString(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return planLastExecutedDate;
  }

  public String getLoginCount(String orgId, Instant after, Instant before) {
    String numLoginsLastMonth = "";
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(COUNT_LOGINS)) {
      stmt.setString(1, orgId);
      stmt.setString(2, DB_DATE_FORMATTER.format(after));
      stmt.setString(3, DB_DATE_FORMATTER.format(before));

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          numLoginsLastMonth = rs.getString(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return numLoginsLastMonth;
  }

  public String totalPageLoadCount(String orgId, Instant after, Instant before) {
    String numLoginsLastMonth = "";
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(COUNT_PAGE_LOADS)) {
      stmt.setString(1, orgId);
      stmt.setString(2, DB_DATE_FORMATTER.format(after));
      stmt.setString(3, DB_DATE_FORMATTER.format(before));
      stmt.setString(4, DB_DATE_FORMATTER.format(after));
      stmt.setString(5, DB_DATE_FORMATTER.format(before));

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          numLoginsLastMonth = rs.getString(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return numLoginsLastMonth;
  }

  public String getTotalPlanerPageLoads(String orgId) {
    String numPlannerPageLoads = "";
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(COUNT_PLANNER_PAGE_LOADS)) {
      stmt.setString(1, orgId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          numPlannerPageLoads = rs.getString(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return numPlannerPageLoads;
  }

  public ImmutableMap<String, Integer> getPageLoads(String orgId, Instant after, Instant before) {
    ImmutableMap.Builder<String, Integer> pageLoadMap = ImmutableMap.builder();

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(PAGE_LOAD_STATS)) {
      stmt.setString(1, orgId);
      stmt.setString(2, DB_DATE_FORMATTER.format(after));
      stmt.setString(3, DB_DATE_FORMATTER.format(before));
      stmt.setString(4, DB_DATE_FORMATTER.format(after));
      stmt.setString(5, DB_DATE_FORMATTER.format(before));

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          pageLoadMap.put(rs.getString(1), rs.getInt(2));
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get page load count", ex);
    }
    return pageLoadMap.build();
  }

  public int getNumCustomWidgetsCreated(String orgId) {
    int numCostWidgets = 0;
    int numUSageWidgets = 0;

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(COUNT_CUSTOM_COST_REPORTS)) {
      stmt.setString(1, orgId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          numCostWidgets = rs.getInt(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }

    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(COUNT_CUSTOM_USAGE_REPORTS)) {
      stmt.setString(1, orgId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          numUSageWidgets = rs.getInt(1);
        }
      }
    } catch (Exception ex) {
      log.error("Unable to get Active Orgs", ex);
    }
    return numCostWidgets + numUSageWidgets;
  }

  public static enum LOGIN_STAT_WINDOW {
    MONTHLY_INCREMENT,
    DAILY_INCREMENT
  }
}
