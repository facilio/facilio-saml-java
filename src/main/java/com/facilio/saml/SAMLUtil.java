package com.facilio.saml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
	
	private static final SimpleDateFormat SAML_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final SimpleDateFormat SAML_DATE_FORMAT_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	public static Date parseDate(String dateTime) throws Exception {
		SAML_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		SAML_DATE_FORMAT_MILLIS.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		Date parsedData = null;
		try {
			parsedData = SAML_DATE_FORMAT.parse(dateTime);
		} catch(Exception e) {
			return SAML_DATE_FORMAT_MILLIS.parse(dateTime);
		}
		return parsedData;
	}
	
	public static String formatDate(Date date) throws Exception {
		SAML_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		SAML_DATE_FORMAT_MILLIS.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		return SAML_DATE_FORMAT.format(date);
	}
	
	public static X509Certificate loadCertificate(String certificate) throws Exception {
		ByteArrayInputStream bis = null;
		try {
			certificate = formatCert(certificate, true);
			
			bis = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
		
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(bis);
		}
		catch (Exception e) {
			throw new Exception("Couldn't load public key", e);
		}
		finally {
			if (bis != null) {
				bis.close();
			}
		}
	}
	
	public static String formatCert(String cert, Boolean heads) {
		String x509cert = "";

		if (cert != null) {
			x509cert = cert.replace("\\x0D", "").replace("\r", "").replace("\n", "").replace(" ", "");

			if (!x509cert.trim().isEmpty()) {
				x509cert = x509cert.replace("-----BEGINCERTIFICATE-----", "").replace("-----ENDCERTIFICATE-----", "");

				if (heads) {
					x509cert = "-----BEGIN CERTIFICATE-----\n" + chunkString(x509cert, 64) + "-----END CERTIFICATE-----";
				}
			}
		}
		return x509cert;
	}
	
	private static String chunkString(String str, int chunkSize) {
		String newStr = "";
		int stringLength = str.length();
		for (int i = 0; i < stringLength; i += chunkSize) {
			if (i + chunkSize > stringLength) {
				chunkSize = stringLength - i;
			}
			newStr += str.substring(i, chunkSize + i) + '\n';
		}
		return newStr;
	}
}
