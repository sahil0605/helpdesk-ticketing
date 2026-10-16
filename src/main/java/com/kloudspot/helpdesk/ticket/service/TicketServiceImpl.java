package com.kloudspot.helpdesk.ticket.service;

import com.kloudspot.helpdesk.auth.model.AuthUser;
import com.kloudspot.helpdesk.exception.BadRequestException;
import com.kloudspot.helpdesk.exception.ForbiddenException;
import com.kloudspot.helpdesk.exception.ResourceNotFoundException;
import com.kloudspot.helpdesk.ticket.dto.CommentResponse;
import com.kloudspot.helpdesk.ticket.dto.CreateTicketRequest;
import com.kloudspot.helpdesk.ticket.dto.HistoryResponse;
import com.kloudspot.helpdesk.ticket.dto.TicketDetailResponse;
import com.kloudspot.helpdesk.ticket.dto.TicketResponse;
import com.kloudspot.helpdesk.ticket.dto.UpdateTicketRequest;
import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.ticket.model.ETicket;
import com.kloudspot.helpdesk.ticket.model.ETicketHistory;
import com.kloudspot.helpdesk.ticket.repository.ITicketCommentRepository;
import com.kloudspot.helpdesk.ticket.repository.ITicketHistoryRepository;
import com.kloudspot.helpdesk.ticket.repository.ITicketRepository;
import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.user.model.EUser;
import com.kloudspot.helpdesk.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements ITicketService {

    private final ITicketRepository ticketRepository;
    private final IUserRepository userRepository;
    private final ITicketCommentRepository ticketCommentRepository;
    private final ITicketHistoryRepository ticketHistoryRepository;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, AuthUser user) {
        if (!user.isCustomer()) {
            log.warn("Create ticket blocked for {} role={}", user.getEmail(), user.getRole());
            throw new ForbiddenException("Only customers can create tickets");
        }
        EUser requester = requireUser(user);
        ETicket ticket = new ETicket();
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequester(requester);
        ticket.setOrganization(requester.getOrganization());
        ETicket saved = ticketRepository.save(ticket);
        log.info("Ticket created id={} by {} orgId={}", saved.getId(), user.getEmail(), user.getOrganizationId());
        return TicketResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> listTickets(
            TicketStatus status,
            TicketPriority priority,
            Long assignedAgentId,
            String search,
            Pageable pageable,
            AuthUser user) {
        Long requesterId = user.isCustomer() ? user.getUserId() : null;
        return ticketRepository
                .search(user.getOrganizationId(), requesterId, status, priority, assignedAgentId, search, pageable)
                .map(TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicket(Long id, AuthUser user) {
        ETicket ticket = visibleTicket(id, user);
        var comments = ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
        if (user.isCustomer()) {
            comments = comments.stream().filter(c -> !c.isInternal()).toList();
        }
        var history = ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticket.getId());
        return new TicketDetailResponse(
                TicketResponse.from(ticket),
                comments.stream().map(CommentResponse::from).toList(),
                history.stream().map(HistoryResponse::from).toList());
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request, AuthUser user) {
        if (user.isCustomer()) {
            log.warn("Update ticket {} blocked for customer {}", id, user.getEmail());
            throw new ForbiddenException("Not allowed to modify tickets");
        }
        ETicket ticket = visibleTicket(id, user);
        EUser actor = requireUser(user);

        if (request.getSubject() != null) {
            ticket.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getStatus() != null && request.getStatus() != ticket.getStatus()) {
            record(ticket, actor, "STATUS", enumName(ticket.getStatus()), request.getStatus().name());
            ticket.setStatus(request.getStatus());
        }
        if (request.getAssignedAgentId() != null
                && !Objects.equals(request.getAssignedAgentId(), ticket.assignedAgentId())) {
            EUser assignee = resolveAssignee(request.getAssignedAgentId(), user.getOrganizationId());
            record(ticket, actor, "ASSIGNEE", stringId(ticket.assignedAgentId()), stringId(assignee.getId()));
            ticket.setAssignedAgent(assignee);
        }
        TicketResponse response = TicketResponse.from(ticketRepository.save(ticket));
        log.info("Ticket {} updated by {}", id, user.getEmail());
        return response;
    }

    private ETicket visibleTicket(Long id, AuthUser user) {
        ETicket ticket = ticketRepository.findByIdAndOrganizationId(id, user.getOrganizationId())
                .orElseThrow(() -> {
                    log.warn("Ticket {} not found orgId={} user={}", id, user.getOrganizationId(), user.getEmail());
                    return new ResourceNotFoundException("Ticket not found");
                });
        if (user.isCustomer() && !ticket.belongsTo(user.getUserId())) {
            log.warn("Ticket {} hidden from customer {}", id, user.getEmail());
            throw new ResourceNotFoundException("Ticket not found");
        }
        return ticket;
    }

    private EUser requireUser(AuthUser user) {
        return userRepository.findByIdAndOrganizationId(user.getUserId(), user.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private EUser resolveAssignee(Long assignedAgentId, Long organizationId) {
        EUser assignee = userRepository.findByIdAndOrganizationId(assignedAgentId, organizationId)
                .orElseThrow(() -> new BadRequestException("Assignee not found in organization"));
        if (assignee.getRole() != Role.SUPPORT_AGENT && assignee.getRole() != Role.ORG_ADMIN) {
            throw new BadRequestException("Assignee must be a support agent or org admin");
        }
        return assignee;
    }

    private void record(ETicket ticket, EUser actor, String field, String oldValue, String newValue) {
        ETicketHistory history = new ETicketHistory();
        history.setTicket(ticket);
        history.setChangedBy(actor);
        history.setField(field);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        ticketHistoryRepository.save(history);
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String stringId(Long id) {
        return id == null ? null : String.valueOf(id);
    }
}
