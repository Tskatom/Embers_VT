package org.vt.entity;

public class Company {
	private String companyName;
	private String stockTicker;
	private String parentIndex;
	
	public Company(String companyName,String stockTicker,String parentIndex)
	{
		this.companyName = companyName;
		this.stockTicker = stockTicker;
		this.parentIndex = parentIndex;
	}
}
