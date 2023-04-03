package com.tjise.mall.order;

import com.tjise.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("haha");
        //1、发送消息，如果发送的消息是对象，我们会使用序列化机制，将对象写出去，所以对象必须实现Serializable
        String msg = "Hello World";

        //2、发送的对象类型的消息，可以是JSON
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java", reasonEntity);
        log.info("消息发送完成{}", reasonEntity);
    }

    /**
     * 1、如何创建Exchange[hello-java-exchange]、Queue、Binging
     *      1）使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Test
    public void contextLoads() {

        //exchange
        /**
         * public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");

    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    @Test
    public void createBind() {
        //String destination【目的地】,
        // Binding.DestinationType destinationType【目的地类型】,
        // String exchange【交换机】,
        // String routingKey【】路由键,
        // Map<String, Object> arguments)【自定义参数】
        //将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello-java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("binding[{}]创建成功", "hello-java-binding");
    }

}
