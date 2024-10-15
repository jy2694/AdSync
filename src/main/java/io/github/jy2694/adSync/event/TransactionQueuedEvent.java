package io.github.jy2694.adSync.event;

import io.github.jy2694.adSync.entity.transaction.Transaction;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered when a transaction is queued for execution
 */
@Getter
public class TransactionQueuedEvent extends Event {
    @Getter
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Transaction transaction;

    public TransactionQueuedEvent(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
