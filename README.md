# Citizens Registry

A RESTful service for managing a citizen registry, built with Spring Boot, together with a command-line client and automated tests.

**Author:** Ioannis Michalakis

## Technologies

- Java 21, Maven (the Maven Wrapper `mvnw` is included)
- Spring Boot 4.1.2 (Web, Data JPA, Validation)
- H2 database (file based, no installation required)
- JUnit 5, AssertJ, Rest-Assured 6.0.1, maven-failsafe-plugin

## Project structure

| Module | Description |
|---|---|
| `domain` | Domain classes: `Citizen` (JPA entity with validation) and `CitizenUpdate`. Shared by the service and the client. |
| `service` | The RESTful service (controller, service, repository, exception handler) and all tests. |
| `client` | Command-line application with a menu that calls the service. |

## Prerequisites

- JDK 21 (`JAVA_HOME` must point to it)
- No Maven installation and no database server are needed.
- On Windows, use a project path without non-Latin characters.

## Build and test

From the project root:

```bash
./mvnw clean verify
```

This runs:

- **Unit tests** (surefire): 30 tests
  - `CitizenEntityTest` (16): constructors, getters/setters, validation, equals/hashCode, JSON date format
  - `CitizenRepositoryOrmTest` (8): ORM mapping, save, delete, search
  - `ConfigurationTest` (5): configuration and beans
  - `CitizensServiceApplicationTests` (1): application context loads
- **Integration tests** (failsafe, Rest-Assured): `CitizenApiIT`, 16 tests covering all 5 endpoints.

Note for Windows: stop the service and the client before running `clean`, otherwise the jar files are locked.

## Running the application

First build the jars:

```bash
./mvnw clean package -DskipTests
```

**Service** (port 8080), in one terminal:

```bash
java -jar service/target/service-0.0.1-SNAPSHOT.jar
```

Data is stored in the H2 file `data/citizens` and is kept between restarts.

**Menu client**, in a second terminal (the service must be running):

```bash
java -jar client/target/client-0.0.1-SNAPSHOT.jar
```

Menu: 1 Insert, 2 Delete, 3 Update (AFM and address), 4 Search, 5 Show by AT.
The menu is shown repeatedly as long as the choice is valid. An invalid or empty choice terminates the program.
The service URL is set in `client/src/main/resources/application.properties` (`citizens.service.url`).

## Citizen fields

| Field | Required | Rule |
|---|---|---|
| `at` (identity card number) | Yes | Exactly 8 characters, the key of the record |
| `firstName` | Yes | Not blank |
| `lastName` | Yes | Not blank |
| `gender` | Yes | Not blank |
| `birthDate` | Yes | Format `dd-MM-yyyy`, e.g. `12-11-2008` |
| `afm` (tax number) | No | If present, exactly 9 digits |
| `address` | No | Free text |

## REST API

| Operation | Method and path | Success |
|---|---|---|
| Insert | `POST /citizens` | 201 |
| Show | `GET /citizens/{at}` | 200 |
| Search | `GET /citizens?at=&firstName=&lastName=&gender=&birthDate=&afm=&address=` | 200 (list, may be empty) |
| Update (only `afm`, `address`) | `PUT /citizens/{at}` | 200 |
| Delete | `DELETE /citizens/{at}` | 204 |

Search accepts any combination of parameters (combined with AND). Text values are compared case-insensitively, and `birthDate` uses the format `dd-MM-yyyy`.

### Errors

Errors are returned as JSON `{"error": "<message>"}`:

| Status | Cause |
|---|---|
| 400 | Invalid data (AT, AFM, date format, missing required fields) |
| 404 | No citizen with the given AT |
| 409 | Citizen already exists (on insert) |

### Example (Git Bash)

```bash
curl -i -X POST localhost:8080/citizens -H "Content-Type: application/json" \
  -d '{"at":"AB123456","firstName":"Nikos","lastName":"Papas","gender":"M","birthDate":"12-11-2008","afm":"123456789","address":"Athens"}'

curl -i localhost:8080/citizens/AB123456
curl -i "localhost:8080/citizens?lastName=papas"
curl -i -X PUT localhost:8080/citizens/AB123456 -H "Content-Type: application/json" -d '{"afm":"111222333","address":"Patra"}'
curl -i -X DELETE localhost:8080/citizens/AB123456
```