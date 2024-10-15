package io.github.jy2694.adSync.entity.transaction;

import io.github.jy2694.adSync.AdSync;
import io.github.jy2694.adSync.event.TransactionCompletedEvent;
import org.bukkit.Bukkit;

public class ReleaseTransaction extends Transaction{
    protected ReleaseTransaction(ReleaseTransactionBuilder builder) {
        super(builder.getRequireEntities());
        super.task = () -> {
            if(result != TransactionResult.NOT_EXECUTED) return;
            for(String[] entity : requireEntities){
                String entityId = entity[0];
                String key = entity[1];
                AdSync.getInstance().getDatabaseConnection().releaseObject(entityId, key);
            }
            result = TransactionResult.RELEASED;
            Bukkit.getPluginManager().callEvent(new TransactionCompletedEvent(this));
        };
    }
}
