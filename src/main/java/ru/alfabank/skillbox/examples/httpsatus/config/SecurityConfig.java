package ru.alfabank.skillbox.examples.httpsatus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain2xx(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf().disable()
            .requestMatchers().antMatchers("/2xx/**", "/3xx/**", "/5xx/**")
            .and()
            .authorizeRequests().anyRequest().permitAll()
            .and()
            .httpBasic().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // @formatter:on
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain4xx(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf().disable()
            .requestMatchers().antMatchers("/4xx/**")
            .and()
            .authorizeRequests()
                .antMatchers("/4xx/401").authenticated()
                .antMatchers(HttpMethod.DELETE, "/4xx/403").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // @formatter:on
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user")
            .password(bCryptPasswordEncoder.encode("test"))
            .roles("USER")
            .build());
        manager.createUser(User.withUsername("admin")
            .password(bCryptPasswordEncoder.encode("admin"))
            .roles("USER", "ADMIN")
            .build());
        return manager;
    }

    @Bean
    public BCryptPasswordEncoder encoder() throws NoSuchAlgorithmException {
        // for other RNG Algorithms info see
        // https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#securerandom-number-generation-algorithms
        return new BCryptPasswordEncoder(10, SecureRandom.getInstance("NativePRNG"));
    }
}
