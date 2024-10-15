package io.github.jy2694.adSync.task;

import io.github.jy2694.adSync.entity.transaction.PreemptTransaction;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaitTransactionExpireTask extends BukkitRunnable {

    public static final Map<PreemptTransaction, BukkitRunnable> tasks = new ConcurrentHashMap<>();

    private final PreemptTransaction transaction;

    public WaitTransactionExpireTask(PreemptTransaction transaction) {
        this.transaction = transaction;
        tasks.put(transaction, this);
    }

    @Override
    public void run() {
        this.transaction.cancel();
        tasks.remove(transaction);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        tasks.remove(transaction);
        super.cancel();
    }
}
