package nl.han.ticketfaster.repository;

import nl.han.ticketfaster.model.Visitor;

import java.util.Optional;

public interface VisitorRepository {
    Optional<Visitor> findByName(String name);

    long createVisitor(String name, boolean vip, String wishes);
}
