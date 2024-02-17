package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.dto.AuthRequestDto;
import net.erasmatov.s3restapi.dto.AuthResponseDto;
import net.erasmatov.s3restapi.dto.UserDto;
import net.erasmatov.s3restapi.dto.UserRegisterRequestDto;
import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.mapper.UserMapper;
import net.erasmatov.s3restapi.security.CustomPrincipal;
import net.erasmatov.s3restapi.security.SecurityService;
import net.erasmatov.s3restapi.service.EventService;
import net.erasmatov.s3restapi.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {

    private final SecurityService securityService;
    private final UserService userService;
    private final EventService eventService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public Mono<UserDto> register(@RequestBody UserRegisterRequestDto dto) {
        return userService.registerUser(UserEntity.builder()
                        .username(dto.getUsername())
                        .password(dto.getPassword())
                        .role(dto.getRole())
                        .build())
                .map(userMapper::map);
    }

    @PostMapping("/login")
    public Mono<AuthResponseDto> login(@RequestBody AuthRequestDto dto) {
        return securityService.authenticate(dto.getUsername(), dto.getPassword())
                .flatMap(tokenDetails -> Mono.just(
                        AuthResponseDto.builder()
                                .userId(tokenDetails.getUserId())
                                .token(tokenDetails.getToken())
                                .issuedAt(tokenDetails.getIssuedAt())
                                .expiresAt(tokenDetails.getExpiresAt())
                                .build()
                ));
    }

    @GetMapping("/info")
    public Mono<UserDto> userInfo(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        return Mono.zip(userService.findUserById(customPrincipal.getId()),
                        eventService.getEventsByUserId(customPrincipal.getId()).collectList())
                .map(tuples -> {
                    UserEntity userEntity = tuples.getT1();
                    userEntity.setEvents(tuples.getT2());
                    return userEntity;
                })
                .map(userMapper::map);
    }
}
