# Demo 3: Message Routing & Registry Patterns

## 🎯 Learning Objectives
- Understand the registry pattern for actor discovery
- Learn how messages are routed between system components
- See error handling for unknown destinations
- Experience monitoring and metrics collection

## 🔧 What This Demo Shows
1. **Registry Pattern**: How DataProcessingManager maintains actor references
2. **Message Routing**: Routing sensor data to appropriate greenhouse actors
3. **Error Handling**: Graceful handling of messages to unknown destinations
4. **Metrics Collection**: Tracking data ingestion and processing


```bash
mvn compile exec:java -Pdemo3
```

## 💡 Key Concepts Demonstrated

### Registry Pattern
```java
// Actors register themselves for message routing
registry.put(reg.greenhouseId, reg.ref);
```

### Message Routing with Error Handling
```java
var target = registry.get(env.greenhouseId);
if (target != null) {
    target.tell(new GreenhouseActor.SensorReading(...));
} else {
    getContext().getLog().warn("no target registered for greenhouse {}", env.greenhouseId);
}
```

## 🛠 Try These Modifications
1. Add multiple greenhouse registrations and test routing
2. Implement load balancing for multiple greenhouses in same region
3. Add message acknowledgment and retry logic
4. Create custom metrics for different sensor types

## ➡️ Next Steps
Move to [Demo 4: Stream Integration](../04-stream-integration/) to see external data integration.
