package com.kloudspot.helpdesk.ticket.dto;

import com.kloudspot.helpdesk.ticket.model.ETicketComment;
import com.kloudspot.helpdesk.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private UserResponse author;
    private String message;
    private boolean internal;
    private LocalDateTime createdAt;

    public static CommentResponse from(ETicketComment comment) {
        return new CommentResponse(
                comment.getId(),
                UserResponse.from(comment.getAuthor()),
                comment.getMessage(),
                comment.isInternal(),
                comment.getCreatedAt());
    }
}
