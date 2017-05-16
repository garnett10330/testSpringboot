package com.test.testSpringboot.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/springboot")
public class HelloWorldController {

    @RequestMapping(value = "/{name}")
    public String sayWorld(@PathVariable("name") String name) {
        return "Hello " + name;
    }
}