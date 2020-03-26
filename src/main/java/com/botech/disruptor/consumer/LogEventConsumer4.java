package com.botech.disruptor.consumer;

import com.botech.disruptor.dto.LogEvent;
import com.lmax.disruptor.EventHandler;

/**
 * @author yurg
 * @version 1.0
 * @date 2020/3/26 10:12
 * @description :自定义消费者
 */
public class LogEventConsumer4 implements EventHandler<LogEvent> {
    public void onEvent(LogEvent logEvent, long l, boolean b) throws Exception {
        System.out.println("消费者4-seq:" + l + ",bool:" + b + ",logEvent:" + logEvent.toString());
    }
}
