package edu.cnm.deepdive.oauth2githubdemo;

import java.io.IOException;
import java.security.Principal;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

@SpringBootApplication
@RestController
@EnableOAuth2Client
public class Oauth2GithubDemoApplication extends WebSecurityConfigurerAdapter {

  @Autowired
  OAuth2ClientContext oauth2ClientContext;

	@RequestMapping({"/user", "/me"})
	public Principal user(Principal principal) {
		return principal;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
    http.antMatcher("/**")
        .authorizeRequests().antMatchers("/", "/login**", "/webjars/**").permitAll().anyRequest()
        .authenticated().and().exceptionHandling()
        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
        .logoutSuccessUrl("/").permitAll().and().csrf()
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
        .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
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

  private Filter ssoFilter() {
    CompositeFilter filter = new CompositeFilter();
    List<Filter> filters = new ArrayList<>();
    filters.add(ssoFilter(github()));
    // Add more filters as appropriate
    filter.setFilters(filters);
    return filter;
  }

  private Filter ssoFilter(ProviderProperties client) {
    OAuth2ClientAuthenticationProcessingFilter filter
        = new OAuth2ClientAuthenticationProcessingFilter(client.getPath().getAuthenticationPath());
    filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler() {
      public void onAuthenticationSuccess(
          HttpServletRequest request, HttpServletResponse response, Authentication authentication)
          throws IOException, ServletException {
        this.setDefaultTargetUrl(client.getPath().getAuthenticationSuccessPath());
        super.onAuthenticationSuccess(request, response, authentication);
      }
    });
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

