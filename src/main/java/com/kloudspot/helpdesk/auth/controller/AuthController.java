package com.kloudspot.helpdesk.auth.controller;

import com.kloudspot.helpdesk.auth.dto.LoginRequest;
import com.kloudspot.helpdesk.auth.dto.LoginResponse;
import com.kloudspot.helpdesk.auth.dto.SignupRequest;
import com.kloudspot.helpdesk.auth.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @GetMapping("/company-domains")
    public List<String> companyDomains() {
        return authService.companyEmailDomains();
    }

    @PostMapping("/signup")
    public LoginResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
