package nl.authentication.management.app.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@Component(modules = {
        AppApiModule.class,
        ActivityModule.class,
        ViewModelModule.class,
        AppModule.class,
        AndroidSupportInjectionModule.class
})
@Singleton
public interface AppComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }


    /*
     * This is our custom Application class
     * */
    void inject(App appController);
}
