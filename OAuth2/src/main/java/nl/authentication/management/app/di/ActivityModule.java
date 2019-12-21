package nl.authentication.management.app.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.authentication.management.app.ui.login.LoginFragment;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract LoginFragment contributeLoginActivityInjector();
}
