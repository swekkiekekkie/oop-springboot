package nl.han.ticketfaster.repository.jdbc;

import nl.han.ticketfaster.model.Visitor;
import nl.han.ticketfaster.repository.VisitorRepository;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VisitorJdbcRepository implements VisitorRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VisitorJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Visitor> findByName(String name) {
        String sql = "SELECT id, name, vip, wishes FROM visitors WHERE name = :name";
        List<Visitor> visitors = jdbcTemplate.query(sql, new MapSqlParameterSource("name", name), (rs, rowNum) ->
                new Visitor(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBoolean("vip"),
                        rs.getString("wishes")
                ));
        return visitors.stream().findFirst();
    }

    @Override
    public long createVisitor(String name, boolean vip, String wishes) {
        String sql = "INSERT INTO visitors (name, vip, wishes) VALUES (:name, :vip, :wishes)";
        var params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("vip", vip)
                .addValue("wishes", wishes);
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKeyAs(Long.class);
    }
}
