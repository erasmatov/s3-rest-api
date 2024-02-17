package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.dto.UserDto;
import net.erasmatov.s3restapi.dto.UserUpdateRequestDto;
import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.entity.UserRole;
import net.erasmatov.s3restapi.mapper.UserMapper;
import net.erasmatov.s3restapi.security.CustomPrincipal;
import net.erasmatov.s3restapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRestControllerV1 {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public Mono<UserDto> createUser(@RequestBody UserDto dto) {
        UserEntity entity = userMapper.map(dto);
        return userService.saveUser(entity).map(userMapper::map);
    }

    @GetMapping("/{userId}")
    public Mono<UserDto> readUser(Authentication authentication, @PathVariable("userId") Long userId) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return userService.findUserById(userId).map(userMapper::map);
        } else {
            return Flux.merge(userService.getUsersByRole(UserRole.USER), userService.findUserById(principal.getId()))
                    .filter(userEntity -> userEntity.getId().equals(userId))
                    .singleOrEmpty().map(userMapper::map);
        }
    }

    @GetMapping
    public Flux<UserDto> readUsers(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return userService.getAllUsers()
                    .map(userMapper::map);
        } else {
            return Flux.merge(userService.getUsersByRole(UserRole.USER), userService.getUsersByRole(UserRole.MODERATOR))
                    .map(userMapper::map);
        }
    }

    @PutMapping("/{userId}")
    public Mono<UserDto> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateRequestDto dto) {
        return userService.updateUser(userId, dto).map(userMapper::map);
    }

    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("userId") Long userId) {
        return userService.deleteUserById(userId).map(userEntity -> ResponseEntity.status(HttpStatus.OK).build());
    }
}
