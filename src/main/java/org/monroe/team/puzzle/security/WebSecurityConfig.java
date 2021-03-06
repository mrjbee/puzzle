package org.monroe.team.puzzle.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic()
            .and()
                //TODO http://stackoverflow.com/questions/21128058/invalid-csrf-token-null-was-found-on-the-request-parameter-csrf-or-header
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                    .antMatchers(HttpMethod.POST).hasRole("ADMIN")
                    .anyRequest().hasAnyRole("ADMIN","USER")
            .and()
                .exceptionHandling()
                    .authenticationEntryPoint(new Http401AuthenticationEntryPoint("Basic realm=\"Please provide your media browser credentials\""));
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
                .withUser("me")
                    .password(System.getProperty("default.password","trust me"))
                    .roles("USER")
            .and()
                .withUser("admin")
                    .password(System.getProperty("default.admin.password","I am admin"))
                    .roles("ADMIN");

    }
}
