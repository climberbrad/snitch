package com.cloudability.snitch.api;

import com.google.common.collect.ImmutableList;

import com.cloudability.snitch.model.Graph;
import com.cloudability.snitch.model.GraphType;
import com.cloudability.snitch.model.SeriesData;

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

  @GET
  public Response healthCheck() {
    return Response.ok().entity("Shhhhhh!").build();
  }

  @GET
  @Path("/org/{orgId}/logins")
  public Response getOrg(@PathParam("orgId") String orgId) {
    Graph graph = Graph.newBuilder()
        .withTitle("Number of Logins")
        .withSubTitle("subtitle here")
        .withXAxisTitle("x axis title")
        .withYAxisTitle("y axis title")
        .withGraphType(GraphType.COLUMN)
        .withXAxisData(
            new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"})
        .withDataPoints(ImmutableList.of(
            new SeriesData("7777-7777-7777", getDataPoints(12)),
            new SeriesData("1234-5678-9876", getDataPoints(12))))
        .build();


    return Response.ok().entity(graph)
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  @GET
  @Path("/org/{orgId}/spend")
  public Response getSpend(@PathParam("orgId") String orgId) {

    Graph graph = Graph.newBuilder()
        .withTitle("Total Spend")
        .withSubTitle("subtitle here")
        .withXAxisTitle("x axis title")
        .withYAxisTitle("y axis title")
        .withGraphType(GraphType.LINE)
        .withXAxisData(
            new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"})
        .withDataPoints(ImmutableList.of(
            new SeriesData("7777-7777-7777", getDataPoints(12))))
        .build();

    return Response.ok().entity(graph)
        .header("Access-Control-Allow-Origin", "*")
        .build();
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
