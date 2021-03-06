package de.wolfgangkronberg;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Prefetches items into a cache, where items remain in the cache as long as they either belong to a named group,
 * or have been accessed recently.
 * @param <K> type of the items' IDs
 * @param <V> type of the items themselves
 */
public class GroupedCacheLoader<K,V> {

    private final Function<K, V> provider;
    private final int lruEntries;
    private final MyThreadPoolExecutor executor;
    private final LinkedBlockingQueue<Runnable> taskQueue;

    private final Object lock = new Object();
    private final LinkedList<K> lruCache = new LinkedList<>();
    private final Map<K, Future<V>> cache = new HashMap<>();
    private final Map<String, Set<K>> groups = new HashMap<>();

    public GroupedCacheLoader(Function<K,V> provider, int numWorkerThreads, int lruEntries) {
        this.provider = provider;
        this.lruEntries = lruEntries;
        taskQueue = new LinkedBlockingQueue<>();
        executor = new MyThreadPoolExecutor(numWorkerThreads, taskQueue);
    }

    public void prefetch(String group, Collection<K> keys) {
        Set<K> newKeys = new LinkedHashSet<>(keys);
        Set<K> addedKeys = new LinkedHashSet<>(newKeys);
        synchronized (lock) {
            addedKeys.removeAll(cache.keySet());
            Set<K> outdatedKeys = new LinkedHashSet<>(groups.computeIfAbsent(group, (k) -> new HashSet<>()));
            outdatedKeys.removeAll(keys);
            groups.put(group, newKeys);
            outdatedKeys.forEach(this::removeUnreferenced);
            addedKeys.forEach((k) -> cache.put(k, queue(k)));
        }
    }

    public Future<V> get(K key) {
        synchronized (lock) {
            lruCache.remove(key);
            lruCache.addFirst(key);
            while (lruCache.size() > lruEntries) {
                removeUnreferencedInGroups(lruCache.removeLast());
            }
            Future<V> result = cache.computeIfAbsent(key, this::queue);
            moveTaskToFront(key);
            return result;
        }
    }

    // caller must synchronize on lock
    private void moveTaskToFront(K priorityKey) {
        ArrayList<Runnable> tasks = new ArrayList<>(taskQueue.size());
        taskQueue.drainTo(tasks);
        for (Runnable task : tasks) {
            MyFutureTask fTask = getMyFutureTask(task);
            if (priorityKey.equals(fTask.key)) {
                taskQueue.offer(task);
                for (Runnable task2 : tasks) {
                    if (!priorityKey.equals(getMyFutureTask(task2).key)) {
                        taskQueue.offer(task);
                    }
                }
                return;
            }
        }
        tasks.forEach(taskQueue::offer);
    }

    private MyFutureTask getMyFutureTask(Runnable r) {
        //noinspection unchecked
        return (MyFutureTask)r;
    }

    // caller must synchronize on lock
    private void removeFromTaskQueue(K key) {
        ArrayList<Runnable> tasks = new ArrayList<>(taskQueue.size());
        taskQueue.drainTo(tasks);
        for (Runnable task : tasks) {
            if (!key.equals(getMyFutureTask(task).key)) {
                taskQueue.offer(task);
            }
        }
    }

    // caller must synchronize on lock
    private void removeUnreferenced(K key) {
        if (!lruCache.contains(key)) {
            removeUnreferencedInGroups(key);
        }
    }

    // caller must synchronize on lock
    private void removeUnreferencedInGroups(K key) {
        for (Set<K> set : groups.values()) {
            if (set.contains(key)) {
                return;
            }
        }
        cache.remove(key);
        removeFromTaskQueue(key);
    }

    // caller must synchronize on lock
    private Future<V> queue(K key) {
        return executor.submit(new MyWorker(key));
    }

    private static class MyThreadFactory implements ThreadFactory {

        private final AtomicInteger num = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread result = new Thread(r, "CacheLoader-" + num.getAndIncrement());
            result.setDaemon(true);
            return result;
        }
    }

    private class MyThreadPoolExecutor extends ThreadPoolExecutor {

        MyThreadPoolExecutor(int poolSize, LinkedBlockingQueue<Runnable> taskQueue) {
            super(poolSize, poolSize, Long.MAX_VALUE, TimeUnit.NANOSECONDS, taskQueue, new MyThreadFactory());
            prestartAllCoreThreads();
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
            //noinspection unchecked
            return (FutureTask<T>)new MyFutureTask((Callable<V>)callable);
        }

    }

    private class MyFutureTask extends FutureTask<V> {

        private final K key;

        MyFutureTask(Callable<V> callable) {
            super(callable);
            MyWorker worker = (MyWorker)callable;
            key = worker.key;
        }

    }

    private class MyWorker implements Callable<V> {

        private final K key;

        MyWorker(K key) {
            this.key = key;
        }

        @Override
        public V call() {
            return provider.apply(key);
        }
    }

}
