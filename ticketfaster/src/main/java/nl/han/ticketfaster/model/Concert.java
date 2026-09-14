package nl.han.ticketfaster.model;

public record Concert(Long id, String artist, String location, Integer concertYear, Integer totalSeats, boolean cancelled) {
}
