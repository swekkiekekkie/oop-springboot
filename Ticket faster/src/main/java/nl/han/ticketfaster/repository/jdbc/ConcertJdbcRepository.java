package nl.han.ticketfaster.repository.jdbc;

import nl.han.ticketfaster.model.Concert;
import nl.han.ticketfaster.repository.ConcertRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConcertJdbcRepository implements ConcertRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ConcertJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Concert> findById(Long id) {
        String sql = "SELECT id, artist, location, concert_year, total_seats, cancelled FROM concerts WHERE id = :id";
        List<Concert> concerts = jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), (rs, rowNum) ->
                new Concert(
                        rs.getLong("id"),
                        rs.getString("artist"),
                        rs.getString("location"),
                        rs.getInt("concert_year"),
                        rs.getInt("total_seats"),
                        rs.getBoolean("cancelled")
                ));
        return concerts.stream().findFirst();
    }
}
