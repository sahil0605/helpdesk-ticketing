package com.kloudspot.helpdesk.user.controller;

import com.kloudspot.helpdesk.exception.ResourceNotFoundException;
import com.kloudspot.helpdesk.user.dto.UserInfoResponse;
import com.kloudspot.helpdesk.user.model.EUser;
import com.kloudspot.helpdesk.user.repository.IUserRepository;
import com.kloudspot.helpdesk.auth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final IUserRepository userRepository;

    @GetMapping("/api/me")
    @Transactional(readOnly = true)
    public UserInfoResponse me(@AuthenticationPrincipal AuthUser authUser) {
        EUser user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String organizationName = user.getOrganization() == null ? null : user.getOrganization().getName();
        return new UserInfoResponse(
                authUser.getUserId(),
                user.getName(),
                authUser.getEmail(),
                authUser.getRole(),
                authUser.getOrganizationId(),
                organizationName);
    }
}
