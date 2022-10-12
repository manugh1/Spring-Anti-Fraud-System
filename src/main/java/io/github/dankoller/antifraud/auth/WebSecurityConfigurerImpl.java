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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@SuppressWarnings({"unused", "deprecation"})
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
        http
                .cors(withDefaults())
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // handles 401 auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, H2 console
                .and()
                .authorizeRequests()
                .mvcMatchers("/h2-console/**").permitAll() // for H2 console
                // Api endpoints
                .mvcMatchers("/api/auth/login").permitAll()
                .mvcMatchers("/api/auth/user", "/actuator/shutdown").permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
                .mvcMatchers("/api/auth/list").hasAnyRole("SUPPORT", "ADMINISTRATOR")
                .mvcMatchers("/api/auth/**").hasRole("ADMINISTRATOR")
                .mvcMatchers("/api/antifraud/**").hasRole("SUPPORT");
//                .anyRequest().authenticated() // Causes restAuthenticationEntryPoint not to be called properly
    }

    @Bean
    public static PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Allow CORS for all origins, headers, and methods for all endpoints (only for development)
     *
     * @return CorsConfigurationSource
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
