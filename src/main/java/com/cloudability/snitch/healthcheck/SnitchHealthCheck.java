package com.cloudability.snitch.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class SnitchHealthCheck extends HealthCheck {

  @Override
  protected Result check() throws Exception {
    return Result.healthy();
  }

}
