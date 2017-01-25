package com.cloudability.snitch.api;

import com.cloudability.snitch.OrgDataBroker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

  @GET
  @Path("/org/{orgId}/graph/{graphName}")
  public Response generateGraph(@PathParam("orgId") String orgId, @PathParam("graphName") String graphName) {

    return Response.ok().entity(orgDataBroker.buildGraph(orgId, graphName, Instant.now().minus(365, ChronoUnit.DAYS), Instant.now()))
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @GET
  @Path("/org/{orgId}/details")
  public Response getReservations(@PathParam("orgId") String orgId) {

    return Response.ok().entity(
        orgDataBroker.getOrgDetail(orgId))
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }
}
