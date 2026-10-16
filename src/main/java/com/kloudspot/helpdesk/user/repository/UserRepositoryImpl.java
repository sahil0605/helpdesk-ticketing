package com.kloudspot.helpdesk.user.repository;

import com.kloudspot.helpdesk.organization.model.EOrganization;
import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

    private static final String SELECT = """
            SELECT u.id, u.name, u.email, u.password, u.role,
                   o.id AS organization_id, o.name AS organization_name
            FROM users u
            LEFT JOIN organization o ON o.id = u.organization_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<EUser> findById(Long id) {
        return jdbc.query(SELECT + " WHERE u.id = :id", new MapSqlParameterSource("id", id), this::mapRow)
                .stream().findFirst();
    }

    @Override
    public Optional<EUser> findByEmail(String email) {
        return jdbc.query(SELECT + " WHERE u.email = :email", new MapSqlParameterSource("email", email), this::mapRow)
                .stream().findFirst();
    }

    @Override
    public Optional<EUser> findByIdAndOrganizationId(Long id, Long organizationId) {
        return jdbc.query(
                SELECT + " WHERE u.id = :id AND u.organization_id = :organizationId",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("organizationId", organizationId),
                this::mapRow).stream().findFirst();
    }

    @Override
    public List<EUser> findByOrganizationIdAndRoleIn(Long organizationId, Collection<Role> roles) {
        return jdbc.query(
                SELECT + " WHERE u.organization_id = :organizationId AND u.role IN (:roles)",
                new MapSqlParameterSource()
                        .addValue("organizationId", organizationId)
                        .addValue("roles", roles.stream().map(Role::name).toList()),
                this::mapRow);
    }

    @Override
    public EUser save(EUser user) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", user.getName())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("role", user.getRole().name())
                .addValue("organizationId", user.getOrganization() != null ? user.getOrganization().getId() : null);
        if (user.getId() == null) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO users (name, email, password, role, organization_id)
                    VALUES (:name, :email, :password, :role, :organizationId)
                    RETURNING id
                    """,
                    params,
                    Long.class);
            user.setId(id);
            return user;
        }
        params.addValue("id", user.getId());
        jdbc.update(
                """
                UPDATE users
                SET name = :name, email = :email, password = :password, role = :role, organization_id = :organizationId
                WHERE id = :id
                """,
                params);
        return user;
    }

    private EUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        EUser user = new EUser();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(Role.valueOf(rs.getString("role")));
        long organizationId = rs.getLong("organization_id");
        if (!rs.wasNull()) {
            EOrganization organization = new EOrganization();
            organization.setId(organizationId);
            organization.setName(rs.getString("organization_name"));
            user.setOrganization(organization);
        }
        return user;
    }
}
