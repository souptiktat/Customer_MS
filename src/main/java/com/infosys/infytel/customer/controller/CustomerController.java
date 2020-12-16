package com.infosys.infytel.customer.controller;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.infosys.infytel.customer.dto.CustomerDTO;
import com.infosys.infytel.customer.dto.LoginDTO;
import com.infosys.infytel.customer.dto.PlanDTO;
import com.infosys.infytel.customer.service.CustHystrixService;
import com.infosys.infytel.customer.service.CustomerService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@EnableAutoConfiguration
//@RibbonClient(name="custribbon")
public class CustomerController {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	CustomerService custService;
	
	//@Value("${friend.uri}")
	//String friendUri;
	
	//@Value("${path.uri}")
	//String planUri;
	
	//@Autowired
	//DiscoveryClient client;
	
	@Autowired
	RestTemplate template;
	
	//@Autowired 
	//CustHystrixService hystService;
	
	//@Autowired
	//CustPlanFeign planFeign;
	
	
	@RequestMapping(value = "/customers", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createCustomer(@RequestBody CustomerDTO custDTO) {
		//logger.info("Creation request for customer {}" , custDTO);
		custService.createCustomer(custDTO);
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean login(@RequestBody LoginDTO loginDTO) {
		//logger.info("Login request for Customer {} with password {}", loginDTO.getPhoneNo(),loginDTO.getPassword());
		return custService.login(loginDTO);
	}

	@HystrixCommand(fallbackMethod="getCustomerProfileFallback")
	@RequestMapping(value = "/customers/{phoneNo}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public CustomerDTO getCustomerProfile(@PathVariable Long phoneNo) throws InterruptedException, ExecutionException {
		
		long overAllStart = System.currentTimeMillis();
		logger.info("Profile request for customer {}" , phoneNo);
		System.out.println("========In Profile===============" + phoneNo);
		
		//List<ServiceInstance> friendInstances =client.getInstances("FRIENDFAMILYMS");
		//ServiceInstance friendInstance=friendInstances.get(0);
		//URI friendUri=friendInstance.getUri();
		//List<ServiceInstance> planInstances =client.getInstances("PLANMS");
		//ServiceInstance planInstance=planInstances.get(0);
		//URI planUri=planInstance.getUri();
		
		
		CustomerDTO custDTO = custService.getCustomerProfile(phoneNo);
		long planStart = System.currentTimeMillis();
		//PlanDTO planDTO = new RestTemplate().getForObject(planUri+"/plan/"+custDTO.getCurrentPlan().getPlanId(), PlanDTO.class);
		
		PlanDTO planDTO = template.getForObject("http://PLANMS"+"/plans/"+custDTO.getCurrentPlan().getPlanId(), PlanDTO.class);
		//PlanDTO planDTO = planFeign.getSpecificPlan(custDTO.getCurrentPlan().getPlanId());
		
		//Future<PlanDTO> planDTOFuture = hystService.getSpecificPlans(custDTO.getCurrentPlan().getPlanId());
		long planStop = System.currentTimeMillis();
		
		long friendStart = System.currentTimeMillis();
		@SuppressWarnings("unchecked")
		//List<Long> friends = template.getForObject("http://custribbon/customers/"+ phoneNo +"/friends", List.class);
		//List<Long> friends = new RestTemplate().getForObject(friendUri+ "/customers/" + phoneNo +"/friends", List.class);
		List<Long> friends = template.getForObject("http://FRIENDFAMILYMS"+ "/customers/" + phoneNo +"/friends", List.class);
		//Future<List<Long>> friendsFuture = hystService.getFriends(phoneNo);
		long friendStop = System.currentTimeMillis();
		
		long overAllStop = System.currentTimeMillis();
		custDTO.setCurrentPlan(planDTO);
		custDTO.setFriendAndFamily(friends);
		System.out.println("Total time for Plan " + (planStop-planStart));
		System.out.println("Total time for Friend " + (friendStop-friendStart));
		System.out.println("Total overall time for request " + (overAllStop-overAllStart));
		return custDTO;
	}
	
	public CustomerDTO getCustomerProfileFallback(Long phoneNo) {
		System.out.println("========In Fallback===============" + phoneNo);
		return new CustomerDTO();
	}
}
