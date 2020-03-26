package com.botech.disruptor.performance;

import com.botech.disruptor.consumer.*;
import com.botech.disruptor.dto.LogEvent;
import com.botech.disruptor.factory.LogEventFactory;
import com.botech.disruptor.producer.LogEventProducer;
import com.botech.disruptor.producer.LogEventProducer2;
import com.botech.disruptor.producer.LogEventProducer3;
import com.botech.disruptor.producer.LogEventProducerWithTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.Executors;

/**
 * @author yurg
 * @version 1.0
 * @date 2020/3/26 10:17
 * @description :测试 Disruptor --> link:https://www.iteye.com/blog/357029540-2395677
 */
@SpringBootTest(classes = LogEventMain.class)
@RunWith(SpringRunner.class)
public class LogEventMain {

    /**
     * 单个生产者和消费者的模式
     *
     * @throws InterruptedException
     */
    @Test
    public void producer() throws InterruptedException {
        LogEventFactory logEventFactory = new LogEventFactory();
        //用于生成RingBuffer大小,其大小必须是2的n次方  
        int ringBufferSize = 8;
        //定义Disruptor初始化信息  
        Disruptor<LogEvent> disruptor = new Disruptor<LogEvent>(logEventFactory, ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());
        //定义处理事件的消费者  
        disruptor.handleEventsWith(new LogEventConsumer());
        //定义事件的开始  
        disruptor.start();

        RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
        //进行事件的发布  
        LogEventProducer logEventProducer = new LogEventProducer(ringBuffer);
        for (int i = 0; i < 10; i++) {
            logEventProducer.onData(i, "c" + i, new Date());
        }
        Thread.sleep(1000);
        //关闭Disruptor  
        disruptor.shutdown();
    }

    /**
     * 使用EventTranslatorVararg的单个生产者和消费者模式
     *
     * @throws InterruptedException
     */
    @Test
    public void producerWithTranslator() throws InterruptedException {
        LogEventFactory logEventFactory = new LogEventFactory();
        //用于生成RingBuffer大小,其大小必须是2的n次方  
        int ringBufferSize = 8;
        //定义Disruptor初始化信息  
        Disruptor<LogEvent> disruptor = new Disruptor<LogEvent>(logEventFactory, ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());
        //定义处理事件的消费者  
        disruptor.handleEventsWith(new LogEventConsumer());
        //定义事件的开始  
        disruptor.start();

        RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
        //进行事件的发布  
        LogEventProducerWithTranslator producerWithTranslator = new LogEventProducerWithTranslator(ringBuffer);
        for (int i = 0; i < 10; i++) {
            producerWithTranslator.onData(i, "c" + i, new Date());
        }
        Thread.sleep(1000);
        //关闭Disruptor  
        disruptor.shutdown();
    }

    /**
     * 一个生产者，3个消费者，其中前面2个消费者完成后第3个消费者才可以消费
     * 即当前面2个消费者把所有的RingBuffer占领完成，同时都消费完成后才会有第3个消费者的消费
     * 当发布的事件数量大于RingBuffer的大小的时候，在第3个消费者消费完RingBuffer大小的时候前面2个消费者才能继续消费，序号递增的
     *
     * @throws InterruptedException
     */
    @Test
    public void multiConsumer() throws InterruptedException {
        LogEventFactory logEventFactory = new LogEventFactory();
        //用于生成RingBuffer大小，其大小必须是2的n次方  
        int ringBufferSize = 8;
        //定义Disruptor初始化信息  
        Disruptor<LogEvent> disruptor = new Disruptor<LogEvent>(logEventFactory, ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());

        //设置多个消费者  
        EventHandlerGroup<LogEvent> eventEventHandlerGroup = disruptor.handleEventsWith(new LogEventConsumer(), new LogEventConsumer2());
        eventEventHandlerGroup.then(new LogEventConsumer3());
        //启动事件的开始  
        disruptor.start();
        RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
        //进行事件的发布  
        LogEventProducerWithTranslator producerWithTranslator = new LogEventProducerWithTranslator(ringBuffer);
        for (int i = 0; i < 10; i++) {
            producerWithTranslator.onData(i, "c" + i, new Date());
        }
        Thread.sleep(1000);
        //关闭Disruptor  
        disruptor.shutdown();
    }

    /**
     * 一个生产者，多个消费者，有2条支线，其中消费者1和消费者3在同一条支线上，
     * 消费者2和消费者4在同一条支线上，消费者5是消费者3和消费者4的终点消费者
     * 这样的消费将会在消费者1和消费者2把所有的RingBuffer大小消费完成后才会执行消费者3和消费者4
     * 在消费者3和消费者4把RingBuffer大小消费完成后才会执行消费者5
     * 消费者5消费完RingBuffer大小后又按照上面的顺序来消费
     * 如果剩余的生产数据比RingBuffer小，那么还是要依照顺序来
     *
     * @throws InterruptedException
     */
    @Test
    public void multiConsumers() throws InterruptedException {
        LogEventFactory logEventFactory = new LogEventFactory();
        //用于生成RingBuffer大小，其大小必须是2的n次方  
        int ringBufferSize = 8;
        //定义Disruptor初始化信息  
        Disruptor<LogEvent> disruptor = new Disruptor<LogEvent>(logEventFactory, ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());
        LogEventConsumer consumer1 = new LogEventConsumer();
        LogEventConsumer2 consumer2 = new LogEventConsumer2();
        LogEventConsumer3 consumer3 = new LogEventConsumer3();
        LogEventConsumer4 consumer4 = new LogEventConsumer4();
        LogEventConsumer5 consumer5 = new LogEventConsumer5();
        //同时执行消费者1和消费者2  
        disruptor.handleEventsWith(consumer1, consumer2);
        //消费者1后面执行消费者3  
        disruptor.after(consumer1).handleEventsWith(consumer3);
        //消费者后面执行消费者4  
        disruptor.after(consumer2).handleEventsWith(consumer4);
        //消费者3和消费者3执行完后执行消费者5  
        disruptor.after(consumer3, consumer4).handleEventsWith(consumer5);
        //定义事件的开始  
        disruptor.start();

        RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
        //进行事件的发布  
        LogEventProducer logEventProducer = new LogEventProducer(ringBuffer);
        for (int i = 0; i < 10; i++) {
            logEventProducer.onData(i, "c" + i, new Date());
        }
        Thread.sleep(1000);
        //关闭Disruptor  
        disruptor.shutdown();
    }

    /**
     * 多个生产者，多个消费者，有2条消费者支线，其中消费者1和消费者3在同一条支线上，
     * 消费者2和消费者4在同一条支线上，消费者5是消费者3和消费者4的终点消费者
     * 这样的消费将会在消费者1和消费者2把所有的RingBuffer大小消费完成后才会执行消费者3和消费者4
     * 在消费者3和消费者4把RingBuffer大小消费完成后才会执行消费者5
     * 消费者5消费完RingBuffer大小后又按照上面的顺序来消费
     * 如果剩余的生产数据比RingBuffer小，那么还是要依照顺序来
     * 生产者只是多生产了数据
     *
     * @throws InterruptedException
     */
    @Test
    public void multiProcedureConsumers() throws InterruptedException {
        LogEventFactory logEventFactory = new LogEventFactory();
        //用于生成RingBuffer大小，其大小必须是2的n次方  
        int ringBufferSize = 8;
        //定义Disruptor初始化信息  
        Disruptor<LogEvent> disruptor = new Disruptor<LogEvent>(logEventFactory, ringBufferSize, Executors.defaultThreadFactory(), ProducerType.MULTI, new YieldingWaitStrategy());
        LogEventConsumer consumer1 = new LogEventConsumer();
        LogEventConsumer2 consumer2 = new LogEventConsumer2();
        LogEventConsumer3 consumer3 = new LogEventConsumer3();
        LogEventConsumer4 consumer4 = new LogEventConsumer4();
        LogEventConsumer5 consumer5 = new LogEventConsumer5();
        //同时执行消费者1和消费者2  
        disruptor.handleEventsWith(consumer1, consumer2);
        //消费者1后面执行消费者3  
        disruptor.after(consumer1).handleEventsWith(consumer3);
        //消费者后面执行消费者4  
        disruptor.after(consumer2).handleEventsWith(consumer4);
        //消费者3和消费者3执行完后执行消费者5  
        disruptor.after(consumer3, consumer4).handleEventsWith(consumer5);
        //定义事件的开始  
        disruptor.start();

        RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
        //进行事件的发布  
        LogEventProducer logEventProducer = new LogEventProducer(ringBuffer);
        LogEventProducer2 logEventProducer2 = new LogEventProducer2(ringBuffer);
        LogEventProducer3 logEventProducer3 = new LogEventProducer3(ringBuffer);
        for (int i = 0; i < 10; i++) {
            logEventProducer.onData(i, "1-c" + i, new Date());
            logEventProducer2.onData(i, "2-c" + i, new Date());
            logEventProducer3.onData(i, "3-c" + i, new Date());
        }
        Thread.sleep(1000);
        //关闭Disruptor  
        disruptor.shutdown();
    }

}
