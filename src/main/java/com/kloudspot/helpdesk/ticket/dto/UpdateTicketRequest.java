package com.kloudspot.helpdesk.ticket.dto;

import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTicketRequest {

    private String subject;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private Long assignedAgentId;
}
