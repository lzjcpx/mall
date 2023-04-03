package com.tjise.mall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 使用rabbitmq
 * 1、引入了mqp场景，RabbitAutoConfiguration就会自动生效
 * 2、给容器中自动配置了 RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 3、给配置文件中配置spring-rabbitmq信息
 * 4、@EnableRabbit：@EnableXxx
 * 5、监听消息：使用@RabbitListener：必须有@EnableRabbit
 *      @RabbitListener：类+方法上
 *      @RabbitHandler：标在方法上
 */
@SpringBootApplication
@EnableRabbit
public class MallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }

}
