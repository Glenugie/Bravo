package com.bravo.model;

import com.bravo.utils.*;

public class User {
	private long userId;
	private String email;
	private String username;
	
	public User(long userId) {
		this.userId = userId;
		if (userId != -4) {
			email = (String) Mysql.queryTerm("email", "users", "WHERE userId='"+userId+"'");
			username = (String) Mysql.queryTerm("username", "users", "WHERE userId='"+userId+"'");
		} else {
			email = "";
			username = "";
		}
	}
	
	public long getId() {
		return userId;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return username;
	}
}
