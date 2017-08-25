package persistence;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by aliismail on 25/08/2017.
 */
public class SnapshotExample {
    public static class ExampleState implements Serializable {
        private static final long serialVersionUID = 1L;
        private final ArrayList<String> received;

        public ExampleState() {
            this(new ArrayList<String>());
        }

        public ExampleState(ArrayList<String> received) {
            this.received = received;
        }

        public ExampleState copy() {
            return new ExampleState(new ArrayList<String>(received));
        }

        public void update(String s) {
            received.add(s);
        }

        @Override
        public String toString() {
            return received.toString();
        }
    }

    public static class ExamplePersistentActor extends AbstractPersistentActor {
        private ExampleState state = new ExampleState();

        @Override
        public String persistenceId() { return "sample-id-3"; }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("print", s -> System.out.println("current state = " + state))
                    .matchEquals("snap", s -> {
                        // IMPORTANT: create a copy of snapshot
                        // because ExampleState is mutable !!!
                        saveSnapshot(state.copy());
                    })
                    .match(String.class, s -> {
                        persist(s, evt -> state.update(evt));
                    })
                    .build();
        }

        @Override
        public Receive createReceiveRecover() {
            return receiveBuilder()
                    .match(String.class, evt -> state.update(evt))
                    .match(SnapshotOffer.class, ss -> {
                        System.out.println("offered state = " + ss);
                        state = (ExampleState) ss.snapshot();
                    })
                    .build();
        }
    }

    public static void main(String... args) throws Exception {
        final ActorSystem system = ActorSystem.create("example");
        final ActorRef persistentActor = system.actorOf(Props.create(ExamplePersistentActor.class), "persistentActor-3-java");

        persistentActor.tell("a", null);
        persistentActor.tell("b", null);
        persistentActor.tell("snap", null);
        persistentActor.tell("c", null);
        persistentActor.tell("d", null);
        persistentActor.tell("print", null);

        Thread.sleep(10000);
        system.terminate();
    }
}
