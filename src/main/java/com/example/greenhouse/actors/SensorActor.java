package com.example.greenhouse.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Random;

public class SensorActor extends AbstractBehavior<SensorActor.Command> {

    public enum Kind { Temperature, Humidity, SoilMoisture }

    public interface Command {}
    public static final class Read implements Command { public final ActorRef<GreenhouseActor.Command> replyTo; public Read(ActorRef<GreenhouseActor.Command> replyTo){this.replyTo=replyTo;} }
    public static final class EmitSample implements Command { public final ActorRef<GreenhouseActor.Command> replyTo; public EmitSample(ActorRef<GreenhouseActor.Command> replyTo){this.replyTo=replyTo;} }

    private final String greenhouseId;
    private final Kind kind;
    private final Random rnd = new Random();

    public static Behavior<Command> create(String greenhouseId, Kind kind) {
        Behavior<Command> behavior = Behaviors.setup(ctx -> new SensorActor(ctx, greenhouseId, kind));
        // Restart on connectivity/transient failures with backoff; resume on simple errors
        return Behaviors.supervise(behavior)
                .onFailure(Exception.class, SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(1), Duration.ofSeconds(30), 0.2));
    }

    private SensorActor(ActorContext<Command> ctx, String greenhouseId, Kind kind) {
        super(ctx);
        this.greenhouseId = greenhouseId;
        this.kind = kind;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Read.class, this::onRead)
                .onMessage(EmitSample.class, this::onEmitSample)
                .build();
    }

    private Behavior<Command> onRead(Read msg) {
        msg.replyTo.tell(sampleReading());
        return this;
    }

    private Behavior<Command> onEmitSample(EmitSample msg) {
        msg.replyTo.tell(sampleReading());
        return this;
    }

    private GreenhouseActor.SensorReading sampleReading() {
        double value;
        String kindStr;
        switch (kind) {
            case Temperature -> { value = 18 + rnd.nextDouble() * 15; kindStr = "temperature"; }
            case Humidity -> { value = 40 + rnd.nextDouble() * 50; kindStr = "humidity"; }
            case SoilMoisture -> { value = 10 + rnd.nextDouble() * 50; kindStr = "soil"; }
            default -> { value = 0; kindStr = "unknown"; }
        }
        return new GreenhouseActor.SensorReading(kindStr + "-" + greenhouseId, kindStr, value);
    }
}

