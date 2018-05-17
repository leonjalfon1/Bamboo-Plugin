package com.cx.plugin.dto;

import java.io.IOException;


/**
 * CxAbortException is used for errors that aborts the current operation
 * Corresponds to AbortException in the Jenkins plugin
 */

public final class CxAbortException extends IOException {
    public CxAbortException() {
    }

    public CxAbortException(String message) {
        super(message);
    }
}
