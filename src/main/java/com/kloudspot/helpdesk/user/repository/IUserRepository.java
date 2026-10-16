package com.kloudspot.helpdesk.user.repository;

import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IUserRepository {

    Optional<EUser> findById(Long id);

    Optional<EUser> findByEmail(String email);

    Optional<EUser> findByIdAndOrganizationId(Long id, Long organizationId);

    List<EUser> findByOrganizationIdAndRoleIn(Long organizationId, Collection<Role> roles);

    EUser save(EUser user);
}
