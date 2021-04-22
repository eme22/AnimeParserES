package com.eme22.animeparseres.Model;

import androidx.annotation.Nullable;

public class AnimeError extends Throwable {

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    Model.SERVER server;
    String message;
    String html;
    int ErrorCode;

    public Model.SERVER getServer() {
        return server;
    }

    public void setServer(Model.SERVER server) {
        this.server = server;
    }

    public int getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(int errorCode) {
        ErrorCode = errorCode;
    }

    public AnimeError() {
    }

    public AnimeError(Model.SERVER server, int errorCode) {
        this.server = server;
        ErrorCode = errorCode;
    }

    public AnimeError(int errorCode) {
        ErrorCode = errorCode;
    }
}
