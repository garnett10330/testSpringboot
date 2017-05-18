package com.test.testSpringboot;



import java.util.Properties;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.context.annotation.Scope;

import com.rabbitmq.client.Channel; 
@Configuration  
public class AmqpConfig {
	public static final String EXCHANGE   = "spring-boot-exchange";  
    public static final String ROUTINGKEY = "spring-boot-routingKey";  
    public static final String QUEUENAME = "spring-boot-queue0517";
    @Autowired
    LoadProperties propertAttr;
    @Bean  
    public ConnectionFactory connectionFactory() {  
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();  
       // connectionFactory.setAddresses("127.0.0.1:15672");
        String propertie = "application.properties";
        System.out.println(Integer.parseInt(propertAttr.getFilePropertie("spring.rabbitmq.port", propertie)));
        connectionFactory.setHost(propertAttr.getFilePropertie("spring.rabbitmq.host", propertie));
        connectionFactory.setPort(Integer.parseInt(propertAttr.getFilePropertie("spring.rabbitmq.port", propertie)));
        connectionFactory.setUsername(propertAttr.getFilePropertie("spring.rabbitmq.username", propertie));  
        connectionFactory.setPassword(propertAttr.getFilePropertie("spring.rabbitmq.password", propertie));  
        connectionFactory.setVirtualHost(propertAttr.getFilePropertie("spring.rabbitmq.virtualHost", propertie));  
//        connectionFactory.setHost("localhost");
//        connectionFactory.setPort(5672);
//        connectionFactory.setUsername("wei");  
//        connectionFactory.setPassword("wei");  
//        connectionFactory.setVirtualHost("/");  
        connectionFactory.setPublisherConfirms(true); //必須要設置 才能進行消息的回調。
        return connectionFactory;  
    }  
    /** 通過使用RabbitTemplate來對開發者提供API操作 用來發送消息*/  
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
    /*Queue，構建隊列，名稱，是否持久化之類*/
    @Bean  
    public Queue queue() {  
        return new Queue(QUEUENAME, true); //隊列持久  
  
    }  
    /*
     * 绑定
     * 將DirectExchange與Queue進行綁定
     * */
    @Bean  
    public Binding binding() {  
        return BindingBuilder.bind(queue()).to(defaultExchange()).with(AmqpConfig.ROUTINGKEY);  
    }  
    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
    /*
     * 消费者
     * 需要將ACK修改為手動確認，避免消息在處理過程中發生異常或是dead letter時造成被誤認為已經成功接收的假象
     * */
    @Bean  
    public SimpleMessageListenerContainer messageContainer() {  
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());  
        container.setQueues(queue());  
        container.setExposeListenerChannel(true);  // 添加隊列信息
        container.setQueueNames(QUEUENAME);
        container.setMaxConcurrentConsumers(1);  
        container.setConcurrentConsumers(1);  // 設置並發消費者數量，默認為1
        container.setAutoDeclare(false);
        /*設置消費者成功消費消息後確認模式，分為兩種
          自動模式，默認模式，在RabbitMQ Broker消息發送到消費者後自動刪除
          手動模式，消費者客戶端顯示編碼確認消息消費完成，Broker給生產者發送回調，消息刪除
        */
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //設置確認模式手工確認  
        //container.setMessageListener(listenerAdapter);
        container.setMessageListener(new ChannelAwareMessageListener() {  
  
            @Override  
            public void onMessage(Message message, Channel channel) throws Exception {  
                byte[] body = message.getBody();  
                System.out.println("receive msg : " + new String(body));  
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //確認消息成功接收
            }  
        });  
        return container;  
    }  
}
