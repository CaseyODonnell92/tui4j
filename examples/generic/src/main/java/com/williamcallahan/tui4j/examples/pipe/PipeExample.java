package com.williamcallahan.tui4j.examples.pipe;

import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.Model;
import com.williamcallahan.tui4j.compat.bubbletea.Program;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.textinput.TextInput;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.KeyAliases;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.KeyAliases.KeyAlias;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.KeyType;
import com.williamcallahan.tui4j.compat.bubbletea.message.KeyPressMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.QuitMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PipeExample implements Model {

    private final TextInput textInput;

    public PipeExample(String initialValue) {
        this.textInput = new TextInput();
        textInput.setPlaceholder("Type something...");
        textInput.setValue(initialValue);
        textInput.setWidth(48);
        textInput.focus();
    }

    @Override
    public Command init() {
        return TextInput::blink;
    }

    @Override
    public UpdateResult<? extends Model> update(Message msg) {
        if (msg instanceof KeyPressMessage keyPressMessage) {
            if (KeyAliases.getKeyType(KeyAlias.KeyEnter) == keyPressMessage.type()
                    || KeyAliases.getKeyType(KeyAlias.KeyCtrlC) == keyPressMessage.type()
                    || KeyType.keyESC == keyPressMessage.type()) {
                return UpdateResult.from(this, QuitMessage::new);
            }
        }

        UpdateResult<? extends Model> updateResult = textInput.update(msg);
        return UpdateResult.from(this, updateResult.command());
    }

    @Override
    public String view() {
        return "\nYou piped in: %s\n\nPress ^C, Esc, or Enter to exit".formatted(textInput.view());
    }

    public static void main(String[] args) {
        String pipedInput = readPipedInput();

        if (pipedInput == null || pipedInput.isEmpty()) {
            System.out.println("Try piping in some text. Example:");
            System.out.println("  echo 'Hello, World!' | java -jar tui4j-examples.jar");
            System.exit(1);
        }

        new Program(new PipeExample(pipedInput.trim())).run();
    }

    private static String readPipedInput() {
        try {
            if (System.in.available() == 0) {
                return null;
            }
        } catch (IOException e) {
            System.err.println("Failed to check system input availability: " + e.getMessage());
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
        } catch (IOException e) {
            System.err.println("Error reading piped input: " + e.getMessage());
            return null;
        }
        return sb.toString();
    }
}
