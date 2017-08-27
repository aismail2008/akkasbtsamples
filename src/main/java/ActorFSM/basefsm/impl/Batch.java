package ActorFSM.basefsm.impl;

import java.util.List;

/**
 * Created by aliismail on 25/08/2017.
 */
public final class Batch {
    private final List<Object> list;

    public Batch(List<Object> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Batch batch = (Batch) o;

        return list.equals(batch.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Batch{list=");
        list.stream().forEachOrdered(e -> {
            builder.append(e);
            builder.append(",");
        });
        int len = builder.length();
        builder.replace(len, len, "}");
        return builder.toString();
    }
}