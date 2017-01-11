package com.cloudability.snitch.model;

import java.util.HashMap;
import java.util.Map;

public class UserLogins {
  public final String userId;
  public final Map<Integer, Integer> monthToCountMap = new HashMap<>();

  public UserLogins(String userId) {
    this.userId = userId;
  }

  public void addMontlyCount(int month, int count) {
    monthToCountMap.put(month, count);
  }

  public double[] getDataPoints() {
    double[] dataPoints = new double[12];
    for(int i=0;i<dataPoints.length;i++) {
        dataPoints[i] = (monthToCountMap.get(i) != null) ? monthToCountMap.get(i) : 0;
    }
    return dataPoints;
  }
}
