package com.kloudspot.helpdesk.ticket.dto;

import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {

    @NotBlank
    @Size(max = 255)
    private String subject;

    @NotBlank
    private String description;

    @NotNull
    private TicketPriority priority;
}
