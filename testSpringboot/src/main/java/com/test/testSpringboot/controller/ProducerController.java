package com.test.testSpringboot.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {
    /*
     * RabbitTemplate 是 spring 幫我們的一個封裝，
     * 我們使用該對象提供的方法即可發送消息。
     * 默認情況下 RabbitTemplate 連接本機的 5672 端口的 rabbitmq，
     * 如果需要連接其他地方，
     * 那麼我們可以自己重寫 RabbitTemplate 的生成，再注入使用即可。
     * */
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @RequestMapping(value = "/test/{abc}",method = RequestMethod.GET)
    public String test(@PathVariable(value = "abc") String abc){

        rabbitTemplate.convertAndSend("spring-boot-exchange","spring-boot", abc + " from RabbitMQ!");
        return  "abc";
    }
}