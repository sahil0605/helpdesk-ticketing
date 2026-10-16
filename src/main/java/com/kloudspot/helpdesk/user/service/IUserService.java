package com.kloudspot.helpdesk.user.service;

import com.kloudspot.helpdesk.user.dto.CreateUserRequest;
import com.kloudspot.helpdesk.user.dto.UserResponse;
import com.kloudspot.helpdesk.auth.model.AuthUser;

import java.util.List;

public interface IUserService {

    List<UserResponse> listStaff(AuthUser user);

    UserResponse createUser(CreateUserRequest request, AuthUser user);
}
