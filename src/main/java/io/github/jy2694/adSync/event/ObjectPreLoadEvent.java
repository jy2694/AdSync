package io.github.jy2694.adSync.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered when an attempt is made to load an object from the database.
 */
@Getter
public class ObjectPreLoadEvent extends Event {
    @Getter
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final String entityId;
    private final String key;

    public ObjectPreLoadEvent(String entityId, String key) {
        this.entityId = entityId;
        this.key = key;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
