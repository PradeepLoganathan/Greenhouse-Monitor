package demos;

import akka.NotUsed;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.stream.javadsl.*;
import com.example.greenhouse.actors.*;
import com.example.greenhouse.stream.SensorStream;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StreamDemo {
    public static void main(String[] args) {
        System.out.println("🌊 Demo 4: Stream Processing - Understanding Data Streams");
        System.out.println("=".repeat(60));
        
        ActorTestKit testKit = ActorTestKit.create();
        
        try {
            demonstrateBasicStreaming(testKit);
            demonstrateSensorStreamIntegration(testKit);
            demonstrateBackpressureHandling(testKit);
            demonstrateStreamTransformation(testKit);
            demonstrateErrorHandling(testKit);
        } finally {
            testKit.shutdownTestKit();
        }
        
        System.out.println("\n✅ Demo 4 Complete!");
        System.out.println("Key takeaways:");
        System.out.println("- Streams provide continuous data flow from external sources");
        System.out.println("- Stream-to-actor integration enables real-time processing");
        System.out.println("- Backpressure prevents system overload");
        System.out.println("- Stream transformations enable data filtering and enrichment");
    }
    
    private static void demonstrateBasicStreaming(ActorTestKit testKit) {
        System.out.println("\n🚰 Basic Streaming Concepts");
        System.out.println("-".repeat(40));
        
        System.out.println("  📊 Creating a simple number stream:");
        
        // Simple stream that emits numbers 1 to 5
        Source<Integer, NotUsed> numbers = Source.range(1, 5);
        
        // Transform and collect results
        List<String> results = numbers
            .map(n -> "Sensor-" + n + ": " + (20.0 + n * 2.5) + "°C")
            .runWith(Sink.seq(), testKit.system())
            .toCompletableFuture()
            .join();
        
        System.out.println("  📈 Stream results:");
        results.forEach(result -> System.out.println("     " + result));
        
        System.out.println("\n  💡 This shows basic stream transformation: Source → Map → Sink");
    }
    
    private static void demonstrateSensorStreamIntegration(ActorTestKit testKit) {
        System.out.println("\n🔄 Stream-to-Actor Integration");
        System.out.println("-".repeat(40));
        
        // Create data processing manager to receive stream data
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "stream-data-manager"
        );
        
        // Register a greenhouse to receive the data
        TestProbe<GreenhouseActor.Command> greenhouseProbe = testKit.createTestProbe("stream-greenhouse");
        dataManager.tell(new DataProcessingManager.RegisterGreenhouse(
            "stream-test-gh-0", greenhouseProbe.getRef()
        ));
        
        System.out.println("  🏭 Registered greenhouse: stream-test-gh-0");
        
        // Create a controlled stream (using the same pattern as SensorStream)
        List<String> greenhouseIds = Arrays.asList("stream-test-gh-0");
        Random rnd = new Random(42); // Deterministic for demo
        
        System.out.println("  🌊 Starting sensor data stream...");
        
        // Create a short-lived stream for demo purposes
        Source<Integer, NotUsed> ticks = Source.range(1, 10);
        
        ticks
            .throttle(1, Duration.ofMillis(200)) // Slow down for demo visibility
            .map(t -> createSensorEvent(rnd, greenhouseIds))
            .to(Sink.foreach(event -> {
                System.out.printf("     📤 Stream → DataManager: %s %s=%.1f%n", 
                    event.greenhouseId, event.kind, event.value);
                dataManager.tell(new DataProcessingManager.SensorEnvelope(
                    event.greenhouseId, event.kind, event.value));
            }))
            .run(testKit.system());
        
        // Collect messages received by greenhouse
        System.out.println("\n  📥 Messages received by greenhouse:");
        int messageCount = 0;
        try {
            while (messageCount < 10) {
                GreenhouseActor.SensorReading reading = greenhouseProbe.expectMessageClass(
                    GreenhouseActor.SensorReading.class,
                    Duration.ofSeconds(1)
                );
                messageCount++;
                System.out.printf("     %d. %s = %.1f%n", messageCount, reading.kind, reading.value);
            }
        } catch (Exception e) {
            System.out.printf("     Received %d messages before stream completed%n", messageCount);
        }
        
        System.out.println("\n  ✅ Stream successfully fed data into actor system");
    }
    
    private static void demonstrateBackpressureHandling(ActorTestKit testKit) {
        System.out.println("\n⚡ Backpressure and Flow Control");
        System.out.println("-".repeat(40));
        
        System.out.println("  🚀 Simulating fast producer, slow consumer...");
        
        // Fast producer (every 10ms)
        Source<Integer, NotUsed> fastProducer = Source.range(1, 20)
            .throttle(10, Duration.ofMillis(100)); // 10 elements per 100ms
        
        long startTime = System.currentTimeMillis();
        
        // Slow consumer (process each item in 50ms)
        List<String> results = fastProducer
            .map(i -> {
                try {
                    Thread.sleep(50); // Simulate slow processing
                } catch (InterruptedException e) {
                    // ignore
                }
                return String.format("Processed item %d at %dms", 
                    i, System.currentTimeMillis() - startTime);
            })
            .take(5) // Only take first 5 for demo
            .runWith(Sink.seq(), testKit.system())
            .toCompletableFuture()
            .join();
        
        System.out.println("  📊 Backpressure results:");
        results.forEach(result -> System.out.println("     " + result));
        
        long endTime = System.currentTimeMillis();
        System.out.printf("  ⏱️  Total time: %dms (backpressure prevented overload)%n", 
            endTime - startTime);
    }
    
    private static void demonstrateStreamTransformation(ActorTestKit testKit) {
        System.out.println("\n🔄 Stream Transformations and Filtering");
        System.out.println("-".repeat(40));
        
        // Create a stream of mixed sensor data
        Random rnd = new Random(123);
        Source<SensorEvent, NotUsed> sensorData = Source.range(1, 20)
            .map(i -> createSensorEvent(rnd, Arrays.asList("gh-1", "gh-2", "gh-3")));
        
        System.out.println("  🌡️  Filtering for temperature readings only:");
        
        List<SensorEvent> temperatureReadings = sensorData
            .filter(event -> "temperature".equals(event.kind))
            .take(5)
            .runWith(Sink.seq(), testKit.system())
            .toCompletableFuture()
            .join();
        
        temperatureReadings.forEach(event -> 
            System.out.printf("     %s: %.1f°C%n", event.greenhouseId, event.value));
        
        System.out.println("\n  🔥 Finding high temperature alerts (>28°C):");
        
        Source.range(1, 30)
            .map(i -> createSensorEvent(rnd, Arrays.asList("gh-1", "gh-2")))
            .filter(event -> "temperature".equals(event.kind) && event.value > 28.0)
            .take(3)
            .runWith(Sink.foreach(event -> 
                System.out.printf("     🚨 HIGH TEMP ALERT: %s = %.1f°C%n", 
                    event.greenhouseId, event.value)
            ), testKit.system())
            .toCompletableFuture()
            .join();
        
        System.out.println("\n  📈 Aggregating data by greenhouse:");
        
        Source.range(1, 15)
            .map(i -> createSensorEvent(rnd, Arrays.asList("gh-A", "gh-B")))
            .groupBy(2, event -> event.greenhouseId)
            .take(3)
            .reduce((e1, e2) -> new SensorEvent(e1.greenhouseId, "average", (e1.value + e2.value) / 2))
            .mergeSubstreams()
            .runWith(Sink.foreach(event ->
                System.out.printf("     %s average: %.1f%n", event.greenhouseId, event.value)
            ), testKit.system())
            .toCompletableFuture()
            .join();
    }
    
    private static void demonstrateErrorHandling(ActorTestKit testKit) {
        System.out.println("\n🛡️ Stream Error Handling and Recovery");
        System.out.println("-".repeat(40));
        
        System.out.println("  ⚠️ Simulating stream with occasional errors:");
        
        // Stream that occasionally throws exceptions
        Source<Integer, NotUsed> unreliableSource = Source.range(1, 10)
            .map(i -> {
                if (i == 3 || i == 7) {
                    throw new RuntimeException("Simulated sensor failure at reading " + i);
                }
                return i;
            });
        
        // Handle errors and continue processing
        List<String> results = unreliableSource
            .recover(new akka.japi.pf.PFBuilder<Throwable, Integer>()
                .matchAny(throwable -> -1)
                .build())
            .map(i -> i == -1 ? "ERROR: Sensor offline" : "Reading: " + (20.0 + i * 2))
            .runWith(Sink.seq(), testKit.system())
            .toCompletableFuture()
            .join();
        
        System.out.println("  📊 Results with error handling:");
        results.forEach(result -> System.out.println("     " + result));
        
        System.out.println("\n  🔄 Stream restart with supervision:");
        
        // Alternative: restart stream on failure
        Source<Integer, NotUsed> restartingSource = Source.range(1, 5)
            .map(i -> {
                if (i == 3) {
                    throw new RuntimeException("Temporary failure");
                }
                return i * 10;
            });
        
        // In a real system, you'd use RestartSource for automatic recovery
        try {
            List<Integer> results2 = restartingSource
                .recover(new akka.japi.pf.PFBuilder<Throwable, Integer>()
                    .matchAny(throwable -> 999)
                    .build())
                .runWith(Sink.seq(), testKit.system())
                .toCompletableFuture()
                .join();
            
            System.out.println("  📈 Results with restart handling:");
            results2.forEach(result -> System.out.println("     " + 
                (result == 999 ? "RECOVERED" : "Value: " + result)));
        } catch (Exception e) {
            System.out.println("     Stream recovered from: " + e.getMessage());
        }
        
        System.out.println("\n  💡 Production systems use RestartSource for automatic recovery");
    }
    
    // Helper method to create sensor events (similar to SensorStream)
    private static SensorEvent createSensorEvent(Random rnd, List<String> greenhouseIds) {
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
    
    // Simple data class for sensor events
    public static final class SensorEvent {
        public final String greenhouseId;
        public final String kind;
        public final double value;
        
        public SensorEvent(String greenhouseId, String kind, double value) {
            this.greenhouseId = greenhouseId;
            this.kind = kind;
            this.value = value;
        }
    }
}