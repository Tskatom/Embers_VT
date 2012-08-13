package org.vt.entity;

import java.util.LinkedList;

import org.vt.datapreprocess.GetMemberCompany.Company;


public class StcokIndex {
	private String indexName;
	private LinkedList<Company> companies;
	
	public StcokIndex(String indexName)
	{
		this.indexName = indexName;
		companies = new LinkedList<Company>();
	}
	
	public void add(Company company)
	{
		companies.add(company);
	}

}
