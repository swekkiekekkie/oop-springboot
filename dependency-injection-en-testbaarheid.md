# Dependency injection en testbaarheid

Dit document volgt op [`spring-boot-als-verantwoordelijkheid.md`](spring-boot-als-verantwoordelijkheid.md).

Daar ging het over: *wie is waarvoor verantwoordelijk?*  
Hier gaat het over: *hoe krijg een class de objecten die hij nodig heeft — en waarom maakt dat testen makkelijker?*

Voorbeelden komen uit TicketFaster:
- [`TicketController`](ticketfaster/src/main/java/nl/han/ticketfaster/controller/TicketController.java)
- [`TicketService`](ticketfaster/src/main/java/nl/han/ticketfaster/service/TicketService.java)
- [`TicketServiceTest`](ticketfaster/src/test/java/nl/han/ticketfaster/service/TicketServiceTest.java)

---

## 1. Het probleem in één zin

Als een class zijn helpers **zelf** aanmaakt met `new`, zitten die helpers **vast** in die class.  
Dan kun je ze in een test niet makkelijk vervangen door een nep-versie.

---

## 2. Zelf spawnen vs. laten aanleveren

### Zelf spawnen (DIY)

Stel: `TicketService` doet dit ergens:

```java
private final TicketRepository ticketRepository = new TicketJdbcRepository(...);
```

Dan:

- de service **kiest zelf** welke repository hij gebruikt;
- in een test kun je die echte database-repository niet zomaar weghalen;
- wie de service wil maken, moet ook snappen hoe de JDBC-repository werkt.

In OOP-taal: de service **steelt** bijna de verantwoordelijkheid om “het archief” te kiezen. Hij zou alleen moeten **vragen** aan een archief.

### Laten aanleveren (dependency injection)

```java
public TicketService(VisitorRepository visitorRepository,
                     ConcertRepository concertRepository,
                     TicketRepository ticketRepository) {
    this.visitorRepository = visitorRepository;
    this.concertRepository = concertRepository;
    this.ticketRepository = ticketRepository;
}
```

Vertaling:

> Service, jij **kent** repositories.  
> Jij spawnt ze niet zelf. Iemand anders geeft ze je.

Dat “iemand anders” is in Spring Boot vaak de **container** (IoC): Spring maakt de objecten en zet ze in elkaar.

---

## 3. Drie manieren om iets “aan te leveren”

### Constructor-injectie (aanrader)

Via de constructor. Velden vaak `private final`.

```java
@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
}
```

Bij **één** constructor hoeft er geen `@Autowired` op: Spring vult dit vanzelf.  
Dit is wat TicketFaster gebruikt.

### Field-injectie

`@Autowired` direct op het veld. Geen constructor nodig — maar minder duidelijk en lastiger in unit tests.

```java
@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;
}
```

### Setter-injectie

Via een `set…`-methode. Spring kan die aanroepen na het maken van het object.

```java
@RestController
@RequestMapping("/tickets")
public class TicketController {

    private TicketService ticketService;

    @Autowired
    public void setTicketService(TicketService ticketService) {
        this.ticketService = ticketService;
    }
}
```

| Manier | Kort | Handig? |
|--------|------|---------|
| Constructor | verplicht bij `new` / Spring | Meestal ja |
| Field | `@Autowired` op veld | Meestal nee |
| Setter | `@Autowired` op setter | Soms; minder strak |

---

## 4. Waarom neppe versies in unit tests?

Een **unit test** wil één stukje gedrag testen — hier: de **regels van `TicketService`**.

Als de service een echte `TicketJdbcRepository` (en dus een database) meesleept, test je ineens:

1. de service, **en**
2. de repository / database.

Dat zijn **twee units**. Gaat er iets mis, dan weet je niet goed waar.  
Bovendien wordt de test trager en breekbaarder.

Daarom geef je in de test een **nep**-repository (`@Mock`): die antwoordt precies wat jij wilt.  
Dan test je alleen de service.

In [`TicketServiceTest`](ticketfaster/src/test/java/nl/han/ticketfaster/service/TicketServiceTest.java):

1. nep-repositories (`@Mock`);
2. die gaan de service in (`@InjectMocks` / constructor);
3. de test zegt: “als de repository *dit* antwoordt, moet de service *dat* doen.”

Dat kan alleen als die repositories **van buiten** komen (DI), niet als de service ze zelf spawnt met `new`.

Kort:

- **Echte dependency zelf gespawnd** → je test vaak 2 units tegelijk  
- **Dependency via constructor** → in de test een mock → je test 1 unit  

---

## 5. IoC / bean — zonder zware woorden

| Term | Simpele betekenis |
|------|-------------------|
| **Dependency** | Iets wat een class nodig heeft (bijv. een repository) |
| **Dependency injection (DI)** | Die dependency wordt **van buiten** gegeven |
| **Inversion of Control (IoC)** | Jij belt niet overal `new`; Spring zet de graaf in elkaar |
| **Bean** | Een object dat Spring beheert (`@Service`, `@Repository`, `@RestController`, …) |

Annotaties zijn labels op banen. DI is het mechanisme: *wie mag aan wie vragen / taken geven?*

---

## 6. Diepe ketens: waarom een container helpt

Zonder Spring:

```
A heeft B nodig
B heeft C nodig
→ wie A maakt, moet ook C kennen en aan B geven
```

Wijzigt C, dan moeten vaak A, B én hun tests mee.

Met Spring zeg je vooral: “A heeft een B nodig” (vaak als interface).  
Spring zoekt een passende bean en vult de keten. Jij hoeft niet overal `new` te schrijven.

Dat past bij OOP: je vraagt aan een **belofte** (`TicketRepository`), niet aan één vaste SQL-class.

---

## 7. Wat je in TicketFaster kunt oefenen

1. Open controller, service, één JDBC-repository.  
2. Noteer per class: **welke object-velden** / dependencies?  
3. Hoe komen die binnen? (constructor / field / setter)  
4. Open `TicketServiceTest`: waar worden dependencies **vervangen**?  
5. Vraag jezelf: *als deze dependency null is of ontbreekt, wat gaat er mis?*

---

## 8. Typische valkuilen

| Wat je ziet | Wat het betekent |
|-------------|------------------|
| `new` van een repository in een service | Dependency niet geïnjecteerd; je test makkelijk 2 units |
| `@Autowired` op private velden overal | Werkt vaak, maar constructor is duidelijker |
| Class zonder dependencies, maar wél database-aanroepen | Verantwoordelijkheid zit verkeerd (waarschijnlijk) |
| Test die de hele Spring-app start voor elke kleine regel | Kan, maar unit test met mocks is lichter voor service-regels |

---

## 9. Samenvatting

1. Object-velden = “aan wie mag ik iets vragen?”  
2. **DI** = die objecten krijg je van buiten, je spawnt ze niet stiekem zelf.  
3. **Constructor-injectie** maakt dat expliciet en goed testbaar.  
4. Neppe dependencies in unit tests → je test **1** unit, niet 2.  
5. Spring (IoC) bouwt de keten van beans; jij houdt de verantwoordelijkheden scherp.

Volgende stap in de leerlijn (los document): Maven / `pom.xml` en builden.

Zie ook [`UITLEG.md`](UITLEG.md) voor wie wat doet in TicketFaster.
