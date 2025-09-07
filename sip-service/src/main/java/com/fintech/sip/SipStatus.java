package com.fintech.sip;

public enum SipStatus {
    ACTIVE("Active"),
    PAUSED("Paused"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String displayName;

    SipStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
