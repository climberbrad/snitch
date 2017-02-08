package com.cloudability.snitch.model.Ankeny;

import com.google.common.collect.ImmutableList;

public class AnkenyPostData {
  public final ImmutableList<String> account_identifiers;
  public final ImmutableList<String> dimensions;
  public final ImmutableList<String> metrics;
  public final int organization_id;
  public final int group_id;
  public final ImmutableList<String> linked_account_identifiers;
  public final String start_at;
  public final String end_at;

  private AnkenyPostData(Builder builder) {
    account_identifiers = builder.account_identifiers;
    dimensions = builder.dimensions;
    metrics = builder.metrics;
    organization_id = builder.organization_id;
    group_id = builder.group_id;
    linked_account_identifiers = builder.linked_account_identifiers;
    start_at = builder.start_at;
    end_at = builder.end_at;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private ImmutableList<String> account_identifiers;
    private ImmutableList<String> dimensions;
    private ImmutableList<String> metrics;
    private int organization_id;
    private int group_id;
    private ImmutableList<String> linked_account_identifiers;
    private String start_at;
    private String end_at;

    private Builder() {
    }

    public Builder withAccount_identifiers(ImmutableList<String> account_identifiers) {
      this.account_identifiers = account_identifiers;
      return this;
    }

    public Builder withDimensions(ImmutableList<String> dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    public Builder withMetrics(ImmutableList<String> metrics) {
      this.metrics = metrics;
      return this;
    }

    public Builder withOrganization_id(int organization_id) {
      this.organization_id = organization_id;
      return this;
    }

    public Builder withGroup_id(int group_id) {
      this.group_id = group_id;
      return this;
    }

    public Builder withLinked_account_identifiers(ImmutableList<String> linked_account_identifiers) {
      this.linked_account_identifiers = linked_account_identifiers;
      return this;
    }

    public Builder withStart_at(String start_at) {
      this.start_at = start_at;
      return this;
    }

    public Builder withEnd_at(String end_at) {
      this.end_at = end_at;
      return this;
    }

    public AnkenyPostData build() {
      return new AnkenyPostData(this);
    }
  }
}