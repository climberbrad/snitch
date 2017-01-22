package com.cloudability.snitch;

import com.google.common.collect.ImmutableMap;

import com.cloudability.snitch.dao.AlexandriaDao;
import com.cloudability.snitch.dao.AnkenyDao;
import com.cloudability.snitch.dao.HibikiDao;
import com.cloudability.snitch.dao.OrgDao;
import com.cloudability.snitch.dao.RedshiftDao;
import com.cloudability.snitch.model.OrgDetail;

public class OrgDetailBroker {
  private OrgDao orgDao;
  private AlexandriaDao alexandriaDao;
  private HibikiDao hibikiDao;
  private AnkenyDao ankenyDao;
  private RedshiftDao redshiftDao;

  public OrgDetail getOrgDetail(String orgId) {
    ImmutableMap<Integer, String> userMap = getUsers(orgId);

    return null;
  }

  private ImmutableMap<Integer, String> getUsers(String orgId) {
    return ImmutableMap.of();
  }
}
