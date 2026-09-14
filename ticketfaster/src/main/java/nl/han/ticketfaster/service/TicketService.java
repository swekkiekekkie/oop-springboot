package nl.han.ticketfaster.service;

import nl.han.ticketfaster.dto.MessageResponse;
import nl.han.ticketfaster.dto.TicketAvailabilityResponse;
import nl.han.ticketfaster.dto.TicketPurchaseRequest;
import nl.han.ticketfaster.exception.NotFoundException;
import nl.han.ticketfaster.exception.ValidationException;
import nl.han.ticketfaster.model.Concert;
import nl.han.ticketfaster.model.TicketPurchase;
import nl.han.ticketfaster.model.Visitor;
import nl.han.ticketfaster.repository.ConcertRepository;
import nl.han.ticketfaster.repository.TicketRepository;
import nl.han.ticketfaster.repository.VisitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final VisitorRepository visitorRepository;
    private final ConcertRepository concertRepository;
    private final TicketRepository ticketRepository;

    public TicketService(VisitorRepository visitorRepository,
                         ConcertRepository concertRepository,
                         TicketRepository ticketRepository) {
        this.visitorRepository = visitorRepository;
        this.concertRepository = concertRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public TicketAvailabilityResponse getAvailability(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new NotFoundException("Concert niet gevonden."));
        int sold = ticketRepository.countSoldTicketsForConcert(concert.id());
        int available = concert.totalSeats() + sold;
        return new TicketAvailabilityResponse(concert.id(), sold, available);
    }

    @Transactional
    public MessageResponse buyTickets(TicketPurchaseRequest request) {
        validateTicketQuantity(request.quantity());
        Visitor visitor = visitorRepository.findByName(request.visitorName())
                .orElseThrow(() -> new NotFoundException("Bezoeker niet gevonden."));
        Concert concert = concertRepository.findById(request.concertId())
                .orElseThrow(() -> new NotFoundException("Concert niet gevonden."));
        ensureConcertCanBeSold(concert);

        int sold = ticketRepository.countSoldTicketsForConcert(concert.id());
        int available = concert.totalSeats() - sold;
        if (request.quantity() > available) {
            throw new ValidationException("Niet genoeg tickets beschikbaar voor dit concert.");
        }

        ticketRepository.createPurchase(visitor.id(), concert.id(), request.quantity());
        return new MessageResponse("Tickets gekocht.");
    }

    @Transactional
    public MessageResponse changeTickets(TicketPurchaseRequest request) {
        validateTicketQuantity(request.quantity());
        Visitor visitor = visitorRepository.findByName(request.visitorName())
                .orElseThrow(() -> new NotFoundException("Bezoeker niet gevonden."));
        Concert concert = concertRepository.findById(request.concertId())
                .orElseThrow(() -> new NotFoundException("Concert niet gevonden."));
        ensureConcertCanBeSold(concert);

        TicketPurchase existing = ticketRepository.findByVisitorAndConcert(visitor.id(), concert.id())
                .orElseThrow(() -> new NotFoundException("Geen tickets gevonden voor combinatie bezoeker en concert."));

        if (existing.quantity().equals(request.quantity())) {
            throw new ValidationException("Nieuw aantal is gelijk aan huidig aantal.");
        }

        int soldWithoutCurrent = ticketRepository.countSoldTicketsForConcert(concert.id()) - existing.quantity();
        int availableForChange = concert.totalSeats() - soldWithoutCurrent;

        if (request.quantity() > availableForChange) {
            throw new ValidationException("Niet genoeg tickets beschikbaar voor deze wijziging.");
        }

        ticketRepository.updatePurchase(existing.id(), request.quantity());
        return new MessageResponse("Tickets gewijzigd.");
    }

    @Transactional
    public MessageResponse cancelTickets(String visitorName, Long concertId) {
        Visitor visitor = visitorRepository.findByName(visitorName)
                .orElseThrow(() -> new NotFoundException("Bezoeker niet gevonden."));
        concertRepository.findById(concertId)
                .orElseThrow(() -> new NotFoundException("Concert niet gevonden."));

        int deleted = ticketRepository.deleteByVisitorAndConcert(visitor.id(), concertId);
        if (deleted == 0) {
            throw new NotFoundException("Geen tickets gevonden voor combinatie bezoeker en concert.");
        }

        return new MessageResponse("Tickets geannuleerd.");
    }

    private void validateTicketQuantity(Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > 5) {
            throw new ValidationException("Aantal tickets moet tussen 1 en 5 liggen.");
        }
    }

    private void ensureConcertCanBeSold(Concert concert) {
        if (concert.cancelled()) {
            throw new ValidationException("Concert is geannuleerd.");
        }
    }
}
