package nl.authentication.management.app.data;


import javax.inject.Inject;

import nl.authentication.management.app.data.login.LoggedInUser;

public class AuthCache {
    private LoggedInUser loggedInUser;

    @Inject
    public AuthCache() {

    }

    public LoggedInUser getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
