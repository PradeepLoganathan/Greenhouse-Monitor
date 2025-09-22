package com.example.greenhouse.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

import java.time.Duration;

public class SiteManager extends AbstractBehavior<SiteManager.Command> {

    public interface Command {}
    public static final class Bootstrap implements Command {}

    private final String regionId;
    private final String siteId;
    private final int greenhousesPerSite;
    private final ActorRef<DataProcessingManager.Command> dataRef;

    public static Behavior<Command> create(String regionId, String siteId, int greenhousesPerSite, ActorRef<DataProcessingManager.Command> dataRef) {
        Behavior<Command> behavior = Behaviors.setup(ctx -> new SiteManager(ctx, regionId, siteId, greenhousesPerSite, dataRef));
        // Restart individual greenhouse controllers; stop actuators on critical failures handled in child actors
        return Behaviors.supervise(behavior)
                .onFailure(Exception.class, SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(1), Duration.ofSeconds(10), 0.2));
    }

    private SiteManager(ActorContext<Command> ctx, String regionId, String siteId, int greenhousesPerSite, ActorRef<DataProcessingManager.Command> dataRef) {
        super(ctx);
        this.regionId = regionId;
        this.siteId = siteId;
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
        getContext().getLog().info("Bootstrapping site {} with {} greenhouses", siteId, greenhousesPerSite);
        // Spawn maintenance actor (non-critical, resume on failures)
        getContext().spawn(MaintenanceActor.create(siteId), "maintenance");
        for (int i = 0; i < greenhousesPerSite; i++) {
            String greenhouseId = siteId + "-gh-" + i;
            var gh = getContext().spawn(GreenhouseActor.create(regionId, siteId, greenhouseId, dataRef), "gh-" + i);
            gh.tell(new GreenhouseActor.Initialize());
        }
        return this;
    }
}
