package org.spring.oneplusone.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatchers((matchers) -> matchers
                        .requestMatchers("/api/**,/api/v1/product/crawling")
                )
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().hasRole("USER")
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

}
