package com.facilio.saml;

import java.util.Locale;
import java.util.Map;

public class Account {
	
	private User user;
	private Organization org;
	
	public Account(User usr, Organization org) {
		this.user = usr;
		this.org = org;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public Organization getOrg() {
		return this.org;
	}
	
	public static Account getAccount(String email, Map<String, String> userData) {
		if (email == null || userData == null) {
			return null;
		}
		
		Long userId = null;
		String name = null;
		String lang = null;
		String country = null;
		String role = null;
		String timezone = null;
		
		Long orgId = null;
		String orgName = null;
		String orgDomain = null;
		String orgTimezone = null;
		
		if (userData.containsKey("USER_ID")) {
			userId = Long.parseLong(userData.get("USER_ID"));
		}
		if (userData.containsKey("NAME")) {
			name = userData.get("NAME");
		}
		if (userData.containsKey("LANGUAGE")) {
			lang = userData.get("LANGUAGE");
		}
		if (userData.containsKey("COUNTRY")) {
			country = userData.get("COUNTRY");
		}
		if (userData.containsKey("ROLE")) {
			role = userData.get("ROLE");
		}
		if (userData.containsKey("TIMEZONE")) {
			timezone = userData.get("TIMEZONE");
		}
		
		if (userData.containsKey("ORG_ID")) {
			orgId = Long.parseLong(userData.get("ORG_ID"));
		}
		if (userData.containsKey("ORG_NAME")) {
			orgName = userData.get("ORG_NAME");
		}
		if (userData.containsKey("ORG_DOMAIN")) {
			orgDomain = userData.get("ORG_DOMAIN");
		}
		if (userData.containsKey("ORG_TIMEZONE")) {
			orgTimezone = userData.get("ORG_TIMEZONE");
		}
		
		User usr = new User().setEmail(email).setName(name).setUserId(userId).setLang(lang).setCountry(country).setTimezone(timezone).setRole(role);
		Organization org = new Organization().setOrgId(orgId).setName(orgName).setDomain(orgDomain).setTimezone(orgTimezone);
		
		return new Account(usr, org);
	}
	
	public static class User {
		private Long userId;
		private String name;
	   	private String email;
		private String lang;
		private String country;
		private Locale locale;
		private String timezone;
		private String role;
		
		public Long getUserId() {
			return userId;
		}
		public User setUserId(Long userId) {
			this.userId = userId;
			return this;
		}
		public String getName() {
			return name;
		}
		public User setName(String name) {
			this.name = name;
			return this;
		}
		public String getEmail() {
			return email;
		}
		public User setEmail(String email) {
			this.email = email;
			return this;
		}
		public String getLang() {
			return lang;
		}
		public User setLang(String lang) {
			this.lang = lang;
			return this;
		}
		public String getCountry() {
			return country;
		}
		public User setCountry(String country) {
			this.country = country;
			return this;
		}
		public Locale getLocale() {
			if (this.locale == null) {
				this.locale = new Locale(this.lang, this.country);
			}
			return this.locale;
		}
		public User setLocale(Locale locale) {
			this.locale = locale;
			return this;
		}
		public String getTimezone() {
			return timezone;
		}
		public User setTimezone(String timezone) {
			this.timezone = timezone;
			return this;
		}
		public String getRole() {
			return role;
		}
		public User setRole(String role) {
			this.role = role;
			return this;
		}
	}
	
	public static class Organization {
		private Long orgId;
		private String name;
		private String domain;
		private String timezone;
		
		public Long getOrgId() {
			return orgId;
		}
		public Organization setOrgId(Long orgId) {
			this.orgId = orgId;
			return this;
		}
		public String getName() {
			return name;
		}
		public Organization setName(String name) {
			this.name = name;
			return this;
		}
		public String getDomain() {
			return domain;
		}
		public Organization setDomain(String domain) {
			this.domain = domain;
			return this;
		}
		public String getTimezone() {
			return timezone;
		}
		public Organization setTimezone(String timezone) {
			this.timezone = timezone;
			return this;
		}
	}
}
