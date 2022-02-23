package com.java9s.designpattern.factory;

public class Sony implements Mobile{
	private int ramSize;
	private String processor;
	
	public Sony(int ramSize, String processor){
		this.ramSize = ramSize;
		this.processor = processor;
	}

	public int sound(int x){
	    return 0;
	}
}
