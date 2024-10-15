package io.github.jy2694.adSync.entity.transaction;

import io.github.jy2694.adSync.AdSync;
import io.github.jy2694.adSync.event.TransactionCompletedEvent;
import io.github.jy2694.adSync.task.WaitTransactionExpireTask;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PreemptTransaction extends Transaction{

    public static final Map<UUID, PreemptTransaction> linkedTransactions = new ConcurrentHashMap<>();
    public static final Map<UUID, String> linkedEntityIds = new ConcurrentHashMap<>();
    public static final Map<UUID, String> linkedKeys = new ConcurrentHashMap<>();

    protected PreemptTransaction(PreemptTransactionBuilder builder) {
        super(builder.getRequireEntities());
        super.task = () -> {
            if(result != TransactionResult.NOT_EXECUTED) return;
            WaitTransactionExpireTask waitTask = new WaitTransactionExpireTask(this);
            waitTask.runTaskTimer(AdSync.getInstance(), 0, AdSync.getInstance().getWaitTimeout() * 20L);
            int waitRequire = 0;
            for(String[] entity : requireEntities){
                String entityId = entity[0];
                String key = entity[1];
                UUID messageId = AdSync.getInstance().getDatabaseConnection().preemptObject(entityId, key);
                if(messageId != null) {
                    linkedTransactions.put(messageId, this);
                    linkedEntityIds.put(messageId, entityId);
                    linkedKeys.put(messageId, key);
                    waitRequire++;
                }
            }
            if(waitRequire == 0) {
                result = TransactionResult.PREEMPTED;
                waitTask.cancel();
                Bukkit.getPluginManager().callEvent(new TransactionCompletedEvent(this));
            } else if(waitRequire < requireEntities.size()){
                result = TransactionResult.PARTIALLY_PREEMPTED;
            }
        };
    }

    public void cancel(){
        if(result == TransactionResult.PREEMPTED) return;
        List<UUID> messageIds = linkedTransactions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(this))
                .map(Map.Entry::getKey)
                .toList();
        for(UUID messageId : messageIds){
            String entityId = linkedEntityIds.get(messageId);
            String key = linkedKeys.get(messageId);
            linkedTransactions.remove(messageId);
            linkedEntityIds.remove(messageId);
            linkedKeys.remove(messageId);
            AdSync.getInstance().getDatabaseConnection().releaseObject(messageId, entityId, key);
        }
        result = TransactionResult.FAILED;
    }
}
