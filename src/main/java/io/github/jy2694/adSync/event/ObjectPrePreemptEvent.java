package io.github.jy2694.adSync.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Event that is triggered when an attempt is made to preempt an object.
 */
@Getter
public class ObjectPrePreemptEvent extends Event {
    @Getter
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID messageId;
    private final String entityId;
    private final String key;
    private final boolean selfMessage;

    public ObjectPrePreemptEvent(UUID messageId, String entityId, String key, boolean selfMessage) {
        this.messageId = messageId;
        this.entityId = entityId;
        this.key = key;
        this.selfMessage = selfMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
