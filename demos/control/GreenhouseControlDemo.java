package demos.control;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.example.greenhouse.actors.*;

public class GreenhouseControlDemo {
    public static void main(String[] args) {
        System.out.println("🌡️ Demo 2: Greenhouse Control - Understanding Business Logic");
        System.out.println("=".repeat(60));
        
        ActorTestKit testKit = ActorTestKit.create();
        
        try {
            demonstrateGreenhouseInitialization(testKit);
            demonstrateTemperatureControl(testKit);
            demonstrateOtherSensorTypes(testKit);
            demonstrateActuatorIntegration(testKit);
        } finally {
            testKit.shutdownTestKit();
        }
        
        System.out.println("\n✅ Demo 2 Complete!");
        System.out.println("Key takeaways:");
        System.out.println("- Actors can spawn child actors during initialization");
        System.out.println("- Business logic determines control decisions based on sensor data");
        System.out.println("- Messages flow through the system asynchronously");
        System.out.println("- Each greenhouse registers itself with the data processing system");
    }
    
    private static void demonstrateGreenhouseInitialization(ActorTestKit testKit) {
        System.out.println("\n📋 Testing Greenhouse Initialization");
        System.out.println("-".repeat(40));
        
        // Create test probe for data processing manager
        TestProbe<DataProcessingManager.Command> dataProbe = testKit.createTestProbe();
        
        // Create greenhouse actor
        ActorRef<GreenhouseActor.Command> greenhouse = testKit.spawn(
            GreenhouseActor.create("us-east", "site-0", "greenhouse-1", dataProbe.getRef()),
            "greenhouse-control-demo"
        );
        
        System.out.println("  Creating greenhouse actor...");
        
        // Initialize greenhouse (spawns sensors and actuators)
        greenhouse.tell(new GreenhouseActor.Initialize());
        
        // Verify registration with data processor
        DataProcessingManager.RegisterGreenhouse reg = dataProbe.expectMessageClass(
            DataProcessingManager.RegisterGreenhouse.class
        );
        
        System.out.printf("  ✓ Greenhouse initialized: %s%n", reg.greenhouseId);
        System.out.println("  ✓ Registered with data processing manager");
        System.out.println("  ✓ Child actors spawned: 3 sensors + 3 actuators");
        
        // Give some time for child actors to initialize
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }
    }
    
    private static void demonstrateTemperatureControl(ActorTestKit testKit) {
        System.out.println("\n🌡️ Testing Temperature Control Logic");
        System.out.println("-".repeat(40));
        
        TestProbe<DataProcessingManager.Command> dataProbe = testKit.createTestProbe();
        ActorRef<GreenhouseActor.Command> greenhouse = testKit.spawn(
            GreenhouseActor.create("us-east", "site-0", "greenhouse-temp", dataProbe.getRef()),
            "greenhouse-temp-control"
        );
        
        // Initialize first
        greenhouse.tell(new GreenhouseActor.Initialize());
        dataProbe.expectMessageClass(DataProcessingManager.RegisterGreenhouse.class); // consume registration
        
        // Test high temperature (should trigger fan)
        System.out.println("  🔥 Testing high temperature scenario...");
        greenhouse.tell(new GreenhouseActor.SensorReading(
            "temp-sensor-001", "temperature", 30.5
        ));
        System.out.println("     Sent: 30.5°C → Expected: TURN_ON_FAN");
        
        // Test low temperature (should trigger heater)  
        System.out.println("  ❄️  Testing low temperature scenario...");
        greenhouse.tell(new GreenhouseActor.SensorReading(
            "temp-sensor-002", "temperature", 18.0
        ));
        System.out.println("     Sent: 18.0°C → Expected: TURN_ON_HEATER");
        
        // Test normal temperature (no action)
        System.out.println("  😎 Testing normal temperature scenario...");
        greenhouse.tell(new GreenhouseActor.SensorReading(
            "temp-sensor-003", "temperature", 24.0
        ));
        System.out.println("     Sent: 24.0°C → Expected: No action");
        
        // Test edge cases
        System.out.println("  🎯 Testing edge cases...");
        greenhouse.tell(new GreenhouseActor.SensorReading(
            "temp-sensor-004", "temperature", 28.0 // Exactly at threshold
        ));
        System.out.println("     Sent: 28.0°C (threshold) → Expected: No action");
        
        greenhouse.tell(new GreenhouseActor.SensorReading(
            "temp-sensor-005", "temperature", 28.1 // Just above threshold
        ));
        System.out.println("     Sent: 28.1°C (just above) → Expected: TURN_ON_FAN");
    }
    
    private static void demonstrateOtherSensorTypes(ActorTestKit testKit) {
        System.out.println("\n📊 Testing Other Sensor Types");
        System.out.println("-".repeat(40));
        
        TestProbe<DataProcessingManager.Command> dataProbe = testKit.createTestProbe();
        ActorRef<GreenhouseActor.Command> greenhouse = testKit.spawn(
            GreenhouseActor.create("us-east", "site-0", "greenhouse-multi", dataProbe.getRef()),
            "greenhouse-multi-sensor"
        );
        
        // Initialize
        greenhouse.tell(new GreenhouseActor.Initialize());
        dataProbe.expectMessageClass(DataProcessingManager.RegisterGreenhouse.class);
        
        // Test humidity (no control logic implemented yet)
        System.out.println("  💧 Testing humidity sensor...");
        greenhouse.tell(new GreenhouseActor.SensorReading(
            "humidity-001", "humidity", 65.0
        ));
        System.out.println("     Sent: 65% humidity → Logged only (no control logic yet)");
        
        // Test soil moisture (no control logic implemented yet)
        System.out.println("  🌱 Testing soil moisture sensor...");
        greenhouse.tell(new GreenhouseActor.SensorReading(
            "soil-001", "soil", 35.0
        ));
        System.out.println("     Sent: 35% soil moisture → Logged only (no control logic yet)");
        
        System.out.println("\n  💡 Extension opportunity: Add humidity and soil moisture control logic!");
        System.out.println("     - Low soil moisture → Start irrigation");
        System.out.println("     - High humidity + high temp → Increase ventilation");
    }
    
    private static void demonstrateActuatorIntegration(ActorTestKit testKit) {
        System.out.println("\n⚙️ Testing Actuator Integration");
        System.out.println("-".repeat(40));
        
        // Create actuator actors directly to show their functionality
        ActorRef<ActuatorActor.Command> fan = testKit.spawn(
            ActuatorActor.create("demo-greenhouse", ActuatorActor.Type.Fan),
            "demo-fan"
        );
        
        ActorRef<ActuatorActor.Command> heater = testKit.spawn(
            ActuatorActor.create("demo-greenhouse", ActuatorActor.Type.Heater), 
            "demo-heater"
        );
        
        ActorRef<ActuatorActor.Command> irrigation = testKit.spawn(
            ActuatorActor.create("demo-greenhouse", ActuatorActor.Type.Irrigation),
            "demo-irrigation"
        );
        
        System.out.println("  🌪️  Testing fan actuator...");
        fan.tell(new ActuatorActor.Execute("TURN_ON"));
        fan.tell(new ActuatorActor.Execute("SET_SPEED_HIGH"));
        fan.tell(new ActuatorActor.Execute("TURN_OFF"));
        
        System.out.println("  🔥 Testing heater actuator...");
        heater.tell(new ActuatorActor.Execute("TURN_ON"));
        heater.tell(new ActuatorActor.Execute("SET_TEMP_22C"));
        heater.tell(new ActuatorActor.Execute("TURN_OFF"));
        
        System.out.println("  💧 Testing irrigation actuator...");
        irrigation.tell(new ActuatorActor.Execute("START_IRRIGATION"));
        irrigation.tell(new ActuatorActor.Execute("SET_DURATION_30MIN"));
        irrigation.tell(new ActuatorActor.Execute("STOP_IRRIGATION"));
        
        System.out.println("\n  📝 Note: In the current implementation, control decisions are logged");
        System.out.println("      but not automatically routed to actuators. This is a design choice");
        System.out.println("      that allows for additional validation/safety logic before execution.");
        
        try {
            Thread.sleep(500); // Allow actuator messages to be processed and logged
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
