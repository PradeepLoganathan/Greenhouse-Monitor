package com.example.greenhouse.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class MaintenanceActor extends AbstractBehavior<MaintenanceActor.Command> {

    public interface Command {}
    public static final class TrackUsage implements Command { public final String component; public TrackUsage(String component){this.component=component;} }

    private final String scopeId;

    public static Behavior<Command> create(String scopeId) {
        // Non-critical: prefer resuming on failure (default behavior resumes; explicit strategy can be added if needed)
        return Behaviors.setup(ctx -> new MaintenanceActor(ctx, scopeId));
    }

    private MaintenanceActor(ActorContext<Command> ctx, String scopeId) {
        super(ctx);
        this.scopeId = scopeId;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TrackUsage.class, this::onTrackUsage)
                .build();
    }

    private Behavior<Command> onTrackUsage(TrackUsage msg) {
        getContext().getLog().info("[maintenance:{}] tracking usage of {}", scopeId, msg.component);
        return this;
    }
}

