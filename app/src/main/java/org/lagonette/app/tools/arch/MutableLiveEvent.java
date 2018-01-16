package org.lagonette.app.tools.arch;

public class MutableLiveEvent<E>
        extends LiveEvent<E> {

    public void postEvent(E event) {
        super.postEvent(event);
    }

    public void sendEvent(E event) {
        super.sendEvent(event);
    }
}