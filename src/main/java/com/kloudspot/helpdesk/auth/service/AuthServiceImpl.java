package com.kloudspot.helpdesk.auth.service;

import com.kloudspot.helpdesk.auth.HelpdeskProperties;
import com.kloudspot.helpdesk.auth.dto.LoginRequest;
import com.kloudspot.helpdesk.auth.dto.LoginResponse;
import com.kloudspot.helpdesk.auth.dto.SignupRequest;
import com.kloudspot.helpdesk.auth.model.AuthUser;
import com.kloudspot.helpdesk.auth.util.JwtUtil;
import com.kloudspot.helpdesk.exception.BadRequestException;
import com.kloudspot.helpdesk.organization.model.EOrganization;
import com.kloudspot.helpdesk.organization.repository.IOrganizationRepository;
import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;
import com.kloudspot.helpdesk.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserRepository userRepository;
    private final IOrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final HelpdeskProperties helpdeskProperties;

    @Override
    public List<String> companyEmailDomains() {
        return helpdeskProperties.getCompanyEmailDomains();
    }

    @Override
    @Transactional
    public LoginResponse signup(SignupRequest request) {
        String email = request.getEmail().trim();
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Signup rejected: email already in use [{}]", email);
            throw new BadRequestException("Email already in use");
        }

        boolean staff = helpdeskProperties.isCompanyEmail(email);
        Role role = staff ? Role.ORG_ADMIN : Role.CUSTOMER;
        EOrganization org = staff ? findOrCreateOrg(HelpdeskProperties.emailDomain(email))
                : joinOrg(request.getOrganizationName());

        EUser user = new EUser();
        user.setName(request.getName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setOrganization(org);
        user = userRepository.save(user);
        log.info("Signup ok userId={} email={} role={} orgId={}",
                user.getId(), user.getEmail(), user.getRole(), org.getId());
        return toLoginResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        return login(request.getEmail(), request.getPassword());
    }

    private LoginResponse login(String email, String password) {
        AuthUser authUser;
        try {
            authUser = (AuthUser) authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(email, password))
                    .getPrincipal();
        } catch (AuthenticationException ex) {
            log.warn("Login failed for [{}]: {}", email, ex.getClass().getSimpleName());
            throw new BadRequestException("Invalid email or password");
        }

        EUser user = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login authenticated but user row missing [{}]", authUser.getEmail());
                    return new BadRequestException("User not found");
                });
        log.info("Login ok userId={} email={} role={}", user.getId(), user.getEmail(), user.getRole());
        return toLoginResponse(user);
    }

    private LoginResponse toLoginResponse(EUser user) {
        Long orgId = user.getOrganization() == null ? null : user.getOrganization().getId();
        String orgName = user.getOrganization() == null ? null : user.getOrganization().getName();
        AuthUser authUser = new AuthUser(user.getId(), orgId, user.getEmail(), user.getPassword(), user.getRole());
        return new LoginResponse(
                jwtUtil.generateToken(authUser),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                orgId,
                orgName);
    }

    private EOrganization findOrCreateOrg(String domain) {
        return organizationRepository.findByNameIgnoreCase(domain).orElseGet(() -> {
            EOrganization org = new EOrganization();
            org.setName(domain);
            return organizationRepository.save(org);
        });
    }

    private EOrganization joinOrg(String organizationName) {
        if (organizationName == null || organizationName.isBlank()) {
            log.warn("Customer signup missing organization name");
            throw new BadRequestException("Enter the company name to join as a customer");
        }
        return organizationRepository.findByNameLoose(organizationName.trim())
                .orElseThrow(() -> {
                    log.warn("Customer signup org not found [{}]", organizationName);
                    return new BadRequestException(
                            "Organization not found. A company user must sign up first (email like you@kloudspot.in). Then join with kloudspot.in");
                });
    }
}
