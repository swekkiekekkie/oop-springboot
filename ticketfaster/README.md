> **OWE-1 FUSTEN — Niveau 2 | Week 3 | Les 3.1**
> Repository: `n2-ticketfaster-api` · Branch: `ticketfaster-api-student-buggy`
> Type: **studentcasus** — debugging
> Docentreferentie: branch `uitwerking-docenten` (`ticketfaster-api-docent`)

# TicketFaster API Student Buggy

Bewust buggy Spring Boot variant van TicketFaster voor debug-oefeningen in les 3.1.

## Doel van deze variant

Studenten analyseren bestaande fouten, debuggen gericht met breakpoints en variabeleninspectie en herstellen de bugs — zonder nieuwe functionaliteit te bouwen.

## Huidige toestand van de applicatie

Wat zit er wel in:

- één `TicketController`
- ticketfeatures:
  - `GET /tickets/available?concertId={id}`
  - `POST /tickets/purchases`
  - `PUT /tickets`
  - `DELETE /tickets?visitorName={name}&concertId={id}`
- service- en repositorylaag voor tickets, bezoekers en concerten
- tests voor controller- en servicelogica

Wat zit er niet in:

- geen CSV-export
- geen `purchase-check`
- geen `purchase-history`
- geen aparte visitor- of concertcontroller
- geen stabiele, volledig betrouwbare applicatiestatus: deze variant bevat bewust fouten

## Starten

```bash
mvn spring-boot:run
```

## Testen

```bash
mvn test
```
