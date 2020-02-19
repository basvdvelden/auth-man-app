package nl.authentication.management.app.api;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Response;

public class NetworkConnectivityInterceptor implements Interceptor {
    private final ConnectivityManager cm;
    private final NetworkNotifier networkNotifier;

    @Inject
    public NetworkConnectivityInterceptor(Application context, NetworkNotifier networkNotifier) {
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.networkNotifier = networkNotifier;
    }
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        if (!isConnected) {
            networkNotifier.networkDown();
        }
        return chain.proceed(chain.request());
    }
}
