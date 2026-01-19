package com.williamcallahan.tui4j.compat.bubbletea.bubbles.key;

import com.williamcallahan.tui4j.compat.bubbletea.message.KeyPressMessage;

/**
 * Port of the Bubble Tea key binding helper used by Bubbles components.
 * Upstream: github.com/charmbracelet/bubbles/key (Binding)
 */
public class Binding {

    /**
     * Port of the binding option hook used during construction.
     * Upstream: github.com/charmbracelet/bubbles/key (BindingOption)
     */
    public interface BindingOption {

        /**
         * Applies the option to the binding.
         *
         * @param binding binding to update
         */
        void apply(Binding binding);
    }

    private String[] keys;

    private Help help;
    private boolean enabled;

    /**
     * Creates a binding configured by the provided options.
     *
     * @param opts binding options
     */
    public Binding(BindingOption... opts) {
        this.keys = null;
        this.help = new Help();
        this.enabled = true;

        for (BindingOption option : opts) {
            option.apply(this);
        }
    }

    /**
     * Returns whether the binding is enabled and has keys assigned.
     *
     * @return true when enabled and bound
     */
    public boolean isEnabled() {
        return enabled && keys != null;
    }

    /**
     * Enables or disables the binding.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Clears keys and help metadata.
     */
    public void unbind() {
        this.keys = null;
        this.help = new Help();
    }

    /**
     * Returns the help metadata for this binding.
     *
     * @return help metadata
     */
    public Help help() {
        return help;
    }

    /**
     * Returns whether the given key press matches this binding.
     *
     * @param keyPressMessage key press message
     * @return true when a key matches
     */
    public boolean matches(KeyPressMessage keyPressMessage) {
        if (keys == null) {
            return false;
        }
        for (String bindingKey : keys) {
            if (bindingKey.equals(keyPressMessage.key())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a binding option that sets the accepted key strings.
     *
     * @param keys key strings
     * @return binding option
     */
    public static BindingOption withKeys(String... keys) {
        return binding -> binding.keys = keys;
    }

    /**
     * Creates a binding option that sets the help metadata.
     *
     * @param key key binding label
     * @param desc help description
     * @return binding option
     */
    public static BindingOption withHelp(String key, String desc) {
        return binding -> binding.help = new Help(key, desc);
    }

    /**
     * Returns whether any enabled binding matches the key press.
     *
     * @param keyPressMessage key press message
     * @param bindings bindings to check
     * @return true when a key matches
     */
    public static boolean matches(KeyPressMessage keyPressMessage, Binding... bindings) {
        for (Binding binding : bindings) {
            if (!binding.isEnabled()) {
                continue;
            }
            if (binding.matches(keyPressMessage)) {
                return true;
            }
        }
        return false;
    }
}
