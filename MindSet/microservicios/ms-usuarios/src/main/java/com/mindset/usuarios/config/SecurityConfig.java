
package com.mindset.usuarios.config;

import com.mindset.usuarios.security.JwtFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtFilter jwtFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/registro",
                                "/api/usuarios/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios"
                        ).hasRole("ADMINISTRADOR")


                        .requestMatchers(
                                "/api/usuarios/mi-cuenta"
                        ).authenticated()


                        .requestMatchers(
                                "/api/usuarios/{id}"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers("/error").permitAll()

                        .anyRequest().denyAll()
                )

                .exceptionHandling(errores -> errores

                        .authenticationEntryPoint((request, response, error) ->
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "No autorizado"
                                )
                        )

                        .accessDeniedHandler((request, response, error) ->
                                response.sendError(
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Acceso denegado"
                                )
                        )
                )

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(
            JwtFilter jwtFilter) {

        FilterRegistrationBean<JwtFilter> registro =
                new FilterRegistrationBean<>(jwtFilter);

        registro.setEnabled(false);

        return registro;
    }
}
