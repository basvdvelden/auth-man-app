package nl.authentication.management.app.api;


import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

@Singleton
public class AuthNotifier {
    private static final String TAG = "AuthNotifier";
    private PublishSubject<Tokens> tokens = PublishSubject.create();
    private BehaviorSubject<AuthInfo> authInfo = BehaviorSubject.create();
    private PublishSubject<Boolean> needToLogout = PublishSubject.create();


    @Inject
    public AuthNotifier() {
    }

    public PublishSubject<Tokens> getTokens() {
        return tokens;
    }

    void setTokens(Tokens tokens) {
        this.tokens.onNext(tokens);
    }

    public void setAuthInfo(UUID uuid, Tokens tokens) {
        AuthInfo authInfo = new AuthInfo(uuid, tokens);
        this.authInfo.onNext(authInfo);
    }

    public BehaviorSubject<AuthInfo> getAuthInfo() {
        return this.authInfo;
    }

    public PublishSubject<Boolean> getNeedToLogout() {
        return needToLogout;
    }

    public void notifyToLogout() {
        this.needToLogout.onNext(true);
    }
}
