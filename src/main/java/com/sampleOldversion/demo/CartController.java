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

	@Value("${cart.success:property from spring boot local for 1.5}")
	private String cartSucess;
	
	@Value("${cart.success1:property from spring boot local for 1.5}")
	private String cartSucess1;
	
	@Value("${cart.failure:property from spring boot local for 1.5}")
	private String cartFailure;
	
	@Value("${cart.failure1:property from spring boot local for 1.5}")
	private String cartFailure1;
	
	@Value("${success:property from spring boot local for 1.5}")
	private String success;
	
	@Value("${failure:property from spring boot local for 1.5}")
	private String failure;
	
	@Value("${success1:property from spring boot local for 1.5}")
    private String success1;
	
	@Value("${failure1:property from spring boot local for 1.5}")
	private String failure1;
	
	/*
	 * @GetMapping("conf/cartsuccess") public String cartSuccessmethod() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getCartSucess()); return cartconf.getCartSucess(); }
	 * 
	 * @GetMapping("conf/cartsuccess1") public String cartSuccessmethod1() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getCartSucess1()); return cartconf.getCartSucess1(); }
	 * 
	 * @GetMapping("conf/cartfailure") public String cartfailuremethod() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getCartFailure()); return cartconf.getCartFailure(); }
	 * 
	 * @GetMapping("conf/cartfailure1") public String cartfailuremethod1() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getFailure1()); return cartconf.getFailure1(); }
	 * 
	 * @GetMapping("conf/success") public String cartSuccessmethod3() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getSuccess()); return cartconf.getSuccess(); }
	 * 
	 * @GetMapping("conf/success1") public String cartSuccessmethod4() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getSuccess1()); return cartconf.getSuccess1(); }
	 * 
	 * @GetMapping("conf/failure") public String cartfailuremethod3() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getFailure()); return cartconf.getFailure(); }
	 * 
	 * @GetMapping("conf/failure1") public String cartfailuremethod4() {
	 * System.out.println("the controoler class config map 2 for 1.5->"+cartconf.
	 * getFailure1()); return cartconf.getFailure1(); }
	 * 
	 * //from controller
	 * 
	 * @GetMapping("cartsuccess") public String cartSuccessmethod11() {
	 * System.out.println("the controoler class config map 2 for 1.5 from contr->"
	 * +cartSucess); return cartconf.getCartSucess(); }
	 */
	
	@GetMapping("cartsuccess1")
    public String cartSuccessmethod12() {
        System.out.println("the controoler class config map 2 for 1.5- from contr >"+cartSucess1);
        return cartSucess1;
    }
	
	@GetMapping("cartfailure")
    public String cartfailuremethod11() {
        System.out.println("the controoler class config map 2 for 1.5- from contr>"+cartFailure);
        return cartFailure;
    }
	
	@GetMapping("cartfailure1")
    public String cartfailuremethod12() {
        System.out.println("the controoler class config map 2 for 1.5- from contr>"+cartFailure1);
        return cartFailure1;
    }
	
	@GetMapping("success")
    public String cartSuccessmethod13() {
        System.out.println("the controoler class config map 2 for 1.5- from contr>"+success);
        return success;
    }
	
	@GetMapping("success1")
    public String cartSuccessmethod14() {
        System.out.println("the controoler class config map 2 for 1.5- from contr>"+success1);
        return success1;
    }
	@GetMapping("failure")
    public String cartfailuremethod13() {
        System.out.println("the controoler class config map 2 for 1.5- from contr>"+failure);
        return failure;
    }
	
	@GetMapping("failure1")
    public String cartfailuremethod14() {
        System.out.println("the controoler class config map 2 for 1.5- from contr>"+failure1);
        return failure1;
    }
	
	@GetMapping("cartsuccessFromService")
    public String cartSuccessmethodfromService() {
        System.out.println("Hello "+cartService.getcartMessage());
        return cartService.getcartMessage();
    }
	
}
