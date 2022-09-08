package io.github.dankoller.antifraud.auth;

import io.github.dankoller.antifraud.entity.user.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@SuppressWarnings("unused")
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final UserDetailsServiceImpl userDetailsService;

    public WebSecurityConfigurerImpl(RestAuthenticationEntryPoint restAuthenticationEntryPoint, UserDetailsServiceImpl userDetailsService) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(getEncoder());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // handles 401 auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, H2 console
                .and()
                .authorizeRequests()
                .mvcMatchers("/h2-console/**").permitAll() // for H2 console
                // Api endpoints
                .mvcMatchers("/api/auth/user", "/actuator/shutdown").permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
                .mvcMatchers("/api/auth/list").hasAnyRole("SUPPORT", "ADMINISTRATOR")
                .mvcMatchers("/api/auth/**").hasRole("ADMINISTRATOR")
                .mvcMatchers("/api/antifraud/**").hasRole("SUPPORT")
                // Web endpoints
                .mvcMatchers("/web/success").permitAll()
                .mvcMatchers("/web/about").permitAll()
                .mvcMatchers("/web/contact").permitAll()
                .mvcMatchers("/web/merchant/**").hasRole("MERCHANT")
                .mvcMatchers("/web/support/list-users").hasAnyRole("SUPPORT", "ADMINISTRATOR")
                .mvcMatchers("/web/support/**").hasRole("SUPPORT")

                // Anyone can create a new user TODO: Move to a more public endpoint
                .mvcMatchers("/web/admin/new-user").hasAnyRole("SUPPORT", "MERCHANT", "ADMINISTRATOR")

                .mvcMatchers("/web/admin/**").hasRole("ADMINISTRATOR")
//                .anyRequest().authenticated() // Causes restAuthenticationEntryPoint not to be called properly
                .and()
                .formLogin() // Allow form login
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/web/"); // Redirect to homepage after logout
    }

    @Bean
    public static PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}
