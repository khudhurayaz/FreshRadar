# FreshRadar

FreshRadar ist eine Webanwendung zur Verwaltung von Lebensmitteln im Haushalt.  
Sie unterstützt dabei, den Vorrat im Blick zu behalten, Lebensmittelverschwendung zu reduzieren und rechtzeitig auf Produkte mit nahendem Haltbarkeitsdatum zu reagieren.

Der Kern der Anwendung lautet:

> Mehr Daten rein, weniger Verschwendung raus.

FreshRadar speichert, **welches Lebensmittel**, **an welchem Lagerort**, **in welcher Menge** vorhanden ist und **wann es abläuft**.

## Funktionen

- Benutzerregistrierung und Anmeldung
- Basic- und Pro-Abonnements
- Verwaltung von Produkten, Kategorien und Lagerorten
- Soll-/Ist-Bestandsverwaltung
- Überwachung von Haltbarkeitsdaten
- Hinweis auf bald ablaufende Produkte
- Inventar-Health-Check für unterversorgte Vorräte
- Benutzerprofile mit optionalem Profilbild
- Kontaktformular
- Administrationsbereich für Benutzer, Produkte, Profile und Nachrichten
- Benutzereinstellungen, inklusive Passwortänderung und Seitengröße

## Technologien

| Bereich | Technologien |
|---|---|
| Sprache | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring MVC, Spring Web |
| Sicherheit | Spring Security |
| Datenbank | MySQL, Spring Data JPA, Hibernate |
| Templates | Thymeleaf, Thymeleaf Extras für Spring Security |
| Build-Tool | Maven |
| Testen | JUnit, Mockito, MockMvc, Spring Boot Test |
| Hilfsbibliotheken | Lombok, dotenv-java |
| Entwicklung | Spring Boot DevTools |
| Frontend | HTML, CSS, JavaScript, Bootstrap |

## Voraussetzungen

Für die lokale Ausführung werden benötigt:

- Java Development Kit 17 oder neuer
- Maven oder Maven Wrapper
- MySQL
- IntelliJ IDEA oder eine andere Java-IDE
- Eine lokale `.env`-Datei mit den Datenbankzugangsdaten

> Das Projekt verwendet in der Maven-Konfiguration Java 17. Eine neuere Java-Version kann funktionieren, sollte aber bei Problemen zunächst auf Java 17 umgestellt werden.

## Installation

1. Repository klonen:

```bash
git clone https://github.com/khudhurayaz/FreshRadar.git
```

2. In das Projektverzeichnis wechseln:

```bash
cd FreshRadar
```

3. Eine `.env`-Datei im Projekt-Hauptverzeichnis anlegen.

4. Datenbankzugangsdaten in der `.env`-Datei hinterlegen.

5. Maven-Abhängigkeiten laden und die Anwendung starten:

```bash
./mvnw spring-boot:run
```

Unter Windows:

```bat
mvnw.cmd spring-boot:run
```

Alternativ kann die Klasse `AppStarter` direkt über IntelliJ IDEA gestartet werden.

## Anwendung öffnen

Nach dem Start ist die Anwendung standardmäßig unter folgender Adresse erreichbar:

```text
http://localhost:8080
```

## Testbenutzer

| E-Mail | Passwort | Rolle | Abo | Beschreibung |
|---|---|---|---|---|
| admin@admin.com | `AdminAdmin2026#` | Admin | Pro | Verwaltung von Benutzern, Produkten, Profilen und Nachrichten |
| tom.mueller@benutzer.com | `TomMueller2026#` | Benutzer | Pro | Benutzer mit Pro-Abonnement |
| lukas.schmidt@benutzer.com | `LukasSchmidt2026#` | Benutzer | Basic | Benutzer mit Basic-Abonnement |
| test@test.de | `TestTest2026#` | Benutzer | Basic | Testbenutzer mit Basic-Abonnement und zehn gespeicherten Produkten |

> **Sicherheitshinweis:** Diese Zugangsdaten sind nur für die lokale Entwicklungs- und Testumgebung vorgesehen. Sie dürfen nicht für ein öffentliches Produktivsystem verwendet werden.

## Produktbeispiele

### Paprika

```text
Name: Paprika (rot)
Lagerort: Kühlschrank
Kategorie: Gemüse
Geöffnet: Nein
Einheit: Stück
Haltbarkeitsdatum: 20.07.2026
Soll-Bestand: 3
Ist-Bestand: 3
```

### Vollkornbrot

```text
Name: Vollkornbrot
Lagerort: Schrank
Kategorie: Backwaren
Geöffnet: Ja
Einheit: Laib
Haltbarkeitsdatum: 18.07.2026
Soll-Bestand: 1
Ist-Bestand: 1
```

## Projektstruktur

```text
FreshRadar/
├── .mvn/                         Maven-Wrapper-Konfiguration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── de/khudhurayaz/freshradar/
│   │   │       ├── component/    Eigene Komponenten, z. B. Login-Handler
│   │   │       ├── config/       Security- und Web-Konfiguration
│   │   │       ├── controller/
│   │   │       │   ├── api/      REST-Controller
│   │   │       │   └── view/     Controller für Thymeleaf-Seiten
│   │   │       ├── dto/          Data Transfer Objects
│   │   │       │   ├── inventory/
│   │   │       │   └── subscription/
│   │   │       ├── exception/    Globale Fehlerbehandlung
│   │   │       ├── interceptor/  Subscription-/Zugriffsprüfung
│   │   │       ├── model/        JPA-Entitäten
│   │   │       ├── repositories/ Spring-Data-Repositories
│   │   │       │   └── setting/
│   │   │       ├── services/     Geschäftslogik
│   │   │       │   └── setting/
│   │   │       ├── util/         Hilfsklassen und Validierungen
│   │   │       ├── AppStarter.java
│   │   │       └── EnvLoader.java
│   │   └── resources/
│   │       ├── static.assets/    CSS, JavaScript, Bilder, Icons und Fonts
│   │       ├── templates/        Thymeleaf-Templates
│   │       │   ├── admin/
│   │       │   ├── dashboard/
│   │       │   ├── error/
│   │       │   └── site/
│   │       └── application.properties
│   └── test/
│       ├── java/                 Java-Testklassen
│       └── resources/            Testressourcen
├── uploads/                      Lokal gespeicherte Uploads, z. B. Profilbilder
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
└── .env                          Lokale Umgebungsvariablen, nicht versionieren
```

## Web-Endpunkte

### Öffentliche und Seiten-Endpunkte

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `GET` | `/` | Keine | `200 OK`, Startseite |
| `POST` | `/login` | JSON mit `email` und `password` | `200 OK` bei erfolgreicher Anmeldung |
| `POST` | `/register` | JSON mit `email` und `password` | `200 OK`, Weiterleitung zur Abo-Auswahl; `400 Bad Request` bei ungültiger E-Mail, schwachem Passwort oder bereits vergebener E-Mail |
| `GET` | `/subscribe` | Keine | `302 Found`, Weiterleitung zum Login oder Dashboard |
| `POST` | `/subscribe` | JSON mit `playType`, `status`, `purchasedAt` | `200 OK` |
| `GET` | `/dashboard` | Keine | `200 OK`; `302 Found`, wenn kein Benutzer angemeldet ist |
| `GET` | `/profile` | Keine | `200 OK`; bei unerwarteten Problemen `500 Internal Server Error` |
| `GET` | `/profile/{id}` | Pfadvariable `id` | `200 OK` mit Benutzerprofil; `404 Not Found`, wenn kein Benutzer existiert |
| `GET` | `/admin` | Keine | `200 OK` für Administratoren; sonst Weiterleitung zum Dashboard |
| `GET` | `/setting` | Keine | `200 OK` mit Einstellungsseite; `404 Not Found`, wenn nicht verfügbar |
| `GET` | `/error` | Keine | Dynamische Fehlerseite mit HTTP-Status, Fehlermeldung und Pfad |

### Kontakt-API

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `GET` | `/api/contact/allMessages` | Keine | `200 OK`, Array von Nachrichten |
| `POST` | `/api/contact/add` | JSON mit `firstName`, `lastName`, `email`, `subject`, `message` | `201 Created` bei Erfolg; `400 Bad Request` bei ungültigen Daten |

Beispiel für eine Kontaktanfrage:

```json
{
  "firstName": "Max",
  "lastName": "Mustermann",
  "email": "max.mustermann@example.com",
  "subject": "Frage zu FreshRadar",
  "message": "Wie kann ich einen neuen Lagerort hinzufügen?"
}
```

### Produkt-API

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `GET` | `/api/product/showProductsWithCategoryAndLocation` | Query-Parameter `category` und `location` | `200 OK`, gefilterte Produktliste; `400 Bad Request` bei ungültigen Parametern |
| `GET` | `/api/product/all` | Keine | `200 OK`, Array aller Produkte |
| `POST` | `/api/product/create` | Produkt als JSON | `201 Created`, Produkt erfolgreich hinzugefügt; `400 Bad Request` bei Fehler |
| `PUT` | `/api/product/{id}` | Pfadvariable `id` und Produkt als JSON | `200 OK`, Produkt aktualisiert; `404 Not Found` oder `400 Bad Request` bei Fehler |
| `DELETE` | `/api/product/delete/{id}` | Pfadvariable `id` | `200 OK`, Produkt gelöscht; `404 Not Found`, wenn die Produkt-ID nicht existiert |
| `GET` | `/api/product/expiring` | Keine | `200 OK`, Anzahl bald ablaufender Produkte |

Beispiel für ein Produkt:

```json
{
  "userId": 1,
  "locationId": 1,
  "categoryId": 1,
  "name": "Paprika (rot)",
  "isOpen": false,
  "unit": "Stück",
  "vorratProzent": 100,
  "soll": 3,
  "ist": 3,
  "vorratFarbe": "green",
  "expiryDate": "2026-07-20",
  "addedAt": "2026-07-16T12:00:00"
}
```

### Inventar-API

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `GET` | `/api/showInventories` | Angemeldeter Benutzer | `200 OK`, Inventarliste |
| `PUT` | `/api/addInventory` | Inventar als JSON | `200 OK` bei erfolgreichem Hinzufügen; `400 Bad Request` bei Fehler |
| `PUT` | `/api/updateInventory` | Inventar als JSON | `200 OK` bei erfolgreicher Aktualisierung; Fehlerstatus bei nicht vorhandenem Inventar |
| `DELETE` | `/api/deleteInventory/{id}` | Pfadvariable `id` | `200 OK` bei Erfolg; `404 Not Found` bei nicht vorhandenem Inventar |
| `GET` | `/api/inventory/{productId}` | Pfadvariable `productId` | `200 OK` mit Inventardaten; `404 Not Found`, falls nicht vorhanden |
| `GET` | `/api/inventory/healthCareCheckup` | Angemeldeter Benutzer | Prüft, ob Inventar vorhanden und ausreichend ist |

### Lagerort-API

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `GET` | `/api/location/get` | Keine | `200 OK`, Liste aller Lagerorte |
| `GET` | `/api/location/get/{id}` | Pfadvariable `id` | `200 OK` mit Lagerort; `404 Not Found`, falls nicht vorhanden |
| `PUT` | `/api/location/add` | JSON mit `location` und optional `added_at` | `200 OK` bei Erfolg; `400 Bad Request` bei Fehler |
| `PUT` | `/api/location/update/{id}` | Pfadvariable `id`, JSON mit `location` | `200 OK` mit Statusmeldung |
| `DELETE` | `/api/location/delete/{id}` | Pfadvariable `id` | `200 OK` bei Erfolg; `404 Not Found` bei Fehler |

### Profil-API

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `POST` | `/api/profile/edit/update` | `multipart/form-data` mit Profildaten und optionaler Datei `logoFile` | `200 OK` bei Erfolg; `400 Bad Request`, wenn kein Benutzer oder Profil nicht gespeichert werden kann |

Unterstützte Formularfelder:

```text
firstname
lastname
area
info
location
existingProfileImage
logoFile
```

Profilbilder werden standardmäßig im lokalen Upload-Verzeichnis gespeichert:

```text
./uploads/profile/{userId}/
```

### Einstellungen-API

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `POST` | `/setting/pagination` | `globalPageSize` | `200 OK`, Seitengröße gespeichert; `500 Internal Server Error` bei Fehler |
| `POST` | `/setting/changePassword` | JSON mit `oldPassword`, `newPassword`, `repeatPassword` | `200 OK`, Passwort geändert; `400 Bad Request` bei Fehler |

### Admin-API

| Methode | Endpunkt | Eingabe | Antwort |
|---|---|---|---|
| `PUT` oder `POST` | `/admin/profile/{profileId}` | Profil als JSON | `200 OK`, Profil gespeichert; `400 Bad Request` bei Fehler |
| `DELETE` | `/admin/profile/{profileId}` | Profil als JSON | `200 OK`, Profil gelöscht; `400 Bad Request` bei Fehler |
| `PUT` oder `POST` | `/admin/product/{productId}` | Produkt als JSON | `200 OK`, Produkt gespeichert; `400 Bad Request` bei Fehler |
| `DELETE` | `/admin/product/{productId}` | Produkt als JSON | `200 OK`, Produkt gelöscht; `400 Bad Request` bei Fehler |
| `DELETE` | `/admin/message/{messageId}` | Nachricht als JSON | `200 OK`, Nachricht gelöscht; `400 Bad Request` bei Fehler |

## Tests

Die automatisierten Tests liegen im Verzeichnis:

```text
src/test/java/de/khudhurayaz/freshradar/
```

Aktuell existieren unter anderem Controller-Tests für:

```text
CategoryRestControllerTest
ContactRestControllerTest
InventoryRestControllerTest
LocationRestControllerTest
ProfileRestControllerTest
```

### Alle Tests ausführen

Unter Linux oder macOS:

```bash
./mvnw test
```

Unter Windows:

```bat
mvnw.cmd test
```

Mit einer global installierten Maven-Version:

```bash
mvn test
```

### Eine bestimmte Testklasse ausführen

Beispiel für den Inventory-Controller-Test:

```bash
mvnw.cmd -Dtest=InventoryRestControllerTest test
```

### Testberichte

Nach einem Maven-Testlauf befinden sich die Berichte in:

```text
target/surefire-reports/
```

## Lizenz

Dieses Projekt steht unter der Apache License, Version 2.0.

Weitere Informationen:

```text
https://www.apache.org/licenses/LICENSE-2.0.txt
```

## Entwickler

**Ayaz Khudhur**

- Rolle: Lead Developer und Architect
- E-Mail: ayazkhudhur@hotmail.com
- Repository: [github.com/khudhurayaz/FreshRadar](https://github.com/khudhurayaz/FreshRadar)