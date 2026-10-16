package com.kloudspot.helpdesk.ticket.controller;

import com.kloudspot.helpdesk.ticket.dto.CommentRequest;
import com.kloudspot.helpdesk.ticket.dto.CommentResponse;
import com.kloudspot.helpdesk.ticket.dto.CreateTicketRequest;
import com.kloudspot.helpdesk.ticket.dto.TicketDetailResponse;
import com.kloudspot.helpdesk.ticket.dto.TicketResponse;
import com.kloudspot.helpdesk.ticket.dto.UpdateTicketRequest;
import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.auth.model.AuthUser;
import com.kloudspot.helpdesk.ticket.service.ICommentService;
import com.kloudspot.helpdesk.ticket.service.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final ITicketService ticketService;
    private final ICommentService commentService;

    @Operation(summary = "Create a ticket")
    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request, user));
    }

    @Operation(summary = "List tickets")
    @GetMapping
    public Page<TicketResponse> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long assignedAgentId,
            @RequestParam(required = false) String search,
            Pageable pageable,
            @AuthenticationPrincipal AuthUser user) {
        return ticketService.listTickets(status, priority, assignedAgentId, search, pageable, user);
    }

    @Operation(summary = "Get ticket details")
    @GetMapping("/{id}")
    public TicketDetailResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return ticketService.getTicket(id, user);
    }

    @Operation(summary = "Update a ticket")
    @PutMapping("/{id}")
    public TicketResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ticketService.updateTicket(id, request, user);
    }

    @Operation(summary = "Add a comment")
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(id, request, user));
    }
}
