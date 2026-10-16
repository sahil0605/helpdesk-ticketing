package com.kloudspot.helpdesk.ticket.model;

import com.kloudspot.helpdesk.user.model.EUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ETicketComment {

    private Long id;
    private ETicket ticket;
    private EUser author;
    private String message;
    private boolean internal;
    private LocalDateTime createdAt;
}
