package com.cloudability.snitch.api;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.AccountUtil;
import com.cloudability.snitch.OrgDataBroker;
import com.cloudability.snitch.model.PayerAccount;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1")
@Produces({MediaType.APPLICATION_JSON})
public class SnitchResource {
  private final OrgDataBroker orgDataBroker;

  public SnitchResource(OrgDataBroker orgDataBroker) {
    this.orgDataBroker = orgDataBroker;
  }

  @GET
  public Response healthCheck() {
    return Response.ok().entity("Shhhhhh!").build();
  }

  @GET
  @Path("/orgs")
  public Response getOrgs() {
    return Response.ok()
        .entity(orgDataBroker.getActiveOrgList())
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @POST
  @Path("/org/{orgId}/linegraph/{graphName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces({MediaType.APPLICATION_JSON})
  public Response lineGraph(
      @PathParam("orgId") String orgId,
      @PathParam("graphName") String graphName,
      List<PayerAccount> payerAccounts)
  {
    return Response.ok()
        .entity(orgDataBroker.buildLineGraph(
            orgId, ImmutableList.copyOf(payerAccounts),
            graphName,
            Instant.now().minus(366, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS),
            Instant.now().truncatedTo(ChronoUnit.DAYS))
        )
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @POST
  @Path("/org/{orgId}/piechart/{graphName}")
  public Response generatePieChart(
      @PathParam("orgId") String orgId,
      @PathParam("graphName") String graphName,
      @QueryParam("payerAccounts") String payerAccountIds,
      @QueryParam("linkedAccounts") String linkedAccountIds
  ) {
    return Response.ok()
        .entity(orgDataBroker.buildPieChart(
            orgId,
            graphName,
            Instant.now().minus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS),
            Instant.now())
        )
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @POST
  @Path("/org/{orgId}/details")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces({MediaType.APPLICATION_JSON})
  public Response getReservations(
      @PathParam("orgId") String orgId,
      List<PayerAccount> payerAccounts
  ) {
    return Response.ok()
        .entity(orgDataBroker.getOrgDetail(orgId, ImmutableList.copyOf(payerAccounts)))
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @GET
  @Path("/org/{orgId}/accounts")
  public Response getAccounts(@PathParam("orgId") String orgId) {

    return Response.ok()
        .entity(AccountUtil.getAccounts(orgId))
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }
}
