package com.facilio.saml;

public class SAMLUtil {

	private static ThreadLocal<Account> currentAccount = new ThreadLocal<Account>();
	
	public static void setCurrentAccount(Account account) throws Exception {
		currentAccount.set(account);
	}
	
	public static Account getCurrentAccount() {
		return currentAccount.get();
	}
	
	public static void cleanCurrentAccount() {
		currentAccount.remove();
	}
	
	public static final String CURRENT_ACCOUNT = "CURRENT_ACCOUNT";
}
