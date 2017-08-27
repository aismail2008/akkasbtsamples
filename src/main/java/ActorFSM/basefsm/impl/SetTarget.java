package ActorFSM.basefsm.impl;

import akka.actor.ActorRef;

/**
 * Created by aliismail on 25/08/2017.
 */
public class SetTarget {
    private final ActorRef ref;

    public SetTarget(ActorRef ref) {
        this.ref = ref;
    }

    public ActorRef getRef() {
        return ref;
    }

    @Override
    public String toString() {
        return "SetTarget{" +
                "ref=" + ref +
                '}';
    }
}