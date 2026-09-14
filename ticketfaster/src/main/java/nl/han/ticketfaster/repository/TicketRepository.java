package nl.han.ticketfaster.repository;

import nl.han.ticketfaster.model.TicketPurchase;

import java.util.Optional;

public interface TicketRepository {
    int countSoldTicketsForConcert(Long concertId);

    Optional<TicketPurchase> findByVisitorAndConcert(Long visitorId, Long concertId);

    void createPurchase(Long visitorId, Long concertId, Integer quantity);

    void updatePurchase(Long purchaseId, Integer quantity);

    int deleteByVisitorAndConcert(Long visitorId, Long concertId);

    void deleteByConcertId(Long concertId);
}
