package com.fintech.sip;

public enum AuditType {
    SIP_PROCESSING("SIP Processing"),
    SIP_CREATION("SIP Creation"),
    SIP_UPDATE("SIP Update"),
    SIP_CANCELLATION("SIP Cancellation"),
    NOTIFICATION_SENT("Notification Sent"),
    ERROR_OCCURRED("Error Occurred"),
    JOB_COMPLETION("Job Completion");

    private final String displayName;

    AuditType(String displayName) {
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
