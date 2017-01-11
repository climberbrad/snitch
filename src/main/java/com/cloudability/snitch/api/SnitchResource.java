package com.cloudability.snitch.api;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.dao.AnkenyDao;
import com.cloudability.snitch.dao.OrgDao;
import com.cloudability.snitch.model.Ankeny.AnkenyResponse;
import com.cloudability.snitch.model.Ankeny.RecordList;
import com.cloudability.snitch.model.Chart;
import com.cloudability.snitch.model.Graph;
import com.cloudability.snitch.model.GraphType;
import com.cloudability.snitch.model.SeriesData;
import com.cloudability.snitch.model.Title;
import com.cloudability.snitch.model.XAxis;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1")
@Produces({MediaType.APPLICATION_JSON})
public class SnitchResource {
  private final OrgDao orgDao;
  private final AnkenyDao ankenyDao;

  public SnitchResource(OrgDao orgDao, AnkenyDao ankenyDao) {
    this.orgDao = orgDao;
    this.ankenyDao = ankenyDao;
  }

  private static final String[] ALL_MONTHS_CATEGORY =
      new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  @GET
  public Response healthCheck() {
    return Response.ok().entity("Shhhhhh!").build();
  }

  @GET
  @Path("/orgs")
  public Response getOrgs() {
    return Response.ok()
        .entity(orgDao.getActiveOrgs())
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @GET
  @Path("/org/{orgId}/logins")
  public Response getOrg(@PathParam("orgId") String orgId) {

    ImmutableList seriesData = ImmutableList.of(
        new SeriesData("7777-7777-7777", getDataPoints(12)),
        new SeriesData("1234-5678-9876", getDataPoints(12)));

    Graph graph = new Graph(
        new Chart(GraphType.column),
        new Title("Number of Logins"),
        new XAxis(ALL_MONTHS_CATEGORY),
        seriesData);

    return Response.ok().entity(graph)
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @GET
  @Path("/org/{orgId}/spend")
  public Response getSpend(@PathParam("orgId") String orgId) {

    ImmutableList seriesData = ImmutableList.of(
        new SeriesData("7777-7777-7777", getAwsSpendFromAnkeny(orgId)));

    Graph graph = new Graph(
        new Chart(GraphType.line),
        new Title("Total Spend"),
        new XAxis(ALL_MONTHS_CATEGORY),
        seriesData);

    return Response.ok().entity(graph)
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  private double[] getAwsSpendFromAnkeny(String orgId) {
    Optional<AnkenyResponse> response =
        ankenyDao.getMontlyCostData(orgId, "accountId", orgDao.getLinkedAccounts(orgId));

    List<RecordList> records = response.get().records;
    double[] result = new double[records.size()];

    for(int i=0;i<records.size();i++) {
      result[i] = Double.valueOf(records.get(i).entry.sum).doubleValue();
    }

    return result;
  }


  private double[] getDataPoints(int numPoints) {
    double result[] = new double[numPoints];
    for (int i = 0; i < numPoints; i++) {
      result[i] = randomDouble();
    }
    return result;
  }

  private double randomDouble() {
    Random random = new Random();
    int rangeMin = 1;
    int rangeMax = 100;
    return rangeMin + (rangeMax - rangeMin) * random.nextDouble();
  }
}
