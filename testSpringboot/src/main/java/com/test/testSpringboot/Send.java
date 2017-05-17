package com.test.testSpringboot;

import java.util.UUID;  

import org.springframework.amqp.rabbit.core.RabbitTemplate;  
import org.springframework.amqp.rabbit.support.CorrelationData;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Component;  
  
/**  
 * 消息生产者  
 *   
 *  
 */  
@Component  
public class Send implements RabbitTemplate.ConfirmCallback {  
  
    private RabbitTemplate rabbitTemplate;  
  
    /**  
     * 依賴注入  
     */  
    @Autowired  
    public Send(RabbitTemplate rabbitTemplate) {  
        this.rabbitTemplate = rabbitTemplate;  
        rabbitTemplate.setConfirmCallback(this); //rabbitTemplate如果為單例的話，那回調就是最後設置的內容
    }  
  
    public void sendMsg(String content) {  
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());  
        //exchange:交換機名稱routingKey:路由關鍵字object:發送的消息內容 correlationData:消息ID
        System.out.println(content);
        System.out.println(correlationId);
        System.out.println(AmqpConfig.EXCHANGE);
        System.out.println(AmqpConfig.ROUTINGKEY);
        rabbitTemplate.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTINGKEY, content, correlationId);  
    }  
  
    /**  
     * callbackfunction 
     */  
    @Override  
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {  
        System.out.println(" 回調id:" + correlationData);  
        if (ack) {  
            System.out.println("消息成功消費");  
        } else {  
            System.out.println("消息消費失敗:" + cause);  
        }  
    }  
  
}  