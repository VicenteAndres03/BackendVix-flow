package apicalendario.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration config = new CorsConfiguration();
                                        config.setAllowedOrigins(List.of(
                                                        "http://localhost:5173",
                                                        "https://vix-flow.com",
                                                        "https://www.vix-flow.com"));
                                        config.setAllowedMethods(
                                                        List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                                        config.setAllowedHeaders(List.of("*"));
                                        config.setAllowCredentials(true);
                                        return config;
                                }))
                                .authorizeHttpRequests(auth -> auth
                                                // RUTAS PÚBLICAS
                                                .requestMatchers(
                                                                "/api/usuarios/registro",
                                                                "/api/usuarios/login",
                                                                "/api/usuarios/recuperar-password",
                                                                "/api/soporte/enviar",
                                                                "/api/pagos/crear-suscripcion",
                                                                "/api/pagos/webhook",
                                                                "/api/public/proyectos/**",
                                                                "/api/auth/refresh",  // ← NUEVO: refresh token
                                                                "/error")
                                                .permitAll()
                                                // TODAS LAS DEMÁS REQUIEREN TOKEN
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
