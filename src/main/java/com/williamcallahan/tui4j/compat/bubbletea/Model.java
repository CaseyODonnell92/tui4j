package com.williamcallahan.tui4j.compat.bubbletea;

/**
 * Defines the Model contract for init, update, and view.
 * tui4j: src/main/java/com/williamcallahan/tui4j/compat/bubbletea/Model.java
 */
public interface Model {

    /**
     * Initializes the model.
     *
     * @return initial command
     */
    Command init();

    /**
     * Updates the model with a message.
     *
     * @param msg message to process
     * @return update result
     */
    UpdateResult<? extends Model> update(Message msg);

    /**
     * Renders the current view.
     *
     * @return rendered view
     */
    String view();
}
