package edu.cnm.deepdive.oauth2githubdemo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

  @JsonProperty
  public int id;

  @JsonProperty
  public String name;

  @JsonProperty("html_url")
  public String htmlUrl;

  @JsonProperty("private")
  public boolean priv;

}
