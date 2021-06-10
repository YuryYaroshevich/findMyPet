package com.yy.petfinder.security.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

public class OAuthTokenWrapper {
  private Map<String, Object> fields = new HashMap<>();

  public OAuthTokenWrapper() {}

  public OAuthTokenWrapper(Map<String, Object> fields) {
    this.fields = fields;
  }

  @JsonAnySetter
  public void unmappedFields(String key, String value) {
    fields.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getFields() {
    return fields;
  }
}
