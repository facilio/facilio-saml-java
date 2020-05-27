package com.facilio.saml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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

import com.coveo.saml.SamlClient;

public class SAMLFilter implements Filter
{
	private static final Logger LOGGER = Logger.getLogger(SAMLFilter.class.getName());
	
	private static String HOME_URL = "/home";
	
	private static String LOGIN_URL = "/login";
	
	private static String ACS_URL = "/acs";
	
	private static String IDP_METADATA = "idp_metadata.xml";
	
	private static String EXCLUDE_PATTERN = null;
	
	private static SamlClient SAML_CLIENT = null;

	public void init(FilterConfig config) throws ServletException {
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
			Reader reader = new InputStreamReader(SAMLFilter.class.getClassLoader().getResourceAsStream(IDP_METADATA), StandardCharsets.UTF_8);
			SAML_CLIENT = SamlClient.fromMetadata("FacilioSAML", ACS_URL, reader);
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
				resp.sendRedirect(getLoginUrl());
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
	
	public static SamlClient getSamlClient() {
		return SAML_CLIENT;
	}
	
	public static String getHomeUrl() {
		return HOME_URL;
	}
	
	public static String getLoginUrl() {
		return LOGIN_URL;
	}
}
