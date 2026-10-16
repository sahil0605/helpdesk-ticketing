package com.kloudspot.helpdesk.auth.service;

import com.kloudspot.helpdesk.auth.dto.LoginRequest;
import com.kloudspot.helpdesk.auth.dto.LoginResponse;
import com.kloudspot.helpdesk.auth.dto.SignupRequest;

import java.util.List;

public interface IAuthService {

    LoginResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);

    List<String> companyEmailDomains();
}
