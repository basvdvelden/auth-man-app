package nl.authentication.management.app;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import nl.authentication.management.app.data.login.LoggedInUser;

@Singleton
public class LoginNotifier {
    private BehaviorSubject<LoggedInUser> loggedIn = BehaviorSubject.create();
    private MutableLiveData<LoggedInUser> loggedInLive = new MutableLiveData<>();

    @Inject
    public LoginNotifier() {}

    public BehaviorSubject<LoggedInUser> getLoggedInSubject() {
        return loggedIn;
    }

    public LiveData<LoggedInUser> getLoggedInLive() {
        return loggedInLive;
    }

    public void setLoggedIn(LoggedInUser user) {
        this.loggedIn.onNext(user);
        this.loggedInLive.setValue(user);
    }
}
