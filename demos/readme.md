# Greenhouse System Demos

This directory contains progressive demonstrations of the Akka-based Greenhouse Monitoring System. Each demo focuses on specific concepts and builds upon the previous ones.

## 🎯 Learning Path

| Demo | Focus Area | Key Concepts | Duration |
|------|------------|--------------|----------|
| [01-basic-actors](./01-basic-actors/) | Individual Actors | Actor lifecycle, message handling, TestKit | 10 min |
| [02-greenhouse-control](./02-greenhouse-control/) | Business Logic | Control algorithms, actor communication | 15 min |
| [03-message-routing](./03-message-routing/) | Message Patterns | Registry pattern, message routing, error handling | 15 min |
| [04-stream-integration](./04-stream-integration/) | Data Streams | Akka Streams, external data integration | 20 min |
| [05-mini-system](./05-mini-system/) | System Integration | Actor hierarchy, initialization patterns | 20 min |
| [06-full-system](./06-full-system/) | Production System | Complete system, configuration, monitoring | 30 min |

## 🚀 Quick Start

### Prerequisites
- Java 11+
- Gradle or Maven
- Your IDE of choice

### Running a Demo
```bash
cd demos/01-basic-actors
chmod +x run.sh
./run.sh
```

Or manually:
```bash
cd demos/01-basic-actors
javac -cp "../../../build/libs/*:../../../lib/*" SimpleSensorDemo.java
java -cp ".:../../../build/libs/*:../../../lib/*" SimpleSensorDemo
```

## 📖 Demo Descriptions

### 01-basic-actors
**Learn**: How individual actors work in isolation
- Spawn actors with TestKit
- Send messages and receive responses  
- Understand actor state and behavior changes
- See supervision strategies in action

### 02-greenhouse-control  
**Learn**: How business logic is implemented in actors
- Temperature-based control decisions
- Actor-to-actor communication
- Message types and protocols
- Error handling and logging

### 03-message-routing
**Learn**: How messages flow between system components  
- Registry pattern for actor discovery
- Message routing and forwarding
- Handling unknown destinations
- Monitoring and metrics

### 04-stream-integration
**Learn**: How external data enters the system
- Akka Streams basics
- Integration with external systems (Kafka simulation)
- Backpressure and flow control
- Stream-to-actor integration

### 05-mini-system
**Learn**: How actors are organized hierarchically
- Actor hierarchy and supervision trees  
- System initialization patterns
- Configuration-driven setup
- Cross-cutting concerns (notifications, maintenance)

### 06-full-system
**Learn**: Production system patterns
- Complete system startup
- Configuration management
- Monitoring and observability
- Graceful shutdown

## 🛠 Development Tips

### Adding New Demos
1. Create new directory following the naming pattern
2. Add README.md with learning objectives
3. Include runnable code with clear output
4. Add run.sh script for easy execution
5. Update this main README

### Customizing Demos
- Modify actor configurations in each demo
- Change message types and protocols
- Add new business logic scenarios
- Experiment with different supervision strategies

## 🤝 Using for Training

### Workshop Format (2-3 hours)
1. **Introduction** (15 min): Akka concepts overview
2. **Hands-on Demos** (90 min): Work through demos 1-5
3. **Full System** (30 min): Demo 6 - see it all together
4. **Q&A and Extension Ideas** (15-30 min)

### Self-Study Format
- Work through demos at your own pace
- Read README for each demo before running
- Experiment with modifications between demos
- Use as reference when building your own systems

## 📚 Additional Resources

- [Akka Documentation](https://doc.akka.io/)
- [Akka Typed Guide](https://doc.akka.io/docs/akka/current/typed/index.html)  
- [Actor Model Concepts](https://en.wikipedia.org/wiki/Actor_model)
- [Reactive Systems Principles](https://www.reactivemanifesto.org/)