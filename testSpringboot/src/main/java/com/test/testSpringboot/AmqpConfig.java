package com.test.testSpringboot;



import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;  
import org.springframework.amqp.core.Binding;  
import org.springframework.amqp.core.BindingBuilder;  
import org.springframework.amqp.core.DirectExchange;  
import org.springframework.amqp.core.Message;  
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;  
import org.springframework.amqp.rabbit.connection.ConnectionFactory;  
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;  
import org.springframework.amqp.rabbit.core.RabbitTemplate;  
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.context.annotation.Scope;
import com.rabbitmq.client.Channel;
import com.test.testSpringboot.repostory.MessageErrorHandler;
@EnableRabbit
@Configuration  
public class AmqpConfig {
	private Logger logger = LoggerFactory.getLogger(AmqpConfig.class);
	/*MQ需要以下三種
	 * 生產端(發送)：即生產者到RabbitMQ的連接，以及消息的發送。
               消費端(接收)：即消費者對RabbitMQ的監聽以及消息的接受。
       RabbitMQ部分：即定義隊列，定義交換機，以及定義隊列和交換機的綁定。
	 * */
	public static final String EXCHANGE   = "spring-boot-exchange";  
    public static final String ROUTINGKEY = "spring-boot-routingKey";  
    public static final String QUEUENAME = "spring-boot-queue0517";
    @Autowired
    LoadProperties propertAttr;
    /*
     *  Returns are when the broker returns a message because it's undeliverable 
		(no matching bindings on the exchange to which the message was published, 
		and the mandatory bit is set).
		
		Confirms are when the broker sends an ack back to the publisher, 
		indicating that a message was successfully routed.
		confirm 主要是用來判斷消息是否有正確到達交換機，如果有，那麼就 ack 就返回 true；如果沒有，則是 false。
        return 則表示如果你的消息已經正確到達交換機，但是後續處理出錯了，那麼就會call back return，
                 並且把信息送回給你（前提是需要設置了Mandatory，不設置那麼就沒有任何作用；
                 但是如果消息沒有到達交換機，那麼不會call back return 的東西。
		*/
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
        connectionFactory.setPublisherConfirms(true); //必須要設置 才能進行消息的回調。
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;  
    }  
    /** 通過使用RabbitTemplate來對開發者提供API操作 用來發送消息*/  
    @Bean  
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  
    //必須是prototype類型 
    public RabbitTemplate rabbitTemplate() {  
        RabbitTemplate template = new RabbitTemplate(connectionFactory());  
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setMandatory(true);//一定要設定,不然會無法接收ReturnCallback
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            	logger.debug("----------------------------------");
            	logger.debug("correlationData = " + correlationData);
            	logger.debug("ack = " + ack);
            	logger.debug("cause = " + cause);
            	logger.debug("----------------------------------");

            }
        });

        template.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                logger.debug("----------------------------------");
            	logger.debug("message = " + message);
            	logger.debug("replyCode = " + replyCode);
            	logger.debug("replyText = " + replyText);
            	logger.debug("exchange = " + exchange);
            	logger.debug("routingKey = " + routingKey);
            	logger.debug("----------------------------------");
            }
        });
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
    /*死信業務隊列*/ 
    @Bean
    public Queue mailQueue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("x-dead-letter-exchange", "dead_letter_exchange");//设置死信交换机
        map.put("x-dead-letter-routing-key", "mail_queue_fail");//设置死信routingKey
        Queue queue = new Queue("testDeadQueue",true, false, false, map);
        return queue;
    }
    /*死信業務交換器*/
    @Bean
    public DirectExchange mailExchange() {
        return new DirectExchange("mailExchange", true, false);
    }
    /*綁定業務隊列和交換機，指定routingKey*/
    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue()).to(mailExchange())
                .with("mailRoutingKey");
    }
    /*死信列隊*/
    @Bean
    public Queue deadQueue(){
        Queue queue = new Queue("dead", true);
        return queue;
    }
    /*
     * 绑定死信列隊
     * 將DirectExchange與Queue進行綁定
     * */
    @Bean  
    public Binding deadBinding() {  
        return BindingBuilder.bind(deadQueue()).to(mailExchange()).with("mailRoutingKey");  
    }  
    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
    /*
     * 消费者
     * 需要將ACK修改為手動確認，避免消息在處理過程中發生異常或是dead letter時造成被誤認為已經成功接收的假象
     * */
//    @Bean  
//    public SimpleMessageListenerContainer messageContainer(MessageListenerAdapter listenerAdapter) {  
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());  
//        container.setQueues(queue());  
//        container.setExposeListenerChannel(true);  // 添加隊列信息
//        container.setQueueNames(QUEUENAME);
//        /*設置消費者成功消費消息後確認模式，分為兩種
//          自動模式，默認模式，在RabbitMQ Broker消息發送到消費者後自動刪除
//          手動模式，消費者客戶端顯示編碼確認消息消費完成，Broker給生產者發送回調，消息刪除
//        */
//        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //設置確認模式手工確認  
//        //container.setMessageListener(listenerAdapter);
//        container.setMessageListener(new ChannelAwareMessageListener() {  
//  
//            @Override  
//            public void onMessage(Message message, Channel channel) throws Exception {  
//                byte[] body = message.getBody();  
//                System.out.println("receive msg : " + new String(body));  
//                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //確認消息成功接收
//            }  
//        });  
//        return container;  
//    } 
    
    @Bean
    SimpleMessageListenerContainer container(Receiver messageListener,MessageErrorHandler errorHandler) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueueNames("queueTest");
        container.setMessageListener(messageListener);
        container.setRecoveryInterval(10000);
        container.setReceiveTimeout(1000);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //設置確認模式手工確認  
        /*
         * DefaultRequeueRejected如果被設置成false，表示當出現異常時不會將消息保留在當前Queue中，
         * 如果設置為true(默認值)，表示出錯後會將消息保留在當前Queue中，並且應用程序會不停地讀取消息，
         * 應根據實際需求處理，通常可以採用Dead Letter(死信)的方式處理*/
        container.setDefaultRequeueRejected(true);
        container.setErrorHandler(errorHandler);
        return container;
    }    
    
}
