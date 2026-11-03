package com.lsvp.InventoryManagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//https://www.codejava.net/frameworks/spring-boot/spring-security-permit-all-requests
//https://www.baeldung.com/spring-security-deactivate

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                // Permite que o console do H2 seja exibido (ele usa frames)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sem login (ou com token inválido/expirado) responde 401 em vez de 403,
                // para o interceptor do frontend tentar o refresh e, se falhar, voltar ao login
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
//            Parte que chama autenticação comentada durante desenvolvimento
                 .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                 // Libera a página de erro do Spring: sem isso, qualquer erro interno (500)
                 // aparece para o frontend como 401/403 e esconde o erro real
                 .requestMatchers("/error").permitAll()
                 // Console do H2 liberado sem login (só existe quando spring.h2.console.enabled=true)
                 .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                 // Qualquer usuário logado pode acessar /api/user/** (antes era só ADMINISTRADOR)
                 .anyRequest().authenticated()
                )
               .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }
}

    /*public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }*/

