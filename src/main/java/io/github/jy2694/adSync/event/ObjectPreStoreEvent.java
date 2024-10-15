package io.github.jy2694.adSync.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered when an attempt is made to store an object to the database.
 */
@Getter
public class ObjectPreStoreEvent extends Event {
    @Getter
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final String entityId;
    private final String key;

    public ObjectPreStoreEvent(String entityId, String key) {
        this.entityId = entityId;
        this.key = key;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
