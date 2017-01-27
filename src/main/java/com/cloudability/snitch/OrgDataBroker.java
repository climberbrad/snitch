package com.cloudability.snitch;

import static com.cloudability.snitch.dao.RedshiftDao.LOGIN_STAT_WINDOW.DAILY_INCREMENT;
import static com.cloudability.snitch.dao.RedshiftDao.LOGIN_STAT_WINDOW.MONTHLY_INCREMENT;
import static java.lang.Math.toIntExact;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.cloudability.snitch.dao.AlexandriaDao;
import com.cloudability.snitch.dao.AnkenyDao;
import com.cloudability.snitch.dao.GuiDao;
import com.cloudability.snitch.dao.HibikiDao;
import com.cloudability.snitch.dao.RedshiftDao;
import com.cloudability.snitch.model.Account;
import com.cloudability.snitch.model.Ankeny.AnkenyCostPerServiceResponse;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;
import com.cloudability.snitch.model.Ankeny.MultiRecordList;
import com.cloudability.snitch.model.Ankeny.RecordList;
import com.cloudability.snitch.model.Chart;
import com.cloudability.snitch.model.DataPoint;
import com.cloudability.snitch.model.GraphType;
import com.cloudability.snitch.model.LineGraph;
import com.cloudability.snitch.model.OrgDetail;
import com.cloudability.snitch.model.OrgSearchResult;
import com.cloudability.snitch.model.PieChart;
import com.cloudability.snitch.model.PieChartSeries;
import com.cloudability.snitch.model.SeriesData;
import com.cloudability.snitch.model.Title;
import com.cloudability.snitch.model.UserLogins;
import com.cloudability.snitch.model.XAxis;
import com.cloudability.snitch.model.hibiki.HibikiResponse;
import com.cloudability.streams.Gullectors;

import org.joda.time.DateTime;
import org.joda.time.Months;

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
  private final GuiDao guiDao;
  private final AnkenyDao ankenyDao;
  private final RedshiftDao redshiftDao;
  private final AlexandriaDao alexandriaDao;
  private final HibikiDao hibikiDao;
  private final AccountCache accountCache;

  // caches
  private static final Map<String, OrgDetail> orgDetailCache = new HashMap<>();

  public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static final String[] ALL_MONTHS_CATEGORY =
      new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  public OrgDataBroker(
      GuiDao guiDao,
      AnkenyDao ankenyDao,
      RedshiftDao redshiftDao,
      AlexandriaDao alexandriaDao,
      HibikiDao hibikiDao,
      AccountCache accountCache) {
    this.guiDao = guiDao;
    this.ankenyDao = ankenyDao;
    this.redshiftDao = redshiftDao;
    this.alexandriaDao = alexandriaDao;
    this.hibikiDao = hibikiDao;
    this.accountCache = accountCache;
  }

  private String[] getMonthlyGraphLabels(Instant startDate, Instant endDate) {

    DateTime startDt = new DateTime(startDate.toEpochMilli());
    DateTime endDt = new DateTime(endDate.toEpochMilli());

    int start = startDt.monthOfYear().get();
    int duration = Months.monthsBetween(startDt, endDt).getMonths();


    String[] result = new String[duration];
    int index = 0;

    for (int i = start; i < start + duration; i++) {
      result[index] = ALL_MONTHS_CATEGORY[i - 1];
      index++;
    }

    return result;
  }

  public LineGraph buildLineGraph(String orgId, String graphName, Instant startDate, Instant endDate) {
    // total logins
    if (graphName.equalsIgnoreCase("totalLogins")) {
      return new LineGraph(
          new Chart(GraphType.column),
          new Title(graphName),
          new XAxis(getMonthlyGraphLabels(startDate, endDate)),
          buildTotalLoginGraph(orgId, startDate, endDate, MONTHLY_INCREMENT)
      );
    }

    // total spend
    if (graphName.equalsIgnoreCase("totalSpend")) {
      return new LineGraph(
          new Chart(GraphType.line),
          new Title(graphName),
          new XAxis(getMonthlyGraphLabels(startDate, endDate)),
          getAwsTotalSpendData(orgId, startDate, endDate)
      );
    }

    // total spend per service
    if (graphName.equalsIgnoreCase("totalSpendPerService")) {
      return new LineGraph(
          new Chart(GraphType.area),
          new Title(graphName),
          new XAxis(getMonthlyGraphLabels(startDate, endDate)),
          getAwsSpendPerServiceData(orgId, startDate, endDate)
      );
    }

    // 1 month spend per service
    if (graphName.equalsIgnoreCase("oneMonthTotalSpend")) {
      int daysBetween = toIntExact(ChronoUnit.DAYS.between(startDate, endDate));
      String[] xAxisLabel = new String[daysBetween];
      for (int i = 0; i < daysBetween; i++) {
        xAxisLabel[i] = String.valueOf(i + 1);
      }

      return new LineGraph(
          new Chart(GraphType.area),
          new Title(graphName),
          new XAxis(xAxisLabel),
          getAwsSpendPerServiceData(orgId, Instant.now().minus(30, ChronoUnit.DAYS), Instant.now())
      );
    }

    if (graphName.equalsIgnoreCase("oneMonthLogins")) {
      Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
      Instant end = Instant.now();
      return new LineGraph(
          new Chart(GraphType.column),
          new Title(graphName),
          new XAxis(getMonthlyGraphLabels(start, end)),
          buildTotalLoginGraph(orgId, start, end, DAILY_INCREMENT)
      );
    }

    if (graphName.equalsIgnoreCase("twoMonthLogins")) {
      Instant start = Instant.now().minus(60, ChronoUnit.DAYS);
      Instant end = Instant.now();
      return new LineGraph(
          new Chart(GraphType.column),
          new Title(graphName),
          new XAxis(getMonthlyGraphLabels(start, end)),
          buildTotalLoginGraph(orgId, start, end, DAILY_INCREMENT)
      );
    }

    return null;
  }

  public PieChart buildPieChart(String orgId, String graphName, Instant after, Instant before) {
    if (graphName.equalsIgnoreCase("pageLoads")) {
      Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
      Instant end = Instant.now();

      return new PieChart(
          new Chart(GraphType.pie),
          new Title(graphName),
          buildPieChart(orgId, start, end)
      );
    }
    return null;
  }

  public ImmutableList<OrgSearchResult> getActiveOrgList() {
    return guiDao.getActiveOrgList();
  }

  /**
   * Total Logins Graph
   */
  public ImmutableList<SeriesData> buildTotalLoginGraph(
      String orgId,
      Instant startDate,
      Instant endDate,
      RedshiftDao.LOGIN_STAT_WINDOW window)
  {
    ImmutableList<UserLogins> loginData = redshiftDao.getLoginData(orgId, startDate, endDate, window);

    ImmutableList.Builder seriesDataBuilder = ImmutableList.builder();
    for (UserLogins loginStats : loginData) {
      seriesDataBuilder.add(new SeriesData(loginStats.userId, loginStats.getDataPoints()));
    }

    return seriesDataBuilder.build();
  }

  public ImmutableList<PieChartSeries> buildPieChart(String orgid, Instant startDate, Instant endDate) {
    ImmutableList.Builder<PieChartSeries> seriesDataBuilder = ImmutableList.builder();
    ImmutableMap<String, Integer> pageLoginMap = redshiftDao.getPageLoads(orgid, startDate, endDate);
    DataPoint[] dataPoints = new DataPoint[pageLoginMap.size()];

    int index = 0;
    for (String page : pageLoginMap.keySet()) {

      String trimmedPageName = page.contains("|") ? page.substring(0, page.indexOf('|')) : page;

      double val = Double.valueOf(pageLoginMap.get(page)).doubleValue();
      dataPoints[index] = new DataPoint(trimmedPageName, val);
      index++;
    }
    seriesDataBuilder.add(new PieChartSeries("Page Loads", dataPoints));

    return seriesDataBuilder.build();
  }

  public OrgDetail getOrgDetail(String orgId) {
    if (orgDetailCache.get(orgId) != null) {
      return orgDetailCache.get(orgId);
    }

    ImmutableList<Account> accounts = accountCache.getAllAccounts(orgId);
    int activeRiCount = alexandriaDao.getActiveRiCount(accounts);

    int numRisExpiringNextMonth = alexandriaDao.getNumRisExpiringNextMonth(accounts);
    String dateOfLastRiPurchase = DATE_FORMAT.format(alexandriaDao.getDateOfLastRiPurchase(accounts));

    double savingsFromPlan = BigDecimal.valueOf(hibikiDao.getCompare(accounts))
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();

    Instant now = Instant.now();
    Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
    Instant twoMonthsAgo = Instant.now().minus(60, ChronoUnit.DAYS);

    String planLastExecuted = redshiftDao.getLastRiPlanDate(orgId);
    String lastLogin = redshiftDao.getLatestLogin(orgId);
    String numTotalPageLoads = redshiftDao.totalPageLoadCount(orgId, monthAgo, now);
    String totalPlannerPageLoads = redshiftDao.getTotalPlanerPageLoads(orgId);
    String numLoginsLastMonth = redshiftDao.getLoginCount(orgId, monthAgo, now);
    String numLoginsLastTwoMonth = redshiftDao.getLoginCount(orgId, twoMonthsAgo, now);
    int numCustomWidgetsCreated = redshiftDao.getNumCustomWidgetsCreated(orgId);

    ImmutableList<SeriesData> serviceSpendLastMonth = getAwsSpendPerServiceData(orgId, monthAgo, now);
    int awsServiceCount = serviceSpendLastMonth.size();

    // subscription start for primary account
    String subscriptionStartsAt = guiDao.getSubscriptionStartDate(orgId);

    ImmutableList<String> payerAccounts = accounts.stream()
        .filter(account -> account.isPayer)
        .map(account -> account.accountIdentifier)
        .collect(Gullectors.toImmutableList());

    String lastDataSyncDate = guiDao.getLastDataSyncDate(payerAccounts);
    Optional<HibikiResponse> response = hibikiDao.getPlan(accounts);
    long sells = 0;
    if (response.isPresent()) {
      sells = response.get().result.products.ec2.accountActions.stream()
          .map(accountActions -> accountActions.actions.stream()
              .filter(act -> act.action.equalsIgnoreCase("sell"))).count();
    }

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
        awsServiceCount,
        sells);
    orgDetailCache.put(orgId, orgDetail);


    return orgDetail;
  }

  private ImmutableList<SeriesData> getAwsTotalSpendData(
      String orgId,
      Instant startDate,
      Instant endDate)
  {
    int groupId = guiDao.getGroupId(orgId);

    Optional<AnkenyResponse> response =
        ankenyDao.getTotalMontlyCostData(
            Integer.valueOf(orgId),
            groupId,
            accountCache.getPayerAccounts(orgId),
            accountCache.getLinkedAccounts(orgId),
            startDate,
            endDate);

    List<RecordList> records = response.get().records;
    double[] dataPoints = new double[records.size()];

    for (int i = 0; i < records.size(); i++) {
      dataPoints[i] = Double.valueOf(records.get(i).entry.sum).doubleValue();
    }

    return ImmutableList.of(
        new SeriesData(accountCache.getPayerAccounts(orgId).get(0), dataPoints));
  }

  /**
   * Spend per AWS Service
   */
  private ImmutableList<SeriesData> getAwsSpendPerServiceData(
      String orgId,
      Instant startDate,
      Instant endDate)
  {
    int groupId = guiDao.getGroupId(orgId);

    Optional<AnkenyCostPerServiceResponse> response =
        ankenyDao.getCostPerService(
            Integer.valueOf(orgId),
            groupId,
            accountCache.getPayerAccounts(orgId),
            accountCache.getLinkedAccounts(orgId),
            startDate,
            endDate);

    List<MultiRecordList> records = response.get().records;

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
}
