package com.salesmanager.core.business.catalog.product.model;

import java.util.ArrayList;
import java.util.List;

import com.salesmanager.core.business.common.model.EntityList;

public class ProductList extends EntityList {
	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7267292601646149482L;
	private List<Product> products = new ArrayList<Product>();
	private List<Object[]> objs;
	public List<Product> getProducts() {
		return products;
	}
	public void setProducts(List<Product> products) {
		this.products = products;
	}
	public List<Object[]> getObjs() {
		return objs;
	}
	public void setObjs(List<Object[]> objs) {
		this.objs = objs;
	}


}