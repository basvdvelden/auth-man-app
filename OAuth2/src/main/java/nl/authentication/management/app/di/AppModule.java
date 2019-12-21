package nl.authentication.management.app.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private static final String OAUTH_PREF_NAME = "auth-man";

    @Singleton
    @Provides
    @Named("auth")
    public SharedPreferences provideAuthSharedPreferences(Application context) {
        return context.getSharedPreferences(OAUTH_PREF_NAME, Context.MODE_PRIVATE);
    }
}
