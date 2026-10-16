package com.kloudspot.helpdesk.ticket.repository;

import com.kloudspot.helpdesk.ticket.model.ETicket;
import com.kloudspot.helpdesk.ticket.model.ETicketComment;
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
public class TicketCommentRepositoryImpl implements ITicketCommentRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<ETicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId) {
        return jdbc.query(
                """
                SELECT c.id, c.ticket_id, c.message, c.internal, c.created_at,
                       c.author_id, u.name AS author_name, u.email AS author_email, u.role AS author_role
                FROM ticket_comment c
                LEFT JOIN users u ON u.id = c.author_id
                WHERE c.ticket_id = :ticketId
                ORDER BY c.created_at ASC
                """,
                new MapSqlParameterSource("ticketId", ticketId),
                this::mapRow);
    }

    @Override
    public ETicketComment save(ETicketComment comment) {
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ticketId", comment.getTicket().getId())
                .addValue("authorId", comment.getAuthor().getId())
                .addValue("message", comment.getMessage())
                .addValue("internal", comment.isInternal())
                .addValue("createdAt", Timestamp.valueOf(comment.getCreatedAt()));
        Long id = jdbc.queryForObject(
                """
                INSERT INTO ticket_comment (ticket_id, author_id, message, internal, created_at)
                VALUES (:ticketId, :authorId, :message, :internal, :createdAt)
                RETURNING id
                """,
                params,
                Long.class);
        comment.setId(id);
        return comment;
    }

    private ETicketComment mapRow(ResultSet rs, int rowNum) throws SQLException {
        ETicketComment comment = new ETicketComment();
        comment.setId(rs.getLong("id"));
        ETicket ticket = new ETicket();
        ticket.setId(rs.getLong("ticket_id"));
        comment.setTicket(ticket);
        comment.setMessage(rs.getString("message"));
        comment.setInternal(rs.getBoolean("internal"));
        comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Long authorId = rs.getObject("author_id", Long.class);
        if (authorId != null) {
            EUser author = new EUser();
            author.setId(authorId);
            author.setName(rs.getString("author_name"));
            author.setEmail(rs.getString("author_email"));
            String role = rs.getString("author_role");
            if (role != null) {
                author.setRole(Role.valueOf(role));
            }
            comment.setAuthor(author);
        }
        return comment;
    }
}
