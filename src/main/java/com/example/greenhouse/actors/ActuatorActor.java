package com.example.greenhouse.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

import java.time.Duration;

public class ActuatorActor extends AbstractBehavior<ActuatorActor.Command> {

    public enum Type { Fan, Heater, Irrigation }

    public interface Command {}
    public static final class Execute implements Command { public final String command; public Execute(String command){this.command=command;} }

    private final String greenhouseId;
    private final Type type;

    public static Behavior<Command> create(String greenhouseId, Type type) {
        Behavior<Command> behavior = Behaviors.setup(ctx -> new ActuatorActor(ctx, greenhouseId, type));
        // Restart on power/transient failures (limited retries), stop on mechanical failures would be modeled via exceptions
        return Behaviors.supervise(behavior)
                .onFailure(Exception.class, SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(1), Duration.ofSeconds(10), 0.2));
    }

    private ActuatorActor(ActorContext<Command> ctx, String greenhouseId, Type type) {
        super(ctx);
        this.greenhouseId = greenhouseId;
        this.type = type;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Execute.class, this::onExecute)
                .build();
    }

    private Behavior<Command> onExecute(Execute exec) {
        getContext().getLog().info("[{}:{}] Executing: {}", greenhouseId, type, exec.command);
        return this;
    }
}

