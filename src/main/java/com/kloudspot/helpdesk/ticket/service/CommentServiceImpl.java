package com.kloudspot.helpdesk.ticket.service;

import com.kloudspot.helpdesk.auth.model.AuthUser;
import com.kloudspot.helpdesk.exception.ResourceNotFoundException;
import com.kloudspot.helpdesk.ticket.dto.CommentRequest;
import com.kloudspot.helpdesk.ticket.dto.CommentResponse;
import com.kloudspot.helpdesk.ticket.model.ETicket;
import com.kloudspot.helpdesk.ticket.model.ETicketComment;
import com.kloudspot.helpdesk.ticket.repository.ITicketCommentRepository;
import com.kloudspot.helpdesk.ticket.repository.ITicketRepository;
import com.kloudspot.helpdesk.user.model.EUser;
import com.kloudspot.helpdesk.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final ITicketRepository ticketRepository;
    private final ITicketCommentRepository ticketCommentRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse addComment(Long ticketId, CommentRequest request, AuthUser user) {
        ETicket ticket = ticketRepository.findByIdAndOrganizationId(ticketId, user.getOrganizationId())
                .orElseThrow(() -> {
                    log.warn("Comment on missing ticket {} orgId={} user={}", ticketId, user.getOrganizationId(), user.getEmail());
                    return new ResourceNotFoundException("Ticket not found");
                });
        if (user.isCustomer() && !ticket.belongsTo(user.getUserId())) {
            log.warn("Comment on ticket {} blocked for customer {}", ticketId, user.getEmail());
            throw new ResourceNotFoundException("Ticket not found");
        }
        EUser author = userRepository.findByIdAndOrganizationId(user.getUserId(), user.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ETicketComment comment = new ETicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setMessage(request.getMessage());
        comment.setInternal(!user.isCustomer() && request.isInternal());
        return CommentResponse.from(ticketCommentRepository.save(comment));
    }
}
