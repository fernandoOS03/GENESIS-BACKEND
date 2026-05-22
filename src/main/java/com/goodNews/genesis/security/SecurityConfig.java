package com.goodNews.genesis.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.goodNews.genesis.repository.TravelInformationRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// private final TravelInformationRepository travelInformationRepository;

	SecurityConfig(TravelInformationRepository travelInformationRepository) {
		// this.travelInformationRepository = travelInformationRepository;
	}
	// CSRF = Cross-Site Request Forgery -> Falsificacion de peticiones entre sitios
	/*
	 * @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws
	 * Exception { http .authorizeHttpRequests(auth -> auth
	 * .anyRequest().authenticated() ) .httpBasic(); return http.build(); }
	 */

	// ==========================================================
	// Seguridad
	// ==========================================================
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.anyRequest().permitAll())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		return http.build();
	}

	// ==========================================================
	// Reglas CORS
	// ==========================================================
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		// Creamos el objeto de confugracion CORS
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://10.0.1.23:5173/3", "http://localhost:5173"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);

		// Creamos la fuente que mapea las URLs -> configuration
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		// Spring security usará esto automaticamente
		return source;
	}

	// ==========================================================
	// Hasheador (Función hash unidireccional)
	// ==========================================================
	@Bean
	public PasswordEncoder passwordEncoder(){
		return  new BCryptPasswordEncoder();
	}



}
