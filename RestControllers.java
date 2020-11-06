package com.Spring;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestControllers {
	@Autowired
	UserRepository ur;

	@GetMapping("/User")
	public User getUser(HttpServletRequest req) {
		@SuppressWarnings("deprecation")
		Date d =new Date(Date.parse("2020-04-21 09:05:19.0")); 
		Date d2 = Calendar.getInstance().getTime();
		long days = TimeUnit.DAYS.convert(Math.abs(d2.getTime() - d.getTime())
				,TimeUnit.MICROSECONDS);
		 //2020-04-21 09:05:19.0 and Thu Jul 09 19:56:07 IST 2020 is : 0
		return new User();
	}

	@PostMapping("/BudgetCreation")
	public String budgetCreation(@RequestBody Map<String,String> userBudget, User usrObj) {
		String categories[] = {"Food","Housing","Lifestyle","Investment","Transport","Other"};
		boolean isCategoryMissing = false;
		if(userBudget!=null){
			for (String name : userBudget.keySet())
					if(!Arrays.asList(categories).contains(name)){
						isCategoryMissing = true;
						break;
					}
		}
		if(isCategoryMissing){
			return "Failed to create budget. Something went wrong";
		}
		//here will store received userBudget map in aws rds
		ur.createBudget(usrObj, userBudget);
		return "Budget created successfully";
	}
	
	@GetMapping("/viewBudget")
	public Map<String,String> viewBudget(@RequestBody int userId) {
		
		//retrieve user specific budget from aws rds and populate in userBudget
		Map<String,String> userBudget = ur.viewBudget(userId);
		return userBudget;
	}
	
	@PostMapping("/createSmartBudget")
	public Map<String,String> createSmartBudget(@RequestBody Map<String,String> smartBudget, User usrObj) {
		
		//here will store received smartBudget map in aws rds
		boolean isAdded = ur.addSmartBudget(usrObj, smartBudget);
		Map<String,String> viewSmartBudget = null;
		if(isAdded){
			//fetch stored smartBudget from aws dynamodb
			viewSmartBudget = ur.viewSmartBudget(usrObj.getUserid());
		}
		return viewSmartBudget;
	}
	
	/*@GetMapping("/viewSmartBudget")
	public Map<String,String> viewSmartBudget(@RequestBody int userId) {
		
		//retrieve user specific budget from aws Dynamo DB and populate in smartBudget
		Map<String,String> smartBudget = ur.viewSmartBudget(userId);
		return smartBudget;
	}*/
	
}
