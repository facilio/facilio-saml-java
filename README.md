# Facilio Java SAML Library

The java library for implementing SAML enabled server-side connected apps for Facilio.

- To learn more about Connected Apps in Facilio - [Refer here](https://guide.facilio.com/knowledge-base/connected-app/)


#### Getting started

  * [Installation](#installation)
  * [Add Filter & Servlets in web.xml](#add-filter--servlets-in-webxml)
	* [SAMLFilter](#samlfilter)
	* [ACS Servlet](#acs-servlet)
	* [Single Logout Servlet](#single-logout-servlet)
  * [IDP Metadata](#idp-metadata)
  * [Current Account](#current-account)

## Installation

### Maven users
Add the below dependency to your ```pom.xml```:

    <dependency>
      <groupId>com.facilio</groupId>
      <artifactId>facilio-saml</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>


## Add Filter & Servlets in web.xml

### SAMLFilter

Add this filter in your web.xml:

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

#### Filter Init Params (optional)

| Init Param  | Default Value | Description |
| ------------- | ------------- | ------------- |
| idp_metadata  | idp_metadata.xml  | IdP metadata XML file location, which can be downloaded from Connected Apps SAML configuration. |
| acs_url  | /acs  | Assertion Consumer Service Url. |
| login_url  | /login  | Login Page Url. |
| home_url  | /home  | Home Page Url. |
| exclude  | ---  | Regex pattern to exclude path from filter. |


Example (add under filter tag):

```
<init-param>
	<param-name>idp_metadata.xml</param-name>
	<param-value>conf/idp_metadata.xml</param-value>
</init-param>
```

### ACS Servlet

Add this servlet mapping for assertion consumer service url which needed for SAML configuration.

```
<servlet>
	<servlet-name>ACSServlet</servlet-name>
	<servlet-class>com.facilio.saml.ACSServlet</servlet-class>
</servlet>
<servlet-mapping>
	<servlet-name>ACSServlet</servlet-name>
	<url-pattern>/acs</url-pattern>
</servlet-mapping>
```

### Single Logout Servlet

Add this servlet for handling logout which can be IdP initiated logout or SP initiated logout.

```
<servlet>
	<servlet-name>SLOServlet</servlet-name>
	<servlet-class>com.facilio.saml.SLOServlet</servlet-class>
</servlet>
<servlet-mapping>
	<servlet-name>SLOServlet</servlet-name>
	<url-pattern>/logout</url-pattern>
</servlet-mapping>
```

## IdP Metadata

To authorize your app with Facilio, download IdP metadata from SAML Settings in Facilio Connected Apps. And, add the downloaded `idp_metadata.xml` xml file under `WEB-INF/classes`.


## Current Account

Method to get current facilio account:
```
Account account = SAMLUtil.getCurrentAccount();
```

Getting user details:
```
User user = account.getUser();

long userId = user.getUserId();
String name = user.getName();
String email = user.getEmail();
String role = user.getRole();
String timezone = user.getTimezone();
Locale locale = user.getLocale();
```

Getting organization details:
```
Organization org = account.getOrg();

long orgId = org.getOrgId();
String orgName = org.getName();
String orgDomain = org.getDomain();
String orgTimezone = org.getTimezone();
```
