package org.spring.oneplusone.Utils.Status;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class CrawlingStatus {
    private final ConcurrentHashMap<String, Boolean> crawlingTasks = new ConcurrentHashMap<>();
    //crawling진행 여부
    private final ReentrantLock lock = new ReentrantLock();
    public boolean isCrawling(String taskName){
        //setter
        return crawlingTasks.getOrDefault(taskName, false);
    }
    public void startCrawling(String taskName){
        lock.lock();
        try{
            crawlingTasks.put(taskName, true);
        } finally {
            lock.unlock();
        }
    }
    public void stopCrawling(String taskName){
        lock.lock();
        try{
            crawlingTasks.put(taskName, false);
        } finally {
            lock.unlock();
        }
    }
}
