package com.gustavobatista.autoconfig.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private static final String AUTH_ADMIN = "ROLE_ADMIN";
        private static final String AUTH_MANAGER = "ROLE_MANAGER";
        private static final String AUTH_SELLER = "ROLE_SELLER";
        private static final String AUTH_VEHICLE_STOCK = "ROLE_VEHICLE_STOCK";
        private static final String AUTH_ACCESSORY_STOCK = "ROLE_ACCESSORY_STOCK";

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                                .cors(Customizer.withDefaults())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/h2-console", "/h2-console/**").permitAll()
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()

                                                // ---- USERS: ADMIN + MANAGER (restrições finas em UserServiceImpl)
                                                .requestMatchers("/users", "/users/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER)

                                                // ---- CARS: seller só GET; ADMIN/MANAGER escrita
                                                .requestMatchers(HttpMethod.GET, "/cars", "/cars/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER,
                                                                AUTH_VEHICLE_STOCK, AUTH_ACCESSORY_STOCK)
                                                .requestMatchers(HttpMethod.POST, "/cars", "/cars/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER)
                                                .requestMatchers(HttpMethod.PUT, "/cars", "/cars/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER)
                                                .requestMatchers(HttpMethod.DELETE, "/cars", "/cars/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER)

                                                // ---- CLIENTS: ADMIN + MANAGER + SELLER
                                                .requestMatchers("/clients", "/clients/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER)

                                                // ---- ORDERS
                                                .requestMatchers(HttpMethod.PATCH, "/orders/*/confirm-vehicle")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_VEHICLE_STOCK, AUTH_SELLER)
                                                .requestMatchers(HttpMethod.PATCH, "/orders/*/confirm-accessories")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_ACCESSORY_STOCK, AUTH_SELLER)
                                                .requestMatchers(HttpMethod.PATCH, "/orders/*/confirm-installation")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER)
                                                .requestMatchers(HttpMethod.GET, "/orders", "/orders/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER,
                                                                AUTH_VEHICLE_STOCK, AUTH_ACCESSORY_STOCK)
                                                .requestMatchers(HttpMethod.POST, "/orders", "/orders/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER)
                                                .requestMatchers(HttpMethod.PUT, "/orders", "/orders/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER)
                                                .requestMatchers(HttpMethod.DELETE, "/orders", "/orders/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER)

                                                // ---- VEHICLES
                                                .requestMatchers(HttpMethod.GET, "/vehicles", "/vehicles/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER,
                                                                AUTH_VEHICLE_STOCK, AUTH_ACCESSORY_STOCK)
                                                .requestMatchers(HttpMethod.POST, "/vehicles", "/vehicles/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_VEHICLE_STOCK)
                                                .requestMatchers(HttpMethod.PUT, "/vehicles", "/vehicles/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_VEHICLE_STOCK)
                                                .requestMatchers(HttpMethod.DELETE, "/vehicles", "/vehicles/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_VEHICLE_STOCK)

                                                // ---- ACCESSORIES: GET inclui seller; escrita ADMIN/MANAGER +
                                                // accessory_stock
                                                .requestMatchers(HttpMethod.GET, "/accessories", "/accessories/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_SELLER,
                                                                AUTH_ACCESSORY_STOCK)
                                                .requestMatchers("/accessories", "/accessories/**")
                                                .hasAnyAuthority(AUTH_ADMIN, AUTH_MANAGER, AUTH_ACCESSORY_STOCK)

                                                // fallback: qualquer outra rota da API exige autenticação
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOriginPatterns(List.of(
                                "http://localhost:5173",
                                "http://localhost:3000",
                                "https://striking-upliftment-production-2300.up.railway.app",
                                "https://autoconfig-api-production.up.railway.app"));

                configuration.setAllowedMethods(List.of(
                                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

                configuration.setAllowedHeaders(List.of("*"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }
}