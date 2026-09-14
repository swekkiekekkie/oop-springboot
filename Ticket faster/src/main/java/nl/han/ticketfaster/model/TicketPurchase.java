package nl.han.ticketfaster.model;

public record TicketPurchase(Long id, Long visitorId, Long concertId, Integer quantity) {
}
