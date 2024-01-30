package net.erasmatov.s3restapi.config;

import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.security.AuthenticationManager;
import net.erasmatov.s3restapi.security.BearerTokenServerAuthenticationConverter;
import net.erasmatov.s3restapi.security.JwtHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    @Value("${jwt.secret}")
    private String secret;
    private final String[] publicRoutes = {"/api/v1/auth/register", "/api/v1/auth/login"};

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationManager manager) {
        return http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS)
                .permitAll()
                .pathMatchers(publicRoutes)
                .permitAll()

                .pathMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/users").hasAnyRole("ADMIN", "MODERATOR")
                .pathMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("ADMIN", "MODERATOR")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAnyRole("ADMIN", "MODERATOR")

                .pathMatchers(HttpMethod.POST, "/api/v1/files").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/files").hasAnyRole("ADMIN", "MODERATOR")
                .pathMatchers(HttpMethod.GET, "/api/v1/files/**").hasAnyRole("ADMIN", "MODERATOR")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/files/**").hasAnyRole("ADMIN", "MODERATOR")

                .pathMatchers(HttpMethod.POST, "/api/v1/events").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/events").hasAnyRole("ADMIN", "MODERATOR")
                .pathMatchers(HttpMethod.GET, "/api/v1/events/**").hasAnyRole("ADMIN", "MODERATOR")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasAnyRole("ADMIN", "MODERATOR")

                .anyExchange()
                .authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) -> {
                    log.error("IN securityWebFilterChain - unauthorized error: {}", e.getMessage());
                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
                })
                .accessDeniedHandler((swe, e) -> {
                    log.error("IN securityWebFilterChain - access denied: {}", e.getMessage());
                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
                })
                .and()
                .addFilterAt(bearerAuthenticationFilter(manager), SecurityWebFiltersOrder.AUTHORIZATION)
                .build();
    }

    private AuthenticationWebFilter bearerAuthenticationFilter(AuthenticationManager manager) {
        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(manager);

        bearerAuthenticationFilter
                .setServerAuthenticationConverter(new BearerTokenServerAuthenticationConverter(new JwtHandler(secret)));

        bearerAuthenticationFilter
                .setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
        return bearerAuthenticationFilter;
    }
}
