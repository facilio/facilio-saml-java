# facilio-saml-java

### Add SAMLFilter in web.xml:

```
<filter>
	<filter-name>SAMLFilter</filter-name>
	<filter-class>com.facilio.saml.SAMLFilter</filter-class>
</filter>
<filter-mapping>
	<filter-name>SAMLFilter</filter-name>
	<url-pattern>*</url-pattern>
</filter-mapping>
```
