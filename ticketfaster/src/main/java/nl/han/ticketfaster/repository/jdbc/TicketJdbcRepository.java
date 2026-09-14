package nl.han.ticketfaster.repository.jdbc;

import nl.han.ticketfaster.model.TicketPurchase;
import nl.han.ticketfaster.repository.TicketRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketJdbcRepository implements TicketRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TicketJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int countSoldTicketsForConcert(Long concertId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM ticket_purchases WHERE concert_id = :concertId";
        Integer total = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("concertId", concertId), Integer.class);
        return total == null ? 0 : total;
    }

    @Override
    public Optional<TicketPurchase> findByVisitorAndConcert(Long visitorId, Long concertId) {
        String sql = """
                SELECT id, visitor_id, concert_id, quantity
                FROM ticket_purchases
                WHERE visitor_id = :visitorId AND concert_id = :concertId
                """;
        List<TicketPurchase> purchases = jdbcTemplate.query(sql,
                new MapSqlParameterSource()
                        .addValue("visitorId", visitorId)
                        .addValue("concertId", concertId),
                (rs, rowNum) -> new TicketPurchase(
                        rs.getLong("id"),
                        rs.getLong("visitor_id"),
                        rs.getLong("concert_id"),
                        rs.getInt("quantity")
                ));
        return purchases.stream().findFirst();
    }

    @Override
    public void createPurchase(Long visitorId, Long concertId, Integer quantity) {
        String sql = """
                INSERT INTO ticket_purchases(visitor_id, concert_id, quantity)
                VALUES (:visitorId, :concertId, :quantity)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("visitorId", visitorId)
                .addValue("concertId", concertId)
                .addValue("quantity", quantity));
    }

    @Override
    public void updatePurchase(Long purchaseId, Integer quantity) {
        String sql = "UPDATE ticket_purchases SET quantity = :quantity WHERE id = :purchaseId";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("quantity", quantity)
                .addValue("purchaseId", purchaseId));
    }

    @Override
    public int deleteByVisitorAndConcert(Long visitorId, Long concertId) {
        String sql = "DELETE FROM ticket_purchases WHERE visitor_id = :visitorId AND concert_id = :concertId";
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("visitorId", visitorId)
                .addValue("concertId", concertId));
    }

    @Override
    public void deleteByConcertId(Long concertId) {
        String sql = "DELETE FROM ticket_purchases WHERE concert_id = :concertId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("concertId", concertId));
    }
}
