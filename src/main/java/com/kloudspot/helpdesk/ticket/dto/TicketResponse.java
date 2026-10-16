package com.kloudspot.helpdesk.ticket.dto;

import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.ticket.model.ETicket;
import com.kloudspot.helpdesk.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String subject;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private UserResponse requester;
    private UserResponse assignedAgent;
    private Long organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketResponse from(ETicket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                UserResponse.from(ticket.getRequester()),
                UserResponse.from(ticket.getAssignedAgent()),
                ticket.organizationId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}
