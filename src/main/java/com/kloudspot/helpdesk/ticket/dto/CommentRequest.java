package com.kloudspot.helpdesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

    @NotBlank
    private String message;

    private boolean internal;
}
