package com.test.testSpringboot;

import org.springframework.amqp.core.AcknowledgeMode;  
import org.springframework.amqp.core.Binding;  
import org.springframework.amqp.core.BindingBuilder;  
import org.springframework.amqp.core.DirectExchange;  
import org.springframework.amqp.core.Message;  
import org.springframework.amqp.core.Queue;  
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;  
import org.springframework.amqp.rabbit.connection.ConnectionFactory;  
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;  
import org.springframework.amqp.rabbit.core.RabbitTemplate;  
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.context.annotation.Scope;  
import com.rabbitmq.client.Channel; 
@Configuration  
public class AmqpConfig {
	public static final String EXCHANGE   = "spring-boot-exchange";  
    public static final String ROUTINGKEY = "spring-boot-routingKey";  
  
    @Bean  
    public ConnectionFactory connectionFactory() {  
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();  
       // connectionFactory.setAddresses("127.0.0.1:15672");  
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");  
        connectionFactory.setPassword("guest ");  
        //connectionFactory.setVirtualHost("/");  
        connectionFactory.setPublisherConfirms(true); //必須要設置 才能進行消息的回調。
        return connectionFactory;  
    }  
    /** 通過使用RabbitTemplate來對開發者提供API操作*/  
    @Bean  
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  
    //必須是prototype類型 
    public RabbitTemplate rabbitTemplate() {  
        RabbitTemplate template = new RabbitTemplate(connectionFactory());  
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;  
    }  
  
    /**
     * 針對消費者配置
     * 設置交換機類型
     * 將隊列綁定到交換機
     *   FanoutExchange: 將消息分發到所有的綁定隊列，無routingkey的概念  
     *   HeadersExchange ：通過添加屬性key-value匹配
     *   DirectExchange:按照routingkey分發到指定隊列 
     *   TopicExchange:多關鍵字匹配
     */  
    @Bean  
    public DirectExchange defaultExchange() {  
        return new DirectExchange(EXCHANGE);  
    }  
  
    @Bean  
    public Queue queue() {  
        return new Queue("spring-boot-queue0517", true); //隊列持久  
  
    }  
    /*
     * 绑定
     * */
    @Bean  
    public Binding binding() {  
        return BindingBuilder.bind(queue()).to(defaultExchange()).with(AmqpConfig.ROUTINGKEY);  
    }  
  
    @Bean  
    public SimpleMessageListenerContainer messageContainer() {  
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());  
        container.setQueues(queue());  
        container.setExposeListenerChannel(true);  // 添加队列信息
        container.setMaxConcurrentConsumers(1);  
        container.setConcurrentConsumers(1);  // 设置并发消费者数量，默认情况为1
        container.setAutoDeclare(false);
        /*设置消费者成功消费消息后确认模式，分为两种
                     自动模式，默认模式，在RabbitMQ Broker消息发送到消费者后自动删除
                     手动模式，消费者客户端显示编码确认消息消费完成，Broker给生产者发送回调，消息删除
         */
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认  
        container.setMessageListener(new ChannelAwareMessageListener() {  
  
            @Override  
            public void onMessage(Message message, Channel channel) throws Exception {  
                byte[] body = message.getBody();  
                System.out.println("receive msg : " + new String(body));  
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //确认消息成功消费  
            }  
        });  
        return container;  
    }  
}
