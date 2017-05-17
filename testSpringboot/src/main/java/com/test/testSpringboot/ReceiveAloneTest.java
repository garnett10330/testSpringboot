package com.test.testSpringboot;


import java.io.IOException;  
import java.util.concurrent.TimeUnit;  
import java.util.concurrent.TimeoutException;  

import com.rabbitmq.client.Channel;  
import com.rabbitmq.client.Connection;  
import com.rabbitmq.client.ConnectionFactory;  
import com.rabbitmq.client.ConsumerCancelledException;  
import com.rabbitmq.client.QueueingConsumer;  
import com.rabbitmq.client.QueueingConsumer.Delivery;  
import com.rabbitmq.client.ShutdownSignalException;  

/**  
* 客户端01  
消息消费者
操作步骤：
創建工廠ConnectionFactory
獲取連接Connection
通過連接獲取通信通道Channel
宣告交換機Exchange：交換機類型分為四類：
FanoutExchange: 將消息分發到所有的綁定隊列，無routingkey的概念
HeadersExchange ：通過添加屬性key-value匹配
DirectExchange:按照routingkey分發到指定隊列
TopicExchange:多關鍵字匹配
宣告Queue
將隊列和交換機綁定
創建消費者
執行消息的消費
*/  
public class ReceiveAloneTest {  
	private final static String QUEUE_NAME = "queue-01";
  public static void main(String[] args) throws IOException, TimeoutException, ShutdownSignalException,  
                                        ConsumerCancelledException, InterruptedException {  
      ConnectionFactory facotry = new ConnectionFactory();  
      facotry.setUsername("test");  
      facotry.setPassword("test");  
      facotry.setVirtualHost("/");  
      facotry.setHost("localhost");  
      facotry.setPort(5672);
      Connection conn = facotry.newConnection(); //取一個連結
      //創建Channel進行通信 
      Channel channel = conn.createChannel();  
      int prefetchCount = 1;  
      channel.basicQos(prefetchCount); //保證公平分發
      /*如果多個客戶端想共享帶有名稱的隊列請用以下寫法---------------------------------------------------------------*/
      /*一個類型為直接，且持久化，非自動刪除的交換器
              一個已知名稱，且持久化的，非私有，非自動刪除隊列*/
      boolean durable = true;  
      //設定交換機
      channel.exchangeDeclare(SendAloneTest.EXCHANGE_NAME, "direct", durable); //按照routingKey過濾  
      //設定訊息隊列
      String queueName = channel.queueDeclare(QUEUE_NAME, true, true, false, null).getQueue();  
      //將隊列和交換器綁定
      String routingKey = "lkl-0";  
      //隊列可以多次綁定，綁定不同的交換器或者路由key  
      channel.queueBind(queueName, SendAloneTest.EXCHANGE_NAME, routingKey);  

      //創建消費者  
      QueueingConsumer consumer = new QueueingConsumer(channel);  
        
      //將消費者和隊列關聯
      channel.basicConsume(queueName, false, consumer); // 設置為false必須手動確認消息消費
      //獲取消息

      System.out.println(" Wait message ....");  
      while (true) {  
    	  //獲取消息，如果沒有消息，這一步將會一直阻塞
          Delivery delivery = consumer.nextDelivery();  
          String msg = new String(delivery.getBody());  
          String key = delivery.getEnvelope().getRoutingKey();  

          System.out.println("  Received '" + key + "':'" + msg + "'");  
          System.out.println(" Handle message");  
          TimeUnit.SECONDS.sleep(3); //mock handle message  
        //確認消息，已經收到
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false); //確定該消息已成功收到
      }  

  }  
}  