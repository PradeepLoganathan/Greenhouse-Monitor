package com.example.greenhouse.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

import java.time.Duration;

public class RegionManager extends AbstractBehavior<RegionManager.Command> {

    public interface Command {}
    public static final class Bootstrap implements Command {}

    private final String regionId;
    private final int sitesPerRegion;
    private final int greenhousesPerSite;
    private final ActorRef<DataProcessingManager.Command> dataRef;

    public static Behavior<Command> create(String regionId, int sitesPerRegion, int greenhousesPerSite, ActorRef<DataProcessingManager.Command> dataRef) {
        Behavior<Command> behavior = Behaviors.setup(ctx -> new RegionManager(ctx, regionId, sitesPerRegion, greenhousesPerSite, dataRef));
        return Behaviors.supervise(behavior)
                .onFailure(Exception.class, SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(1), Duration.ofSeconds(10), 0.2));
    }

    private RegionManager(ActorContext<Command> ctx, String regionId, int sitesPerRegion, int greenhousesPerSite, ActorRef<DataProcessingManager.Command> dataRef) {
        super(ctx);
        this.regionId = regionId;
        this.sitesPerRegion = sitesPerRegion;
        this.greenhousesPerSite = greenhousesPerSite;
        this.dataRef = dataRef;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Bootstrap.class, this::onBootstrap)
                .build();
    }

    private Behavior<Command> onBootstrap(Bootstrap b) {
        getContext().getLog().info("Bootstrapping region {} with {} sites", regionId, sitesPerRegion);
        // Spawn a weather station actor (resumed on failure)
        getContext().spawn(WeatherStationActor.create(regionId), "weather-station");
        for (int i = 0; i < sitesPerRegion; i++) {
            String siteId = regionId + "-site-" + i;
            var site = getContext().spawn(SiteManager.create(regionId, siteId, greenhousesPerSite, dataRef), "site-" + i);
            site.tell(new SiteManager.Bootstrap());
        }
        return this;
    }
}
