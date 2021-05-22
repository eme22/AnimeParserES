package com.eme22.animeparseres.Model;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

public class AnimeResponse<T> {

    private final T mResult;

    private final AnimeError mError;

    private final boolean isSuccess;

    public static <T> AnimeResponse<T> success(T result) {
        return new AnimeResponse<>(result);
    }

    public static <T> AnimeResponse<T> failed(AnimeError anError) {
        return new AnimeResponse<>(anError);
    }

    public AnimeResponse(T mResult) {
        this.mResult = mResult;
        this.isSuccess = true;
        this.mError = null;
    }

    public AnimeResponse(AnimeError mError) {
        this.mResult = null;
        this.isSuccess = false;
        this.mError = mError;
    }

    public T getmResult() {
        return mResult;
    }

    public AnimeError getmError() {
        return mError;
    }

    public boolean isSuccess() {
        return mError == null;
    }

}
