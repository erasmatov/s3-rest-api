package net.erasmatov.s3restapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import net.erasmatov.s3restapi.exception.UnauthorizedException;
import reactor.core.publisher.Mono;

import java.util.Date;

public class JwtHandler {
    private final String secret;

    public JwtHandler(String secret) {
        this.secret = secret;
    }

    public Mono<VerificationResult> checkToken(String accessToken) {
        return Mono.just(verifyToken(accessToken))
                .onErrorResume(e -> Mono.error(new UnauthorizedException(e.getMessage())));
    }

    private VerificationResult verifyToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Date expirationDate = claims.getExpiration();

        if (expirationDate.before(new Date())) {
            throw new RuntimeException("Token expired");
        }

        return new VerificationResult(claims, token);
    }

    private Claims getClaimsFromToken(String token) {
        String secretBase64 = Encoders.BASE64.encode(secret.getBytes());

        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretBase64.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static class VerificationResult {
        public Claims claims;
        public String token;

        public VerificationResult(Claims claims, String token) {
            this.claims = claims;
            this.token = token;
        }
    }
}
