package nl.authentication.management.app.api.oauth2;

import java.util.UUID;

import javax.inject.Singleton;

import nl.authentication.management.app.data.login.LoggedInUser;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

@Singleton
public interface AuthApi {

    @POST("users")
    void register();

    @FormUrlEncoded
    @POST("users/authenticate/native")
    Call<LoggedInUser> authenticateNative(
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("users/authenticate/google")
    Call<LoggedInUser> authenticateGoogle(
            @Field("username") String username,
            @Field("idToken") String password
    );

    @POST("users/{uuid}/logout")
    Call<Void> logout(@Path("uuid") UUID uuid);

}