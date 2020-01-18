package nl.authentication.management.app.di;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import dagger.Module;
import dagger.Provides;
import nl.authentication.management.app.BuildConfig;
import nl.authentication.management.app.api.AuthNotifier;
import nl.authentication.management.app.api.AuthorizationInterceptor;
import nl.authentication.management.app.api.AuthApi;
import nl.authentication.management.app.ssl.SSLTrustManagerHelper;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module(includes = {AppModule.class})
public class AppApiModule {
    private static final String TAG = "AppApiModule";

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder().create();
    }


    @Provides
    @Singleton
    public SSLTrustManagerHelper provideSSLTrustManagerHelper() {
        InputStream keystore = this.getClass().getClassLoader().getResourceAsStream(BuildConfig.KEYSTORE_PATH);
        InputStream truststore = this.getClass().getClassLoader().getResourceAsStream(BuildConfig.KEYSTORE_PATH);
        String keystorePwd = BuildConfig.KEYSTORE_PASSWORD;
        String truststorePwd = BuildConfig.KEYSTORE_PASSWORD;
        SSLTrustManagerHelper ssl = null;
        try {
            ssl = new SSLTrustManagerHelper(keystore, keystorePwd, truststore, truststorePwd);
        } catch (Exception e) {
            Log.e(TAG, "error occured while creating ssl trust manager helper!", e);
        }
        return ssl;
    }

    @Provides
    @Singleton
    public AuthorizationInterceptor provideAuthorizationInterceptor(AuthNotifier notifier) {
        return new AuthorizationInterceptor(notifier);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(SSLTrustManagerHelper sslTrustManagerHelper,
                                            AuthorizationInterceptor authorizationInterceptor) {
        SSLContext context = null;
        try {
            context = sslTrustManagerHelper.clientSSLContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final SSLSocketFactory sslSocketFactory = context.getSocketFactory();
        Log.w(TAG, context.getSocketFactory().toString());
        OkHttpClient okHttpClient = new OkHttpClient();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.level(HttpLoggingInterceptor.Level.BODY);
        return okHttpClient.newBuilder()
                .addInterceptor(new HttpLoggingInterceptor())
                .addInterceptor(authorizationInterceptor)
                // TODO: Create hostname verifier.
                .sslSocketFactory(sslSocketFactory, sslTrustManagerHelper.getTrustManager())
                .hostnameVerifier((hostname, sslSession) -> true)
                .build();
    }

    @Provides
    @Singleton
    @Named("auth")
    public Retrofit provideAuthRetrofit(Gson gson, OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }

    @Provides
    @Singleton
    public AuthApi provideAuthApi(@Named("auth") Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }
}
