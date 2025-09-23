package demos;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.example.greenhouse.actors.*;
import java.time.Duration;

public class DataProcessingDemo {
    public static void main(String[] args) {
        System.out.println("🔄 Demo 3: Message Routing - Understanding Data Flow");
        System.out.println("=".repeat(60));
        
        ActorTestKit testKit = ActorTestKit.create();
        
        try {
            demonstrateBasicRouting(testKit);
            demonstrateMultipleGreenhouses(testKit);
            demonstrateErrorHandling(testKit);
            demonstrateMetricsCollection(testKit);
            demonstrateLoadTesting(testKit);
        } finally {
            testKit.shutdownTestKit();
        }
        
        System.out.println("\n✅ Demo 3 Complete!");
        System.out.println("Key takeaways:");
        System.out.println("- Registry pattern enables dynamic message routing");
        System.out.println("- Error handling prevents system crashes from invalid destinations");  
        System.out.println("- Metrics collection provides observability into data flow");
        System.out.println("- The system scales by adding more greenhouse registrations");
    }
    
    private static void demonstrateBasicRouting(ActorTestKit testKit) {
        System.out.println("\n🎯 Basic Message Routing");
        System.out.println("-".repeat(40));
        
        // Create data processing manager
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "data-manager-basic"
        );
        
        // Create test probe to simulate a greenhouse
        TestProbe<GreenhouseActor.Command> greenhouseProbe = testKit.createTestProbe("greenhouse-1");
        
        System.out.println("  📋 Step 1: Register greenhouse with data manager");
        String greenhouseId = "us-east-site-0-gh-0";
        dataManager.tell(new DataProcessingManager.RegisterGreenhouse(
            greenhouseId, greenhouseProbe.getRef()
        ));
        System.out.printf("     ✓ Registered: %s%n", greenhouseId);
        
        System.out.println("\n  📊 Step 2: Send sensor data");
        dataManager.tell(new DataProcessingManager.SensorEnvelope(
            greenhouseId, "temperature", 25.3
        ));
        System.out.println("     📤 Sent: temperature=25.3°C");
        
        // Verify greenhouse received the message
        GreenhouseActor.SensorReading reading = greenhouseProbe.expectMessageClass(
            GreenhouseActor.SensorReading.class
        );
        System.out.printf("     📥 Received: %s = %.1f%n", reading.kind, reading.value);
        System.out.printf("     🆔 Sensor ID: %s%n", reading.sensorId);
    }
    
    private static void demonstrateMultipleGreenhouses(ActorTestKit testKit) {
        System.out.println("\n🏭 Multiple Greenhouse Routing");
        System.out.println("-".repeat(40));
        
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "data-manager-multi"
        );
        
        // Create multiple greenhouse probes
        TestProbe<GreenhouseActor.Command> gh1 = testKit.createTestProbe("gh-1");
        TestProbe<GreenhouseActor.Command> gh2 = testKit.createTestProbe("gh-2");
        TestProbe<GreenhouseActor.Command> gh3 = testKit.createTestProbe("gh-3");
        
        // Register all greenhouses
        String[] ids = {"us-east-site-0-gh-0", "us-east-site-0-gh-1", "eu-west-site-1-gh-0"};
        TestProbe<GreenhouseActor.Command>[] probes = new TestProbe[]{gh1, gh2, gh3};
        
        System.out.println("  📋 Registering multiple greenhouses:");
        for (int i = 0; i < ids.length; i++) {
            dataManager.tell(new DataProcessingManager.RegisterGreenhouse(ids[i], probes[i].getRef()));
            System.out.printf("     ✓ %s%n", ids[i]);
        }
        
        System.out.println("\n  📊 Sending targeted sensor data:");
        // Send data to each greenhouse
        String[][] sensorData = {
            {"temperature", "22.5"}, {"humidity", "67.0"}, {"soil", "42.0"}
        };
        
        for (int i = 0; i < ids.length; i++) {
            String kind = sensorData[i][0];
            double value = Double.parseDouble(sensorData[i][1]);
            
            dataManager.tell(new DataProcessingManager.SensorEnvelope(ids[i], kind, value));
            System.out.printf("     📤 → %s: %s=%.1f%n", ids[i], kind, value);
            
            // Verify correct routing
            GreenhouseActor.SensorReading reading = probes[i].expectMessageClass(
                GreenhouseActor.SensorReading.class
            );
            System.out.printf("     📥 ← %s received: %s=%.1f%n", ids[i], reading.kind, reading.value);
        }
        
        System.out.println("\n  ✅ All messages routed correctly to their target greenhouses");
    }
    
    private static void demonstrateErrorHandling(ActorTestKit testKit) {
        System.out.println("\n⚠️ Error Handling for Unknown Destinations");
        System.out.println("-".repeat(40));
        
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "data-manager-errors"
        );
        
        // Register one greenhouse
        TestProbe<GreenhouseActor.Command> knownGreenhouse = testKit.createTestProbe("known-gh");
        dataManager.tell(new DataProcessingManager.RegisterGreenhouse(
            "known-greenhouse", knownGreenhouse.getRef()
        ));
        
        System.out.println("  📋 Registered greenhouse: 'known-greenhouse'");
        
        // Send to unknown greenhouse
        System.out.println("\n  🚫 Testing unknown destination:");
        dataManager.tell(new DataProcessingManager.SensorEnvelope(
            "unknown-greenhouse", "temperature", 25.0
        ));
        System.out.println("     📤 Sent to: 'unknown-greenhouse' (should be logged as warning)");
        
        // Send to known greenhouse (should work)
        System.out.println("\n  ✅ Testing known destination:");
        dataManager.tell(new DataProcessingManager.SensorEnvelope(
            "known-greenhouse", "humidity", 55.0
        ));
        System.out.println("     📤 Sent to: 'known-greenhouse'");
        
        GreenhouseActor.SensorReading reading = knownGreenhouse.expectMessageClass(
            GreenhouseActor.SensorReading.class
        );
        System.out.printf("     📥 Received: %s=%.1f%n", reading.kind, reading.value);
        
        // Test null/empty greenhouse ID
        System.out.println("\n  🔍 Testing edge cases:");
        dataManager.tell(new DataProcessingManager.SensorEnvelope(
            "", "temperature", 20.0
        ));
        System.out.println("     📤 Sent to: '' (empty string)");
        
        dataManager.tell(new DataProcessingManager.SensorEnvelope(
            null, "temperature", 20.0
        ));
        System.out.println("     📤 Sent to: null");
        
        System.out.println("\n  💡 System remains stable despite invalid routing attempts");
    }
    
    private static void demonstrateMetricsCollection(ActorTestKit testKit) {
        System.out.println("\n📈 Metrics Collection and Monitoring");
        System.out.println("-".repeat(40));
        
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "data-manager-metrics"
        );
        
        System.out.println("  📊 Sending various ingest metrics:");
        
        // Simulate different data sources
        String[][] metrics = {
            {"kafka-sensors-topic", "Processed batch of 1,000 temperature readings"},
            {"kafka-weather-topic", "Ingested weather data for 5 regions"},
            {"http-api-sensors", "Received 250 manual sensor readings"},
            {"file-import", "Imported historical data: 10,000 records"},
            {"mqtt-iot-bridge", "Connected devices: 45, Messages: 2,300"}
        };
        
        for (String[] metric : metrics) {
            dataManager.tell(new DataProcessingManager.IngestMetric(metric[0], metric[1]));
            System.out.printf("     📈 %s: %s%n", metric[0], metric[1]);
            
            // Small delay for better visualization
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        System.out.println("\n  💡 In production, these metrics would be:");
        System.out.println("     - Exported to monitoring systems (Prometheus, CloudWatch)");
        System.out.println("     - Used for alerting on data flow issues");
        System.out.println("     - Analyzed for capacity planning");
    }
    
    private static void demonstrateLoadTesting(ActorTestKit testKit) {
        System.out.println("\n🚀 Load Testing Message Routing");
        System.out.println("-".repeat(40));
        
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "data-manager-load"
        );
        
        // Register several greenhouses
        TestProbe<GreenhouseActor.Command>[] probes = new TestProbe[5];
        String[] greenhouseIds = new String[5];
        
        System.out.println("  📋 Setting up load test environment:");
        for (int i = 0; i < 5; i++) {
            greenhouseIds[i] = "load-test-gh-" + i;
            probes[i] = testKit.createTestProbe("gh-" + i);
            dataManager.tell(new DataProcessingManager.RegisterGreenhouse(
                greenhouseIds[i], probes[i].getRef()
            ));
        }
        System.out.printf("     ✓ Registered %d greenhouses%n", probes.length);
        
        // Send burst of messages
        System.out.println("\n  🔥 Sending burst of 50 sensor readings...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 50; i++) {
            String greenhouseId = greenhouseIds[i % greenhouseIds.length];
            String[] sensorTypes = {"temperature", "humidity", "soil"};
            String sensorType = sensorTypes[i % sensorTypes.length];
            double value = 20.0 + (Math.random() * 30.0); // Random value 20-50
            
            dataManager.tell(new DataProcessingManager.SensorEnvelope(
                greenhouseId, sensorType, value
            ));
        }
        
        // Count received messages
        int totalReceived = 0;
        for (TestProbe<GreenhouseActor.Command> probe : probes) {
            int count = 0;
            try {
                while (true) {
                    probe.expectMessageClass(
                        GreenhouseActor.SensorReading.class,
                        Duration.ofMillis(100)
                    );
                    count++;
                    totalReceived++;
                }
            } catch (Exception e) {
                // No more messages for this probe
            }
            System.out.printf("     📥 %s received %d messages%n", probe.getRef().path().name(), count);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("\n  ✅ Load test complete: %d messages processed in %d ms%n", 
            totalReceived, (endTime - startTime));
        System.out.printf("     📊 Throughput: %.1f messages/second%n", 
            totalReceived * 1000.0 / (endTime - startTime));
    }
}
