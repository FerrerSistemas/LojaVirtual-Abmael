package com.lojavirtualabmael.config;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.lojavirtualabmael.service.DBService;
import com.lojavirtualabmael.service.EmailService;
import com.lojavirtualabmael.service.MockEmailService;

@Configuration
@Profile("test")
public class TestConfig {
	@Autowired
	private DBService dbService;
	
	@Bean
	public boolean instantiateDatabase() throws ParseException {
		dbService.instanteateTestDatabase();
		
		return true;
	}
	
	@Bean
	public EmailService emailService(){
		return new MockEmailService();
	}
	

/*@Bean
	public EmailService emailService(){
		return new SmtpEmailService();
	}
*/
}
