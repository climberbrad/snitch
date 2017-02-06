package com.cloudability.snitch.api;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.AccountUtil;
import com.cloudability.snitch.SnitchRequestBroker;
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
  private final SnitchRequestBroker snitchRequestBroker;

  public SnitchResource(SnitchRequestBroker snitchRequestBroker) {
    this.snitchRequestBroker = snitchRequestBroker;
  }

  @GET
  @Path("/orgs")
  public Response getOrgs() {
    return Response.ok()
        .entity(snitchRequestBroker.getActiveOrgList())
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
        .entity(snitchRequestBroker.buildLineGraph(
            orgId, ImmutableList.copyOf(payerAccounts),
            graphName,
            Instant.now().minus(366, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS),
            Instant.now().truncatedTo(ChronoUnit.DAYS))
        )
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
        .entity(snitchRequestBroker.buildPieChart(
            orgId,
            graphName,
            Instant.now().minus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS),
            Instant.now())
        )
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
        .entity(snitchRequestBroker.getOrgDetail(orgId, ImmutableList.copyOf(payerAccounts)))
        .build();
  }

  @GET
  @Path("/org/{orgId}/accounts")
  public Response getAccounts(@PathParam("orgId") String orgId) {

    return Response.ok()
        .entity(AccountUtil.getAccounts(orgId))
        .build();
  }
}
