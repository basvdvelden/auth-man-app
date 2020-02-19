package nl.authentication.management.app.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nl.authentication.management.app.LoginNotifier;
import nl.authentication.management.app.api.AuthNotifier;
import nl.authentication.management.app.api.AuthApi;
import nl.authentication.management.app.data.AuthCache;
import nl.authentication.management.app.data.OAuthSharedPreferences;
import nl.authentication.management.app.data.login.LoginDataSource;
import nl.authentication.management.app.data.login.LoginRepository;

@Module(includes = {AppApiModule.class})
public class LoginModule {
    @Provides
    @Singleton
    public LoginDataSource provideLoginDataSource(AuthApi authApi) {
        return new LoginDataSource(authApi);
    }

    @Provides
    @Singleton
    AuthCache provideAuthCache() {
        return new AuthCache();
    }

    @Provides
    @Singleton
    AuthNotifier provideAuthNotifier() {
        return new AuthNotifier();
    }

    @Provides
    @Singleton
    LoginRepository provideLoginRepository(LoginDataSource ds, OAuthSharedPreferences sp, AuthCache ac,
                                           AuthNotifier an, LoginNotifier ln) {
        return new LoginRepository(ds, sp, ac, an, ln);
    }
}
