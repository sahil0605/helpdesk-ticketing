package com.kloudspot.helpdesk.user.service;

import com.kloudspot.helpdesk.auth.model.AuthUser;
import com.kloudspot.helpdesk.exception.BadRequestException;
import com.kloudspot.helpdesk.exception.ForbiddenException;
import com.kloudspot.helpdesk.exception.ResourceNotFoundException;
import com.kloudspot.helpdesk.organization.repository.IOrganizationRepository;
import com.kloudspot.helpdesk.user.dto.CreateUserRequest;
import com.kloudspot.helpdesk.user.dto.UserResponse;
import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;
import com.kloudspot.helpdesk.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final IOrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listStaff(AuthUser user) {
        if (user.isCustomer()) {
            log.warn("List staff blocked for customer {}", user.getEmail());
            throw new ForbiddenException("Not allowed to list staff");
        }
        return userRepository.findByOrganizationIdAndRoleIn(
                        user.getOrganizationId(),
                        List.of(Role.SUPPORT_AGENT, Role.ORG_ADMIN))
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request, AuthUser user) {
        if (!user.isOrgAdmin()) {
            log.warn("Create user blocked for {} role={}", user.getEmail(), user.getRole());
            throw new ForbiddenException("Only org admins can create users");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Create user rejected: email already in use [{}]", request.getEmail());
            throw new BadRequestException("Email already in use");
        }
        EUser created = new EUser();
        created.setName(request.getName());
        created.setEmail(request.getEmail());
        created.setPassword(passwordEncoder.encode(request.getPassword()));
        created.setRole(request.getRole());
        created.setOrganization(organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found")));
        UserResponse response = UserResponse.from(userRepository.save(created));
        log.info("User created id={} email={} role={} by {}", response.getId(), created.getEmail(), created.getRole(), user.getEmail());
        return response;
    }
}
