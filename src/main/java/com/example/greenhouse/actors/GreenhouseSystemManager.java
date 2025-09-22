package com.example.greenhouse.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import com.example.greenhouse.stream.SensorStream;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreenhouseSystemManager extends AbstractBehavior<GreenhouseSystemManager.Command> {

    public interface Command {}

    public static final class Initialize implements Command {
        public final List<String> regions;
        public final int sitesPerRegion;
        public final int greenhousesPerSite;
        public Initialize(List<String> regions, int sitesPerRegion, int greenhousesPerSite) {
            this.regions = regions;
            this.sitesPerRegion = sitesPerRegion;
            this.greenhousesPerSite = greenhousesPerSite;
        }
    }

    public static Behavior<Command> create() {
        Behavior<Command> behavior = Behaviors.setup(GreenhouseSystemManager::new);
        // Critical component: restart on failures, limited backoff
        return Behaviors.supervise(behavior)
                .onFailure(Exception.class, SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(1), Duration.ofSeconds(10), 0.2));
    }

    private GreenhouseSystemManager(ActorContext<Command> ctx) {
        super(ctx);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Initialize.class, this::onInitialize)
                .build();
    }

    private Behavior<Command> onInitialize(Initialize msg) {
        getContext().getLog().info("Initializing system for regions: {}", msg.regions);
        // Spawn shared service supervisors (stubs)
        ActorRef<DataProcessingManager.Command> dataProc = getContext().spawn(DataProcessingManager.create(), "data-processing");
        getContext().spawn(NotificationManager.create(), "notification");
        getContext().spawn(MaintenanceScheduler.create(), "maintenance-scheduler");

        for (String region : msg.regions) {
            var child = getContext().spawn(RegionManager.create(region, msg.sitesPerRegion, msg.greenhousesPerSite, dataProc), "region-" + region);
            child.tell(new RegionManager.Bootstrap());
        }

        // Start stubbed stream simulating Kafka sensor events routed via data processing
        SensorStream.run(getContext().getSystem(), dataProc, computeGreenhouseIds(msg.regions, msg.sitesPerRegion, msg.greenhousesPerSite));
        return this;
    }

    private static java.util.List<String> computeGreenhouseIds(java.util.List<String> regions, int sitesPerRegion, int greenhousesPerSite) {
        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        for (String region : regions) {
            for (int s = 0; s < sitesPerRegion; s++) {
                String siteId = region + "-site-" + s;
                for (int g = 0; g < greenhousesPerSite; g++) {
                    ids.add(siteId + "-gh-" + g);
                }
            }
        }
        return ids;
    }
}
