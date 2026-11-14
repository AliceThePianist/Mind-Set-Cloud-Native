package com.mindset.profesionales.config;
import com.mindset.profesionales.security.JwtFilter;
import com.mindset.profesionales.security.ServicioInternoFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http, JwtFilter jwt, ServicioInternoFilter interno) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/interno/reservas/{solicitudId}").hasRole("SISTEMA")
                        .requestMatchers("/api/profesionales/mi-perfil", "/api/profesionales/mis-horarios/**").hasRole("PROFESIONAL")
                        .requestMatchers(HttpMethod.GET, "/api/profesionales", "/api/profesionales/{id}").hasRole("ESTUDIANTE")
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e.authenticationEntryPoint((q, r, x) -> r.sendError(401))
                        .accessDeniedHandler((q, r, x) -> r.sendError(403)))
                .httpBasic(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(interno, JwtFilter.class).build();
    }
    @Bean
    public FilterRegistrationBean<ServicioInternoFilter> registroInterno(ServicioInternoFilter filtro) {
        FilterRegistrationBean<ServicioInternoFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }
    @Bean
    public FilterRegistrationBean<JwtFilter> registro(JwtFilter jwt) {
        FilterRegistrationBean<JwtFilter> registro = new FilterRegistrationBean<>(jwt);
        registro.setEnabled(false);
        return registro;
    }
}
