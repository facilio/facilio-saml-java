package com.facilio.saml;

import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.facilio.saml.SAMLClient.SAMLResponse;

/**
 * 
 * Assertion Consumer Service Servlet
 *
 */
public class ACSServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void service(HttpServletRequest req, HttpServletResponse res) {
		String SAMLResponse = req.getParameter("SAMLResponse");
		String RelayState = req.getParameter("RelayState");
		
		String origin = req.getParameter("origin");
		String cAppId = req.getParameter("capp_id");
    	if (SAMLResponse == null) {
    		return;
    	}
    	
        SAMLResponse samlResponse;
		try {
			samlResponse = SAMLFilter.getSamlClient().validateSAMLResponse(SAMLResponse);
			String authUserEmail = samlResponse.getNameId();
			
			Map<String, String> userData = samlResponse.getAttributes();
			
			Account currentAccount = Account.getAccount(authUserEmail, userData);
			if (currentAccount != null) {
				SAMLUtil.setCurrentAccount(currentAccount);
				req.getSession().setAttribute(SAMLUtil.CURRENT_ACCOUNT, currentAccount);
				
				String redirectURL = RelayState != null ? RelayState : SAMLFilter.getHomeUrl();
				if (origin != null && !origin.isEmpty() && cAppId != null && !cAppId.isEmpty()) {
					String sdkParams = "origin=" + URLEncoder.encode(origin, "UTF-8") + "&capp_id=" + cAppId;
					
					if (redirectURL.indexOf("?") > 0) {
						redirectURL += "&" + sdkParams;
					}
					else {
						redirectURL += "?" + sdkParams;
					}
				}
				res.sendRedirect(redirectURL);
			}
			else {
				throw new Exception("Invalid SAML login.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
