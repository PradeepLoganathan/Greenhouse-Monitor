package demos;

import akka.actor.typed.ActorRef;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.example.greenhouse.actors.SensorActor;
import com.example.greenhouse.actors.GreenhouseActor;

public class SimpleSensorDemo {
    public static void main(String[] args) {
        System.out.println("Demo 1: Basic Actors - Understanding Individual Components");
        System.out.println("=".repeat(60));
        
        ActorTestKit testKit = ActorTestKit.create();
        
        try {
            demonstrateSensorTypes(testKit);
            demonstrateMultipleReadings(testKit);
            demonstrateMessageTypes(testKit);
        } finally {
            testKit.shutdownTestKit();
        }
        
        System.out.println("\n✅ Demo 1 Complete!");
        System.out.println("Key takeaways:");
        System.out.println("- Actors respond to messages asynchronously");
        System.out.println("- Each sensor type generates realistic value ranges");  
        System.out.println("- TestProbe allows us to capture and verify messages");
        System.out.println("- Actors maintain their behavior across multiple messages");
    }
    
    private static void demonstrateSensorTypes(ActorTestKit testKit) {
        System.out.println("\n Testing Different Sensor Types");
        System.out.println("-".repeat(40));
        
        TestProbe<GreenhouseActor.Command> probe = testKit.createTestProbe();
        
        // Test each sensor type
        SensorActor.Kind[] types = {
            SensorActor.Kind.Temperature, 
            SensorActor.Kind.Humidity, 
            SensorActor.Kind.SoilMoisture
        };
        
        for (SensorActor.Kind type : types) {
            ActorRef<SensorActor.Command> sensor = testKit.spawn(
                SensorActor.create("demo-greenhouse", type), 
                "sensor-" + type.name().toLowerCase()
            );
            
            sensor.tell(new SensorActor.EmitSample(probe.getRef()));
            
            GreenhouseActor.SensorReading reading = probe.expectMessageClass(
                GreenhouseActor.SensorReading.class
            );
            
            String unit = getUnit(type);
            System.out.printf("  %s: %.2f %s (ID: %s)%n", 
                type.name(), reading.value, unit, reading.sensorId);
        }
    }
    
    private static void demonstrateMultipleReadings(ActorTestKit testKit) {
        System.out.println("\n📊 Multiple Temperature Readings");
        System.out.println("-".repeat(40));
        
        TestProbe<GreenhouseActor.Command> probe = testKit.createTestProbe();
        ActorRef<SensorActor.Command> sensor = testKit.spawn(
            SensorActor.create("demo-greenhouse", SensorActor.Kind.Temperature), 
            "temp-sensor-multi"
        );
        
        double min = Double.MAX_VALUE, max = Double.NEGATIVE_INFINITY, sum = 0;
        int count = 5;
        
        for (int i = 0; i < count; i++) {
            sensor.tell(new SensorActor.Read(probe.getRef()));
            GreenhouseActor.SensorReading reading = probe.expectMessageClass(
                GreenhouseActor.SensorReading.class
            );
            
            System.out.printf("  Reading %d: %.2f°C%n", i+1, reading.value);
            min = Math.min(min, reading.value);
            max = Math.max(max, reading.value);
            sum += reading.value;
        }
        
        System.out.printf("  Summary: Min=%.1f°C, Max=%.1f°C, Avg=%.1f°C%n", 
            min, max, sum/count);
    }
    
    private static void demonstrateMessageTypes(ActorTestKit testKit) {
        System.out.println("\n💬 Different Message Types");
        System.out.println("-".repeat(40));
        
        TestProbe<GreenhouseActor.Command> probe = testKit.createTestProbe();
        ActorRef<SensorActor.Command> sensor = testKit.spawn(
            SensorActor.create("demo-greenhouse", SensorActor.Kind.Humidity), 
            "humidity-sensor-msgs"
        );
        
        System.out.println("  Sending 'Read' message...");
        sensor.tell(new SensorActor.Read(probe.getRef()));
        GreenhouseActor.SensorReading reading1 = probe.expectMessageClass(
            GreenhouseActor.SensorReading.class
        );
        System.out.printf("    Response: %s = %.1f%%%n", reading1.kind, reading1.value);
        
        System.out.println("  Sending 'EmitSample' message...");
        sensor.tell(new SensorActor.EmitSample(probe.getRef()));
        GreenhouseActor.SensorReading reading2 = probe.expectMessageClass(
            GreenhouseActor.SensorReading.class
        );
        System.out.printf("    Response: %s = %.1f%%%n", reading2.kind, reading2.value);
        
        System.out.println("  Both message types produce the same result!");
    }
    
    private static String getUnit(SensorActor.Kind type) {
        return switch (type) {
            case Temperature -> "°C";
            case Humidity -> "%";
            case SoilMoisture -> "%";
            default -> "";
        };
    }
}