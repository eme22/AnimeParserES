package com.eme22.animeparseres.Util;

public class BypassInfo {

    public enum BypasStatus {
        BYPASSING, SUCCEED, FAULURE, NOT_NEEDED
    }

    BypasStatus isBypassing;
    String cookie;

    public BypasStatus getIsBypassing() {
        return isBypassing;
    }

    public void setIsBypassing(BypasStatus isBypassing) {
        this.isBypassing = isBypassing;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
