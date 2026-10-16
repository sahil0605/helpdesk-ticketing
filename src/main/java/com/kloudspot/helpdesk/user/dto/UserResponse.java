package com.kloudspot.helpdesk.user.dto;

import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;

    public static UserResponse from(EUser user) {
        return user == null ? null : new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
