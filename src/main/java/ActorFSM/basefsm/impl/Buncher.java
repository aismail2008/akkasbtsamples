package ActorFSM.basefsm.impl;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.japi.pf.UnitMatch;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static ActorFSM.basefsm.impl.State.Active;
import static ActorFSM.basefsm.impl.State.Idle;

/**
 * Created by aliismail on 25/08/2017.
 */

// states
enum State {
    Idle, Active
}

// state data
interface Data {
}
enum Uninitialized implements Data {
    Uninitialized
}
final class Todo implements Data {
    private final ActorRef target;
    private final List<Object> queue;

    public Todo(ActorRef target, List<Object> queue) {
        this.target = target;
        this.queue = queue;
    }

    public ActorRef getTarget() {
        return target;
    }

    public List<Object> getQueue() {
        return queue;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "target=" + target +
                ", queue=" + queue +
                '}';
    }

    public Todo addElement(Object element) {
        List<Object> nQueue = new LinkedList<>(queue);
        nQueue.add(element);
        return new Todo(this.target, nQueue);
    }

    public Todo copy(List<Object> queue) {
        return new Todo(this.target, queue);
    }

    public Todo copy(ActorRef target) {
        return new Todo(target, this.queue);
    }
}

public class Buncher extends AbstractFSM<State, Data> {
    {
        startWith(Idle, Uninitialized.Uninitialized);

        when(Idle,
                matchEvent(SetTarget.class, Uninitialized.class,
                        (setTarget, uninitialized) ->
                                stay().using(new Todo(setTarget.getRef(), new LinkedList<>())))); // stay at current state and keep this new Todo class as your current data which include the received actorRef

        when(Active, Duration.create(1, "second"),
                matchEvent(Arrays.asList(Flush.class, StateTimeout()), Todo.class,
                        (event, todo) -> goTo(Idle).using(todo.copy(new LinkedList<>()))));

        whenUnhandled(
                matchEvent(Queue.class, Todo.class,
                        (queue, todo) -> goTo(Active).using(todo.addElement(queue.getObj()))).
                        anyEvent((event, state) -> {
                            log().warning("received unhandled request {} in state {}/{}",
                                    event, stateName(), state);
                            return stay();
                        }));

        onTransition(
                matchState(Active, Idle, () -> {
                    // reuse this matcher
                    final UnitMatch<Data> m = UnitMatch.create(
                            matchData(Todo.class,
                                    todo -> todo.getTarget().tell(new Batch(todo.getQueue()), ActorRef.noSender())));
                    m.match(stateData());
                }).
                        state(Idle, Active, () -> {/* Do something here */}));

        initialize();
    }
}
