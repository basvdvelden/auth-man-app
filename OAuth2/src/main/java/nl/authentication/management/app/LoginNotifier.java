package nl.authentication.management.app;

import javax.inject.Inject;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class LoginNotifier {
    private PublishSubject<Boolean> loggedIn = PublishSubject.create();

    @Inject
    public LoginNotifier() {}

    public PublishSubject<Boolean> getLoggedInSubject() {
        return loggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn.onNext(loggedIn);
    }
}
