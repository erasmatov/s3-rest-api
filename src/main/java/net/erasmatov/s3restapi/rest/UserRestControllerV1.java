package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.dto.UserDto;
import net.erasmatov.s3restapi.entity.UserRole;
import net.erasmatov.s3restapi.mapper.UserMapper;
import net.erasmatov.s3restapi.security.CustomPrincipal;
import net.erasmatov.s3restapi.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRestControllerV1 {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public Flux<UserDto> getUsers(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        //authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("MODERATOR"))
        if (authentication.getAuthorities().contains("ADMIN")) {
            return userService.getAllUsers()
                    .map(userMapper::map);
        } else {
            return Flux.merge(userService.getUsersByRole(UserRole.USER), userService.getUsersByRole(UserRole.MODERATOR))
                    .map(userMapper::map);
        }
    }

    @GetMapping("/{userId}")
    public Mono<UserDto> getUser(Authentication authentication, @PathVariable("userId") Long userId) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        if (authentication.getAuthorities().contains("ADMIN")) {
            return userService.findUserById(userId)
                    .map(userMapper::map);
        } else if (authentication.getAuthorities().contains("MODERATOR")) {
            return Flux.merge(userService.getUsersByRole(UserRole.USER), userService.findUserById(principal.getId()))
                    .filter(userEntity -> userEntity.getId().equals(userId))
                    .singleOrEmpty()
                    .map(userMapper::map);
        } else {
            return principal.getId().equals(userId) ?
                    userService.findUserById(userId).map(userMapper::map) : Mono.empty();
        }
    }
}
