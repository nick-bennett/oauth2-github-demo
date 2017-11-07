package edu.cnm.deepdive.oauth2githubdemo;

import java.io.IOException;
import java.security.Principal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.servlet.ModelAndView;

@SpringBootApplication
@RestController
@EnableOAuth2Client
public class Oauth2GithubDemoApplication extends WebSecurityConfigurerAdapter {

  @Autowired
  OAuth2ClientContext oauth2ClientContext;

  @Override
	protected void configure(HttpSecurity http) throws Exception {
    http.antMatcher("/**")
        .authorizeRequests().antMatchers("/", "/login**", "/webjars/**").permitAll().anyRequest()
        .authenticated().and().exceptionHandling()
        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
        .logoutSuccessUrl("/").permitAll().and().csrf()
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
        .addFilterBefore(ssoFilter(github()), BasicAuthenticationFilter.class);
	}

  @Bean
  public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(filter);
    registration.setOrder(-100);
    return registration;
  }

  @Bean
  @ConfigurationProperties(prefix = "github")
  public ProviderProperties github() {
    return new ProviderProperties();
  }

  @GetMapping({"/user", "/me"})
  public Principal user(Principal principal) {
    return principal;
  }

  @GetMapping("/repos")
  public ModelAndView displayRepositories(Principal principal, Map<String, Object> model) {
    RestTemplate template = new RestTemplate();
    OAuth2Authentication auth = (OAuth2Authentication) principal;
    Map<String, Object> details = (Map) auth.getUserAuthentication().getDetails();
    Repository[] repos = template.getForObject((String) details.get("repos_url"), Repository[].class);
    model.put("user", details.get("name"));
    model.put("repositories", repos);
    return new ModelAndView("repositories", model);
  }

  private Filter ssoFilter(ProviderProperties client) {
    OAuth2ClientAuthenticationProcessingFilter filter
        = new OAuth2ClientAuthenticationProcessingFilter(client.getPath().getAuthenticationPath());
    OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
    filter.setRestTemplate(oAuth2RestTemplate);
    UserInfoTokenServices tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(),
        client.getClient().getClientId());
    tokenServices.setRestTemplate(oAuth2RestTemplate);
    filter.setTokenServices(tokenServices);
    return filter;
  }

  public static void main(String[] args) {
		SpringApplication.run(Oauth2GithubDemoApplication.class, args);
	}

}

