package com.example.greenhouse.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.HashMap;
import java.util.Map;

public class DataProcessingManager extends AbstractBehavior<DataProcessingManager.Command> {

    public interface Command {}

    // Registration of greenhouse actors for routing
    public static final class RegisterGreenhouse implements Command {
        public final String greenhouseId;
        public final ActorRef<GreenhouseActor.Command> ref;
        public RegisterGreenhouse(String greenhouseId, ActorRef<GreenhouseActor.Command> ref) { this.greenhouseId = greenhouseId; this.ref = ref; }
    }

    // Stream envelope representing sensor data from Kafka (stubbed)
    public static final class SensorEnvelope implements Command {
        public final String greenhouseId;
        public final String kind; // temperature/humidity/soil
        public final double value;
        public SensorEnvelope(String greenhouseId, String kind, double value) { this.greenhouseId = greenhouseId; this.kind = kind; this.value = value; }
    }

    // Generic ingest logging (optional)
    public static final class IngestMetric implements Command { public final String source; public final String payload; public IngestMetric(String source, String payload){this.source=source;this.payload=payload;} }

    public static Behavior<Command> create() { return Behaviors.setup(DataProcessingManager::new); }

    private final Map<String, ActorRef<GreenhouseActor.Command>> registry = new HashMap<>();

    private DataProcessingManager(ActorContext<Command> ctx) { super(ctx); }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RegisterGreenhouse.class, this::onRegister)
                .onMessage(SensorEnvelope.class, this::onSensorEnvelope)
                .onMessage(IngestMetric.class, this::onIngest)
                .build();
    }

    private Behavior<Command> onRegister(RegisterGreenhouse reg) {
        getContext().getLog().info("[data] registered greenhouse {}", reg.greenhouseId);
        registry.put(reg.greenhouseId, reg.ref);
        return this;
    }

    private Behavior<Command> onSensorEnvelope(SensorEnvelope env) {
        var target = registry.get(env.greenhouseId);
        if (target != null) {
            target.tell(new GreenhouseActor.SensorReading(env.greenhouseId + "-" + env.kind, env.kind, env.value));
        } else {
            getContext().getLog().warn("[data] no target registered for greenhouse {}", env.greenhouseId);
        }
        return this;
    }

    private Behavior<Command> onIngest(IngestMetric msg) {
        getContext().getLog().info("[data] ingest from {}: {}", msg.source, msg.payload);
        return this;
    }
}
