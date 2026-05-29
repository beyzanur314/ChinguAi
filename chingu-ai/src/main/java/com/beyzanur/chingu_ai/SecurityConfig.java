package com.beyzanur.chingu_ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Güvenlik sınıfının içerisindeki filtre metodunun içi tam olarak böyle olmalı:
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Giriş ekranını, CSS/JS dosyalarını ve Google bağlantı linklerini herkese açıyoruz
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/chingu/ui", true) // true -> Zorunlu olarak buraya yönlendirir
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/chingu/ui", true) // true -> Google'dan dönünce zorla chat ekranını açar
                        .permitAll()
                );

        return http.build();
    }
}