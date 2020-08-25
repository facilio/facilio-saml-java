package com.facilio.saml;

import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * Request Servlet
 *
 */
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void service(HttpServletRequest req, HttpServletResponse res) {
		
		try {
			String redirectTo = req.getParameter("redirect");
			
			SAMLClient samlClient = SAMLFilter.getSamlClient();
			String samlRequest = samlClient.getSAMLRequest();
			samlRequest = URLEncoder.encode(samlRequest, "UTF-8");
			
			String idpUrl = samlClient.getIdpURL();
			
			String ssoURL = idpUrl;
			if (ssoURL.indexOf('?') == -1) {
    			ssoURL=ssoURL + "?SAMLRequest=" + samlRequest;
    		} else {
    			ssoURL=ssoURL + "&SAMLRequest=" + samlRequest;
    		}
			if (redirectTo != null && !redirectTo.trim().isEmpty()) {
				ssoURL = ssoURL + "&RelayState=" +  URLEncoder.encode(redirectTo, "UTF-8");
			}
			
			res.sendRedirect(ssoURL);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
