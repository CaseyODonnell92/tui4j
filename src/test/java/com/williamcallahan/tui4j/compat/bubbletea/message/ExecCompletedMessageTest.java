package com.williamcallahan.tui4j.compat.bubbletea.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExecCompletedMessageTest {

    @Test
    void testSuccessRequiresZeroExitCodeAndNoError() {
        ExecCompletedMessage success = new ExecCompletedMessage(0, null);
        ExecCompletedMessage nonZeroExit = new ExecCompletedMessage(1, null);
        ExecCompletedMessage error = new ExecCompletedMessage(0, new IllegalStateException("boom"));

        assertThat(success.success()).isTrue();
        assertThat(nonZeroExit.success()).isFalse();
        assertThat(error.success()).isFalse();
    }

    @Test
    void testErrorMessagePrefersThrowable() {
        ExecCompletedMessage withError = new ExecCompletedMessage(0, new IllegalStateException("boom"));
        ExecCompletedMessage withExitCode = new ExecCompletedMessage(2, null);
        ExecCompletedMessage ok = new ExecCompletedMessage(0, null);

        assertThat(withError.errorMessage()).isEqualTo("boom");
        assertThat(withExitCode.errorMessage()).isEqualTo("Process exited with code 2");
        assertThat(ok.errorMessage()).isNull();
    }
}
