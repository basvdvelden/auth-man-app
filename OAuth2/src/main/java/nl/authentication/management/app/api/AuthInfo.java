package nl.authentication.management.app.api;

import java.util.UUID;

import androidx.annotation.NonNull;

public class AuthInfo {
    private UUID userId;
    private Tokens tokens;

    public AuthInfo(@NonNull UUID userId, @NonNull Tokens tokens) {
        this.userId = userId;
        this.tokens = tokens;
    }

    public UUID getUserId() {
        return userId;
    }

    public Tokens getTokens() {
        return tokens;
    }

    public void setTokens(Tokens tokens) {
        this.tokens = tokens;
    }
}
