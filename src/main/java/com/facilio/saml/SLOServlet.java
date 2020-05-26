package com.facilio.saml;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SLOServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void service(HttpServletRequest req, HttpServletResponse res) {
		
		try {
			req.getSession().invalidate();
			res.sendRedirect(SAMLFilter.getLoginUrl());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
