package com.kloudspot.helpdesk.ticket.service;

import com.kloudspot.helpdesk.ticket.dto.CreateTicketRequest;
import com.kloudspot.helpdesk.ticket.dto.TicketDetailResponse;
import com.kloudspot.helpdesk.ticket.dto.TicketResponse;
import com.kloudspot.helpdesk.ticket.dto.UpdateTicketRequest;
import com.kloudspot.helpdesk.organization.model.EOrganization;
import com.kloudspot.helpdesk.user.enums.Role;
import com.kloudspot.helpdesk.ticket.model.ETicket;
import com.kloudspot.helpdesk.ticket.model.ETicketComment;
import com.kloudspot.helpdesk.ticket.model.ETicketHistory;
import com.kloudspot.helpdesk.ticket.enums.TicketPriority;
import com.kloudspot.helpdesk.ticket.enums.TicketStatus;
import com.kloudspot.helpdesk.user.model.EUser;
import com.kloudspot.helpdesk.exception.BadRequestException;
import com.kloudspot.helpdesk.exception.ForbiddenException;
import com.kloudspot.helpdesk.exception.ResourceNotFoundException;
import com.kloudspot.helpdesk.ticket.repository.ITicketCommentRepository;
import com.kloudspot.helpdesk.ticket.repository.ITicketHistoryRepository;
import com.kloudspot.helpdesk.ticket.repository.ITicketRepository;
import com.kloudspot.helpdesk.user.repository.IUserRepository;
import com.kloudspot.helpdesk.auth.model.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private ITicketRepository ticketRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private ITicketCommentRepository ticketCommentRepository;
    @Mock
    private ITicketHistoryRepository ticketHistoryRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private EOrganization organization;
    private EUser requester;
    private AuthUser staffUser;

    @BeforeEach
    void setUp() {
        organization = new EOrganization();
        organization.setId(10L);
        requester = user(1L, Role.CUSTOMER);
        staffUser = auth(2L, Role.SUPPORT_AGENT);
    }

    @Test
    void getTicket_otherOrganization_throwsResourceNotFound() {
        AuthUser user = auth(1L, Role.SUPPORT_AGENT);
        when(ticketRepository.findByIdAndOrganizationId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.getTicket(99L, user));
    }

    @Test
    void getTicket_customer_hidesInternalComments() {
        AuthUser customer = auth(1L, Role.CUSTOMER);
        ETicket ticket = existingTicket(TicketStatus.OPEN);
        ETicketComment pub = new ETicketComment();
        pub.setMessage("visible");
        pub.setInternal(false);
        ETicketComment internal = new ETicketComment();
        internal.setMessage("staff only");
        internal.setInternal(true);
        when(ticketRepository.findByIdAndOrganizationId(5L, 10L)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(5L)).thenReturn(List.of(pub, internal));
        when(ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(5L)).thenReturn(List.of());

        TicketDetailResponse response = ticketService.getTicket(5L, customer);

        assertEquals(1, response.getComments().size());
        assertEquals("visible", response.getComments().get(0).getMessage());
    }

    @Test
    void createTicket_loggedInUser_setsRequesterOrganizationAndOpenStatus() {
        AuthUser user = auth(1L, Role.CUSTOMER);
        CreateTicketRequest request = new CreateTicketRequest();
        request.setSubject("Printer down");
        request.setDescription("Won't print");
        request.setPriority(TicketPriority.HIGH);
        when(userRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(requester));
        when(ticketRepository.save(any(ETicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.createTicket(request, user);

        ArgumentCaptor<ETicket> captor = ArgumentCaptor.forClass(ETicket.class);
        verify(ticketRepository).save(captor.capture());
        ETicket saved = captor.getValue();
        assertEquals(requester, saved.getRequester());
        assertEquals(organization, saved.getOrganization());
        assertEquals(TicketStatus.OPEN, saved.getStatus());
        assertEquals(TicketStatus.OPEN, response.getStatus());
    }

    @Test
    void createTicket_staff_throwsForbidden() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setSubject("Should not work");
        request.setDescription("Staff cannot file tickets");
        request.setPriority(TicketPriority.MEDIUM);

        assertThrows(ForbiddenException.class, () -> ticketService.createTicket(request, staffUser));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void updateTicket_statusChanged_savesTicketHistory() {
        ETicket ticket = existingTicket(TicketStatus.OPEN);
        when(ticketRepository.findByIdAndOrganizationId(5L, 10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByIdAndOrganizationId(2L, 10L)).thenReturn(Optional.of(user(2L, Role.SUPPORT_AGENT)));
        when(ticketRepository.save(any(ETicket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        ticketService.updateTicket(5L, request, staffUser);

        ArgumentCaptor<ETicketHistory> captor = ArgumentCaptor.forClass(ETicketHistory.class);
        verify(ticketHistoryRepository).save(captor.capture());
        assertEquals("STATUS", captor.getValue().getField());
        assertEquals(TicketStatus.OPEN.name(), captor.getValue().getOldValue());
        assertEquals(TicketStatus.IN_PROGRESS.name(), captor.getValue().getNewValue());
    }

    @Test
    void updateTicket_assigneeChanged_savesTicketHistory() {
        ETicket ticket = existingTicket(TicketStatus.OPEN);
        EUser assignee = user(8L, Role.SUPPORT_AGENT);
        when(ticketRepository.findByIdAndOrganizationId(5L, 10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByIdAndOrganizationId(2L, 10L)).thenReturn(Optional.of(user(2L, Role.SUPPORT_AGENT)));
        when(userRepository.findByIdAndOrganizationId(8L, 10L)).thenReturn(Optional.of(assignee));
        when(ticketRepository.save(any(ETicket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setAssignedAgentId(8L);

        ticketService.updateTicket(5L, request, staffUser);

        ArgumentCaptor<ETicketHistory> captor = ArgumentCaptor.forClass(ETicketHistory.class);
        verify(ticketHistoryRepository).save(captor.capture());
        assertEquals("ASSIGNEE", captor.getValue().getField());
        assertEquals("8", captor.getValue().getNewValue());
    }

    @Test
    void updateTicket_assigneeFromOtherOrg_throwsBadRequest() {
        ETicket ticket = existingTicket(TicketStatus.OPEN);
        when(ticketRepository.findByIdAndOrganizationId(5L, 10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByIdAndOrganizationId(2L, 10L)).thenReturn(Optional.of(user(2L, Role.SUPPORT_AGENT)));
        when(userRepository.findByIdAndOrganizationId(99L, 10L)).thenReturn(Optional.empty());
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setAssignedAgentId(99L);

        assertThrows(BadRequestException.class, () -> ticketService.updateTicket(5L, request, staffUser));
        verify(ticketHistoryRepository, never()).save(any());
    }

    @Test
    void updateTicket_customer_throwsForbidden() {
        AuthUser customer = auth(1L, Role.CUSTOMER);
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.CLOSED);

        assertThrows(ForbiddenException.class, () -> ticketService.updateTicket(5L, request, customer));
    }

    private ETicket existingTicket(TicketStatus status) {
        ETicket ticket = new ETicket();
        ticket.setId(5L);
        ticket.setStatus(status);
        ticket.setRequester(requester);
        ticket.setOrganization(organization);
        return ticket;
    }

    private EUser user(Long id, Role role) {
        EUser user = new EUser();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@example.com");
        user.setRole(role);
        user.setOrganization(organization);
        return user;
    }

    private AuthUser auth(Long userId, Role role) {
        return new AuthUser(userId, 10L, "user" + userId + "@example.com", "pw", role);
    }
}
