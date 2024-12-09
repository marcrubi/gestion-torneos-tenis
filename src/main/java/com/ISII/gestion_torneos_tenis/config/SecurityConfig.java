// src/main/java/com/ISII/gestion_torneos_tenis/config/SecurityConfig.java
package com.ISII.gestion_torneos_tenis.config;

import com.ISII.gestion_torneos_tenis.service.JugadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                .csrf() // CSRF habilitado por defecto
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/home",
                                "/jugadores/register",
                                "/jugadores/verificar",
                                "/jugadores/recuperar",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/register",
                                "/login"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/torneos/disponibles",
                                "/torneos/todos",
                                "/torneos/seleccionar-jugadores",
                                "/inscripciones/**",
                                "/emparejamientos/**",
                                "/main",
                                "/jugadores/perfil",
                                "/jugadores/mis-torneos",
                                "/jugadores/mis-resultados",
                                "/jugadores/estadisticas",
                                "/ranking/global"
                        ).hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/jugadores/login")
                        .loginProcessingUrl("/jugadores/login")
                        .usernameParameter("nombreUsuario")
                        .passwordParameter("contrasena")
                        .defaultSuccessUrl("/main", true)
                        .permitAll()
                )
                // Permitir GET en logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/jugadores/logout", "GET"))
                        .logoutSuccessUrl("/jugadores/login?logout")
                        .permitAll()
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
