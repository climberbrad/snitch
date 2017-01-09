package com.cloudability.snitch.api;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Graph;
import com.cloudability.snitch.model.GraphType;
import com.cloudability.snitch.model.SeriesData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1")
@Produces({MediaType.APPLICATION_JSON})
public class SnitchResource {

  @GET
  public Response healthCheck() {
    return Response.ok().entity("Shhhhhh!").build();
  }

  @GET
  @Path("/org/{orgId}")
  public Response getOrg(@PathParam("orgId") String orgId) {
    System.out.println("looking for org " + orgId);

    return Response.ok().entity(buildGraph(orgId))
        .header("Access-Control-Allow-Origin","*")
        .build();
  }

  private Graph buildGraph(String orgId) {
    return Graph.newBuilder()
        .withTitle("My Special Graph for org " + orgId)
        .withSubTitle("subtitle here")
        .withXAxisTitle("x axis title")
        .withYAxisTitle("y axis title")
        .withGraphType(GraphType.COLUMN)
        .withXAxisData(new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"})
        .withDataPoints(ImmutableList.of(
            new SeriesData("6666-6666-6666", getDataPoints()),
            new SeriesData("7777-7777-7777", getDataPoints())
        ))
        .build();
  }

  private double[] getDataPoints() {
    return new double[] {29.9,
        71.5,
        106.4,
        129.2,
        144,
        176,
        135.6,
        148.5,
        216.4,
        194.1,
        295.6,
        454.4};
  }

  private String getJson() {
    return "{"
        + "\"org\": \"Nike\","
        + "\"accountId\": \"1234-3456-6789\","
        + "\"lastLogin\": \"today\","
        + "\"data\": ["
        + "29.9,"
        + "71.5,"
        + "106.4,"
        + "129.2,"
        + "144,"
        + "176,"
        + "135.6,"
        + "148.5,"
        + "216.4,"
        + "194.1,"
        + "295.6,"
        + "454.4"
        + "]"
        + "}";
  }
}
