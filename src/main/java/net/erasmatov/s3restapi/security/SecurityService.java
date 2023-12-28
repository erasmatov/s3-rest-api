package net.erasmatov.s3restapi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.exception.AuthException;
import net.erasmatov.s3restapi.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class SecurityService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${jwt.issuer}")
    private String issuer;

    private TokenDetails generateToken(UserEntity user) {
        Map<String, Object> claims = Map.of("role", user.getRole(), "username", user.getUsername());
        return generateToken(claims, user.getId().toString());
    }

    private TokenDetails generateToken(Map<String, Object> claims, String subject) {
        Long expirationTimeInMillis = expirationInSeconds * 1000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeInMillis);

        return generateToken(expirationDate, claims, subject);
    }

    private TokenDetails generateToken(Date expirationDate, Map<String, Object> claims, String subject) {
        Date createdDate = new Date();
        String secretBase64 = Encoders.BASE64.encode(secret.getBytes());
        Key key = Keys.hmacShaKeyFor(secretBase64.getBytes());

        String token = Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject(subject)
                .issuedAt(createdDate)
                .id(UUID.randomUUID().toString())
                .expiration(expirationDate)
                .signWith(key)
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }

    public Mono<TokenDetails> authenticate(String username, String password) {
        return userService.findUserByUsername(username)
                .flatMap(user -> {
                    if (user.getStatus().equals(EntityStatus.INACTIVE)) {
                        return Mono.error(new AuthException("Account INACTIVE", "USER_ACCOUNT_STATUS"));
                    }

                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException("Invalid password", "USER_ACCOUNT_PASSWORD"));
                    }

                    return Mono.just(generateToken(user).toBuilder()
                            .userId(user.getId())
                            .build());
                })
                .switchIfEmpty(Mono.error(new AuthException("Invalid username", "USER_ACCOUNT_USERNAME")));
    }
}
