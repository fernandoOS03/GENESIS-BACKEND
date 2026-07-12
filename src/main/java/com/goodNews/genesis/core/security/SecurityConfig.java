package com.goodNews.genesis.core.security;

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

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.goodNews.genesis.core.security.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import com.goodNews.genesis.modulos.viajes.repositories.TravelInformationRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtFilter jwtFilter;

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
						.requestMatchers(HttpMethod.POST, "/auth").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/participantes").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/participantes/viaje/verificar").permitAll()
						.requestMatchers(HttpMethod.PATCH, "/api/v1/participantes/viaje/*").permitAll()
						.requestMatchers("/api/usuarios/**").hasAuthority("ROLE_SUPER_ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/pagos").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_COUNTRY_ADMIN", "ROLE_EDITOR")
						.requestMatchers("/api/pagos/**").hasAuthority("ROLE_SUPER_ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/tarifas/**").authenticated()
						.requestMatchers("/api/tarifas/**").hasAuthority("ROLE_SUPER_ADMIN")

						//.requestMatchers("/api/usuarios/**").permitAll()
						.requestMatchers("/api/v1/tarifas/**").permitAll()
						.requestMatchers("api/v1/boletos/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_COUNTRY_ADMIN", "ROLE_EDITOR")

						.anyRequest().authenticated())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}


	// ==========================================================
	// Reglas CORS
	// ==========================================================
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		// Creamos el objeto de confugracion CORS
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://192.168.1.176:5173/", "http://localhost:5173", "https://iyf-eventos.netlify.app"));
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


