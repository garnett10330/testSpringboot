package com.test.testSpringboot;


import java.util.concurrent.CountDownLatch;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message; 
import com.rabbitmq.client.Channel; 
@Component
public class Receiver implements ChannelAwareMessageListener{

	private Logger logger = LoggerFactory.getLogger(Receiver.class);
    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    @Override  
     public void onMessage(Message message, Channel channel) throws Exception {  
      byte[] body = message.getBody();  
      String bodyContent = new String(message.getBody());
      System.out.println("receive msg : " + bodyContent);
      logger.info("receive msg : " + bodyContent);
     // messageRepository.save(new ws.message.entity.Message(bodyContent)); 原意是要模擬存DB 但h2有點問題
      if(bodyContent.equals("error")) {
    	  throw new RuntimeException("============Simulate Error\n\n");
      }
      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //確認消息成功接收
  }

	
 

}
