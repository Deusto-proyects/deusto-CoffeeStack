package com.deusto.coffeestack.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.Map;

/**
 * Spring Security configuration.
 *
 * <h3>Role matrix</h3>
 * <pre>
 * Endpoint                         ROOT  PROPIETARIO  EMPLEADO
 * GET /api/items/**                 ✓       ✓            ✓
 * POST/PUT/DELETE /api/items/**     ✓       ✓            ✗
 * GET /api/stock/**                 ✓       ✓            ✓
 * POST/DELETE /api/stock/**         ✓       ✓            ✗
 * /api/usuarios/**                  ✓       ✗            ✗
 * </pre>
 *
 * <h3>Error handling</h3>
 * <ul>
 *   <li><b>401</b> – no credentials supplied (AuthenticationEntryPoint)</li>
 *   <li><b>403</b> – authenticated but insufficient role (AccessDeniedHandler)</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── disable CSRF (stateless REST API) ────────────────────────────
            .csrf(csrf -> csrf.disable())
            // ── H2 console needs frames ──────────────────────────────────────
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
            // ── session: stateless (HTTP Basic per request) ──────────────────
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // ── authorization rules ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_PATHS).permitAll()

                    // Items: read → any authenticated; write → PROPIETARIO or ROOT
                    .requestMatchers(HttpMethod.GET, "/api/items/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/items/**").hasAnyRole("PROPIETARIO", "ROOT")
                    .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("PROPIETARIO", "ROOT")
                    .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyRole("PROPIETARIO", "ROOT")

                    // Stock: read → any authenticated; write → PROPIETARIO or ROOT
                    .requestMatchers(HttpMethod.GET, "/api/stock/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/stock/**").hasAnyRole("PROPIETARIO", "ROOT")
                    .requestMatchers(HttpMethod.DELETE, "/api/stock/**").hasAnyRole("PROPIETARIO", "ROOT")

                    // User management → ROOT only (also enforced via @PreAuthorize)
                    .requestMatchers("/api/usuarios/**").hasRole("ROOT")

                    .anyRequest().authenticated()
            )
            // ── HTTP Basic auth ───────────────────────────────────────────────
            .httpBasic(Customizer.withDefaults())
            // ── 401: not authenticated ────────────────────────────────────────
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    /**
     * 401 – The caller did not supply valid credentials.
     * Returns a JSON body so clients don't have to parse HTML.
     */
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

    /**
     * 403 – The caller is authenticated but lacks the required role.
     * Returns a JSON body so clients don't have to parse HTML.
     */
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
