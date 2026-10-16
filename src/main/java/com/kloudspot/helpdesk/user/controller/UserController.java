package com.kloudspot.helpdesk.user.controller;

import com.kloudspot.helpdesk.user.dto.CreateUserRequest;
import com.kloudspot.helpdesk.user.dto.UserResponse;
import com.kloudspot.helpdesk.auth.model.AuthUser;
import com.kloudspot.helpdesk.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @Operation(summary = "List staff users")
    @GetMapping
    public List<UserResponse> listStaff(@AuthenticationPrincipal AuthUser user) {
        return userService.listStaff(user);
    }

    @Operation(summary = "Create a user")
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request, user));
    }
}
