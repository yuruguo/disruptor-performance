package com.botech.disruptor.producer;

import com.botech.disruptor.dto.LogEvent;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;

import java.util.Date;

/**
 * @author yurg
 * @version 1.0
 * @date 2020/3/26 10:01
 * @description :使用translator方式到事件生产者发布事件,通常使用该方法
 */

public class LogEventProducerWithTranslator {

    private EventTranslatorVararg eventTranslatorVararg = new EventTranslatorVararg<LogEvent>() {
        public void translateTo(LogEvent logEvent, long l, Object... objects) {
            logEvent.setLogId((Long) objects[0]);
            logEvent.setContent((String) objects[1]);
            logEvent.setDate((Date) objects[2]);
        }
    };

    private RingBuffer<LogEvent> ringBuffer;

    public LogEventProducerWithTranslator(RingBuffer<LogEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(long logId, String content, Date date) {
        ringBuffer.publishEvent(eventTranslatorVararg, logId, content, date);
    }
}