package com.kloudspot.helpdesk.ticket.repository;

import com.kloudspot.helpdesk.organization.model.EOrganization;
import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.ticket.model.ETicket;
import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryImpl implements ITicketRepository {

    private static final String SELECT = """
            SELECT t.id, t.subject, t.description, t.priority, t.status, t.created_at, t.updated_at,
                   t.organization_id, o.name AS organization_name,
                   t.requester_id, ru.name AS requester_name, ru.email AS requester_email, ru.role AS requester_role,
                   t.assigned_agent_id, au.name AS agent_name, au.email AS agent_email, au.role AS agent_role
            FROM ticket t
            LEFT JOIN organization o ON o.id = t.organization_id
            LEFT JOIN users ru ON ru.id = t.requester_id
            LEFT JOIN users au ON au.id = t.assigned_agent_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<ETicket> findByIdAndOrganizationId(Long id, Long organizationId) {
        return jdbc.query(
                SELECT + " WHERE t.id = :id AND t.organization_id = :organizationId",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("organizationId", organizationId),
                this::mapRow).stream().findFirst();
    }

    @Override
    public Page<ETicket> search(
            Long organizationId,
            Long requesterId,
            TicketStatus status,
            TicketPriority priority,
            Long assignedAgentId,
            String search,
            Pageable pageable) {
        StringBuilder where = new StringBuilder(" WHERE t.organization_id = :organizationId");
        MapSqlParameterSource params = new MapSqlParameterSource("organizationId", organizationId);
        if (requesterId != null) {
            where.append(" AND t.requester_id = :requesterId");
            params.addValue("requesterId", requesterId);
        }
        if (status != null) {
            where.append(" AND t.status = :status");
            params.addValue("status", status.name());
        }
        if (priority != null) {
            where.append(" AND t.priority = :priority");
            params.addValue("priority", priority.name());
        }
        if (assignedAgentId != null) {
            where.append(" AND t.assigned_agent_id = :assignedAgentId");
            params.addValue("assignedAgentId", assignedAgentId);
        }
        if (search != null && !search.isBlank()) {
            where.append(" AND (LOWER(t.subject) LIKE :search OR LOWER(t.description) LIKE :search)");
            params.addValue("search", "%" + search.toLowerCase() + "%");
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM ticket t" + where, params, Long.class);
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());
        var rows = jdbc.query(
                SELECT + where + " ORDER BY t.created_at DESC LIMIT :limit OFFSET :offset",
                params,
                this::mapRow);
        return new PageImpl<>(rows, pageable, total == null ? 0 : total);
    }

    @Override
    public ETicket save(ETicket ticket) {
        LocalDateTime now = LocalDateTime.now();
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(now);
        }
        ticket.setUpdatedAt(now);
        MapSqlParameterSource params = ticketParams(ticket);
        if (ticket.getId() == null) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO ticket (subject, description, priority, status, requester_id, assigned_agent_id,
                                        organization_id, created_at, updated_at)
                    VALUES (:subject, :description, :priority, :status, :requesterId, :assignedAgentId,
                            :organizationId, :createdAt, :updatedAt)
                    RETURNING id
                    """,
                    params,
                    Long.class);
            ticket.setId(id);
            return ticket;
        }
        params.addValue("id", ticket.getId());
        jdbc.update(
                """
                UPDATE ticket
                SET subject = :subject, description = :description, priority = :priority, status = :status,
                    requester_id = :requesterId, assigned_agent_id = :assignedAgentId,
                    organization_id = :organizationId, updated_at = :updatedAt
                WHERE id = :id
                """,
                params);
        return ticket;
    }

    private MapSqlParameterSource ticketParams(ETicket ticket) {
        return new MapSqlParameterSource()
                .addValue("subject", ticket.getSubject())
                .addValue("description", ticket.getDescription())
                .addValue("priority", ticket.getPriority().name())
                .addValue("status", ticket.getStatus().name())
                .addValue("requesterId", ticket.getRequester() != null ? ticket.getRequester().getId() : null)
                .addValue("assignedAgentId", ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null)
                .addValue("organizationId", ticket.getOrganization() != null ? ticket.getOrganization().getId() : null)
                .addValue("createdAt", Timestamp.valueOf(ticket.getCreatedAt()))
                .addValue("updatedAt", Timestamp.valueOf(ticket.getUpdatedAt()));
    }

    private ETicket mapRow(ResultSet rs, int rowNum) throws SQLException {
        ETicket ticket = new ETicket();
        ticket.setId(rs.getLong("id"));
        ticket.setSubject(rs.getString("subject"));
        ticket.setDescription(rs.getString("description"));
        ticket.setPriority(TicketPriority.valueOf(rs.getString("priority")));
        ticket.setStatus(TicketStatus.valueOf(rs.getString("status")));
        ticket.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        ticket.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        long organizationId = rs.getLong("organization_id");
        if (!rs.wasNull()) {
            EOrganization organization = new EOrganization();
            organization.setId(organizationId);
            organization.setName(rs.getString("organization_name"));
            ticket.setOrganization(organization);
        }
        ticket.setRequester(mapUser(
                rs.getObject("requester_id", Long.class),
                rs.getString("requester_name"),
                rs.getString("requester_email"),
                rs.getString("requester_role")));
        ticket.setAssignedAgent(mapUser(
                rs.getObject("assigned_agent_id", Long.class),
                rs.getString("agent_name"),
                rs.getString("agent_email"),
                rs.getString("agent_role")));
        return ticket;
    }

    private EUser mapUser(Long id, String name, String email, String role) {
        if (id == null) {
            return null;
        }
        EUser user = new EUser();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        if (role != null) {
            user.setRole(Role.valueOf(role));
        }
        return user;
    }
}
