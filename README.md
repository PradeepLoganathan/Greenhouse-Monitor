# Greenhouse Monitor (Akka Typed, Java)

A multi-actor system scaffold demonstrating an enterprise-style hierarchy for managing smart greenhouses. Built with Akka Typed (Java) and Maven. Persistence and integrations are stubbed.

## Features
- Actor hierarchy: System → Region → Site → Greenhouse → Sensors/Actuators
- Supervision strategies per component (restart/resume/stop with backoff)
- Stubs for data processing, notifications, maintenance, and scheduling
- Jackson serialization and basic logging
- Akka Streams stub simulating Kafka sensor events into the system

## Build
- Java 17+
- Maven 3.8+

```
mvn -q -DskipTests package
```

## Run
Produces a fat JAR and starts a simulated sensor stream:

```
java -jar target/greenhouse-monitor-0.1.0-SNAPSHOT-shaded.jar
```

## Structure
- `com.example.greenhouse.app.Main` — boots the actor system
- `actors` — system, region, site, greenhouse, sensors, actuators, and services
- `stream/SensorStream` — stubbed Akka Streams pipeline (Kafka-like)
- `application.conf` — Akka configuration (local dev)

## Next steps
- Implement real persistence and external integrations
- Add HTTP endpoints and stream processing
- Extend simulator scenarios
