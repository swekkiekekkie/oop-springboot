package nl.han.ticketfaster.controller;

import jakarta.validation.Valid;
import nl.han.ticketfaster.dto.MessageResponse;
import nl.han.ticketfaster.dto.TicketAvailabilityResponse;
import nl.han.ticketfaster.dto.TicketPurchaseRequest;
import nl.han.ticketfaster.service.TicketService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/available")
    public TicketAvailabilityResponse getAvailability(@RequestParam Long concertId) {
        return ticketService.getAvailability(concertId);
    }

    @PostMapping("/purchases")
    public MessageResponse buyTickets(@Valid @RequestBody TicketPurchaseRequest request) {
        return ticketService.buyTickets(request);
    }

    @PutMapping
    public MessageResponse changeTickets(@Valid @RequestBody TicketPurchaseRequest request) {
        return ticketService.changeTickets(request);
    }

    @DeleteMapping
    public MessageResponse cancelTickets(@RequestParam String visitorName, @RequestParam Long concertId) {
        return ticketService.cancelTickets(visitorName, concertId);
    }
}
