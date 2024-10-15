package io.github.jy2694.adSync.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered when an object has been loaded from the database.
 */
@Getter
public class ObjectLoadedEvent extends Event {
    @Getter
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final String entityId;
    private final String key;
    private final Object loadedObject;

    public ObjectLoadedEvent(String entityId, String key, Object loaded) {
        this.entityId = entityId;
        this.key = key;
        this.loadedObject = loaded;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
