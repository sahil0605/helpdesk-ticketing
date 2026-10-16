package com.kloudspot.helpdesk.user.model;

import com.kloudspot.helpdesk.organization.model.EOrganization;
import com.kloudspot.helpdesk.user.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EUser {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private EOrganization organization;
}
