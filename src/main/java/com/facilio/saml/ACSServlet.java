package com.facilio.saml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;

import com.coveo.saml.SamlResponse;

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
    	if (SAMLResponse == null) {
    		return;
    	}
    	
        SamlResponse samlResponse;
		try {
			samlResponse = SAMLFilter.getSamlClient().decodeAndValidateSamlResponse(SAMLResponse);
			String authUserEmail = samlResponse.getNameID();
			
			Map<String, String> userData = new HashMap<String, String>();
			for (AttributeStatement attributeStatement : samlResponse.getAssertion().getAttributeStatements()) {
		        
				for (Attribute attribute : attributeStatement.getAttributes()) {
					
					List<XMLObject> attributeValues = attribute.getAttributeValues();
					if (!attributeValues.isEmpty()) {
						String value = attributeValues.get(0).getDOM().getTextContent();
						userData.put(attribute.getName(), value);
					}
		        }
		    }
			
			Account currentAccount = Account.getAccount(authUserEmail, userData);
			if (currentAccount != null) {
				SAMLUtil.setCurrentAccount(currentAccount);
				req.getSession().setAttribute(SAMLUtil.CURRENT_ACCOUNT, currentAccount);
				
				String redirectURL = RelayState != null ? RelayState : SAMLFilter.getHomeUrl();
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
