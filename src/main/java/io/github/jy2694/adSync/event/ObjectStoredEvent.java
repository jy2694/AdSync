package io.github.jy2694.adSync.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered when an object is stored in the database.
 */
@Getter
public class ObjectStoredEvent extends Event {
    @Getter
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final String entityId;
    private final String key;
    private final Object stored;

    public ObjectStoredEvent(String entityId, String key, Object stored) {
        this.entityId = entityId;
        this.key = key;
        this.stored = stored;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
