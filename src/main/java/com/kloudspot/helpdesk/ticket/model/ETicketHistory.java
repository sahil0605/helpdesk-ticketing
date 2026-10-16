package com.kloudspot.helpdesk.ticket.model;

import com.kloudspot.helpdesk.user.model.EUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ETicketHistory {

    private Long id;
    private ETicket ticket;
    private EUser changedBy;
    private String field;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
}
