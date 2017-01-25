package com.cloudability.snitch;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.dao.AlexandriaDao;
import com.cloudability.snitch.dao.AnkenyDao;
import com.cloudability.snitch.dao.HibikiDao;
import com.cloudability.snitch.dao.OrgDao;
import com.cloudability.snitch.dao.RedshiftDao;
import com.cloudability.snitch.model.Account;
import com.cloudability.snitch.model.Ankeny.AnkenyCostPerServiceResponse;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;
import com.cloudability.snitch.model.Ankeny.MultiRecordList;
import com.cloudability.snitch.model.Ankeny.RecordList;
import com.cloudability.snitch.model.Chart;
import com.cloudability.snitch.model.Graph;
import com.cloudability.snitch.model.GraphType;
import com.cloudability.snitch.model.OrgDetail;
import com.cloudability.snitch.model.Organization;
import com.cloudability.snitch.model.SeriesData;
import com.cloudability.snitch.model.Title;
import com.cloudability.snitch.model.UserLogins;
import com.cloudability.snitch.model.XAxis;
import com.cloudability.streams.Gullectors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrgDataBroker {
  private final OrgDao orgDao;
  private final AnkenyDao ankenyDao;
  private final RedshiftDao redshiftDao;
  private final AlexandriaDao alexandriaDao;
  private final HibikiDao hibikiDao;

  // caches
  private static final Map<String, ImmutableList<Account>> accountCache = new HashMap<>();
  private static final Map<String, OrgDetail> orgDetailCache = new HashMap<>();

  public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static final String[] ALL_MONTHS_CATEGORY =
      new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  public OrgDataBroker(
      OrgDao orgDao,
      AnkenyDao ankenyDao,
      RedshiftDao redshiftDao,
      AlexandriaDao alexandriaDao,
      HibikiDao hibikiDao)
  {
    this.orgDao = orgDao;
    this.ankenyDao = ankenyDao;
    this.redshiftDao = redshiftDao;
    this.alexandriaDao = alexandriaDao;
    this.hibikiDao = hibikiDao;
  }

  public Graph buildGraph(String orgId, String graphName, Instant startDate, Instant endDate) {
    ImmutableList<Account> accounts = getAccounts(orgId);

    // total logins
    if (graphName.equalsIgnoreCase("logins")) {
      return new Graph(
          new Chart(GraphType.column),
          new Title(graphName),
          new XAxis(ALL_MONTHS_CATEGORY),
          buildLoginGraph(orgId));
    }

    // total spend
    if (graphName.equalsIgnoreCase("totalSpend")) {
      return new Graph(
          new Chart(GraphType.line),
          new Title(graphName),
          new XAxis(ALL_MONTHS_CATEGORY),
          getAwsTotalSpendData(orgId, accounts, startDate, endDate)
      );
    }

    // total spend per service
    if (graphName.equalsIgnoreCase("spendPerService")) {
      return new Graph(
          new Chart(GraphType.area),
          new Title(graphName),
          new XAxis(ALL_MONTHS_CATEGORY),
          getAwsSpendPerServiceData(orgId, accounts, startDate, endDate)
      );
    }

    return null;
  }

  public ImmutableList<Organization> getActiveOrgList() {
    return orgDao.getActiveOrgs();
  }

  public ImmutableList<SeriesData> buildLoginGraph(String orgId) {
    ImmutableList<UserLogins> loginData = redshiftDao.getLoginData(orgId);

    ImmutableList.Builder seriesDataBuilder = ImmutableList.builder();
    for (UserLogins loginStats : loginData) {
      seriesDataBuilder.add(new SeriesData(loginStats.userId, loginStats.getDataPoints()));
    }

    return seriesDataBuilder.build();
  }

  public OrgDetail getOrgDetail(String orgId) {
    if (orgDetailCache.get(orgId) != null) {
      return orgDetailCache.get(orgId);
    }

    ImmutableList<Account> accounts = getAccounts(orgId);
    int activeRiCount = alexandriaDao.getActiveRiCount(accounts);

    int numRisExpiringNextMonth = alexandriaDao.getNumRisExpiringNextMonth(accounts);
    String dateOfLastRiPurchase = DATE_FORMAT.format(alexandriaDao.getDateOfLastRiPurchase(accounts));

    double savingsFromPlan = BigDecimal.valueOf(hibikiDao.getPlan(accounts))
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();


    String planLastExecuted = redshiftDao.getLastRiPlanDate(orgId);
    String lastLogin = redshiftDao.getLatestLogin(orgId);
    String numTotalPageLoads = redshiftDao.getTotalPageLoads(orgId);
    String totalPlannerPageLoads = redshiftDao.getTotalPlanerPageLoads(orgId);
    String numLoginsLastMonth = redshiftDao.getLoginCount(orgId, "2016-12-01", "2016-12-31");
    String numLoginsLastTwoMonth = redshiftDao.getLoginCount(orgId, "2016-11-01", "2016-12-31");
    int numCustomWidgetsCreated = redshiftDao.getNumCustomWidgetsCreated(orgId);


    ImmutableList<SeriesData> serviceSpendLastMonth = getAwsSpendPerServiceData(orgId, accounts, Instant.now().minus(90, ChronoUnit.DAYS), Instant.now());
    int awsServiceCount = serviceSpendLastMonth.size();

    // subscription start for primary account
    String subscriptionStartsAt = accounts.stream()
        .filter(account -> account.isPrimary)
        .map(account -> account.subscriptionStartsAt)
        .findFirst().get();

    ImmutableList<String> payerAccounts = accounts.stream()
        .filter(account -> account.isPrimary)
        .map(account -> account.accountIdentifier)
        .collect(Gullectors.toImmutableList());

    String lastDataSyncDate = orgDao.getLastDataSyncDate(payerAccounts);

    OrgDetail orgDetail = new OrgDetail(
        orgId,
        subscriptionStartsAt,
        activeRiCount,
        accounts.size(),
        savingsFromPlan,
        lastLogin,
        numRisExpiringNextMonth,
        dateOfLastRiPurchase,
        planLastExecuted,
        numLoginsLastMonth,
        numLoginsLastTwoMonth,
        numTotalPageLoads,
        totalPlannerPageLoads,
        numCustomWidgetsCreated,
        lastDataSyncDate,
        awsServiceCount);
    orgDetailCache.put(orgId, orgDetail);

    return orgDetail;
  }

  /**
   * Total AWS spend
   *
   * @param orgId
   * @param accounts
   * @param startDate
   * @param endDate
   * @return
   */
  private ImmutableList<SeriesData> getAwsTotalSpendData(String orgId, ImmutableList<Account> accounts, Instant startDate, Instant endDate) {
    String primaryAccount = accounts.stream()
        .filter(account -> account.isPrimary)
        .map(account -> account.accountIdentifier)
        .findFirst().get();

    ImmutableList<String> linkedAccounts = accounts.stream()
        .filter(account -> account.isPrimary == false)
        .map(account -> account.accountIdentifier)
        .collect(Gullectors.toImmutableList());

    int groupId = accounts.stream().map(account -> account.groupId).findFirst().get();

    Optional<AnkenyResponse> response =
        ankenyDao.getTotalMontlyCostData(Integer.valueOf(orgId), groupId, primaryAccount, linkedAccounts, startDate, endDate);

    List<RecordList> records = response.get().records;
    double[] dataPoints = new double[records.size()];

    for (int i = 0; i < records.size(); i++) {
      dataPoints[i] = Double.valueOf(records.get(i).entry.sum).doubleValue();
    }

    return ImmutableList.of(
        new SeriesData(primaryAccount, dataPoints));
  }

  /**
   * Spend per AWS Service
   *
   * @param orgId
   * @param accounts
   * @param startDate
   * @param endDate
   * @return
   */
  private ImmutableList<SeriesData> getAwsSpendPerServiceData(String orgId, ImmutableList<Account> accounts, Instant startDate, Instant endDate) {
    String primaryAccount = accounts.stream()
        .filter(account -> account.isPrimary)
        .map(account -> account.accountIdentifier)
        .findFirst().get();

    ImmutableList<String> linkedAccounts = accounts.stream()
        .filter(account -> account.isPrimary == false)
        .map(account -> account.accountIdentifier)
        .collect(Gullectors.toImmutableList());

    int groupId = accounts.stream().map(account -> account.groupId).findFirst().get();

    Optional<AnkenyCostPerServiceResponse> response =
        ankenyDao.getCostPerService(Integer.valueOf(orgId), groupId, primaryAccount, linkedAccounts,startDate, endDate);


    List<MultiRecordList> records = response.get().records;

    int numAwsServices = records.size();

    Map<String, double[]> serviceMonthlyData = new HashMap<>();

    for (MultiRecordList record : records) {
      String awsServiceName = record.serviceName;
      int index = Integer.valueOf(record.index).intValue();

      if (serviceMonthlyData.get(awsServiceName) == null) {
        serviceMonthlyData.put(awsServiceName, new double[12]);
      }
      serviceMonthlyData.get(awsServiceName)[index - 1] = Double.valueOf(record.entry.sum).doubleValue();
    }

    ImmutableList.Builder<SeriesData> seriesDataBuilder = ImmutableList.builder();
    for (String serviceName : serviceMonthlyData.keySet()) {
      seriesDataBuilder.add(new SeriesData(serviceName, serviceMonthlyData.get(serviceName)));
    }
    return seriesDataBuilder.build();
  }

  private ImmutableList<Account> getAccounts(String orgId) {
    ImmutableList<Account> accounts = accountCache.get(orgId);

    if (accounts == null) {
      accounts = orgDao.getAccounts(orgId);
      accountCache.put(orgId, accounts);
    }
    return accounts;
  }
}
