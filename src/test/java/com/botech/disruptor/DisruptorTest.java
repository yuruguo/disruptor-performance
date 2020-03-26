package com.botech.disruptor;

import com.botech.disruptor.consumer.LogEventConsumer;
import com.botech.disruptor.dto.LogEvent;
import com.botech.disruptor.factory.LogEventFactory;
import com.botech.disruptor.producer.LogEventProducerWithTranslator;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.Date;

/**
 * @author yurg
 * @version 1.0
 * @date 2020/3/26 11:18
 * @description :
 */
public class DisruptorTest {

    //5000000:542,499,550,547,605,502,743,505,657,608     576
    //10000000:1252,1048,1031,1075,1022,1207,1056,1494,1118,1258   1156
    //50000000:5489,5125,5265,5609,5201,5482,4982,4891,5351,5758  5315
    public static void main(String[] args) {
        LogEventFactory factory = new LogEventFactory();
        int ringBufferSize = 65536;
        final Disruptor<LogEvent> disruptor = new Disruptor<LogEvent>(factory,
                ringBufferSize, DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, new BusySpinWaitStrategy());

        LogEventConsumer consumer = new LogEventConsumer();
        disruptor.handleEventsWith(consumer);
        disruptor.start();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
                //进行事件的发布
                LogEventProducer logEventProducer = new LogEventProducer(ringBuffer);
                for(int i = 0; i < BlockingQueueTest.eventNum; i++){
                    logEventProducer.onData(i, "c" + i, new Date());
                }
            }
        }).start();*/

        new Thread(new Runnable() {
            @Override
            public void run() {
                RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
                //进行事件的发布
                LogEventProducerWithTranslator producerWithTranslator = new LogEventProducerWithTranslator(ringBuffer);
                for (int i = 0; i < BlockingQueueTest.eventNum; i++) {
                    producerWithTranslator.onData(i, "c" + i, new Date());
                }
            }
        }).start();
        //disruptor.shutdown();
    }
}
