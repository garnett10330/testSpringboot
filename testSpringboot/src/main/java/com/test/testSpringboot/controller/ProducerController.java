package com.test.testSpringboot.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.test.testSpringboot.Send;

@RestController
public class ProducerController {
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    Send send;
    @RequestMapping(value = "/test/{abc}",method = RequestMethod.GET)
    public String test(@PathVariable(value = "abc") String abc){

        //rabbitTemplate.convertAndSend("spring-boot-exchange","spring-boot", abc + " from RabbitMQ!");
    	send.sendMsg(abc);
        return  "abc";
    }
}