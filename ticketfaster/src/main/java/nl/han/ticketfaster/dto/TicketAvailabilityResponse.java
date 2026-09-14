package nl.han.ticketfaster.dto;

public record TicketAvailabilityResponse(Long concertId, Integer soldTickets, Integer availableSeats) {
}
