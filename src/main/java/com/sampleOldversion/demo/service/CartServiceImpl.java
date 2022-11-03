package com.sampleOldversion.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class CartServiceImpl implements CartService {

	@Value("${planogram.api.endpoint}")
	private String planogram;
	
	@Value("${password}")
	private String username;
	
	public String getPlanogram() {
		System.out.println("The cart message from service"+username);
		return planogram;
		
	}
}
