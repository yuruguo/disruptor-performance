package com.botech.disruptor;

import com.botech.disruptor.dto.LogEvent;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author yurg
 * @version 1.0
 * @date 2020/3/26 11:17
 * @description :
 */
public class BlockingQueueTest {
    public static int eventNum = 50000000;

    //5000000:974,932,943,946,993,1073,1044,1018,1027,971  992
    //10000000:1845,1851,2433,2041,1789,1911,1953,2105,1862,1896   1969
    //50000000:9828,9595,9377,9273,9020,9450,9873,9994,8882,9695  9499
    public static void main(String[] args) {
        final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<LogEvent>(65536);
        final long startTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < eventNum) {
                    LogEvent logEvent = new LogEvent();
                    logEvent.setLogId(i);
                    logEvent.setContent("c" + i);
                    logEvent.setDate(new Date());
                    try {
                        queue.put(logEvent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int k = 0;
                while (k < eventNum) {
                    try {
                        queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    k++;
                }
                long endTime = System.currentTimeMillis();
                System.out.println("BlockingQueue 花费时间：" + (endTime - startTime) + "ms");
            }
        }).start();

    }
}