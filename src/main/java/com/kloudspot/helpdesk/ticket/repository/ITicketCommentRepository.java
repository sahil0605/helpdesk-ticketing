package com.kloudspot.helpdesk.ticket.repository;

import com.kloudspot.helpdesk.ticket.model.ETicketComment;

import java.util.List;

public interface ITicketCommentRepository {

    List<ETicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    ETicketComment save(ETicketComment comment);
}
