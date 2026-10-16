package com.kloudspot.helpdesk.ticket.repository;

import com.kloudspot.helpdesk.ticket.model.ETicket;
import com.kloudspot.helpdesk.ticket.model.ETicketHistory;
import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TicketHistoryRepositoryImpl implements ITicketHistoryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<ETicketHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId) {
        return jdbc.query(
                """
                SELECT h.id, h.ticket_id, h.field, h.old_value, h.new_value, h.changed_at,
                       h.changed_by_id, u.name AS changed_by_name, u.email AS changed_by_email, u.role AS changed_by_role
                FROM ticket_history h
                LEFT JOIN users u ON u.id = h.changed_by_id
                WHERE h.ticket_id = :ticketId
                ORDER BY h.changed_at ASC
                """,
                new MapSqlParameterSource("ticketId", ticketId),
                this::mapRow);
    }

    @Override
    public ETicketHistory save(ETicketHistory history) {
        if (history.getChangedAt() == null) {
            history.setChangedAt(LocalDateTime.now());
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ticketId", history.getTicket().getId())
                .addValue("changedById", history.getChangedBy().getId())
                .addValue("field", history.getField())
                .addValue("oldValue", history.getOldValue())
                .addValue("newValue", history.getNewValue())
                .addValue("changedAt", Timestamp.valueOf(history.getChangedAt()));
        Long id = jdbc.queryForObject(
                """
                INSERT INTO ticket_history (ticket_id, changed_by_id, field, old_value, new_value, changed_at)
                VALUES (:ticketId, :changedById, :field, :oldValue, :newValue, :changedAt)
                RETURNING id
                """,
                params,
                Long.class);
        history.setId(id);
        return history;
    }

    private ETicketHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
        ETicketHistory history = new ETicketHistory();
        history.setId(rs.getLong("id"));
        ETicket ticket = new ETicket();
        ticket.setId(rs.getLong("ticket_id"));
        history.setTicket(ticket);
        history.setField(rs.getString("field"));
        history.setOldValue(rs.getString("old_value"));
        history.setNewValue(rs.getString("new_value"));
        history.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
        Long changedById = rs.getObject("changed_by_id", Long.class);
        if (changedById != null) {
            EUser changedBy = new EUser();
            changedBy.setId(changedById);
            changedBy.setName(rs.getString("changed_by_name"));
            changedBy.setEmail(rs.getString("changed_by_email"));
            String role = rs.getString("changed_by_role");
            if (role != null) {
                changedBy.setRole(Role.valueOf(role));
            }
            history.setChangedBy(changedBy);
        }
        return history;
    }
}
