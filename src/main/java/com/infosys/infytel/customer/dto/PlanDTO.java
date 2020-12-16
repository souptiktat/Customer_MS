package com.infosys.infytel.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PlanDTO {
	
	Integer planId;
	String planName;
	Integer nationalRate;
	Integer localRate;

}
