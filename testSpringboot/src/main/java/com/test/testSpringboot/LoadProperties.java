package com.test.testSpringboot;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class LoadProperties {
	private Logger logger = LoggerFactory.getLogger(AmqpConfig.class);
	 public String getFilePropertie(String propertie,String file) {
	    String returnPropertie = null;
	    Properties prop = new Properties();
	    Assert.notNull(file,"file is null & file為空值");
	    Assert.notNull(propertie,"propertie is null & propertie為空值");
		try {
			prop.load(LoadProperties.class.getClassLoader().getResourceAsStream(file.trim()));
			returnPropertie = prop.getProperty(propertie.trim());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
      return returnPropertie.trim();
	}
	 public static void main(String[] args) throws Exception {  
		 LoadProperties attr = new LoadProperties();
		 String propertie = "application.properties";
		 System.out.println(attr.getFilePropertie("application.properties","spring.rabbitmq.port"));
//		 Properties prop = new Properties();
//		 prop.load(LoadProperties.class.getClassLoader().getResourceAsStream("application.properties"));
//		 String	returnPropertie = prop.getProperty("spring.rabbitmq.port");
//		 System.out.println(returnPropertie);
	 }
}
