package nl.authentication.management.app.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

@Singleton
public class NetworkNotifier {
    private BehaviorSubject<Event> networkStatus = BehaviorSubject.create();

    @Inject
    public NetworkNotifier() {}

    public BehaviorSubject<Event> getNetworkStatus() {
        // recreate every time so that there's always only one observer
        networkStatus = BehaviorSubject.create();
        return networkStatus;
    }

    public void networkDown() {
        networkStatus.onNext(Event.NETWORK_DOWN);
    }

    public enum Event {
        NETWORK_DOWN
    }
}
