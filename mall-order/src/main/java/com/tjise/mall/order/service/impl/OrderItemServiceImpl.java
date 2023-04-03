package com.tjise.mall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.tjise.mall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjise.common.utils.PageUtils;
import com.tjise.common.utils.Query;

import com.tjise.mall.order.dao.OrderItemDao;
import com.tjise.mall.order.entity.OrderItemEntity;
import com.tjise.mall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queue：声明需要监听的所有队列
     *
     * org.springframework.amqp.core.Message
     * 参数可以写一下类型
     * 1、Message message 原生消息详细信息，头+体
     * 2、T<发送的消息的类型> OrderReturnReasonEntity content
     * 3、Channel channel:当前传输数据的通道
     *
     * Queue：可以很多人都来监听，只要收到消息，队列删除消息，而且只能有一个收到此消息
     *
     * 场景：
     *      1）订单服务启动多个：同一个消息，只能由一个客户端收到
     *      2）只有一个消息完全处理完，方法运行结束，才能接收到下一个消息
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void recieveMessgae(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) {
        //Body:'{"id":1,"name":"haha","sort":null,"status":null,"createTime":1680517185808}'
        byte[] body = message.getBody();
        //消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        System.out.println("接收到。。。。消息：" + message + "==>内容：" + content);
    }

}