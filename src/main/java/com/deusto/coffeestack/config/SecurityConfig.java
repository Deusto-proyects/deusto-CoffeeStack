package com.deusto.coffeestack.config;

import com.deusto.coffeestack.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/h2-console/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_PATHS).permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/items/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/items/**").hasAnyRole("PROPIETARIO", "ROOT")
                    .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("PROPIETARIO", "ROOT")
                    .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyRole("PROPIETARIO", "ROOT")

                    .requestMatchers(HttpMethod.GET, "/api/stock/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/stock/**").hasAnyRole("PROPIETARIO", "ROOT")
                    .requestMatchers(HttpMethod.DELETE, "/api/stock/**").hasAnyRole("PROPIETARIO", "ROOT")

<<<<<<< Updated upstream
                    // Ajustes (mermas/roturas): read → any authenticated; write → PROPIETARIO or ROOT
                    .requestMatchers(HttpMethod.GET, "/api/ajustes/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/ajustes/**").hasAnyRole("PROPIETARIO", "ROOT")

                    // User management → ROOT only (also enforced via @PreAuthorize)
                    .requestMatchers(HttpMethod.GET, "/api/proveedores/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/proveedores/**").hasAnyRole("EMPLEADO", "PROPIETARIO", "ROOT")

                    // Lotes (batch reception): read → any authenticated; write → EMPLEADO or above
                    .requestMatchers(HttpMethod.GET, "/api/lotes/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/lotes/**").hasAnyRole("EMPLEADO", "PROPIETARIO", "ROOT")

=======
>>>>>>> Stashed changes
                    .requestMatchers("/api/usuarios/**").hasRole("ROOT")

                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                org.springframework.security.core.AuthenticationException authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(
                    response.getOutputStream(),
                    Map.of("error", "No autenticado", "message", "Debe identificarse para acceder a este recurso")
            );
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(
                    response.getOutputStream(),
                    Map.of("error", "Acceso denegado", "message", "No tiene permisos suficientes para realizar esta operación")
            );
        };
    }
}
