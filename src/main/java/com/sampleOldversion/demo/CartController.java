package com.sampleOldversion.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sampleOldversion.demo.service.CartService;

@RefreshScope
@RequestMapping("/cart")
@RestController
public class CartController {
	
	/*
	 * @Autowired private CartConfiguration cartconf;
	 */
	
	@Autowired
	private CartService cartService;
	
	@Value("${endpointsMap.categoryBreadcrumb}")
	private String endpointUrl;
	
	
	@GetMapping("cartsuccessFromService")
    public String cartSuccessmethodfromService() {
        System.out.println("Hello "+cartService.getcartMessage());
        return cartService.getcartMessage();
    }
	
	@GetMapping("endpointUrl")
    public String endpointUrl() {
        System.out.println("Hello endpointUrl "+endpointUrl);
        return endpointUrl;
    }
	
	
}
