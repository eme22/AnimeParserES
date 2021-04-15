package com.eme22.animeparseres.Model;

public class AnimeError extends Throwable {

    Model.SERVER server;

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
