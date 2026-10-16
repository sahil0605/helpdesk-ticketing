package com.kloudspot.helpdesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TicketDetailResponse {

    private TicketResponse ticket;
    private List<CommentResponse> comments;
    private List<HistoryResponse> history;
}
