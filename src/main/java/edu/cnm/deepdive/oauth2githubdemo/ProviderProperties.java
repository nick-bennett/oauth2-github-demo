package edu.cnm.deepdive.oauth2githubdemo;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

@Configuration
public class ProviderProperties {

  @NestedConfigurationProperty
  private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

  @NestedConfigurationProperty
  private ResourceServerProperties resource = new ResourceServerProperties();

  @NestedConfigurationProperty
  private PathProperties path = new PathProperties();

  public AuthorizationCodeResourceDetails getClient() {
    return client;
  }

  public ResourceServerProperties getResource() {
    return resource;
  }

  public PathProperties getPath() {
    return path;
  }

  public static class PathProperties {

    @NotBlank
    private String authenticationPath;
    @NotBlank
    private String authenticationSuccessPath;

    public String getAuthenticationPath() {
      return authenticationPath;
    }

    public void setAuthenticationPath(String authenticationPath) {
      this.authenticationPath = authenticationPath;
    }

    public String getAuthenticationSuccessPath() {
      return authenticationSuccessPath;
    }

    public void setAuthenticationSuccessPath(String authenticationSuccessPath) {
      this.authenticationSuccessPath = authenticationSuccessPath;
    }
  }

}
