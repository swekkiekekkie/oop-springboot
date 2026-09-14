package nl.han.ticketfaster.repository;

import nl.han.ticketfaster.model.Concert;

import java.util.Optional;

public interface ConcertRepository {
    Optional<Concert> findById(Long id);
}
