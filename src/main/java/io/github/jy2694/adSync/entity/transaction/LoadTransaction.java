package io.github.jy2694.adSync.entity.transaction;

import io.github.jy2694.adSync.AdSync;
import io.github.jy2694.adSync.exception.NotPreemptedException;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadTransaction extends Transaction{
    private final Map<String, Object> loadedObjects = new ConcurrentHashMap<>();
    protected LoadTransaction(LoadTransactionBuilder builder) {
        super(builder.getRequireEntities());
        super.task = () -> Bukkit.getScheduler().runTaskAsynchronously(AdSync.getInstance(), () -> {
            try{
                for(String[] entity : requireEntities){
                    String entityId = entity[0];
                    String key = entity[1];
                    loadedObjects.put(entityId + ":" + key, AdSync.getInstance().getDatabaseConnection().loadObject(entityId, key));
                    result = TransactionResult.PARTIALLY_LOADED;
                }
                result = TransactionResult.LOADED;
            } catch(NotPreemptedException e){
                result = TransactionResult.FAILED;
                loadedObjects.clear();
            }
        });
    }

    public Object getLoadedObject(String entityId, String key){
        return loadedObjects.get(entityId + ":" + key);
    }
}
