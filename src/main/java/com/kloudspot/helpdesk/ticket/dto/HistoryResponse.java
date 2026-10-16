package com.kloudspot.helpdesk.ticket.dto;

import com.kloudspot.helpdesk.ticket.model.ETicketHistory;
import com.kloudspot.helpdesk.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class HistoryResponse {

    private Long id;
    private UserResponse changedBy;
    private String field;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;

    public static HistoryResponse from(ETicketHistory history) {
        return new HistoryResponse(
                history.getId(),
                UserResponse.from(history.getChangedBy()),
                history.getField(),
                history.getOldValue(),
                history.getNewValue(),
                history.getChangedAt());
    }
}
