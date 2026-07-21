package com.beyzanur.chingu_ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF korumasını kapatıyoruz
                .csrf(csrf -> csrf.disable())

                // 2. Sayfa Erişim İzinleri (Araya nokta koyarak zinciri devam ettiriyoruz)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/login/oauth2/code/**",
                                "/chingu/ui/chat",
                                "/chingu/ui/clear"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 3. Standart Form Login Ayarı
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/chingu/ui", true)
                        .permitAll()
                )

                // 4. Google OAuth2 Login Ayarı
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/chingu/ui", true)
                        .permitAll()
                );

        return http.build();
    }
}