package nl.authentication.management.app.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Inject;

import nl.authentication.management.app.di.App;
import okhttp3.Interceptor;
import okhttp3.Response;

public class NetworkConnectivityInterceptor implements Interceptor {
    private final ConnectivityManager cm;

    @Inject
    public NetworkConnectivityInterceptor(App context) {
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
