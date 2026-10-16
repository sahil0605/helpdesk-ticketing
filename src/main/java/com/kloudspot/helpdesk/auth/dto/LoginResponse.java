package com.kloudspot.helpdesk.auth.dto;

import com.kloudspot.helpdesk.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private Long organizationId;
    private String organizationName;
}
