package com.kloudspot.helpdesk.ticket.service;

import com.kloudspot.helpdesk.ticket.dto.CreateTicketRequest;
import com.kloudspot.helpdesk.ticket.dto.TicketDetailResponse;
import com.kloudspot.helpdesk.ticket.dto.TicketResponse;
import com.kloudspot.helpdesk.ticket.dto.UpdateTicketRequest;
import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.auth.model.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITicketService {

    TicketResponse createTicket(CreateTicketRequest request, AuthUser user);

    Page<TicketResponse> listTickets(
            TicketStatus status,
            TicketPriority priority,
            Long assignedAgentId,
            String search,
            Pageable pageable,
            AuthUser user);

    TicketDetailResponse getTicket(Long id, AuthUser user);

    TicketResponse updateTicket(Long id, UpdateTicketRequest request, AuthUser user);
}
