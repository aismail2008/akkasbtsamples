package persistence;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;

import java.util.Random;

import static java.util.Arrays.asList;

/**
 * Created by aliismail on 25/08/2017.
 */
public class ExamplePersistentActor extends AbstractPersistentActor{

    private ExampleState state = new ExampleState();
    private int snapShotInterval = 1000;

    public int getNumEvents() {
        return state.size();
    }


    @Override
    public String persistenceId() {
        return "sample-id-1";
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Evt.class, state::update)
                .match(SnapshotOffer.class, ss -> state = (ExampleState) ss.snapshot())
                .match(RecoveryCompleted.class, r -> {
                    System.out.println(" ========RecoveryCompleted========== : " + r.toString());
                    // perform init after recovery, before any other messages
                    // ...
                })
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cmd.class, c -> {
                    System.out.println(" ========Command Received========== : " + c.getData());
                    final String data = c.getData();

                    final Evt evt1 = new Evt(data + "-" + getNumEvents());
                    final Evt evt2 = new Evt(data + "-" + (getNumEvents() + 1));
                    persistAll(asList(evt1, evt2), (Evt evt) -> {
                        state.update(evt);
                        if (evt.equals(evt2)) {
                            getContext().system().eventStream().publish(evt);
                        }

                        if (lastSequenceNr() % snapShotInterval == 0 && lastSequenceNr() != 0)
                            // IMPORTANT: create a copy of snapshot because ExampleState is mutable
                            saveSnapshot(state.copy());
                    });
                })
                .matchEquals("snap", s -> saveSnapshot(state.copy()))
                .matchEquals("print", s -> System.out.println("******** : " + state))
                .build();
    }

    public static void main(String... args) throws Exception {
        final ActorSystem system = ActorSystem.create("example");
        final ActorRef persistentActor = system.actorOf(Props.create(ExamplePersistentActor.class), "persistentActor-4-java8");
        persistentActor.tell(new Cmd("foo"), persistentActor);
        persistentActor.tell(new Cmd("baz"), persistentActor);
        persistentActor.tell(new Cmd("bar)"), persistentActor);
        persistentActor.tell("snap", persistentActor);
        persistentActor.tell(new Cmd("buzz"), persistentActor);
        persistentActor.tell("print", persistentActor);

        Thread.sleep(10000);
        system.terminate();
    }
}
