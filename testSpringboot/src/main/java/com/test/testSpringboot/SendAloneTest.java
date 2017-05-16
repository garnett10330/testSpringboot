package com.test.testSpringboot;
import java.io.IOException;  
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.Channel;  
import com.rabbitmq.client.ConfirmListener;  
import com.rabbitmq.client.Connection;  
import com.rabbitmq.client.ConnectionFactory;  
import com.rabbitmq.client.MessageProperties;  
  
/**  
 * 消息publish  
 *  消息生產者
	操作步驟：
	創建連接工廠ConnectionFactory
	獲取連縣線Connection
	通過連接獲取通信通道Channel
	發送消息 
 */  
public class SendAloneTest {  
    public final static String EXCHANGE_NAME = "test-exchange";  
    @Autowired
    private  Send send;
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {  
        /**  
         * 配置amqp broker 連接信息
         */  
        ConnectionFactory facotry = new ConnectionFactory();  
        //不知為何寫了會錯QQ
        facotry.setUsername("guest");  
        facotry.setPassword("guest");  
        facotry.setVirtualHost("/");  
        facotry.setPort(5672);
        facotry.setHost("localhost");  
        Connection conn = facotry.newConnection(); //取一個連結
        //創建Channel進行通信
        //Channel可用來發送和接收消息
        Channel channel = conn.createChannel();  
  
        // channel.exchangeDeclare(Send.EXCHANGE_NAME, "direct", true); //如果消費者已創建，這裡可不聲明 
        //在設置消息被消費的回調前需顯示調用
        //否則callback函數無法調用 
        //先執行消費者，消費者會輪詢是否有消息的到來，在web控制也可以觀察，再啟動生產者發送消息。
        channel.confirmSelect(); //Enables publisher acknowledgements on this channel  
        channel.addConfirmListener(new ConfirmListener() {  
  
            @Override  
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {  
                System.out.println("[handleNack] :" + deliveryTag + "," + multiple);  
  
            }  
  
            @Override  
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {  
                System.out.println("[handleAck] :" + deliveryTag + "," + multiple);  
            }  
        });  
  
        String message = "lkl-";  
        //消息持久化 MessageProperties.PERSISTENT_TEXT_PLAIN  
        //發送多條信息，每條消息對應routekey都不一致  
        for (int i = 0; i < 10; i++) { 
        	//要向交換器中發布消息，使用Channel.basicPublish方法：
            channel.basicPublish(EXCHANGE_NAME, message + (i % 2), MessageProperties.PERSISTENT_TEXT_PLAIN,  
                (message + i).getBytes());  
            System.out.println("[send] msg " + (message + i) + " of routingKey is " + (message + (i % 2)));  
        }
//        Send send = new Send();
        //send.sendMsg("測試一號");
  
    }  
}  
