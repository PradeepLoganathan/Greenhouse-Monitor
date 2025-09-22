package com.example.greenhouse.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class NotificationManager extends AbstractBehavior<NotificationManager.Command> {

    public interface Command {}
    public static final class Notify implements Command { public final String level; public final String message; public Notify(String level, String message){this.level=level;this.message=message;} }

    public static Behavior<Command> create() { return Behaviors.setup(NotificationManager::new); }

    private NotificationManager(ActorContext<Command> ctx) { super(ctx); }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Notify.class, this::onNotify)
                .build();
    }

    private Behavior<Command> onNotify(Notify n) {
        getContext().getLog().info("[notify:{}] {}", n.level, n.message);
        return this;
    }
}

