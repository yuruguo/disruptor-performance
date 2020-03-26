package com.botech.disruptor.factory;

import com.botech.disruptor.dto.LogEvent;
import com.lmax.disruptor.EventFactory;

/**
 * @author yurg
 * @version 1.0
 * @date 2020/3/26 9:49
 * @description :事件生成工厂,用来初始化预分配事件对象,即根据RingBuffer大小创建的实体对象
 */

public class LogEventFactory implements EventFactory<LogEvent> {

    @Override
    public LogEvent newInstance() {
        System.out.println("新建LogEvent数据.....");
        return new LogEvent();
    }
}
