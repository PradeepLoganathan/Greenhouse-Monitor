package com.example.greenhouse.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class WeatherStationActor extends AbstractBehavior<WeatherStationActor.Command> {

    public interface Command {}
    public static final class Refresh implements Command {}

    private final String regionId;

    public static Behavior<Command> create(String regionId) {
        return Behaviors.setup(ctx -> new WeatherStationActor(ctx, regionId));
    }

    private WeatherStationActor(ActorContext<Command> ctx, String regionId) {
        super(ctx);
        this.regionId = regionId;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Refresh.class, this::onRefresh)
                .build();
    }

    private Behavior<Command> onRefresh(Refresh r) {
        getContext().getLog().info("[weather:{}] refreshed", regionId);
        return this;
    }
}

