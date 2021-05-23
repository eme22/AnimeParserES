package com.eme22.animeparseres.Model;

import androidx.annotation.Nullable;

import com.androidnetworking.error.ANError;

public class AnimeError {

    private String errormessage;
    private Model.SERVER server;
    private ANError error;
    private Exception othererror;
    private boolean isNetworkError;
    private boolean isApiError;

    public Model.SERVER getServer() {
        return server;
    }

    public void setServer(Model.SERVER server) {
        this.server = server;
    }

    public ANError getError() {
        return error;
    }

    public void setError(ANError error) {
        this.error = error;
    }

    public Exception getOthererror() {
        return othererror;
    }

    public void setOthererror(Exception othererror) {
        this.othererror = othererror;
    }

    public boolean isNetworkError() {
        return isNetworkError;
    }

    public void setNetworkError(boolean networkError) {
        isNetworkError = networkError;
    }

    public boolean isApiError() {
        return isApiError;
    }

    public void setApiError(boolean apiError) {
        isApiError = apiError;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }

    public AnimeError(Model.SERVER server, ANError error) {
        this.server = server;
        this.error = error;
        this.isApiError = false;
        this.isNetworkError = true;
        errormessage = error.getLocalizedMessage();
    }

    public AnimeError(Model.SERVER server, Exception othererror) {
        this.server = server;
        this.othererror = othererror;
        this.isNetworkError = false;
        this.isApiError = false;
        errormessage = othererror.getMessage();
    }
    public AnimeError(ANError error) {
        this.error = error;
        this.isApiError = false;
        this.isNetworkError = true;
        errormessage = error.getMessage();
    }

    public AnimeError(Exception othererror) {
        this.othererror = othererror;
        this.isNetworkError = false;
        this.isApiError = false;
        errormessage = othererror.getMessage();
    }
}
