package com.facilio.saml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SAMLClient {
	
	private String spEntityId;
	private String acsURL;
	private String idpURL;
	private String idpEntityId;
	private String idpLogoutURL;
	private X509Certificate certificate;

	public SAMLClient(String spEntityId, String acsURL, String idpEntityId, String idpURL, String certificate) throws Exception {
		this.spEntityId = spEntityId;
		this.acsURL = acsURL;
		this.idpURL = idpURL;
		this.idpEntityId = idpEntityId;
		this.certificate = this.loadCertificate(certificate);
	}
	
	public SAMLClient(String spEntityId, String acsURL, File idpMetadataXml) throws Exception {
		this.spEntityId = spEntityId;
		this.acsURL = acsURL;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		factory.setNamespaceAware(true);

		Document document = factory.newDocumentBuilder().parse(idpMetadataXml);
		this.parseIdPMetadata(document);
	}
	
	public SAMLClient(String spEntityId, String acsURL, InputStream idpMetadataXml) throws Exception {
		this.spEntityId = spEntityId;
		this.acsURL = acsURL;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		factory.setNamespaceAware(true);

		Document document = factory.newDocumentBuilder().parse(idpMetadataXml);
		this.parseIdPMetadata(document);
	}

	public String getSpEntityId() {
		return spEntityId;
	}

	public SAMLClient setSpEntityId(String spEntityId) {
		this.spEntityId = spEntityId;
		return this;
	}

	public String getAcsURL() {
		return acsURL;
	}

	public SAMLClient setAcsURL(String acsURL) {
		this.acsURL = acsURL;
		return this;
	}

	public String getIdpURL() {
		return idpURL;
	}

	public SAMLClient setIdpURL(String idpURL) {
		this.idpURL = idpURL;
		return this;
	}

	public String getIdpEntityId() {
		return idpEntityId;
	}

	public SAMLClient setIdpEntityId(String idpEntityId) {
		this.idpEntityId = idpEntityId;
		return this;
	}
	
	public String getIdpLogoutURL() {
		return idpLogoutURL;
	}

	public SAMLClient setIdpLogoutURL(String idpLogoutURL) {
		this.idpLogoutURL = idpLogoutURL;
		return this;
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public SAMLClient setCertificate(String certificate) throws Exception {
		this.certificate = this.loadCertificate(certificate);
		return this;
	}
	
	public SAMLClient setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
		return this;
	}

	public SAMLResponse validateSAMLResponse(String samlResponse) throws Exception {

		Document document = parseResponse(samlResponse, "POST");

		return this.validate(document);
	}
	
	public SAMLResponse validateSAMLResponse(String samlResponse, String method) throws Exception {

		Document document = parseResponse(samlResponse, method);

		return this.validate(document);
	}
	
	public String getSAMLRequest() throws Exception {
		String uid = "FACILIO_" + UUID.randomUUID().toString();
		
		Map<String, String> templateProps = new HashMap<>();
		templateProps.put("ID", uid);
		templateProps.put("IssueInstant", SAMLUtil.formatDate(new Date()));
		templateProps.put("ProviderName", "Facilio");
		templateProps.put("Destination", getIdpURL());
		templateProps.put("AssertionConsumerServiceURL", getAcsURL());
		templateProps.put("Issuer", getSpEntityId());
		
		String authnRequest = getAuthnRequestTemplate(templateProps);
		
		return encodeSAMLRequest(authnRequest);
	}
	
	private void parseIdPMetadata(Document document) throws Exception {
		
		String entityID = document.getDocumentElement().getAttribute("entityID");
		if (entityID != null && !entityID.trim().isEmpty()) {
			setIdpEntityId(entityID);
		}
		
		Element x509CertElem = (Element) document.getElementsByTagNameNS("*", "X509Certificate").item(0);
		if (x509CertElem != null) {
			setCertificate(x509CertElem.getTextContent());
		}
		
		NodeList ssElements = document.getElementsByTagNameNS("*", "SingleSignOnService");
		if (ssElements != null && ssElements.getLength() > 0) {
			for (int i=0; i < ssElements.getLength(); i++) {
				Element loginElm = (Element) ssElements.item(i);
				String binding = loginElm.getAttribute("Binding");
				if (binding != null && "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect".equalsIgnoreCase(binding)) {
					String idpLoginUrl = loginElm.getAttribute("Location");
					setIdpURL(idpLoginUrl);
				}
			}
		}
		
		NodeList slElements = document.getElementsByTagNameNS("*", "SingleLogoutService");
		if (slElements != null && slElements.getLength() > 0) {
			for (int i=0; i < slElements.getLength(); i++) {
				Element logoutElm = (Element) ssElements.item(i);
				String binding = logoutElm.getAttribute("Binding");
				if (binding != null && "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect".equalsIgnoreCase(binding)) {
					String idpLogoutUrl = logoutElm.getAttribute("Location");
					setIdpLogoutURL(idpLogoutUrl);
				}
			}
		}
		
		if (getIdpURL() == null || getCertificate() == null) {
			throw new Exception("IdP Url or Certificate missing.");
		}
	}
	
	private String encodeSAMLRequest(String samlRequest) throws IOException {
		if (samlRequest != null && !samlRequest.trim().isEmpty()) {
			ByteArrayOutputStream bos = null;
			DeflaterOutputStream dos = null;
			try {
				Deflater deflater = new Deflater(Deflater.DEFLATED, true);
				byte[] b = samlRequest.getBytes("UTF-8");
				bos = new ByteArrayOutputStream();
				dos = new DeflaterOutputStream(bos, deflater);
				dos.write(b, 0, b.length);
				dos.finish();
				
				String encMessage = Base64.encodeBase64String(bos.toByteArray());
				return encMessage;
			} finally {
				if (bos != null)  {
					try {
						bos.close();
					} catch (Exception e) {
					}
				}
				if (dos != null) {
					try {
						dos.close();
					} catch (Exception e) {
					}
				}
			}
		}
		return null;
	}

	private String getAuthnRequestTemplate(Map<String, String> props) {
		
		StringBuilder template = new StringBuilder();
		template.append("<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" ID=\"${ID}\" Version=\"2.0\" IssueInstant=\"${IssueInstant}\" ProviderName=\"${ProviderName}\" Destination=\"${Destination}\" ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" AssertionConsumerServiceURL=\"${AssertionConsumerServiceURL}\">");
		template.append("<saml:Issuer>${Issuer}</saml:Issuer>");
		template.append("<samlp:NameIDPolicy Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress\" AllowCreate=\"true\" />");
		template.append("<samlp:RequestedAuthnContext Comparison=\"exact\"><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml:AuthnContextClassRef></samlp:RequestedAuthnContext>");
		template.append("</samlp:AuthnRequest>");
		String templateStr = template.toString();
		
		Iterator<String> itr = props.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = props.get(key);
			templateStr = templateStr.replaceAll("\\$\\{" + key + "\\}", value);
		}
		return templateStr;
	}
	
	private Document parseResponse(String encodedResponse, String method) throws Exception {
		
		InputStream xmlIns = decodeAndInflate(encodedResponse, method);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		factory.setNamespaceAware(true);

		return factory.newDocumentBuilder().parse(xmlIns);
	}
	
	private SAMLResponse validate(Document document) throws Exception {
		
		this.validateStatus(document);
		this.validateIssuer(document);
		this.validateAssertion(document);
		this.validateSignature(document);
		
		NodeList nameidTags = document.getElementsByTagNameNS("*", "NameID");
		if (nameidTags == null) {
			throw new Exception("NameeID missing");
		}
		
		String userEmail = null;
		for (int i = 0; i < nameidTags.getLength(); i++) {
			Node nameID = nameidTags.item(i);
			if ("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress".equals(nameID.getAttributes().getNamedItem("Format").getTextContent())) {
				userEmail = nameID.getTextContent().trim();;
			}
		}
		if (userEmail == null) {
			throw new Exception("NameID emailAddress not present.");
		}
		
		Element attributeStateMent = (Element) document.getElementsByTagNameNS("*", "AttributeStatement").item(0);
		Map<String, String> userDetails = new HashMap<>();
		if (attributeStateMent!=null && attributeStateMent.hasChildNodes()) {
			NodeList attributes = attributeStateMent.getChildNodes();
			for(int i = 0; i < attributes.getLength(); i++) {
				Node temp = attributes.item(i);
				if (temp.getNodeType() ==  Node.ELEMENT_NODE) {
					Element attribute = (Element) temp;
					String name = attribute.getAttribute("Name");
					String value = attribute.getChildNodes().item(0).getTextContent();
					userDetails.put(name, value);
				}
			}
		}
		
		return new SAMLResponse().setNameId(userEmail).setAttributes(userDetails);
	}
	
	private boolean validateStatus(Document document) throws Exception {
		Element element = (Element) document.getElementsByTagNameNS("*", "StatusCode").item(0);
		String value = element.getAttribute("Value");
		if (value != null && value.indexOf("Success") >= 0) {
			return true;
		}
		return false;
	}
	
	private boolean validateIssuer(Document document) throws Exception {
		Element element = (Element) document.getElementsByTagNameNS("*", "Issuer").item(0);
		String issuer = element.getTextContent();
		
		if (getIdpEntityId() != null && !getIdpEntityId().trim().isEmpty()) {
			if (issuer != null && issuer.trim().equals(getIdpEntityId())) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return true;
		}
	}
	
	private boolean validateAssertion(Document document) throws Exception {
		Element assertion = (Element) document.getElementsByTagNameNS("*", "Assertion").item(0);
		
		Element issuerElm = (Element) assertion.getElementsByTagNameNS("*", "Issuer").item(0);
		String issuer = issuerElm.getTextContent();
		
		if (getIdpEntityId() != null && !getIdpEntityId().trim().isEmpty()) {
			if (issuer == null || !issuer.trim().equals(getIdpEntityId())) {
				throw new Exception("The assertion issuer didn't match the expected value");
			}
		}
		
		NodeList nameidTags = assertion.getElementsByTagNameNS("*", "NameID");
		if (nameidTags == null || nameidTags.getLength() == 0) {
			throw new Exception("The NameID value is missing from the SAML response; this is likely an IDP configuration issue");
		}
		
		return this.validateConditions(assertion);
	}
	
	private boolean validateConditions(Element assertion) throws Exception {
		Date now = new Date();
		
		NodeList conditions = assertion.getElementsByTagNameNS("*", "Conditions");
		if (conditions != null && conditions.getLength() > 0) {
			Element condition = (Element) conditions.item(0);
			
			String notBefore = condition.getAttribute("NotBefore");
			if (notBefore != null && !notBefore.trim().isEmpty()) {
				Date notBeforeDate = SAMLUtil.parseDate(notBefore);
				if (now.before(notBeforeDate)) {
					throw new Exception("The assertion cannot be used before " + notBefore.toString());
				}
			}
			String notOnOrAfter = condition.getAttribute("NotOnOrAfter");
			if (notOnOrAfter != null && !notOnOrAfter.trim().isEmpty()) {
				Date notOnOrAfterDate = SAMLUtil.parseDate(notOnOrAfter);
				if (now.after(notOnOrAfterDate)) {
					throw new Exception("The assertion cannot be used after " + notOnOrAfterDate.toString());
				}
			}
		}
		return true;
	}
	
	private boolean validateSignature(Document document) throws Exception {
		
		NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
        	throw new  Exception("SAML Signature Missing");
        }
        
        Node signatureTag = nl.item(0);
        
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", java.security.Security.getProvider("XMLDSig"));
        PublicKey validationKey = getCertificate().getPublicKey();
        DOMValidateContext valContext = new DOMValidateContext(validationKey, signatureTag);

        /*
         * Validate The signature and the content received through saml response
         */
        valContext.setIdAttributeNS((Element) document.getElementsByTagNameNS("*", "Response").item(0), null, "ID");
        valContext.setIdAttributeNS((Element) document.getElementsByTagNameNS("*", "Assertion").item(0), null, "ID");
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);
		Boolean isValid = signature.validate(valContext);
		
		if (isValid) {
			return true;
		}
		else {
			return false;
		}
	}

	private InputStream decodeAndInflate(String encodedResponse, String method) {
		ByteArrayInputStream afterB64Decode =
				new ByteArrayInputStream(Base64.decodeBase64(encodedResponse));

		if ("GET".equals(method)) {
			// If the request was a GET request, the value will have been deflated
			InputStream afterInflate = new InflaterInputStream(afterB64Decode, new Inflater(true));
			return afterInflate;
		} else {
			return afterB64Decode;
		}
	}
	
	private X509Certificate loadCertificate(String certificate) throws Exception {
		return SAMLUtil.loadCertificate(certificate);
	}

	public static class SAMLResponse {
		private String nameId;
		private Map<String, String> attributes;
		
		public String getNameId() {
			return nameId;
		}
		public SAMLResponse setNameId(String nameId) {
			this.nameId = nameId;
			return this;
		}
		public Map<String, String> getAttributes() {
			return attributes;
		}
		public SAMLResponse setAttributes(Map<String, String> attributes) {
			this.attributes = attributes;
			return this;
		}
	}
}
