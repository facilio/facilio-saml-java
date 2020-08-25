package com.facilio.saml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SAMLFilter implements Filter
{
	private static final Logger LOGGER = Logger.getLogger(SAMLFilter.class.getName());
	
	private static String ENTITY_ID = "https://localhost:8080";
	
	private static String HOME_URL = "/home";
	
	private static String LOGIN_URL = "/login";
	
	private static String ACS_URL = "/acs";
	
	private static String IDP_METADATA = "idp_metadata.xml";
	
	private static String EXCLUDE_PATTERN = null;
	
	private static SAMLClient SAML_CLIENT = null;

	public void init(FilterConfig config) throws ServletException {
		if (config.getInitParameter("entity_id") != null) {
			ENTITY_ID = config.getInitParameter("entity_id");
		}
		
		if (config.getInitParameter("home_url") != null) {
			HOME_URL = config.getInitParameter("home_url");
		}
		
		if (config.getInitParameter("login_url") != null) {
			LOGIN_URL = config.getInitParameter("login_url");
		}
		
		if (config.getInitParameter("acs_url") != null) {
			ACS_URL = config.getInitParameter("acs_url");
		}
		
		if (config.getInitParameter("idp_metadata") != null) {
			IDP_METADATA = config.getInitParameter("idp_metadata");
		}
		
		if (config.getInitParameter("exclude") != null) {
			EXCLUDE_PATTERN = config.getInitParameter("exclude");
		}
		
		try {
			InputStream idpMetadataXml = SAMLFilter.class.getClassLoader().getResourceAsStream(IDP_METADATA);
			SAML_CLIENT = new SAMLClient(ENTITY_ID, ACS_URL, idpMetadataXml);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "SAML Initialize failed.... ", e);
		}
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try
		{
			HttpServletRequest hreq = (HttpServletRequest)request;
			HttpServletResponse resp = (HttpServletResponse)response;

			String reqURI = hreq.getRequestURI();
			if (ACS_URL.equalsIgnoreCase(reqURI) || LOGIN_URL.equalsIgnoreCase(reqURI) || (EXCLUDE_PATTERN != null && Pattern.matches(EXCLUDE_PATTERN, reqURI))) {
				chain.doFilter(request, response);
				return;
			}

			Account account = (Account) hreq.getSession().getAttribute(SAMLUtil.CURRENT_ACCOUNT);
			if (account != null) {
				SAMLUtil.setCurrentAccount(account);
			}
			else {
				String currentUrl = hreq.getRequestURI() + (hreq.getQueryString() != null ? "?" + hreq.getQueryString() : "");
				String loginUrl = getLoginUrl() + "?redirect=" + URLEncoder.encode(currentUrl, "UTF-8");
				resp.sendRedirect(loginUrl);
				return;
			}
			chain.doFilter(request, response);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			SAMLUtil.cleanCurrentAccount();
		}
	}
	
	public static SAMLClient getSamlClient() {
		return SAML_CLIENT;
	}
	
	public static String getHomeUrl() {
		return HOME_URL;
	}
	
	public static String getLoginUrl() {
		return LOGIN_URL;
	}
}
