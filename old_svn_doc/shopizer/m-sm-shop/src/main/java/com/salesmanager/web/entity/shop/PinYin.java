package com.salesmanager.web.entity.shop;

import java.io.Serializable;
import java.util.List;

public class PinYin implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3329217859503038768L;
	String code;
	List<PinYinName> lists;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<PinYinName> getLists() {
		return lists;
	}
	public void setLists(List<PinYinName> lists) {
		this.lists = lists;
	}

}
