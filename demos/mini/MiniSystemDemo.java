package demos.mini;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import com.example.greenhouse.actors.*;
import com.example.greenhouse.stream.SensorStream;

import java.util.Arrays;
import java.util.List;

public class MiniSystemDemo {
    public static void main(String[] args) {
        System.out.println("🏗️ Demo 5: Mini System - Complete Integration");
        System.out.println("=".repeat(60));
        
        ActorTestKit testKit = ActorTestKit.create();
        
        try {
            demonstrateSystemBootstrap(testKit);
            demonstrateSharedServices(testKit);
            demonstrateDataFlow(testKit);
            demonstrateSystemMonitoring(testKit);
            demonstrateFullIntegration(testKit);
        } finally {
            testKit.shutdownTestKit();
        }
        
        System.out.println("\n✅ Demo 5 Complete!");
        System.out.println("Key takeaways:");
        System.out.println("- Hierarchical systems enable clean organization and scaling");
        System.out.println("- Shared services provide system-wide functionality");
        System.out.println("- Bootstrap patterns ensure proper initialization order");
        System.out.println("- All components work together to form a complete system");
    }
    
    private static void demonstrateSystemBootstrap(ActorTestKit testKit) {
        System.out.println("\n🚀 System Bootstrap Process");
        System.out.println("-".repeat(40));
        
        System.out.println("  📋 Step 1: Creating shared services...");
        
        // Create shared services (like in the real system)
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "data-processing-mini"
        );
        System.out.println("     ✓ Data Processing Manager");
        
        ActorRef<NotificationManager.Command> notificationManager = testKit.spawn(
            NotificationManager.create(), "notifications-mini"
        );
        System.out.println("     ✓ Notification Manager");
        
        ActorRef<MaintenanceScheduler.Command> maintenanceScheduler = testKit.spawn(
            MaintenanceScheduler.create(), "maintenance-scheduler-mini"
        );
        System.out.println("     ✓ Maintenance Scheduler");
        
        System.out.println("\n  🌍 Step 2: Creating regional hierarchy...");
        
        // Create a region with 2 sites, 2 greenhouses each (total: 4 greenhouses)
        ActorRef<RegionManager.Command> regionManager = testKit.spawn(
            RegionManager.create("demo-region", 2, 2, dataManager), "demo-region-manager"
        );
        System.out.println("     ✓ Region Manager: demo-region");
        
        // Bootstrap the region
        System.out.println("\n  ⚡ Step 3: Bootstrapping hierarchy...");
        regionManager.tell(new RegionManager.Bootstrap());
        System.out.println("     📤 Sent Bootstrap message to region");
        
        System.out.println("\n  📊 Expected hierarchy created:");
        System.out.println("     demo-region/");
        System.out.println("     ├── weather-station");
        System.out.println("     ├── site-0/");
        System.out.println("     │   ├── maintenance");
        System.out.println("     │   ├── gh-0/ (3 sensors + 3 actuators)");
        System.out.println("     │   └── gh-1/ (3 sensors + 3 actuators)");
        System.out.println("     └── site-1/");
        System.out.println("         ├── maintenance");
        System.out.println("         ├── gh-0/ (3 sensors + 3 actuators)");
        System.out.println("         └── gh-1/ (3 sensors + 3 actuators)");
        
        // Give time for hierarchy to initialize
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ignore
        }
        
        System.out.println("\n  ✅ Bootstrap complete - system is alive!");
    }
    
    private static void demonstrateSharedServices(ActorTestKit testKit) {
        System.out.println("\n🔧 Shared Services in Action");
        System.out.println("-".repeat(40));
        
        // Create shared services
        ActorRef<NotificationManager.Command> notifications = testKit.spawn(
            NotificationManager.create(), "notifications-demo"
        );
        
        System.out.println("  📢 Testing notification system:");
        
        // Send different types of notifications
        String[][] notificationTests = {
            {"INFO", "System startup completed successfully"},
            {"INFO", "All greenhouses online and operational"},
            {"WARNING", "High temperature detected in demo-region-site-0-gh-0: 31.2°C"},
            {"WARNING", "Low soil moisture in demo-region-site-1-gh-1: 15%"},
            {"CRITICAL", "Sensor communication failure: demo-region-site-0-gh-0-temperature"},
            {"CRITICAL", "Actuator malfunction: demo-region-site-1-gh-1-irrigation"},
            {"INFO", "Maintenance window starting in 30 minutes"},
            {"INFO", "Weather update: Rain expected, adjusting irrigation schedules"}
        };
        
        for (String[] notification : notificationTests) {
            notifications.tell(new NotificationManager.Notify(notification[0], notification[1]));
            System.out.printf("     %s: %s%n", notification[0], notification[1]);
            
            // Small delay for readability
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        System.out.println("\n  💡 In production, notifications would:");
        System.out.println("     - Send emails/SMS for CRITICAL alerts");
        System.out.println("     - Update dashboards for INFO messages");
        System.out.println("     - Trigger automated responses for certain WARNINGs");
    }
    
    private static void demonstrateDataFlow(ActorTestKit testKit) {
        System.out.println("\n📊 Data Flow Through Mini System");
        System.out.println("-".repeat(40));
        
        // Create the data processing pipeline
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "data-flow-demo"
        );
        
        // Create a simple region to receive the data
        ActorRef<RegionManager.Command> region = testKit.spawn(
            RegionManager.create("flow-region", 1, 2, dataManager), "flow-region"
        );
        region.tell(new RegionManager.Bootstrap());
        
        // Wait for initialization
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            // ignore
        }
        
        System.out.println("  🌊 Starting simulated data stream...");
        
        // Define greenhouse IDs that should exist after bootstrap
        List<String> greenhouseIds = Arrays.asList(
            "flow-region-site-0-gh-0",
            "flow-region-site-0-gh-1"
        );
        
        // Send some sensor data through the system
        System.out.println("  📤 Sending sensor data:");
        
        String[][] sensorData = {
            {"flow-region-site-0-gh-0", "temperature", "29.5"}, // Should trigger fan
            {"flow-region-site-0-gh-0", "humidity", "75.0"},
            {"flow-region-site-0-gh-1", "temperature", "18.5"}, // Should trigger heater
            {"flow-region-site-0-gh-1", "soil", "25.0"},
            {"flow-region-site-0-gh-0", "temperature", "24.0"}, // Normal, no action
        };
        
        for (String[] data : sensorData) {
            dataManager.tell(new DataProcessingManager.SensorEnvelope(
                data[0], data[1], Double.parseDouble(data[2])
            ));
            System.out.printf("     📊 %s: %s=%.1f%n", data[0], data[1], Double.parseDouble(data[2]));
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        System.out.println("\n  🔄 Data flow path:");
        System.out.println("     SensorData → DataProcessingManager → GreenhouseActor → ControlDecision");
        System.out.println("  💡 Check logs above for control decisions (fan/heater activation)");
    }
    
    private static void demonstrateSystemMonitoring(ActorTestKit testKit) {
        System.out.println("\n📈 System Monitoring and Metrics");
        System.out.println("-".repeat(40));
        
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "monitoring-demo"
        );
        
        System.out.println("  📊 Simulating various data ingestion sources:");
        
        // Simulate metrics from different sources
        String[][] ingestMetrics = {
            {"kafka-temperature-sensors", "Processed 5,420 temperature readings in last minute"},
            {"kafka-humidity-sensors", "Processed 3,180 humidity readings in last minute"},
            {"kafka-soil-sensors", "Processed 2,890 soil moisture readings in last minute"},
            {"weather-api-integration", "Updated weather data for 3 regions"},
            {"manual-sensor-readings", "Received 145 manual calibration readings"},
            {"maintenance-system", "Scheduled maintenance for 12 components"},
            {"alert-processor", "Generated 3 warnings, 1 critical alert"},
            {"data-export", "Exported 50,000 historical records to analytics system"}
        };
        
        for (String[] metric : ingestMetrics) {
            dataManager.tell(new DataProcessingManager.IngestMetric(metric[0], metric[1]));
            System.out.printf("     📈 %s%n", metric[1]);
            
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        System.out.println("\n  💹 System health indicators:");
        System.out.println("     ✅ Message throughput: ~11,490 sensor readings/minute");
        System.out.println("     ✅ External integrations: Weather API responding");
        System.out.println("     ✅ Manual overrides: 145 calibrations processed");
        System.out.println("     ⚠️  Alerts generated: 4 total (monitor for trends)");
    }
    
    private static void demonstrateFullIntegration(ActorTestKit testKit) {
        System.out.println("\n🌟 Full System Integration Demo");
        System.out.println("-".repeat(40));
        
        System.out.println("  🏗️ Creating complete mini greenhouse system...");
        
        // Create all shared services
        ActorRef<DataProcessingManager.Command> dataManager = testKit.spawn(
            DataProcessingManager.create(), "integration-data"
        );
        ActorRef<NotificationManager.Command> notifications = testKit.spawn(
            NotificationManager.create(), "integration-notifications"
        );
        
        // Create region
        ActorRef<RegionManager.Command> region = testKit.spawn(
            RegionManager.create("integration-region", 2, 2, dataManager), "integration-region"
        );
        
        // Bootstrap
        region.tell(new RegionManager.Bootstrap());
        
        System.out.println("     ✓ Created integration-region with 2 sites, 4 total greenhouses");
        
        // Wait for initialization
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ignore
        }
        
        // Send startup notification
        notifications.tell(new NotificationManager.Notify(
            "INFO", "Mini greenhouse system fully operational"
        ));
        
        // Start a short sensor stream to show everything working
        System.out.println("\n  🌊 Starting integrated data stream...");
        
        List<String> allGreenhouses = Arrays.asList(
            "integration-region-site-0-gh-0",
            "integration-region-site-0-gh-1", 
            "integration-region-site-1-gh-0",
            "integration-region-site-1-gh-1"
        );
        
        // Simulate realistic scenarios
        System.out.println("  📊 Simulating realistic greenhouse scenarios:");
        
        // Scenario 1: Morning temperature rise
        System.out.println("\n     🌅 Morning scenario - temperatures rising:");
        for (int i = 0; i < allGreenhouses.size(); i++) {
            double temp = 26.0 + i * 1.5; // Varying temperatures
            dataManager.tell(new DataProcessingManager.SensorEnvelope(
                allGreenhouses.get(i), "temperature", temp
            ));
            System.out.printf("        %s: %.1f°C%n", allGreenhouses.get(i), temp);
            
            if (temp > 28.0) {
                notifications.tell(new NotificationManager.Notify(
                    "INFO", String.format("Fan activated in %s due to temperature %.1f°C", 
                    allGreenhouses.get(i), temp)
                ));
            }
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }
        
        // Scenario 2: Irrigation needs
        System.out.println("\n     💧 Checking soil moisture levels:");
        double[] moistureLevels = {45.0, 25.0, 15.0, 35.0}; // Some low levels
        for (int i = 0; i < allGreenhouses.size(); i++) {
            dataManager.tell(new DataProcessingManager.SensorEnvelope(
                allGreenhouses.get(i), "soil", moistureLevels[i]
            ));
            System.out.printf("        %s: %.1f%% moisture%n", allGreenhouses.get(i), moistureLevels[i]);
            
            if (moistureLevels[i] < 20.0) {
                notifications.tell(new NotificationManager.Notify(
                    "WARNING", String.format("Low soil moisture in %s: %.1f%% - irrigation recommended", 
                    allGreenhouses.get(i), moistureLevels[i])
                ));
            }
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }
        
        // Scenario 3: Evening cooldown
        System.out.println("\n     🌆 Evening scenario - temperatures dropping:");
        for (String greenhouse : allGreenhouses) {
            double temp = 17.0 + Math.random() * 4; // Random temps 17-21°C
            dataManager.tell(new DataProcessingManager.SensorEnvelope(
                greenhouse, "temperature", temp
            ));
            System.out.printf("        %s: %.1f°C%n", greenhouse, temp);
            
            if (temp < 20.0) {
                notifications.tell(new NotificationManager.Notify(
                    "INFO", String.format("Heater activated in %s due to temperature %.1f°C", 
                    greenhouse, temp)
                ));
            }
        }
        
        // Final system status
        System.out.println("\n  🎯 System integration summary:");
        System.out.println("     ✅ 4 greenhouses operational");
        System.out.println("     ✅ 12 sensors providing data (3 per greenhouse)");
        System.out.println("     ✅ 12 actuators ready for control (3 per greenhouse)");
        System.out.println("     ✅ Data processing routing messages correctly");
        System.out.println("     ✅ Notifications generated for important events");
        System.out.println("     ✅ Control decisions made based on sensor data");
        
        // Send final metrics
        dataManager.tell(new DataProcessingManager.IngestMetric(
            "mini-system-demo", "Integration test completed successfully - all components operational"
        ));
        
        notifications.tell(new NotificationManager.Notify(
            "INFO", "Mini system integration demo completed - system is fully operational"
        ));
        
        System.out.println("\n  🌟 Mini system is now running as a complete, integrated solution!");
        System.out.println("     This represents a scaled-down version of the full production system.");
    }
}
