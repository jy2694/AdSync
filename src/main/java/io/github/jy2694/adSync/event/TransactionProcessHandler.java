package io.github.jy2694.adSync.event;

import io.github.jy2694.adSync.entity.transaction.Transaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransactionProcessHandler implements Listener {
    public static Queue<Transaction> transactionProcessQueue = new ConcurrentLinkedQueue<>();

    @EventHandler
    public void onTransactionCompleted(TransactionCompletedEvent event){
        transactionProcessQueue.remove(event.getTransaction());
        Transaction next = transactionProcessQueue.peek();
        if(next == null) return;
        next.getTask().run();
    }

    @EventHandler
    public void onTransactionQueued(TransactionQueuedEvent event){
        Transaction next = transactionProcessQueue.peek();
        if(next == null) return;
        if(!next.equals(event.getTransaction())) return;
        event.getTransaction().getTask().run();
    }
}
