package nl.authentication.management.app.ui;

import androidx.annotation.Nullable;

public class UIResult<T> {
    @Nullable
    private T success;
    @Nullable
    private Integer error;

    public UIResult(@Nullable Integer error) { this.error = error; }

    public UIResult(@Nullable T success) { this.success = success; }

    @Nullable
    public T getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}
