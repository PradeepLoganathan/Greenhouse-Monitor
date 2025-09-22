package com.example.greenhouse.actors;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataProcessingManagerTest {

    private static ActorTestKit testKit;

    @BeforeAll
    static void setup() {
        testKit = ActorTestKit.create();
    }

    @AfterAll
    static void tearDown() {
        testKit.shutdownTestKit();
    }

    @Test
    void forwardsSensorEnvelopeToRegisteredGreenhouse() {
        ActorRef<DataProcessingManager.Command> data = testKit.spawn(DataProcessingManager.create());
        TestProbe<GreenhouseActor.Command> greenhouseProbe = testKit.createTestProbe();

        String greenhouseId = "us-east-site-0-gh-0";
        data.tell(new DataProcessingManager.RegisterGreenhouse(greenhouseId, greenhouseProbe.getRef()));

        data.tell(new DataProcessingManager.SensorEnvelope(greenhouseId, "temperature", 31.2));

        GreenhouseActor.SensorReading reading = greenhouseProbe.expectMessageClass(GreenhouseActor.SensorReading.class);
        assertEquals("temperature", reading.kind);
        assertEquals(31.2, reading.value, 0.0001);
    }
}

