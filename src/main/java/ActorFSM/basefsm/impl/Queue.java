package ActorFSM.basefsm.impl;

/**
 * Created by aliismail on 25/08/2017.
 */
public final class Queue {
    private final Object obj;

    public Queue(Object obj) {
        this.obj = obj;
    }

    public Object getObj() {
        return this.obj;
    }

    @Override
    public String toString() {
        return "Queue{" +
                "obj=" + obj +
                '}';
    }
}