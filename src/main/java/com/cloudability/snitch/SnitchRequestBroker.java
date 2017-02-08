package com.cloudability.snitch;

import static com.cloudability.snitch.AccountUtil.getPayerAccountIdentifiers;
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
import com.cloudability.snitch.model.PayerAccount;
import com.cloudability.snitch.model.PieChart;
import com.cloudability.snitch.model.PieChartSeries;
import com.cloudability.snitch.model.SeriesData;
import com.cloudability.snitch.model.Title;
import com.cloudability.snitch.model.UserLogins;
import com.cloudability.snitch.model.XAxis;
import com.cloudability.snitch.model.hibiki.HibikiResponse;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SnitchRequestBroker {
  private final GuiDao guiDao;
  private final AnkenyDao ankenyDao;
  private final RedshiftDao redshiftDao;
  private final AlexandriaDao alexandriaDao;
  private final HibikiDao hibikiDao;

  // caches
  private static final Map<String, OrgDetail> orgDetailCache = new HashMap<>();

  public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static final String[] ALL_MONTHS_CATEGORY =
      new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  public SnitchRequestBroker(
      GuiDao guiDao,
      AnkenyDao ankenyDao,
      RedshiftDao redshiftDao,
      AlexandriaDao alexandriaDao,
      HibikiDao hibikiDao) {
    this.guiDao = guiDao;
    this.ankenyDao = ankenyDao;
    this.redshiftDao = redshiftDao;
    this.alexandriaDao = alexandriaDao;
    this.hibikiDao = hibikiDao;
  }

  private String[] getMonthlyGraphLabels(Instant startDate, Instant endDate) {
    DateTime startDt = new DateTime(startDate.toEpochMilli());
    DateTime endDt = new DateTime(endDate.toEpochMilli());
    int start = startDt.monthOfYear().get();
    int duration = Months.monthsBetween(startDt, endDt).getMonths();

    String[] result = new String[duration];
    int index = 0;

    int month;
    for (int i = start; i < start + duration; i++) {
      month = i > 12 ? 1 : i;
      result[index] = ALL_MONTHS_CATEGORY[month - 1];
      index++;
    }

    return result;
  }

  public LineGraph buildLineGraph(
      String orgId,
      ImmutableList<PayerAccount> payerAccounts,
      String graphName,
      Instant startDate,
      Instant endDate)
  {
    int groupId = guiDao.getGroupId(orgId);

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
          getAwsTotalSpendData(orgId, groupId, payerAccounts, startDate, endDate)
      );
    }

    // total spend per service
    if (graphName.equalsIgnoreCase("totalSpendPerService")) {
      return new LineGraph(
          new Chart(GraphType.area),
          new Title(graphName),
          new XAxis(getMonthlyGraphLabels(startDate, endDate)),
          getAwsSpendPerServiceData(orgId, payerAccounts, groupId, startDate, endDate)
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
          getAwsSpendPerServiceData(orgId, payerAccounts, groupId, Instant.now().minus(30, ChronoUnit.DAYS), Instant.now())
      );
    }

    // one month logins
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

    // two month logins
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

  public OrgDetail getOrgDetail(String orgId, ImmutableList<PayerAccount> payerAccounts) {
    int groupId = guiDao.getGroupId(orgId);

    int activeRiCount = alexandriaDao.getActiveRiCount(payerAccounts);
    int numRisExpiringNextMonth = alexandriaDao.getNumRisExpiringNextMonth(payerAccounts);
    String dateOfLastRiPurchase = DATE_FORMAT.format(alexandriaDao.getDateOfLastRiPurchase(payerAccounts));

    NumberFormat formatter = NumberFormat.getCurrencyInstance();
    String savingsFromPlan = formatter.format(BigDecimal.valueOf(hibikiDao.getComparisonData(payerAccounts))
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue());

    Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
    Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
    Instant twoMonthsAgo = Instant.now().minus(60, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
    Instant yearAgo = Instant.now().minus(366, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);


    String planLastExecuted = redshiftDao.getLastRiPlanDate(orgId);

    DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale( Locale.UK )
            .withZone( ZoneId.systemDefault() );
    ImmutableMap<Instant, Integer> loginMap = redshiftDao.getLoginsPerDay(orgId, twoMonthsAgo, now);
    String lastLogin = loginMap.keySet().size() > 0 ? dateTimeFormatter.format(loginMap.keySet().iterator().next()) : "None";

    String numTotalPageLoads = redshiftDao.totalPageLoadCount(orgId, monthAgo, now);
    String totalPlannerPageLoads = redshiftDao.getTotalPlanerPageLoads(orgId);
    int numCustomWidgetsCreated = redshiftDao.getNumCustomWidgetsCreated(orgId);

//    ImmutableList<SeriesData> serviceSpendLastMonth = getAwsSpendPerServiceData(orgId, payerAccounts, groupId, yearAgo, now);
//    int awsServiceCount = serviceSpendLastMonth.size();
    int awsServiceCount = 0;

    // subscription start for primary account
    String subscriptionStartsAt = guiDao.getSubscriptionStartDate(orgId);


    String lastDataSyncDate = guiDao.getLastDataSyncDate(payerAccounts);
    Optional<HibikiResponse> response = hibikiDao.getPlan(payerAccounts);
    long sells = 0;
    if (response.isPresent() && response.get().result != null) {
      sells = response.get().result.products.ec2.accountActions.stream()
          .map(accountActions -> accountActions.actions.stream()
              .filter(act -> act.action.equalsIgnoreCase("sell"))).count();
    }


    OrgDetail orgDetail = new OrgDetail(
        orgId,
        subscriptionStartsAt,
        activeRiCount,
        savingsFromPlan,
        lastLogin,
        numRisExpiringNextMonth,
        dateOfLastRiPurchase,
        planLastExecuted,
        getLastLoginCount(loginMap, monthAgo),
        getLastLoginCount(loginMap, twoMonthsAgo),
        numTotalPageLoads,
        totalPlannerPageLoads,
        numCustomWidgetsCreated,
        lastDataSyncDate,
        awsServiceCount,
        sells);
    orgDetailCache.put(orgId, orgDetail);


    return orgDetail;
  }

  private int getLastLoginCount(ImmutableMap<Instant, Integer> loginCounts, Instant after) {
    int count = 0;
    for(Instant instant : loginCounts.keySet()) {
      if(instant.isAfter(after)) {
        count = count + loginCounts.get(instant);
      }
    }
    return count;
  }

  private ImmutableList<SeriesData> getAwsTotalSpendData(
      String orgId,
      int groupId,
      ImmutableList<PayerAccount> payerAccounts,
      Instant startDate,
      Instant endDate)
  {

    Optional<AnkenyResponse> response =
        ankenyDao.getTotalSpend(
            orgId,
            payerAccounts,
            groupId,
            startDate,
            endDate);

    List<RecordList> records = response.get().records;
    if(records == null) {
      return ImmutableList.of();
    }
    double[] dataPoints = new double[records.size()];

    for (int i = 0; i < records.size(); i++) {
      dataPoints[i] = Double.valueOf(records.get(i).entry.sum).doubleValue();
    }

    return ImmutableList.of(
        new SeriesData(getPayerAccountIdentifiers(payerAccounts).get(0), dataPoints));
  }

  /**
   * Spend per AWS Service
   */
  private ImmutableList<SeriesData> getAwsSpendPerServiceData(
      String orgId,
      ImmutableList<PayerAccount> payerAccounts,
      int groupId,
      Instant startDate,
      Instant endDate)
  {
    Optional<AnkenyCostPerServiceResponse> response =
        ankenyDao.getCostPerService(orgId, payerAccounts, groupId, startDate, endDate);

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
