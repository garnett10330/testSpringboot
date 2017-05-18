package com.test.testSpringboot.repostory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import com.test.testSpringboot.Receiver;

@Component
public class MessageErrorHandler implements ErrorHandler {
	private Logger logger = LoggerFactory.getLogger(MessageErrorHandler.class);

    @Override
    public void handleError(Throwable t) {
    	logger.error("=======Error: %s\n\n", t.getMessage());
    }
}
