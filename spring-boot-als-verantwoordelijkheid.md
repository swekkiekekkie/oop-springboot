# Spring Boot als verantwoordelijkheidshiërarchie

Dit document bouwt voort op de OOP-filosofie: **Wie is waarvoor verantwoordelijk?**  
Primitives = wat ik zelf weet. Object-velden = aan wie ik iets kan vragen of een taak kan geven. Methoden = vragen beantwoorden of taken uitvoeren.

Spring Boot is geen ander soort denken. Het is vooral:

1. een **gestandaardiseerde verdeling van verantwoordelijkheden** (vaste “banen” in je applicatie), en  
2. **standaardfunctionaliteit** die je niet zelf hoeft te bouwen (webserver, JSON, wiring, validatie, database-toegang, …), zodat je minder fouten maakt in die infrastructuur en je aandacht houdt bij domeinregels.

---

## 1. Van één `main` naar een organisatie

In de eerdere opdrachten bouwde je een hiërarchie met de hand:

```
Main
 └── Game / Cinema / Clinic
      └── … objecten die vragen beantwoorden en taken doen
```

Een web-API doet hetzelfde, maar de “klant” is geen `System.out`, maar een **HTTP-request**. Spring Boot geeft je een standaard organisatiemodel:

```
HTTP-request
 └── Controller     ← “loket”
      └── Service   ← “afdeling met regels”
           └── Repository ← “archief / database”
                └── Database
```

Dat is opnieuw: bovenkant hoeft niet alles te weten. Hij weet vooral **aan wie** hij de vraag of taak moet geven.

---

## 2. Gestandaardiseerde banen (verantwoordelijkheden)

| Rol | OOP-vertaling | Wel / niet |
|-----|---------------|------------|
| **Controller** | Neemt externe vragen/taken aan (HTTP) | Geen SQL, geen diepe businessregels |
| **Service** | Kent de regels van het proces | Mag repositories vragen; gooit duidelijke fouten |
| **Repository** | Bewaart en haalt gegevens | Geen HTTP; liefst geen businessbeleid |
| **Model** | Domeingegevens | “Wat weet dit stukje wereld?” |
| **DTO** | Boodschap van/naar buiten | Geen verborgen business in setters |
| **Exception handler** | Vertaalt fouten naar een antwoord naar buiten | Zoals UI-strings niet in de Enemy horen |

Dit is dezelfde les als bij `Enemy.takeDamage` vs. `setHealth` vanaf buiten: **niet in andermans administratie neuzen**. De controller “pakt” niet zelf de database; hij vraagt de service. De service “pakt” niet zelf ResultSets; hij vraagt de repository.

---

## 3. Object-velden = aan wie mag ik iets vragen?

In TicketFaster zie je letterlijk:

```java
public class TicketController {
    private final TicketService ticketService;
    // ...
}
```

Vertaling: *Controller kent een TicketService — dat is iemand aan wie hij taken kan geven.*

```java
public class TicketService {
    private final VisitorRepository visitorRepository;
    private final ConcertRepository concertRepository;
    private final TicketRepository ticketRepository;
}
```

Vertaling: *Service kent drie repositories — dat zijn medewerkers voor data-vragen en -taken.*

Spring Boot **vult die velden** via de constructor (dependency injection). Jij schrijft niet `new TicketService(...)` in de controller. Dat is standaardfunctionaliteit: minder koppelingsfouten, makkelijker testen (je kunt een nep-repository meegeven).

Mentale plaatje:

```
TicketController
 └── ticketService          ← object waaraan Controller mag delegeren
      ├── visitorRepository
      ├── concertRepository
      └── ticketRepository  ← objecten waaraan Service mag delegeren
```

---

## 4. Interface vs. implementatie = belofte vs. hoe

```
ConcertRepository  (interface)     = “Belooft: vind concert op id”
ConcertJdbcRepository              = “Doet dat met SQL”
```

De service vraagt:

> Repository, heb jij dit concert?

Niet:

> Geef mij je `JdbcTemplate` en Connection, dan schrijf ik zelf SQL in de service.

Dat is precies het verschil tussen **vragen stellen** en **in andermans administratie neuzen** uit de OOP-filosofie.

---

## 5. Wat Spring Boot standaard voor je doet

Zonder Spring zou je zelf moeten:

- een webserver starten en HTTP-paden mappen;
- JSON van/naar Java-objecten vertalen;
- objecten handmatig aan elkaar knopen;
- vaak zelf validatie en foutresponses bouwen;
- JDBC-boilerplate herhalen.

Spring Boot-starters pakken dat in. Jij focust op:

- welke **vragen** en **taken** horen bij welke class;
- welke **regels** in de service horen;
- welke **data** in repository/model horen.

Oftewel: verantwoordelijkheid eerst, frameworks daarna.

---

## 6. Een request als delegatieketen

Voorbeeld: tickets kopen.

```
Browser / Swagger
 │  POST /tickets/purchases
 ▼
TicketController.buyTickets(request)
 │  "Service, voer deze aankoop uit"
 ▼
TicketService.buyTickets(request)
 │  "Klopt het aantal?"
 │  "VisitorRepository, ken je deze bezoeker?"
 │  "ConcertRepository, ken je dit concert?"
 │  "Mag dit concert nog verkocht worden?"
 │  "TicketRepository, hoeveel zijn er al verkocht?"
 │  "TicketRepository, sla deze aankoop op"
 ▼
TicketJdbcRepository (SQL)
 ▼
Database
```

En bij fout:

```
TicketService
 │  throw NotFoundException / ValidationException
 ▼
ApiExceptionHandler
 │  "Maak hier een HTTP 404 of 400 van"
 ▼
JSON MessageResponse naar de client
```

Zelfde patroon als:

```
Game → Player.attack → Weapon.getDamage → Enemy.takeDamage
```

Alleen is de “buitenwereld” nu HTTP in plaats van `main`.

---

## 7. Waar beginners struikelen (in verantwoordelijkheidstaal)

| Symptoom | Vertaling |
|----------|-----------|
| SQL in de controller | Loket doet het archiefwerk zelf |
| Businessregels alleen in SQL of alleen in de UI | Regels hebben geen vaste eigenaar (service) |
| Controller die repositories direct kent | Loket belt het archief zonder afdeling |
| Dikke “Utils” die alles doen | Geen hiërarchie, wel een rommelzolder |
| Strings/HTML in het model | Domeinobject doet opeens UI-werk |

---

## 8. Samenvatting (drie regels + Spring)

Uit de OOP-filosofie:

1. Mijn primitive variabelen zijn dingen die ik zelf weet.  
2. Mijn objectvariabelen zijn anderen aan wie ik vragen kan stellen of taken kan geven.  
3. Mijn methoden zijn die vragen en taken.

Daar komt bij Spring Boot bij:

4. **Rollen zijn gestandaardiseerd** (controller / service / repository / …), zodat teams dezelfde hiërarchie herkennen.  
5. **Infrastructuur is standaard**, zodat jij die hiërarchie niet steeds opnieuw uitvindt — en minder fouten maakt in boilerplate.

Als je TicketFaster verkent: vraag bij elke class opnieuw *wat weet jij zelf, wie ken je, wat kunnen anderen je vragen, wat delegeer je?* De annotaties (`@RestController`, `@Service`, `@Repository`) zijn labels op die banen — niet magie die OOP vervangt.

Zie ook de concrete tabellen en ketens in [`UITLEG.md`](UITLEG.md).
