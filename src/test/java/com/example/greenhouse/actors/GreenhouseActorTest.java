package com.example.greenhouse.actors;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GreenhouseActorTest {

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
    void registersWithDataProcessingOnInitialize() {
        TestProbe<DataProcessingManager.Command> dataProbe = testKit.createTestProbe();
        ActorRef<GreenhouseActor.Command> gh = testKit.spawn(
                GreenhouseActor.create("us-east", "us-east-site-0", "us-east-site-0-gh-0", dataProbe.getRef())
        );

        gh.tell(new GreenhouseActor.Initialize());

        DataProcessingManager.RegisterGreenhouse reg = dataProbe.expectMessageClass(DataProcessingManager.RegisterGreenhouse.class);
        assertEquals("us-east-site-0-gh-0", reg.greenhouseId);
        assertNotNull(reg.ref);
    }
}

