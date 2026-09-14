package nl.han.ticketfaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.han.ticketfaster.dto.MessageResponse;
import nl.han.ticketfaster.dto.TicketAvailabilityResponse;
import nl.han.ticketfaster.dto.TicketPurchaseRequest;
import nl.han.ticketfaster.exception.ApiExceptionHandler;
import nl.han.ticketfaster.exception.NotFoundException;
import nl.han.ticketfaster.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import(ApiExceptionHandler.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @Test
    void getAvailability_shouldReturn200() throws Exception {
        when(ticketService.getAvailability(1L)).thenReturn(new TicketAvailabilityResponse(1L, 5, 15));

        mockMvc.perform(get("/tickets/availability").param("concertId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").value(1))
                .andExpect(jsonPath("$.soldTickets").value(5))
                .andExpect(jsonPath("$.availableSeats").value(15));
    }

    @Test
    void getAvailability_shouldReturn404WhenConcertUnknown() throws Exception {
        when(ticketService.getAvailability(99L)).thenThrow(new NotFoundException("Concert niet gevonden."));

        mockMvc.perform(get("/tickets/availability").param("concertId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Concert niet gevonden."));
    }

    @Test
    void postBuyTickets_shouldReturn200() throws Exception {
        when(ticketService.buyTickets(any(TicketPurchaseRequest.class))).thenReturn(new MessageResponse("Tickets gekocht."));

        mockMvc.perform(post("/tickets/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TicketPurchaseRequest("Alice", 1L, 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tickets gekocht."));
    }

    @Test
    void postBuyTickets_shouldReturn400ForInvalidBody() throws Exception {
        mockMvc.perform(post("/tickets/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ongeldige input."));
    }

    @Test
    void putChangeTickets_shouldReturn200() throws Exception {
        when(ticketService.changeTickets(any(TicketPurchaseRequest.class))).thenReturn(new MessageResponse("Tickets gewijzigd."));

        mockMvc.perform(put("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TicketPurchaseRequest("Alice", 1L, 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tickets gewijzigd."));
    }

    @Test
    void putChangeTickets_shouldReturn404WhenMissingPurchase() throws Exception {
        when(ticketService.changeTickets(any(TicketPurchaseRequest.class)))
                .thenThrow(new NotFoundException("Geen tickets gevonden voor combinatie bezoeker en concert."));

        mockMvc.perform(put("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TicketPurchaseRequest("Alice", 1L, 3))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Geen tickets gevonden voor combinatie bezoeker en concert."));
    }

    @Test
    void deleteCancelTickets_shouldReturn200() throws Exception {
        when(ticketService.cancelTickets(eq("Alice"), eq(1L))).thenReturn(new MessageResponse("Tickets gewijzigd."));

        mockMvc.perform(delete("/tickets")
                        .param("visitorName", "Alice")
                        .param("concertId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tickets geannuleerd."));
    }

    @Test
    void deleteCancelTickets_shouldReturn404() throws Exception {
        when(ticketService.cancelTickets(eq("Alice"), eq(99L))).thenThrow(new NotFoundException("Concert niet gevonden."));

        mockMvc.perform(delete("/tickets")
                        .param("visitorName", "Alice")
                        .param("concertId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Concert niet gevonden."));
    }
}
