package com.example.greenhouse.stream;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.*;
import com.example.greenhouse.actors.DataProcessingManager;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public final class SensorStream {

    public static void run(ActorSystem<?> system,
                           ActorRef<DataProcessingManager.Command> dataRef,
                           List<String> greenhouseIds) {
        if (greenhouseIds.isEmpty()) return;
        Random rnd = new Random();

        Source<Long, NotUsed> ticks = Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(2), 1L)
                .mapMaterializedValue(cancellable -> NotUsed.getInstance());

        ticks
            .map(t -> randomEvent(rnd, greenhouseIds))
            .to(Sink.foreach(ev -> dataRef.tell(
                    new DataProcessingManager.SensorEnvelope(ev.greenhouseId, ev.kind, ev.value)
            )))
            .run(system);
    }

    private static SensorEvent randomEvent(Random rnd, List<String> greenhouseIds) {
        String greenhouseId = greenhouseIds.get(rnd.nextInt(greenhouseIds.size()));
        String kind;
        double value;
        int pick = rnd.nextInt(3);
        switch (pick) {
            case 0 -> { kind = "temperature"; value = 18 + rnd.nextDouble() * 15; }
            case 1 -> { kind = "humidity"; value = 40 + rnd.nextDouble() * 50; }
            default -> { kind = "soil"; value = 10 + rnd.nextDouble() * 50; }
        }
        return new SensorEvent(greenhouseId, kind, value);
    }

    public static final class SensorEvent {
        public final String greenhouseId;
        public final String kind;
        public final double value;
        public SensorEvent(String greenhouseId, String kind, double value) {
            this.greenhouseId = greenhouseId; this.kind = kind; this.value = value;
        }
    }
}

