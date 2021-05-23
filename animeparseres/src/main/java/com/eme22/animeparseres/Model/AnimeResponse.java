package com.eme22.animeparseres.Model;

public class AnimeResponse<T> {

    private final T mResult;

    private final AnimeError mError;

    private final boolean isSuccess;

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
        return isSuccess;
    }

}
