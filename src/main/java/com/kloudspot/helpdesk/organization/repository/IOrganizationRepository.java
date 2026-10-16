package com.kloudspot.helpdesk.organization.repository;

import com.kloudspot.helpdesk.organization.model.EOrganization;

import java.util.Optional;

public interface IOrganizationRepository {

    Optional<EOrganization> findById(Long id);

    Optional<EOrganization> findByNameIgnoreCase(String name);

    Optional<EOrganization> findByNameLoose(String name);

    EOrganization save(EOrganization organization);
}
