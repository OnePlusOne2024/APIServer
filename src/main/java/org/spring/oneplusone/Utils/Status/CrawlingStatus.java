package org.spring.oneplusone.Utils.Status;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
public class CrawlingStatus {
    private boolean isCrawling = false;//crawling진행 여부
    private final ReentrantLock lock = new ReentrantLock();
    public boolean isCrawling(){
        return isCrawling;//setter
    }
    public void startCrawling(){
        lock.lock();
        try{
            isCrawling = true;
        } finally {
            lock.unlock();
        }
    }
    public void stopCrawling(){
        lock.lock();
        try{
            isCrawling = false;
        } finally{
            lock.unlock();
        }
    }
}
