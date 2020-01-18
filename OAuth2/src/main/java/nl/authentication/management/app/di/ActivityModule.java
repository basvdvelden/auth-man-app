package nl.authentication.management.app.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.authentication.management.app.ui.login.LoginFragment;
import nl.authentication.management.app.ui.register.RegisterFragment;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract LoginFragment contributeLoginFragmentInjector();
    @ContributesAndroidInjector
    abstract RegisterFragment contributeRegisterFragmentInjector();
}
