package com.inbank.dengine.config.security;

import com.inbank.dengine.config.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AppSecurityConfig implements WebMvcConfigurer {

    private final JwtFilter jwtFilter;

    // authentication
    @Bean
    public AuthenticationManager getAuthenticationManger(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    // authorization
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{

        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.authorizeRequests()
                .antMatchers("/api/authenticate").permitAll()
                .anyRequest().authenticated();

        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        /*
         *  for jwt authentication, disabling csrf is okay
         *  read more here
         *  https://www.baeldung.com/spring-security-csrf
         */
        httpSecurity.cors().and().csrf().disable();
        httpSecurity.headers().frameOptions().disable();
        return httpSecurity.build();
    }


    // password encoder
    @Bean
    public PasswordEncoder initPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }




}
