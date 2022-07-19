package com.sampleOldversion.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class CartServiceImpl implements CartService {

	@Value("${cart.success}")
	private String cartMessage;
	
	public String getcartMessage() {
		System.out.println("The cart message from service");
		return cartMessage;
		
	}
}
