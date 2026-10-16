package com.kloudspot.helpdesk.ticket.service;

import com.kloudspot.helpdesk.ticket.dto.CommentRequest;
import com.kloudspot.helpdesk.ticket.dto.CommentResponse;
import com.kloudspot.helpdesk.auth.model.AuthUser;

public interface ICommentService {

    CommentResponse addComment(Long ticketId, CommentRequest request, AuthUser user);
}
