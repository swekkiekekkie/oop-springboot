package nl.han.ticketfaster.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private VisitorRepository visitorRepository;
    @Mock
    private ConcertRepository concertRepository;
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    private Visitor visitor;
    private Concert concert;

    @BeforeEach
    void setUp() {
        visitor = new Visitor(1L, "Alice", true, "Wens");
        concert = new Concert(10L, "Artiest", "Locatie", 2027, 10, false);
    }

    @Test
    void getAvailability_shouldReturnValues() {
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.countSoldTicketsForConcert(10L)).thenReturn(4);

        TicketAvailabilityResponse response = ticketService.getAvailability(10L);

        assertEquals(10L, response.concertId());
        assertEquals(4, response.soldTickets());
        assertEquals(6, response.availableSeats());
    }

    @Test
    void getAvailability_shouldThrowWhenConcertUnknown() {
        when(concertRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ticketService.getAvailability(10L));
    }

    @Test
    void buyTickets_shouldThrowWhenQuantityNull() {
        assertThrows(ValidationException.class, () -> ticketService.buyTickets(new TicketPurchaseRequest("Alice", 10L, null)));
    }

    @Test
    void buyTickets_shouldThrowWhenQuantityTooHigh() {
        assertThrows(ValidationException.class, () -> ticketService.buyTickets(new TicketPurchaseRequest("Alice", 10L, 6)));
    }

    @Test
    void buyTickets_shouldThrowWhenVisitorUnknown() {
        when(visitorRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.buyTickets(new TicketPurchaseRequest("Unknown", 10L, 2)));
    }

    @Test
    void buyTickets_shouldThrowWhenConcertUnknown() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.buyTickets(new TicketPurchaseRequest("Alice", 99L, 2)));
    }

    @Test
    void buyTickets_shouldThrowWhenConcertCancelled() {
        Concert cancelled = new Concert(10L, "Artiest", "Locatie", 2027, 10, true);
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(cancelled));

        assertThrows(ValidationException.class,
                () -> ticketService.buyTickets(new TicketPurchaseRequest("Alice", 10L, 2)));
    }

    @Test
    void buyTickets_shouldThrowWhenInsufficientSeats() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.countSoldTicketsForConcert(10L)).thenReturn(9);

        assertThrows(ValidationException.class,
                () -> ticketService.buyTickets(new TicketPurchaseRequest("Alice", 10L, 2)));
        verify(ticketRepository, never()).createPurchase(1L, 10L, 2);
    }

    @Test
    void buyTickets_shouldCreatePurchaseWhenValid() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.countSoldTicketsForConcert(10L)).thenReturn(2);

        var response = ticketService.buyTickets(new TicketPurchaseRequest("Alice", 10L, 3));

        assertEquals("Tickets gekocht.", response.message());
        verify(ticketRepository).createPurchase(1L, 10L, 3);
    }

    @Test
    void changeTickets_shouldThrowWhenQuantityTooLow() {
        assertThrows(ValidationException.class,
                () -> ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 0)));
    }

    @Test
    void changeTickets_shouldThrowWhenVisitorUnknown() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 2)));
    }

    @Test
    void changeTickets_shouldThrowWhenConcertUnknown() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 2)));
    }

    @Test
    void changeTickets_shouldThrowWhenConcertCancelled() {
        Concert cancelled = new Concert(10L, "Artiest", "Locatie", 2027, 10, true);
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(cancelled));

        assertThrows(ValidationException.class,
                () -> ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 2)));
    }

    @Test
    void changeTickets_shouldThrowWhenNoExistingPurchase() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.findByVisitorAndConcert(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 2)));
    }

    @Test
    void changeTickets_shouldThrowWhenSameAmount() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.findByVisitorAndConcert(1L, 10L)).thenReturn(Optional.of(new TicketPurchase(5L, 1L, 10L, 3)));

        assertThrows(ValidationException.class,
                () -> ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 3)));
    }

    @Test
    void changeTickets_shouldThrowWhenInsufficientSeatsForChange() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.findByVisitorAndConcert(1L, 10L)).thenReturn(Optional.of(new TicketPurchase(5L, 1L, 10L, 3)));
        when(ticketRepository.countSoldTicketsForConcert(10L)).thenReturn(10);

        assertThrows(ValidationException.class,
                () -> ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 4)));
    }

    @Test
    void changeTickets_shouldUpdateWhenValid() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.findByVisitorAndConcert(1L, 10L)).thenReturn(Optional.of(new TicketPurchase(5L, 1L, 10L, 3)));
        when(ticketRepository.countSoldTicketsForConcert(10L)).thenReturn(7);

        var response = ticketService.changeTickets(new TicketPurchaseRequest("Alice", 10L, 5));

        assertEquals("Tickets gewijzigd.", response.message());
        verify(ticketRepository).updatePurchase(5L, 5);
    }

    @Test
    void cancelTickets_shouldThrowWhenVisitorUnknown() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.cancelTickets("Alice", 10L));
    }

    @Test
    void cancelTickets_shouldThrowWhenConcertUnknown() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.cancelTickets("Alice", 10L));
    }

    @Test
    void cancelTickets_shouldThrowWhenNoRowsDeleted() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.deleteByVisitorAndConcert(1L, 10L)).thenReturn(0);

        assertThrows(NotFoundException.class,
                () -> ticketService.cancelTickets("Alice", 10L));
    }

    @Test
    void cancelTickets_shouldReturnMessageWhenDeleted() {
        when(visitorRepository.findByName("Alice")).thenReturn(Optional.of(visitor));
        when(concertRepository.findById(10L)).thenReturn(Optional.of(concert));
        when(ticketRepository.deleteByVisitorAndConcert(1L, 10L)).thenReturn(2);

        var response = ticketService.cancelTickets("Alice", 10L);

        assertEquals("Tickets geannuleerd.", response.message());
    }
}
