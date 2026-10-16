package com.kloudspot.helpdesk.ticket.model;

import com.kloudspot.helpdesk.organization.model.EOrganization;
import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.user.model.EUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ETicket {

    private Long id;
    private String subject;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private EUser requester;
    private EUser assignedAgent;
    private EOrganization organization;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean belongsTo(Long userId) {
        return requester != null && requester.getId().equals(userId);
    }

    public Long organizationId() {
        return organization == null ? null : organization.getId();
    }

    public Long assignedAgentId() {
        return assignedAgent == null ? null : assignedAgent.getId();
    }
}
