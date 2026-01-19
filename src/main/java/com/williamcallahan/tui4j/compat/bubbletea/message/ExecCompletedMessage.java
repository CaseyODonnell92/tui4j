package com.williamcallahan.tui4j.compat.bubbletea.message;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Message sent when a process execution completes.
 * Bubble Tea: bubbletea/exec.go
 *
 * @param exitCode process exit code
 * @param error execution error, if any
 */
public record ExecCompletedMessage(int exitCode, Throwable error) implements Message {

    /**
     * Checks if the process completed successfully (exit code 0 and no execution error).
     * <p>
     * Note: This method returns false if an execution error occurred (e.g., failed to start),
     * even if exitCode happens to be 0 (though -1 is standard for execution errors).
     */
    public boolean success() {
        return error == null && exitCode == 0;
    }

    public String errorMessage() {
        if (error != null) {
            return error.getMessage();
        }
        if (exitCode != 0) {
            return "Process exited with code " + exitCode;
        }
        return null;
    }
}
