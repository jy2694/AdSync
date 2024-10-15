package io.github.jy2694.adSync.entity.transaction;

import io.github.jy2694.adSync.AdSync;
import io.github.jy2694.adSync.exception.NotPreemptedException;
import org.bukkit.Bukkit;

import java.util.Map;

public class StoreTransaction extends Transaction{
    private final Map<String, Object> linkedObjects;
    protected StoreTransaction(StoreTransactionBuilder builder) {
        super(builder.getRequireEntities());
        this.linkedObjects = builder.getLinkedObjects();
        super.task = () -> Bukkit.getScheduler().runTaskAsynchronously(AdSync.getInstance(), ()->{
            for(String[] entity : requireEntities){
                String entityId = entity[0];
                String key = entity[1];
                Object object = linkedObjects.get(entityId + key);
                if(object != null) {
                    try {
                        AdSync.getInstance().getDatabaseConnection().storeObject(entityId, key, object);
                    } catch (NotPreemptedException e) {
                        result = TransactionResult.FAILED;
                        return;
                    }
                }
                result = TransactionResult.PARTIALLY_STORED;
            }
            result = TransactionResult.STORED;
        });
    }
}
