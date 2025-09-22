# demos/01-basic-actors/README.md
# Demo 1: Basic Actors

## Learning Objectives
- Understand how to create and test individual actors
- Learn actor message handling patterns  
- See actor state management in action
- Experience Akka TestKit for actor testing

## What This Demo Shows
1. **Actor Creation**: How to spawn actors with specific behaviors
2. **Message Passing**: Sending messages and receiving responses
3. **State Management**: How actors maintain internal state
4. **Testing**: Using TestKit and TestProbe for verification

## Running the Demo
```bash
./run.sh
```

## 🔍 Expected Output
```
=== Testing Temperature Sensor ===
Received: temperature = 23.45
Sensor ID: temperature-test-greenhouse

=== Multiple readings ===
Reading 1: 21.32°C
Reading 2: 28.76°C
Reading 3: 19.88°C
Reading 4: 25.12°C
Reading 5: 30.45°C
```

## 💡 Key Concepts Demonstrated

### Actor Lifecycle
```java
// Actors are created with specific behaviors and context
ActorRef<SensorActor.Command> sensor = testKit.spawn(
    SensorActor.create("test-greenhouse", SensorActor.Kind.Temperature), 
    "temp-sensor"
);
```

### Message Protocols
```java
// Actors respond to specific message types
sensor.tell(new SensorActor.EmitSample(probe.getRef()));
```

### Testing with TestKit
```java
// TestProbe captures messages for verification
TestProbe<GreenhouseActor.Command> probe = testKit.createTestProbe();
GreenhouseActor.SensorReading reading = probe.expectMessageClass(
    GreenhouseActor.SensorReading.class
);
```

## Try These Modifications
1. Create humidity and soil moisture sensors
2. Modify the sensor value ranges
3. Add error simulation (throw exceptions)
4. Create custom message types

## Next Steps
Move to [Demo 2: Greenhouse Control](../02-greenhouse-control/) to see how actors implement business logic.

---