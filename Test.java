package com.Spring;

import java.io.Serializable;

public enum Test implements Serializable {

	CHARGEBACK("chargeback");
	private String value;

	Test(String val){
		value = val;
	}
}
