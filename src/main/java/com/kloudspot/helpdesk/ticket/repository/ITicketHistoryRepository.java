package com.kloudspot.helpdesk.ticket.repository;

import com.kloudspot.helpdesk.ticket.model.ETicketHistory;

import java.util.List;

public interface ITicketHistoryRepository {

    List<ETicketHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);

    ETicketHistory save(ETicketHistory history);
}
