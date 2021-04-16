package com.eme22.animeparseres.Model;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

public class AnimeResponse<T> {

    private final T mResult;

    private final AnimeError mError;

    public static <T> AnimeResponse<T> success(T result) {
        return new AnimeResponse<>(result);
    }

    public static <T> AnimeResponse<T> failed(AnimeError anError) {
        return new AnimeResponse<>(anError);
    }

    public AnimeResponse(T mResult) {
        this.mResult = mResult;
        this.mError = null;
    }

    public AnimeResponse(AnimeError mError) {
        this.mResult = null;
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
