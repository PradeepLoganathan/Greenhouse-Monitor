package com.example.greenhouse.app;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import com.example.greenhouse.actors.GreenhouseSystemManager;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ActorSystem<GreenhouseSystemManager.Command> system =
                ActorSystem.create(GreenhouseSystemManager.create(), "GreenhouseSystem");

        system.log().info("Greenhouse Monitor system started");

        // Kick off initialization with config-driven regions/sites
        List<String> regions = getRegions();
        int sitesPerRegion = getInt("greenhouse.sites-per-region", 2);
        int greenhousesPerSite = getInt("greenhouse.greenhouses-per-site", 2);

        system.tell(new GreenhouseSystemManager.Initialize(regions, sitesPerRegion, greenhousesPerSite));


        // Add a shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            system.log().info("Terminating Greenhouse Monitor system...");
            system.terminate();
            try {
                system.getWhenTerminated().toCompletableFuture().get();
            } catch (Exception ignored) { }
        }));
    }

    private static List<String> getRegions() {
        String raw = System.getProperty("greenhouse.regions");
        if (raw != null && !raw.isBlank()) {
            return Arrays.asList(raw.split(","));
        }
        // Fallback defaults (match application.conf)
        return Arrays.asList("us-east", "eu-west");
    }

    private static int getInt(String key, int dflt) {
        String v = System.getProperty(key);
        if (v == null) return dflt;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return dflt; }
    }

}
