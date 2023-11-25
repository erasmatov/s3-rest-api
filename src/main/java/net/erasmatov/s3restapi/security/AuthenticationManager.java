package net.erasmatov.s3restapi.security;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.exception.UnauthorizedException;
import net.erasmatov.s3restapi.service.UserService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final UserService userService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return userService.getUserById(principal.getId())
                .filter(UserEntity -> UserEntity.getStatus().equals(EntityStatus.ACTIVE))
                .switchIfEmpty(Mono.error(new UnauthorizedException("User disabled")))
                .map(user -> authentication);
    }
}