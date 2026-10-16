package com.kloudspot.helpdesk.organization.repository;

import com.kloudspot.helpdesk.organization.model.EOrganization;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements IOrganizationRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<EOrganization> findById(Long id) {
        return jdbc.query(
                "SELECT id, name FROM organization WHERE id = :id",
                new MapSqlParameterSource("id", id),
                this::mapRow).stream().findFirst();
    }

    @Override
    public Optional<EOrganization> findByNameIgnoreCase(String name) {
        return jdbc.query(
                "SELECT id, name FROM organization WHERE LOWER(name) = LOWER(:name)",
                new MapSqlParameterSource("name", name),
                this::mapRow).stream().findFirst();
    }

    @Override
    public Optional<EOrganization> findByNameLoose(String name) {
        String trimmed = name.trim();
        String stem = trimmed.toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s+(org|organization|inc|ltd)\\.?$", "")
                .trim();
        return jdbc.query(
                """
                SELECT id, name FROM organization
                WHERE LOWER(name) = LOWER(:name)
                   OR LOWER(REPLACE(name, ' ', '')) = LOWER(REPLACE(:name, ' ', ''))
                   OR LOWER(name) LIKE :stem || '.%'
                   OR LOWER(SPLIT_PART(name, '.', 1)) = :stem
                """,
                new MapSqlParameterSource()
                        .addValue("name", trimmed)
                        .addValue("stem", stem),
                this::mapRow).stream().findFirst();
    }

    @Override
    public EOrganization save(EOrganization organization) {
        if (organization.getId() == null) {
            Long id = jdbc.queryForObject(
                    "INSERT INTO organization (name) VALUES (:name) RETURNING id",
                    new MapSqlParameterSource("name", organization.getName()),
                    Long.class);
            organization.setId(id);
            return organization;
        }
        jdbc.update(
                "UPDATE organization SET name = :name WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("id", organization.getId())
                        .addValue("name", organization.getName()));
        return organization;
    }

    private EOrganization mapRow(ResultSet rs, int rowNum) throws SQLException {
        EOrganization organization = new EOrganization();
        organization.setId(rs.getLong("id"));
        organization.setName(rs.getString("name"));
        return organization;
    }
}
