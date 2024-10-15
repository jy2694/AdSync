package io.github.jy2694.adSync.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Event that is triggered when an object has been preempted from the database.
 */
@Getter
public class ObjectPreemptedEvent extends Event {
    @Getter
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final UUID messageId;
    private final String entityId;
    private final String key;

    public ObjectPreemptedEvent(UUID messageId, String entityId, String key) {
        this.messageId = messageId;
        this.entityId = entityId;
        this.key = key;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
