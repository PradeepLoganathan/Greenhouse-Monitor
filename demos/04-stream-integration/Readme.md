# Demo 4: Stream Processing & External Integration

## 🎯 Learning Objectives
- Understand Akka Streams integration with actors
- Learn about external data source simulation (Kafka-like)
- See backpressure and flow control in action
- Experience real-time data processing patterns

## 🔧 What This Demo Shows
1. **Stream-to-Actor Integration**: How Akka Streams feed data to actors
2. **External Data Simulation**: Simulated Kafka sensor streams
3. **Backpressure Handling**: How streams handle different processing speeds
4. **Real-time Processing**: Continuous data flow and processing

## 🚀 Running the Demo
```bash
./run.sh
```
Or from project root:
```bash
mvn compile exec:java -Pdemo4
```

## 💡 Key Concepts Demonstrated

### Stream Integration Pattern
```java
// Stream feeds directly into actor system
Source<Long, NotUsed> ticks = Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(2), 1L);
ticks.map(t -> randomEvent(rnd, greenhouseIds))
     .to(Sink.foreach(ev -> dataRef.tell(new DataProcessingManager.SensorEnvelope(...))))
     .run(system);
```

### Backpressure Management
- Streams naturally handle backpressure
- Slower consumers don't overwhelm faster producers
- System remains stable under varying loads

## 🛠 Try These Modifications
1. Change stream tick rates to see backpressure effects
2. Add stream filtering for specific sensor types
3. Implement batching for high-volume scenarios
4. Add error handling and stream recovery

## ➡️ Next Steps
Move to [Demo 5: Mini System](../05-mini-system/) to see complete system integration.
