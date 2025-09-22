package com.example.greenhouse.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;

public class MaintenanceScheduler extends AbstractBehavior<MaintenanceScheduler.Command> {

    public interface Command {}
    public static final class Tick implements Command {}

    public static Behavior<Command> create() {
        return Behaviors.setup(ctx -> {
            var actor = new MaintenanceScheduler(ctx);
            ctx.getSystem().scheduler().scheduleAtFixedRate(
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(60),
                    () -> ctx.getSelf().tell(new Tick()),
                    ctx.getSystem().executionContext());
            return actor;
        });
    }

    private MaintenanceScheduler(ActorContext<Command> ctx) { super(ctx); }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Tick.class, this::onTick)
                .build();
    }

    private Behavior<Command> onTick(Tick t) {
        getContext().getLog().info("[maintenance-scheduler] periodic health check");
        return this;
    }
}

