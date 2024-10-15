package io.github.jy2694.adSync.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Event that is triggered when an object is released.
 */
@Getter
public class ObjectReleasedEvent extends Event {
    @Getter
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final UUID messageId;
    private final String entityId;
    private final String key;
    private final boolean selfMessage;

    public ObjectReleasedEvent(UUID messageId, String entityId, String key, boolean selfMessage) {
        this.messageId = messageId;
        this.entityId = entityId;
        this.key = key;
        this.selfMessage = selfMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
