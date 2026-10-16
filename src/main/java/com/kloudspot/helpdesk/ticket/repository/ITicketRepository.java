package com.kloudspot.helpdesk.ticket.repository;

import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.ticket.model.ETicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ITicketRepository {

    Optional<ETicket> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<ETicket> search(
            Long organizationId,
            Long requesterId,
            TicketStatus status,
            TicketPriority priority,
            Long assignedAgentId,
            String search,
            Pageable pageable);

    ETicket save(ETicket ticket);
}
