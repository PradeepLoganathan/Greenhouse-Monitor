package com.example.greenhouse.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

import java.time.Duration;

public class GreenhouseActor extends AbstractBehavior<GreenhouseActor.Command> {

    public interface Command {}
    public static final class Initialize implements Command {}

    // Sensor data and control messages
    public static final class SensorReading implements Command, java.io.Serializable {
        public final String sensorId;
        public final String kind; // temperature/humidity/soil
        public final double value;
        public SensorReading(String sensorId, String kind, double value) {
            this.sensorId = sensorId; this.kind = kind; this.value = value;
        }
        public String toString() { return kind + "(" + sensorId + ")=" + value; }
    }

    public static final class ControlDecision implements Command {
        public enum Action { TURN_ON_FAN, TURN_OFF_FAN, TURN_ON_HEATER, TURN_OFF_HEATER, START_IRRIGATION, STOP_IRRIGATION }
        public final Action action;
        public ControlDecision(Action action) { this.action = action; }
    }

    private final String regionId;
    private final String siteId;
    private final String greenhouseId;
    private final ActorRef<DataProcessingManager.Command> dataRef;

    public static Behavior<Command> create(String regionId, String siteId, String greenhouseId, ActorRef<DataProcessingManager.Command> dataRef) {
        Behavior<Command> behavior = Behaviors.setup(ctx -> new GreenhouseActor(ctx, regionId, siteId, greenhouseId, dataRef));
        return Behaviors.supervise(behavior)
                .onFailure(Exception.class, SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(1), Duration.ofSeconds(10), 0.2));
    }

    private GreenhouseActor(ActorContext<Command> ctx, String regionId, String siteId, String greenhouseId, ActorRef<DataProcessingManager.Command> dataRef) {
        super(ctx);
        this.regionId = regionId;
        this.siteId = siteId;
        this.greenhouseId = greenhouseId;
        this.dataRef = dataRef;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Initialize.class, this::onInitialize)
                .onMessage(SensorReading.class, this::onSensorReading)
                .onMessage(ControlDecision.class, this::onControlDecision)
                .build();
    }

    private Behavior<Command> onInitialize(Initialize init) {
        getContext().getLog().info("Initializing greenhouse {}", greenhouseId);
        // Register with data processing for routing
        dataRef.tell(new DataProcessingManager.RegisterGreenhouse(greenhouseId, getContext().getSelf()));
        // Spawn sensors and actuators under this greenhouse
        var sensors = getContext().spawn(SensorActor.create(greenhouseId, SensorActor.Kind.Temperature), "sensor-temperature");
        getContext().spawn(SensorActor.create(greenhouseId, SensorActor.Kind.Humidity), "sensor-humidity");
        getContext().spawn(SensorActor.create(greenhouseId, SensorActor.Kind.SoilMoisture), "sensor-soil");

        getContext().spawn(ActuatorActor.create(greenhouseId, ActuatorActor.Type.Fan), "actuator-fan");
        getContext().spawn(ActuatorActor.create(greenhouseId, ActuatorActor.Type.Heater), "actuator-heater");
        getContext().spawn(ActuatorActor.create(greenhouseId, ActuatorActor.Type.Irrigation), "actuator-irrigation");

        // Ask one sensor to emit a sample reading (boot smoke)
        sensors.tell(new SensorActor.EmitSample(getContext().getSelf()));
        return this;
    }

    private Behavior<Command> onSensorReading(SensorReading reading) {
        getContext().getLog().info("[{}] Received reading: {}", greenhouseId, reading);
        // Dummy rule: if temperature > 28C turn on fan, if < 20C turn on heater
        if ("temperature".equals(reading.kind)) {
            if (reading.value > 28.0) {
                return onControlDecision(new ControlDecision(ControlDecision.Action.TURN_ON_FAN));
            } else if (reading.value < 20.0) {
                return onControlDecision(new ControlDecision(ControlDecision.Action.TURN_ON_HEATER));
            }
        }
        return this;
    }

    private Behavior<Command> onControlDecision(ControlDecision decision) {
        getContext().getLog().info("[{}] Control decision: {}", greenhouseId, decision.action);
        // In a full implementation, we'd route to actuators; here we just log
        return this;
    }
}
