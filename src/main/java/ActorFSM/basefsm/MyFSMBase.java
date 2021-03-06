package ActorFSM.basefsm;

import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * Created by aliismail on 25/08/2017.
 */
public abstract  class MyFSMBase extends UntypedActor {
    public final Object flush = new Object();

    /*
       * This is the mutable state of this state machine.
       */
    protected enum State {
        IDLE, ACTIVE;
    }

    private State state = State.IDLE;
    private ActorRef target;
    private List<Object> queue;

    /*
     * Then come all the mutator methods:
     */
    protected void init(ActorRef target) {
        this.target = target;
        queue = new ArrayList<Object>();
    }

    protected void setState(State s) {
        if (state != s) {
            transition(state, s);
            state = s;
        }
    }

    protected void enqueue(Object o) {
        if (queue != null)
            queue.add(o);
    }

    protected List<Object> drainQueue() {
        final List<Object> q = queue;
        if (q == null)
            throw new IllegalStateException("drainQueue(): not yet initialized");
        queue = new ArrayList<Object>();
        return q;
    }

    /*
     * Here are the interrogation methods:
     */
    protected boolean isInitialized() {
        return target != null;
    }

    protected State getState() {
        return state;
    }

    protected ActorRef getTarget() {
        if (target == null)
            throw new IllegalStateException("getTarget(): not yet initialized");
        return target;
    }

    /*
     * And finally the callbacks (only one in this example: react to state change)
     */
    abstract protected void transition(State old, State next);
}