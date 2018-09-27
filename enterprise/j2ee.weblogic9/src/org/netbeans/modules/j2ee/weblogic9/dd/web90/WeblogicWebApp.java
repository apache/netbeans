/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 *	This generated bean class WeblogicWebApp matches the schema element 'weblogic-web-app'.
 *
 *	Generated on Tue Jul 25 03:27:06 PDT 2017
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the following bean graph:
 *
 *	weblogicWebApp <weblogic-web-app> : WeblogicWebApp
 *		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		(
 *		  | description <description> : java.lang.String[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | weblogicVersion <weblogic-version> : java.lang.String[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | securityRoleAssignment <security-role-assignment> : SecurityRoleAssignmentType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	roleName <role-name> : java.lang.String
 *		  | 	| principalName <principal-name> : java.lang.String[1,n] 	[whiteSpace (collapse)]
 *		  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	| externallyDefined <externally-defined> : EmptyType
 *		  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | runAsRoleAssignment <run-as-role-assignment> : RunAsRoleAssignmentType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	roleName <role-name> : java.lang.String
 *		  | 	runAsPrincipalName <run-as-principal-name> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | resourceDescription <resource-description> : ResourceDescriptionType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	resRefName <res-ref-name> : java.lang.String
 *		  | 	| jndiName <jndi-name> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	| resourceLink <resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | resourceEnvDescription <resource-env-description> : ResourceEnvDescriptionType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	resourceEnvRefName <resource-env-ref-name> : java.lang.String
 *		  | 	| jndiName <jndi-name> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	| resourceLink <resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | ejbReferenceDescription <ejb-reference-description> : EjbReferenceDescriptionType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	ejbRefName <ejb-ref-name> : java.lang.String
 *		  | 	jndiName <jndi-name> : java.lang.String
 *		  | serviceReferenceDescription <service-reference-description> : ServiceReferenceDescriptionType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	serviceRefName <service-ref-name> : java.lang.String
 *		  | 	wsdlUrl <wsdl-url> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	callProperty <call-property> : PropertyNamevalueType[0,n]
 *		  | 		name <name> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		value <value> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	portInfo <port-info> : PortInfoType[0,n]
 *		  | 		portName <port-name> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		stubProperty <stub-property> : PropertyNamevalueType[0,n]
 *		  | 			name <name> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 			value <value> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		callProperty <call-property> : PropertyNamevalueType[0,n]
 *		  | 			name <name> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 			value <value> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | messageDestinationDescriptor <message-destination-descriptor> : MessageDestinationDescriptorType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	messageDestinationName <message-destination-name> : java.lang.String
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	| destinationJndiName <destination-jndi-name> : java.lang.String
 *		  | 	| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	| initialContextFactory <initial-context-factory> : java.lang.String[0,1]
 *		  | 	| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	| providerUrl <provider-url> : java.lang.String[0,1]
 *		  | 	| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	| destinationResourceLink <destination-resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | sessionDescriptor <session-descriptor> : SessionDescriptorType[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	timeoutSecs <timeout-secs> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	invalidationIntervalSecs <invalidation-interval-secs> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	debugEnabled <debug-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	idLength <id-length> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	trackingEnabled <tracking-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	cacheSize <cache-size> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	maxInMemorySessions <max-in-memory-sessions> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	cookiesEnabled <cookies-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	cookieName <cookie-name> : java.lang.String[0,1]
 *		  | 	cookiePath <cookie-path> : java.lang.String[0,1]
 *		  | 	cookieDomain <cookie-domain> : java.lang.String[0,1]
 *		  | 	cookieComment <cookie-comment> : java.lang.String[0,1]
 *		  | 	cookieSecure <cookie-secure> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	cookieMaxAgeSecs <cookie-max-age-secs> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	persistentStoreType <persistent-store-type> : java.lang.String[0,1]
 *		  | 	persistentStoreCookieName <persistent-store-cookie-name> : java.lang.String[0,1]
 *		  | 	persistentStoreDir <persistent-store-dir> : java.lang.String[0,1]
 *		  | 	persistentStorePool <persistent-store-pool> : java.lang.String[0,1]
 *		  | 	persistentStoreTable <persistent-store-table> : java.lang.String[0,1]
 *		  | 	jdbcColumnNameMaxInactiveInterval <jdbc-column-name-max-inactive-interval> : java.lang.String[0,1]
 *		  | 	jdbcConnectionTimeoutSecs <jdbc-connection-timeout-secs> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	urlRewritingEnabled <url-rewriting-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	httpProxyCachingOfCookies <http-proxy-caching-of-cookies> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	encodeSessionIdInQueryParams <encode-session-id-in-query-params> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	monitoringAttributeName <monitoring-attribute-name> : java.lang.String[0,1]
 *		  | 	sharingEnabled <sharing-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | jspDescriptor <jsp-descriptor> : JspDescriptorType[0,1]
 *		  | 	keepgenerated <keepgenerated> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	packagePrefix <package-prefix> : java.lang.String[0,1]
 *		  | 	superClass <super-class> : java.lang.String[0,1]
 *		  | 	pageCheckSeconds <page-check-seconds> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	precompile <precompile> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	precompileContinue <precompile-continue> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	verbose <verbose> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	workingDir <working-dir> : java.lang.String[0,1]
 *		  | 	printNulls <print-nulls> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	backwardCompatible <backward-compatible> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	encoding <encoding> : java.lang.String[0,1]
 *		  | 	exactMapping <exact-mapping> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	defaultFileName <default-file-name> : java.lang.String[0,1]
 *		  | 	rtexprvalueJspParamName <rtexprvalue-jsp-param-name> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	debug <debug> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | authFilter <auth-filter> : java.lang.String[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | containerDescriptor <container-descriptor> : ContainerDescriptorType[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	checkAuthOnForward <check-auth-on-forward> : EmptyType[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	filterDispatchedRequestsEnabled <filter-dispatched-requests-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	redirectContentType <redirect-content-type> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	redirectContent <redirect-content> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	redirectWithAbsoluteUrl <redirect-with-absolute-url> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	indexDirectoryEnabled <index-directory-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	indexDirectorySortBy <index-directory-sort-by> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	servletReloadCheckSecs <servlet-reload-check-secs> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	resourceReloadCheckSecs <resource-reload-check-secs> : java.math.BigInteger[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	singleThreadedServletPoolSize <single-threaded-servlet-pool-size> : long[0,1] 	[minInclusive (0)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	sessionMonitoringEnabled <session-monitoring-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	saveSessionsEnabled <save-sessions-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	preferWebInfClasses <prefer-web-inf-classes> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	defaultMimeType <default-mime-type> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	clientCertProxyEnabled <client-cert-proxy-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	reloginEnabled <relogin-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	allowAllRoles <allow-all-roles> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	nativeIoEnabled <native-io-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	minimumNativeFileSize <minimum-native-file-size> : long[0,1]
 *		  | 	disableImplicitServletMappings <disable-implicit-servlet-mappings> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	tempDir <temp-dir> : java.lang.String[0,1]
 *		  | 	optimisticSerialization <optimistic-serialization> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | charsetParams <charset-params> : CharsetParamsType[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	inputCharset <input-charset> : InputCharsetType[0,n]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		resourcePath <resource-path> : java.lang.String
 *		  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		javaCharsetName <java-charset-name> : java.lang.String
 *		  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	charsetMapping <charset-mapping> : CharsetMappingType[0,n]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		ianaCharsetName <iana-charset-name> : java.lang.String
 *		  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		javaCharsetName <java-charset-name> : java.lang.String
 *		  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | virtualDirectoryMapping <virtual-directory-mapping> : VirtualDirectoryMappingType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	localPath <local-path> : java.lang.String
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	urlPattern <url-pattern> : java.lang.String[1,n]
 *		  | urlMatchMap <url-match-map> : java.lang.String[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | securityPermission <security-permission> : SecurityPermissionType[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	description <description> : java.lang.String[0,1]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 		[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 *		  | 	securityPermissionSpec <security-permission-spec> : java.lang.String 	[whiteSpace (collapse)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | contextRoot <context-root> : java.lang.String[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | wlDispatchPolicy <wl-dispatch-policy> : java.lang.String[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | servletDescriptor <servlet-descriptor> : ServletDescriptorType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	servletName <servlet-name> : java.lang.String 	[minLength (1)]
 *		  | 	runAsPrincipalName <run-as-principal-name> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	initAsPrincipalName <init-as-principal-name> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	destroyAsPrincipalName <destroy-as-principal-name> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	dispatchPolicy <dispatch-policy> : java.lang.String[0,1]
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | workManager <work-manager> : WorkManagerType[0,n]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	name <name> : java.lang.String
 *		  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	(
 *		  | 	  | responseTimeRequestClass <response-time-request-class> : ResponseTimeRequestClassType
 *		  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	name <name> : java.lang.String
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	goalMs <goal-ms> : java.math.BigInteger
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | fairShareRequestClass <fair-share-request-class> : FairShareRequestClassType
 *		  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	name <name> : java.lang.String
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	fairShare <fair-share> : java.math.BigInteger
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | contextRequestClass <context-request-class> : ContextRequestClassType
 *		  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	name <name> : java.lang.String
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	contextCase <context-case> : ContextCaseType[1,n]
 *		  | 	  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 		| userName <user-name> : java.lang.String
 *		  | 	  | 		| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 		| groupName <group-name> : java.lang.String
 *		  | 	  | 		| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 		requestClassName <request-class-name> : java.lang.String
 *		  | 	  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | requestClassName <request-class-name> : java.lang.String
 *		  | 	  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	)[0,1]
 *		  | 	(
 *		  | 	  | minThreadsConstraint <min-threads-constraint> : MinThreadsConstraintType
 *		  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	name <name> : java.lang.String
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	count <count> : java.math.BigInteger
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | minThreadsConstraintName <min-threads-constraint-name> : java.lang.String
 *		  | 	  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	)[0,1]
 *		  | 	(
 *		  | 	  | maxThreadsConstraint <max-threads-constraint> : MaxThreadsConstraintType
 *		  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	name <name> : java.lang.String
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	| count <count> : java.math.BigInteger
 *		  | 	  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	| poolName <pool-name> : java.lang.String
 *		  | 	  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | maxThreadsConstraintName <max-threads-constraint-name> : java.lang.String
 *		  | 	  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	)[0,1]
 *		  | 	(
 *		  | 	  | capacity <capacity> : CapacityType
 *		  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	name <name> : java.lang.String
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	count <count> : java.math.BigInteger
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | capacityName <capacity-name> : java.lang.String
 *		  | 	  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	)[0,1]
 *		  | 	(
 *		  | 	  | workManagerShutdownTrigger <work-manager-shutdown-trigger> : WorkManagerShutdownTriggerType
 *		  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	maxStuckThreadTime <max-stuck-thread-time> : java.math.BigInteger[0,1]
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | 	stuckThreadCount <stuck-thread-count> : java.math.BigInteger
 *		  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	  | ignoreStuckThreads <ignore-stuck-threads> : boolean
 *		  | 	  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	)[0,1]
 *		  | logging <logging> : LoggingType[0,1]
 *		  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	logFilename <log-filename> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	loggingEnabled <logging-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	rotationType <rotation-type> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	numberOfFilesLimited <number-of-files-limited> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	fileCount <file-count> : long[0,1] 	[minExclusive (0)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	fileSizeLimit <file-size-limit> : long[0,1] 	[minExclusive (0)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	rotateLogOnStartup <rotate-log-on-startup> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		  | 	logFileRotationDir <log-file-rotation-dir> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	rotationTime <rotation-time> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | 	fileTimeSpan <file-time-span> : long[0,1] 	[minExclusive (0)]
 *		  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		  | libraryRef <library-ref> : LibraryRefType[0,n]
 *		  | 	libraryName <library-name> : java.lang.String
 *		  | 	specificationVersion <specification-version> : java.lang.String[0,1]
 *		  | 	implementationVersion <implementation-version> : java.lang.String[0,1]
 *		  | 	exactMatch <exact-match> : boolean[0,1]
 *		  | 	contextRoot <context-root> : java.lang.String[0,1]
 *		)[0,n]
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class WeblogicWebApp extends org.netbeans.modules.schema2beans.BaseBean
	 implements org.netbeans.modules.j2ee.weblogic9.dd.model.WeblogicWebApp
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	private static final String SERIALIZATION_HELPER_CHARSET = "UTF-8";	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String DESCRIPTIONID = "DescriptionId";	// NOI18N
	static public final String WEBLOGIC_VERSION = "WeblogicVersion";	// NOI18N
	static public final String WEBLOGICVERSIONID = "WeblogicVersionId";	// NOI18N
	static public final String SECURITY_ROLE_ASSIGNMENT = "SecurityRoleAssignment";	// NOI18N
	static public final String RUN_AS_ROLE_ASSIGNMENT = "RunAsRoleAssignment";	// NOI18N
	static public final String RESOURCE_DESCRIPTION = "ResourceDescription";	// NOI18N
	static public final String RESOURCE_ENV_DESCRIPTION = "ResourceEnvDescription";	// NOI18N
	static public final String EJB_REFERENCE_DESCRIPTION = "EjbReferenceDescription";	// NOI18N
	static public final String SERVICE_REFERENCE_DESCRIPTION = "ServiceReferenceDescription";	// NOI18N
	static public final String MESSAGE_DESTINATION_DESCRIPTOR = "MessageDestinationDescriptor";	// NOI18N
	static public final String SESSION_DESCRIPTOR = "SessionDescriptor";	// NOI18N
	static public final String JSP_DESCRIPTOR = "JspDescriptor";	// NOI18N
	static public final String AUTH_FILTER = "AuthFilter";	// NOI18N
	static public final String AUTHFILTERID = "AuthFilterId";	// NOI18N
	static public final String CONTAINER_DESCRIPTOR = "ContainerDescriptor";	// NOI18N
	static public final String CHARSET_PARAMS = "CharsetParams";	// NOI18N
	static public final String VIRTUAL_DIRECTORY_MAPPING = "VirtualDirectoryMapping";	// NOI18N
	static public final String URL_MATCH_MAP = "UrlMatchMap";	// NOI18N
	static public final String URLMATCHMAPID = "UrlMatchMapId";	// NOI18N
	static public final String SECURITY_PERMISSION = "SecurityPermission";	// NOI18N
	static public final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
	static public final String CONTEXTROOTID = "ContextRootId";	// NOI18N
	static public final String WL_DISPATCH_POLICY = "WlDispatchPolicy";	// NOI18N
	static public final String WLDISPATCHPOLICYID = "WlDispatchPolicyId";	// NOI18N
	static public final String SERVLET_DESCRIPTOR = "ServletDescriptor";	// NOI18N
	static public final String WORK_MANAGER = "WorkManager";	// NOI18N
	static public final String LOGGING = "Logging";	// NOI18N
	static public final String LIBRARY_REF = "LibraryRef";	// NOI18N

	public WeblogicWebApp() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public WeblogicWebApp(org.w3c.dom.Node doc, int options) {
		this(Common.NO_DEFAULT_VALUES);
		try {
			initFromNode(doc, options);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("weblogic-web-app");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "weblogic-web-app"));
		}
		Node n = GraphManager.getElementNode("weblogic-web-app", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "weblogic-web-app", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public WeblogicWebApp(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("weblogic-web-app", "WeblogicWebApp",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, WeblogicWebApp.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(23);
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("weblogic-version", 	// NOI18N
			WEBLOGIC_VERSION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(WEBLOGIC_VERSION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("security-role-assignment", 	// NOI18N
			SECURITY_ROLE_ASSIGNMENT, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityRoleAssignmentType.class);
		this.createAttribute(SECURITY_ROLE_ASSIGNMENT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("run-as-role-assignment", 	// NOI18N
			RUN_AS_ROLE_ASSIGNMENT, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			RunAsRoleAssignmentType.class);
		this.createAttribute(RUN_AS_ROLE_ASSIGNMENT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("resource-description", 	// NOI18N
			RESOURCE_DESCRIPTION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceDescriptionType.class);
		this.createAttribute(RESOURCE_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("resource-env-description", 	// NOI18N
			RESOURCE_ENV_DESCRIPTION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceEnvDescriptionType.class);
		this.createAttribute(RESOURCE_ENV_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("ejb-reference-description", 	// NOI18N
			EJB_REFERENCE_DESCRIPTION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EjbReferenceDescriptionType.class);
		this.createAttribute(EJB_REFERENCE_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("service-reference-description", 	// NOI18N
			SERVICE_REFERENCE_DESCRIPTION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServiceReferenceDescriptionType.class);
		this.createAttribute(SERVICE_REFERENCE_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("message-destination-descriptor", 	// NOI18N
			MESSAGE_DESTINATION_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageDestinationDescriptorType.class);
		this.createAttribute(MESSAGE_DESTINATION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("session-descriptor", 	// NOI18N
			SESSION_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SessionDescriptorType.class);
		this.createAttribute(SESSION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("jsp-descriptor", 	// NOI18N
			JSP_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JspDescriptorType.class);
		this.createProperty("auth-filter", 	// NOI18N
			AUTH_FILTER, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(AUTH_FILTER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("container-descriptor", 	// NOI18N
			CONTAINER_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ContainerDescriptorType.class);
		this.createAttribute(CONTAINER_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("charset-params", 	// NOI18N
			CHARSET_PARAMS, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CharsetParamsType.class);
		this.createAttribute(CHARSET_PARAMS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("virtual-directory-mapping", 	// NOI18N
			VIRTUAL_DIRECTORY_MAPPING, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			VirtualDirectoryMappingType.class);
		this.createAttribute(VIRTUAL_DIRECTORY_MAPPING, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("url-match-map", 	// NOI18N
			URL_MATCH_MAP, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(URL_MATCH_MAP, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("security-permission", 	// NOI18N
			SECURITY_PERMISSION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityPermissionType.class);
		this.createAttribute(SECURITY_PERMISSION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("context-root", 	// NOI18N
			CONTEXT_ROOT, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CONTEXT_ROOT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("wl-dispatch-policy", 	// NOI18N
			WL_DISPATCH_POLICY, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(WL_DISPATCH_POLICY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("servlet-descriptor", 	// NOI18N
			SERVLET_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServletDescriptorType.class);
		this.createAttribute(SERVLET_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("work-manager", 	// NOI18N
			WORK_MANAGER, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WorkManagerType.class);
		this.createAttribute(WORK_MANAGER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("logging", 	// NOI18N
			LOGGING, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LoggingType.class);
		this.createAttribute(LOGGING, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("library-ref", 	// NOI18N
			LIBRARY_REF, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LibraryRefType.class);
		this.createAttribute("id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {
		setDefaultNamespace("http://www.bea.com/ns/weblogic/90");

	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		setAttributeValue(ID, value);
	}

	//
	public java.lang.String getId() {
		return getAttributeValue(ID);
	}

	// This attribute is an array, possibly empty
	public void setDescription(int index, java.lang.String value) {
		this.setValue(DESCRIPTION, index, value);
	}

	//
	public java.lang.String getDescription(int index) {
		return (java.lang.String)this.getValue(DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeDescription() {
		return this.size(DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setDescription(java.lang.String[] value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public java.lang.String[] getDescription() {
		return (java.lang.String[])this.getValues(DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addDescription(java.lang.String value) {
		int positionOfNewItem = this.addValue(DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDescription(java.lang.String value) {
		return this.removeValue(DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setDescriptionId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESCRIPTION) == 0) {
			addValue(DESCRIPTION, "");
		}
		setAttributeValue(DESCRIPTION, index, "Id", value);
	}

	//
	public java.lang.String getDescriptionId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESCRIPTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DESCRIPTION, index, "Id");
		}
	}

	// Return the number of properties
	public int sizeDescriptionId() {
		return this.size(DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setWeblogicVersion(int index, java.lang.String value) {
		this.setValue(WEBLOGIC_VERSION, index, value);
	}

	//
	public java.lang.String getWeblogicVersion(int index) {
		return (java.lang.String)this.getValue(WEBLOGIC_VERSION, index);
	}

	// Return the number of properties
	public int sizeWeblogicVersion() {
		return this.size(WEBLOGIC_VERSION);
	}

	// This attribute is an array, possibly empty
	public void setWeblogicVersion(java.lang.String[] value) {
		this.setValue(WEBLOGIC_VERSION, value);
	}

	//
	public java.lang.String[] getWeblogicVersion() {
		return (java.lang.String[])this.getValues(WEBLOGIC_VERSION);
	}

	// Add a new element returning its index in the list
	public int addWeblogicVersion(java.lang.String value) {
		int positionOfNewItem = this.addValue(WEBLOGIC_VERSION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWeblogicVersion(java.lang.String value) {
		return this.removeValue(WEBLOGIC_VERSION, value);
	}

	// This attribute is an array, possibly empty
	public void setWeblogicVersionId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(WEBLOGIC_VERSION) == 0) {
			addValue(WEBLOGIC_VERSION, "");
		}
		setAttributeValue(WEBLOGIC_VERSION, index, "Id", value);
	}

	//
	public java.lang.String getWeblogicVersionId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(WEBLOGIC_VERSION) == 0) {
			return null;
		} else {
			return getAttributeValue(WEBLOGIC_VERSION, index, "Id");
		}
	}

	// Return the number of properties
	public int sizeWeblogicVersionId() {
		return this.size(WEBLOGIC_VERSION);
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleAssignment(int index, SecurityRoleAssignmentType value) {
		this.setValue(SECURITY_ROLE_ASSIGNMENT, index, value);
	}

	//
	public SecurityRoleAssignmentType getSecurityRoleAssignment(int index) {
		return (SecurityRoleAssignmentType)this.getValue(SECURITY_ROLE_ASSIGNMENT, index);
	}

	// Return the number of properties
	public int sizeSecurityRoleAssignment() {
		return this.size(SECURITY_ROLE_ASSIGNMENT);
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleAssignment(SecurityRoleAssignmentType[] value) {
		this.setValue(SECURITY_ROLE_ASSIGNMENT, value);
	}

	//
	public SecurityRoleAssignmentType[] getSecurityRoleAssignment() {
		return (SecurityRoleAssignmentType[])this.getValues(SECURITY_ROLE_ASSIGNMENT);
	}

	// Add a new element returning its index in the list
	public int addSecurityRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.web90.SecurityRoleAssignmentType value) {
		int positionOfNewItem = this.addValue(SECURITY_ROLE_ASSIGNMENT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSecurityRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.web90.SecurityRoleAssignmentType value) {
		return this.removeValue(SECURITY_ROLE_ASSIGNMENT, value);
	}

	// This attribute is an array, possibly empty
	public void setRunAsRoleAssignment(int index, RunAsRoleAssignmentType value) {
		this.setValue(RUN_AS_ROLE_ASSIGNMENT, index, value);
	}

	//
	public RunAsRoleAssignmentType getRunAsRoleAssignment(int index) {
		return (RunAsRoleAssignmentType)this.getValue(RUN_AS_ROLE_ASSIGNMENT, index);
	}

	// Return the number of properties
	public int sizeRunAsRoleAssignment() {
		return this.size(RUN_AS_ROLE_ASSIGNMENT);
	}

	// This attribute is an array, possibly empty
	public void setRunAsRoleAssignment(RunAsRoleAssignmentType[] value) {
		this.setValue(RUN_AS_ROLE_ASSIGNMENT, value);
	}

	//
	public RunAsRoleAssignmentType[] getRunAsRoleAssignment() {
		return (RunAsRoleAssignmentType[])this.getValues(RUN_AS_ROLE_ASSIGNMENT);
	}

	// Add a new element returning its index in the list
	public int addRunAsRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.web90.RunAsRoleAssignmentType value) {
		int positionOfNewItem = this.addValue(RUN_AS_ROLE_ASSIGNMENT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeRunAsRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.web90.RunAsRoleAssignmentType value) {
		return this.removeValue(RUN_AS_ROLE_ASSIGNMENT, value);
	}

	// This attribute is an array, possibly empty
	public void setResourceDescription(int index, ResourceDescriptionType value) {
		this.setValue(RESOURCE_DESCRIPTION, index, value);
	}

	//
	public ResourceDescriptionType getResourceDescription(int index) {
		return (ResourceDescriptionType)this.getValue(RESOURCE_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeResourceDescription() {
		return this.size(RESOURCE_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setResourceDescription(ResourceDescriptionType[] value) {
		this.setValue(RESOURCE_DESCRIPTION, value);
	}

	//
	public ResourceDescriptionType[] getResourceDescription() {
		return (ResourceDescriptionType[])this.getValues(RESOURCE_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addResourceDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.ResourceDescriptionType value) {
		int positionOfNewItem = this.addValue(RESOURCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.ResourceDescriptionType value) {
		return this.removeValue(RESOURCE_DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setResourceEnvDescription(int index, ResourceEnvDescriptionType value) {
		this.setValue(RESOURCE_ENV_DESCRIPTION, index, value);
	}

	//
	public ResourceEnvDescriptionType getResourceEnvDescription(int index) {
		return (ResourceEnvDescriptionType)this.getValue(RESOURCE_ENV_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeResourceEnvDescription() {
		return this.size(RESOURCE_ENV_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setResourceEnvDescription(ResourceEnvDescriptionType[] value) {
		this.setValue(RESOURCE_ENV_DESCRIPTION, value);
	}

	//
	public ResourceEnvDescriptionType[] getResourceEnvDescription() {
		return (ResourceEnvDescriptionType[])this.getValues(RESOURCE_ENV_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addResourceEnvDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.ResourceEnvDescriptionType value) {
		int positionOfNewItem = this.addValue(RESOURCE_ENV_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceEnvDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.ResourceEnvDescriptionType value) {
		return this.removeValue(RESOURCE_ENV_DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setEjbReferenceDescription(int index, EjbReferenceDescriptionType value) {
		this.setValue(EJB_REFERENCE_DESCRIPTION, index, value);
	}

	//
	public EjbReferenceDescriptionType getEjbReferenceDescription(int index) {
		return (EjbReferenceDescriptionType)this.getValue(EJB_REFERENCE_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeEjbReferenceDescription() {
		return this.size(EJB_REFERENCE_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setEjbReferenceDescription(EjbReferenceDescriptionType[] value) {
		this.setValue(EJB_REFERENCE_DESCRIPTION, value);
	}

	//
	public EjbReferenceDescriptionType[] getEjbReferenceDescription() {
		return (EjbReferenceDescriptionType[])this.getValues(EJB_REFERENCE_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addEjbReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.EjbReferenceDescriptionType value) {
		int positionOfNewItem = this.addValue(EJB_REFERENCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEjbReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.EjbReferenceDescriptionType value) {
		return this.removeValue(EJB_REFERENCE_DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setServiceReferenceDescription(int index, ServiceReferenceDescriptionType value) {
		this.setValue(SERVICE_REFERENCE_DESCRIPTION, index, value);
	}

	//
	public ServiceReferenceDescriptionType getServiceReferenceDescription(int index) {
		return (ServiceReferenceDescriptionType)this.getValue(SERVICE_REFERENCE_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeServiceReferenceDescription() {
		return this.size(SERVICE_REFERENCE_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setServiceReferenceDescription(ServiceReferenceDescriptionType[] value) {
		this.setValue(SERVICE_REFERENCE_DESCRIPTION, value);
	}

	//
	public ServiceReferenceDescriptionType[] getServiceReferenceDescription() {
		return (ServiceReferenceDescriptionType[])this.getValues(SERVICE_REFERENCE_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addServiceReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.ServiceReferenceDescriptionType value) {
		int positionOfNewItem = this.addValue(SERVICE_REFERENCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeServiceReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.web90.ServiceReferenceDescriptionType value) {
		return this.removeValue(SERVICE_REFERENCE_DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setMessageDestinationDescriptor(int index, MessageDestinationDescriptorType value) {
		this.setValue(MESSAGE_DESTINATION_DESCRIPTOR, index, value);
	}

	//
	public MessageDestinationDescriptorType getMessageDestinationDescriptor(int index) {
		return (MessageDestinationDescriptorType)this.getValue(MESSAGE_DESTINATION_DESCRIPTOR, index);
	}

	// Return the number of properties
	public int sizeMessageDestinationDescriptor() {
		return this.size(MESSAGE_DESTINATION_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setMessageDestinationDescriptor(MessageDestinationDescriptorType[] value) {
		this.setValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
	}

	//
	public MessageDestinationDescriptorType[] getMessageDestinationDescriptor() {
		return (MessageDestinationDescriptorType[])this.getValues(MESSAGE_DESTINATION_DESCRIPTOR);
	}

	// Add a new element returning its index in the list
	public int addMessageDestinationDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.MessageDestinationDescriptorType value) {
		int positionOfNewItem = this.addValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessageDestinationDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.MessageDestinationDescriptorType value) {
		return this.removeValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
	}

	// This attribute is an array, possibly empty
	public void setSessionDescriptor(int index, SessionDescriptorType value) {
		this.setValue(SESSION_DESCRIPTOR, index, value);
	}

	//
	public SessionDescriptorType getSessionDescriptor(int index) {
		return (SessionDescriptorType)this.getValue(SESSION_DESCRIPTOR, index);
	}

	// Return the number of properties
	public int sizeSessionDescriptor() {
		return this.size(SESSION_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setSessionDescriptor(SessionDescriptorType[] value) {
		this.setValue(SESSION_DESCRIPTOR, value);
	}

	//
	public SessionDescriptorType[] getSessionDescriptor() {
		return (SessionDescriptorType[])this.getValues(SESSION_DESCRIPTOR);
	}

	// Add a new element returning its index in the list
	public int addSessionDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.SessionDescriptorType value) {
		int positionOfNewItem = this.addValue(SESSION_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSessionDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.SessionDescriptorType value) {
		return this.removeValue(SESSION_DESCRIPTOR, value);
	}

	// This attribute is an array, possibly empty
	public void setJspDescriptor(int index, JspDescriptorType value) {
		this.setValue(JSP_DESCRIPTOR, index, value);
	}

	//
	public JspDescriptorType getJspDescriptor(int index) {
		return (JspDescriptorType)this.getValue(JSP_DESCRIPTOR, index);
	}

	// Return the number of properties
	public int sizeJspDescriptor() {
		return this.size(JSP_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setJspDescriptor(JspDescriptorType[] value) {
		this.setValue(JSP_DESCRIPTOR, value);
	}

	//
	public JspDescriptorType[] getJspDescriptor() {
		return (JspDescriptorType[])this.getValues(JSP_DESCRIPTOR);
	}

	// Add a new element returning its index in the list
	public int addJspDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.JspDescriptorType value) {
		int positionOfNewItem = this.addValue(JSP_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJspDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.JspDescriptorType value) {
		return this.removeValue(JSP_DESCRIPTOR, value);
	}

	// This attribute is an array, possibly empty
	public void setAuthFilter(int index, java.lang.String value) {
		this.setValue(AUTH_FILTER, index, value);
	}

	//
	public java.lang.String getAuthFilter(int index) {
		return (java.lang.String)this.getValue(AUTH_FILTER, index);
	}

	// Return the number of properties
	public int sizeAuthFilter() {
		return this.size(AUTH_FILTER);
	}

	// This attribute is an array, possibly empty
	public void setAuthFilter(java.lang.String[] value) {
		this.setValue(AUTH_FILTER, value);
	}

	//
	public java.lang.String[] getAuthFilter() {
		return (java.lang.String[])this.getValues(AUTH_FILTER);
	}

	// Add a new element returning its index in the list
	public int addAuthFilter(java.lang.String value) {
		int positionOfNewItem = this.addValue(AUTH_FILTER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeAuthFilter(java.lang.String value) {
		return this.removeValue(AUTH_FILTER, value);
	}

	// This attribute is an array, possibly empty
	public void setAuthFilterId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(AUTH_FILTER) == 0) {
			addValue(AUTH_FILTER, "");
		}
		setAttributeValue(AUTH_FILTER, index, "Id", value);
	}

	//
	public java.lang.String getAuthFilterId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(AUTH_FILTER) == 0) {
			return null;
		} else {
			return getAttributeValue(AUTH_FILTER, index, "Id");
		}
	}

	// Return the number of properties
	public int sizeAuthFilterId() {
		return this.size(AUTH_FILTER);
	}

	// This attribute is an array, possibly empty
	public void setContainerDescriptor(int index, ContainerDescriptorType value) {
		this.setValue(CONTAINER_DESCRIPTOR, index, value);
	}

	//
	public ContainerDescriptorType getContainerDescriptor(int index) {
		return (ContainerDescriptorType)this.getValue(CONTAINER_DESCRIPTOR, index);
	}

	// Return the number of properties
	public int sizeContainerDescriptor() {
		return this.size(CONTAINER_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setContainerDescriptor(ContainerDescriptorType[] value) {
		this.setValue(CONTAINER_DESCRIPTOR, value);
	}

	//
	public ContainerDescriptorType[] getContainerDescriptor() {
		return (ContainerDescriptorType[])this.getValues(CONTAINER_DESCRIPTOR);
	}

	// Add a new element returning its index in the list
	public int addContainerDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.ContainerDescriptorType value) {
		int positionOfNewItem = this.addValue(CONTAINER_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeContainerDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.ContainerDescriptorType value) {
		return this.removeValue(CONTAINER_DESCRIPTOR, value);
	}

	// This attribute is an array, possibly empty
	public void setCharsetParams(int index, CharsetParamsType value) {
		this.setValue(CHARSET_PARAMS, index, value);
	}

	//
	public CharsetParamsType getCharsetParams(int index) {
		return (CharsetParamsType)this.getValue(CHARSET_PARAMS, index);
	}

	// Return the number of properties
	public int sizeCharsetParams() {
		return this.size(CHARSET_PARAMS);
	}

	// This attribute is an array, possibly empty
	public void setCharsetParams(CharsetParamsType[] value) {
		this.setValue(CHARSET_PARAMS, value);
	}

	//
	public CharsetParamsType[] getCharsetParams() {
		return (CharsetParamsType[])this.getValues(CHARSET_PARAMS);
	}

	// Add a new element returning its index in the list
	public int addCharsetParams(org.netbeans.modules.j2ee.weblogic9.dd.web90.CharsetParamsType value) {
		int positionOfNewItem = this.addValue(CHARSET_PARAMS, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCharsetParams(org.netbeans.modules.j2ee.weblogic9.dd.web90.CharsetParamsType value) {
		return this.removeValue(CHARSET_PARAMS, value);
	}

	// This attribute is an array, possibly empty
	public void setVirtualDirectoryMapping(int index, VirtualDirectoryMappingType value) {
		this.setValue(VIRTUAL_DIRECTORY_MAPPING, index, value);
	}

	//
	public VirtualDirectoryMappingType getVirtualDirectoryMapping(int index) {
		return (VirtualDirectoryMappingType)this.getValue(VIRTUAL_DIRECTORY_MAPPING, index);
	}

	// Return the number of properties
	public int sizeVirtualDirectoryMapping() {
		return this.size(VIRTUAL_DIRECTORY_MAPPING);
	}

	// This attribute is an array, possibly empty
	public void setVirtualDirectoryMapping(VirtualDirectoryMappingType[] value) {
		this.setValue(VIRTUAL_DIRECTORY_MAPPING, value);
	}

	//
	public VirtualDirectoryMappingType[] getVirtualDirectoryMapping() {
		return (VirtualDirectoryMappingType[])this.getValues(VIRTUAL_DIRECTORY_MAPPING);
	}

	// Add a new element returning its index in the list
	public int addVirtualDirectoryMapping(org.netbeans.modules.j2ee.weblogic9.dd.web90.VirtualDirectoryMappingType value) {
		int positionOfNewItem = this.addValue(VIRTUAL_DIRECTORY_MAPPING, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeVirtualDirectoryMapping(org.netbeans.modules.j2ee.weblogic9.dd.web90.VirtualDirectoryMappingType value) {
		return this.removeValue(VIRTUAL_DIRECTORY_MAPPING, value);
	}

	// This attribute is an array, possibly empty
	public void setUrlMatchMap(int index, java.lang.String value) {
		this.setValue(URL_MATCH_MAP, index, value);
	}

	//
	public java.lang.String getUrlMatchMap(int index) {
		return (java.lang.String)this.getValue(URL_MATCH_MAP, index);
	}

	// Return the number of properties
	public int sizeUrlMatchMap() {
		return this.size(URL_MATCH_MAP);
	}

	// This attribute is an array, possibly empty
	public void setUrlMatchMap(java.lang.String[] value) {
		this.setValue(URL_MATCH_MAP, value);
	}

	//
	public java.lang.String[] getUrlMatchMap() {
		return (java.lang.String[])this.getValues(URL_MATCH_MAP);
	}

	// Add a new element returning its index in the list
	public int addUrlMatchMap(java.lang.String value) {
		int positionOfNewItem = this.addValue(URL_MATCH_MAP, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeUrlMatchMap(java.lang.String value) {
		return this.removeValue(URL_MATCH_MAP, value);
	}

	// This attribute is an array, possibly empty
	public void setUrlMatchMapId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(URL_MATCH_MAP) == 0) {
			addValue(URL_MATCH_MAP, "");
		}
		setAttributeValue(URL_MATCH_MAP, index, "Id", value);
	}

	//
	public java.lang.String getUrlMatchMapId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(URL_MATCH_MAP) == 0) {
			return null;
		} else {
			return getAttributeValue(URL_MATCH_MAP, index, "Id");
		}
	}

	// Return the number of properties
	public int sizeUrlMatchMapId() {
		return this.size(URL_MATCH_MAP);
	}

	// This attribute is an array, possibly empty
	public void setSecurityPermission(int index, SecurityPermissionType value) {
		this.setValue(SECURITY_PERMISSION, index, value);
	}

	//
	public SecurityPermissionType getSecurityPermission(int index) {
		return (SecurityPermissionType)this.getValue(SECURITY_PERMISSION, index);
	}

	// Return the number of properties
	public int sizeSecurityPermission() {
		return this.size(SECURITY_PERMISSION);
	}

	// This attribute is an array, possibly empty
	public void setSecurityPermission(SecurityPermissionType[] value) {
		this.setValue(SECURITY_PERMISSION, value);
	}

	//
	public SecurityPermissionType[] getSecurityPermission() {
		return (SecurityPermissionType[])this.getValues(SECURITY_PERMISSION);
	}

	// Add a new element returning its index in the list
	public int addSecurityPermission(org.netbeans.modules.j2ee.weblogic9.dd.web90.SecurityPermissionType value) {
		int positionOfNewItem = this.addValue(SECURITY_PERMISSION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSecurityPermission(org.netbeans.modules.j2ee.weblogic9.dd.web90.SecurityPermissionType value) {
		return this.removeValue(SECURITY_PERMISSION, value);
	}

	// This attribute is an array, possibly empty
	public void setContextRoot(int index, java.lang.String value) {
		this.setValue(CONTEXT_ROOT, index, value);
	}

	//
	public java.lang.String getContextRoot(int index) {
		return (java.lang.String)this.getValue(CONTEXT_ROOT, index);
	}

	// Return the number of properties
	public int sizeContextRoot() {
		return this.size(CONTEXT_ROOT);
	}

	// This attribute is an array, possibly empty
	public void setContextRoot(java.lang.String[] value) {
		this.setValue(CONTEXT_ROOT, value);
	}

	//
	public java.lang.String[] getContextRoot() {
		return (java.lang.String[])this.getValues(CONTEXT_ROOT);
	}

	// Add a new element returning its index in the list
	public int addContextRoot(java.lang.String value) {
		int positionOfNewItem = this.addValue(CONTEXT_ROOT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeContextRoot(java.lang.String value) {
		return this.removeValue(CONTEXT_ROOT, value);
	}

	// This attribute is an array, possibly empty
	public void setContextRootId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONTEXT_ROOT) == 0) {
			addValue(CONTEXT_ROOT, "");
		}
		setAttributeValue(CONTEXT_ROOT, index, "Id", value);
	}

	//
	public java.lang.String getContextRootId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONTEXT_ROOT) == 0) {
			return null;
		} else {
			return getAttributeValue(CONTEXT_ROOT, index, "Id");
		}
	}

	// Return the number of properties
	public int sizeContextRootId() {
		return this.size(CONTEXT_ROOT);
	}

	// This attribute is an array, possibly empty
	public void setWlDispatchPolicy(int index, java.lang.String value) {
		this.setValue(WL_DISPATCH_POLICY, index, value);
	}

	//
	public java.lang.String getWlDispatchPolicy(int index) {
		return (java.lang.String)this.getValue(WL_DISPATCH_POLICY, index);
	}

	// Return the number of properties
	public int sizeWlDispatchPolicy() {
		return this.size(WL_DISPATCH_POLICY);
	}

	// This attribute is an array, possibly empty
	public void setWlDispatchPolicy(java.lang.String[] value) {
		this.setValue(WL_DISPATCH_POLICY, value);
	}

	//
	public java.lang.String[] getWlDispatchPolicy() {
		return (java.lang.String[])this.getValues(WL_DISPATCH_POLICY);
	}

	// Add a new element returning its index in the list
	public int addWlDispatchPolicy(java.lang.String value) {
		int positionOfNewItem = this.addValue(WL_DISPATCH_POLICY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWlDispatchPolicy(java.lang.String value) {
		return this.removeValue(WL_DISPATCH_POLICY, value);
	}

	// This attribute is an array, possibly empty
	public void setWlDispatchPolicyId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(WL_DISPATCH_POLICY) == 0) {
			addValue(WL_DISPATCH_POLICY, "");
		}
		setAttributeValue(WL_DISPATCH_POLICY, index, "Id", value);
	}

	//
	public java.lang.String getWlDispatchPolicyId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(WL_DISPATCH_POLICY) == 0) {
			return null;
		} else {
			return getAttributeValue(WL_DISPATCH_POLICY, index, "Id");
		}
	}

	// Return the number of properties
	public int sizeWlDispatchPolicyId() {
		return this.size(WL_DISPATCH_POLICY);
	}

	// This attribute is an array, possibly empty
	public void setServletDescriptor(int index, ServletDescriptorType value) {
		this.setValue(SERVLET_DESCRIPTOR, index, value);
	}

	//
	public ServletDescriptorType getServletDescriptor(int index) {
		return (ServletDescriptorType)this.getValue(SERVLET_DESCRIPTOR, index);
	}

	// Return the number of properties
	public int sizeServletDescriptor() {
		return this.size(SERVLET_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setServletDescriptor(ServletDescriptorType[] value) {
		this.setValue(SERVLET_DESCRIPTOR, value);
	}

	//
	public ServletDescriptorType[] getServletDescriptor() {
		return (ServletDescriptorType[])this.getValues(SERVLET_DESCRIPTOR);
	}

	// Add a new element returning its index in the list
	public int addServletDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.ServletDescriptorType value) {
		int positionOfNewItem = this.addValue(SERVLET_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeServletDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.web90.ServletDescriptorType value) {
		return this.removeValue(SERVLET_DESCRIPTOR, value);
	}

	// This attribute is an array, possibly empty
	public void setWorkManager(int index, WorkManagerType value) {
		this.setValue(WORK_MANAGER, index, value);
	}

	//
	public WorkManagerType getWorkManager(int index) {
		return (WorkManagerType)this.getValue(WORK_MANAGER, index);
	}

	// Return the number of properties
	public int sizeWorkManager() {
		return this.size(WORK_MANAGER);
	}

	// This attribute is an array, possibly empty
	public void setWorkManager(WorkManagerType[] value) {
		this.setValue(WORK_MANAGER, value);
	}

	//
	public WorkManagerType[] getWorkManager() {
		return (WorkManagerType[])this.getValues(WORK_MANAGER);
	}

	// Add a new element returning its index in the list
	public int addWorkManager(org.netbeans.modules.j2ee.weblogic9.dd.web90.WorkManagerType value) {
		int positionOfNewItem = this.addValue(WORK_MANAGER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWorkManager(org.netbeans.modules.j2ee.weblogic9.dd.web90.WorkManagerType value) {
		return this.removeValue(WORK_MANAGER, value);
	}

	// This attribute is an array, possibly empty
	public void setLogging(int index, LoggingType value) {
		this.setValue(LOGGING, index, value);
	}

	//
	public LoggingType getLogging(int index) {
		return (LoggingType)this.getValue(LOGGING, index);
	}

	// Return the number of properties
	public int sizeLogging() {
		return this.size(LOGGING);
	}

	// This attribute is an array, possibly empty
	public void setLogging(LoggingType[] value) {
		this.setValue(LOGGING, value);
	}

	//
	public LoggingType[] getLogging() {
		return (LoggingType[])this.getValues(LOGGING);
	}

	// Add a new element returning its index in the list
	public int addLogging(org.netbeans.modules.j2ee.weblogic9.dd.web90.LoggingType value) {
		int positionOfNewItem = this.addValue(LOGGING, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLogging(org.netbeans.modules.j2ee.weblogic9.dd.web90.LoggingType value) {
		return this.removeValue(LOGGING, value);
	}

	// This attribute is an array, possibly empty
	public void setLibraryRef(int index, LibraryRefType value) {
		this.setValue(LIBRARY_REF, index, value);
	}

	//
	public LibraryRefType getLibraryRef(int index) {
		return (LibraryRefType)this.getValue(LIBRARY_REF, index);
	}

	// Return the number of properties
	public int sizeLibraryRef() {
		return this.size(LIBRARY_REF);
	}

	// This attribute is an array, possibly empty
	public void setLibraryRef(LibraryRefType[] value) {
		this.setValue(LIBRARY_REF, value);
	}

	//
	public LibraryRefType[] getLibraryRef() {
		return (LibraryRefType[])this.getValues(LIBRARY_REF);
	}

	// Add a new element returning its index in the list
	public int addLibraryRef(org.netbeans.modules.j2ee.weblogic9.dd.web90.LibraryRefType value) {
		int positionOfNewItem = this.addValue(LIBRARY_REF, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLibraryRef(org.netbeans.modules.j2ee.weblogic9.dd.web90.LibraryRefType value) {
		return this.removeValue(LIBRARY_REF, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityRoleAssignmentType newSecurityRoleAssignmentType() {
		return new SecurityRoleAssignmentType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public RunAsRoleAssignmentType newRunAsRoleAssignmentType() {
		return new RunAsRoleAssignmentType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResourceDescriptionType newResourceDescriptionType() {
		return new ResourceDescriptionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResourceEnvDescriptionType newResourceEnvDescriptionType() {
		return new ResourceEnvDescriptionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EjbReferenceDescriptionType newEjbReferenceDescriptionType() {
		return new EjbReferenceDescriptionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ServiceReferenceDescriptionType newServiceReferenceDescriptionType() {
		return new ServiceReferenceDescriptionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageDestinationDescriptorType newMessageDestinationDescriptorType() {
		return new MessageDestinationDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SessionDescriptorType newSessionDescriptorType() {
		return new SessionDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JspDescriptorType newJspDescriptorType() {
		return new JspDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ContainerDescriptorType newContainerDescriptorType() {
		return new ContainerDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public CharsetParamsType newCharsetParamsType() {
		return new CharsetParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public VirtualDirectoryMappingType newVirtualDirectoryMappingType() {
		return new VirtualDirectoryMappingType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityPermissionType newSecurityPermissionType() {
		return new SecurityPermissionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ServletDescriptorType newServletDescriptorType() {
		return new ServletDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WorkManagerType newWorkManagerType() {
		return new WorkManagerType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LoggingType newLoggingType() {
		return new LoggingType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LibraryRefType newLibraryRefType() {
		return new LibraryRefType();
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static WeblogicWebApp createGraph(org.w3c.dom.Node doc) {
		return new WeblogicWebApp(doc, Common.NO_DEFAULT_VALUES);
	}

	public static WeblogicWebApp createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static WeblogicWebApp createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static WeblogicWebApp createGraph(java.io.InputStream in, boolean validate) {
		try {
			Document doc = GraphManager.createXmlDocument(in, validate);
			return createGraph(doc);
		}
		catch (Exception t) {
			throw new RuntimeException(Common.getMessage(
				"DOMGraphCreateFailed_msg",
				t));
		}
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static WeblogicWebApp createGraph() {
		return new WeblogicWebApp();
	}


	
                    public LibraryRefType addLibraryRef() {
                        LibraryRefType libRef = new LibraryRefType();
                        addLibraryRef(libRef);
                        return libRef;
                    }

                    public JspDescriptorType addJspDescriptor() {
                        JspDescriptorType descriptor = new JspDescriptorType();
                        addJspDescriptor(descriptor);
                        return descriptor;
                    }

                    public ResourceDescriptionType addResourceDescription() {
                        ResourceDescriptionType description = new ResourceDescriptionType();
                        addResourceDescription(description);
                        return description;
                    }
                
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property id
		if (getId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "id", this);	// NOI18N
			}
		}
		// Validating property description
		// Validating property descriptionId
		// Validating property weblogicVersion
		// Validating property weblogicVersionId
		// Validating property securityRoleAssignment
		for (int _index = 0; _index < sizeSecurityRoleAssignment(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.SecurityRoleAssignmentType element = getSecurityRoleAssignment(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property runAsRoleAssignment
		for (int _index = 0; _index < sizeRunAsRoleAssignment(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.RunAsRoleAssignmentType element = getRunAsRoleAssignment(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property resourceDescription
		for (int _index = 0; _index < sizeResourceDescription(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.ResourceDescriptionType element = getResourceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property resourceEnvDescription
		for (int _index = 0; _index < sizeResourceEnvDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.ResourceEnvDescriptionType element = getResourceEnvDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property ejbReferenceDescription
		for (int _index = 0; _index < sizeEjbReferenceDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.EjbReferenceDescriptionType element = getEjbReferenceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property serviceReferenceDescription
		for (int _index = 0; _index < sizeServiceReferenceDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.ServiceReferenceDescriptionType element = getServiceReferenceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property messageDestinationDescriptor
		for (int _index = 0; _index < sizeMessageDestinationDescriptor(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.MessageDestinationDescriptorType element = getMessageDestinationDescriptor(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property sessionDescriptor
		for (int _index = 0; _index < sizeSessionDescriptor(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.SessionDescriptorType element = getSessionDescriptor(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property jspDescriptor
		for (int _index = 0; _index < sizeJspDescriptor(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.JspDescriptorType element = getJspDescriptor(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property authFilter
		// Validating property authFilterId
		// Validating property containerDescriptor
		for (int _index = 0; _index < sizeContainerDescriptor(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.ContainerDescriptorType element = getContainerDescriptor(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property charsetParams
		for (int _index = 0; _index < sizeCharsetParams(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.CharsetParamsType element = getCharsetParams(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property virtualDirectoryMapping
		for (int _index = 0; _index < sizeVirtualDirectoryMapping(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.VirtualDirectoryMappingType element = getVirtualDirectoryMapping(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property urlMatchMap
		// Validating property urlMatchMapId
		// Validating property securityPermission
		for (int _index = 0; _index < sizeSecurityPermission(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.SecurityPermissionType element = getSecurityPermission(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property contextRoot
		// Validating property contextRootId
		// Validating property wlDispatchPolicy
		// Validating property wlDispatchPolicyId
		// Validating property servletDescriptor
		for (int _index = 0; _index < sizeServletDescriptor(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.ServletDescriptorType element = getServletDescriptor(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property workManager
		for (int _index = 0; _index < sizeWorkManager(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.WorkManagerType element = getWorkManager(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property logging
		for (int _index = 0; _index < sizeLogging(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.LoggingType element = getLogging(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property libraryRef
		for (int _index = 0; _index < sizeLibraryRef(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web90.LibraryRefType element = getLibraryRef(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		out.defaultWriteObject();
		final int MAX_SIZE = 0XFFFF;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			write(baos, SERIALIZATION_HELPER_CHARSET);
			final byte [] array = baos.toByteArray();
			final int numStrings = array.length / MAX_SIZE;
			final int leftover = array.length % MAX_SIZE;
			out.writeInt(numStrings + (0 == leftover ? 0 : 1));
			out.writeInt(MAX_SIZE);
			int offset = 0;
			for (int i = 0; i < numStrings; i++){
				out.writeUTF(new String(array, offset, MAX_SIZE, SERIALIZATION_HELPER_CHARSET));
				offset += MAX_SIZE;
			}
			if (leftover > 0){
				final int count = array.length - offset;
				out.writeUTF(new String(array, offset, count, SERIALIZATION_HELPER_CHARSET));
			}
		}
		catch (Schema2BeansException ex){
			throw new Schema2BeansRuntimeException(ex);
		}
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			in.defaultReadObject();
			init(comparators, runtimeVersion);
			// init(comparators, new GenBeans.Version(1, 0, 8))
			final int numStrings = in.readInt();
			final int max_size = in.readInt();
			final StringBuilder sb = new StringBuilder(numStrings * max_size);
			for (int i = 0; i < numStrings; i++){
				sb.append(in.readUTF());
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(SERIALIZATION_HELPER_CHARSET));
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e){
			throw new RuntimeException(e);
		}
	}

	public void _setSchemaLocation(String location) {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
		}
		setAttributeValue("xsi:schemaLocation", location);
	}

	public String _getSchemaLocation() {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
		}
		return getAttributeValue("xsi:schemaLocation");
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Description["+this.sizeDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getDescription(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("WeblogicVersion["+this.sizeWeblogicVersion()+"]");	// NOI18N
		for(int i=0; i<this.sizeWeblogicVersion(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getWeblogicVersion(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(WEBLOGIC_VERSION, i, str, indent);
		}

		str.append(indent);
		str.append("SecurityRoleAssignment["+this.sizeSecurityRoleAssignment()+"]");	// NOI18N
		for(int i=0; i<this.sizeSecurityRoleAssignment(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityRoleAssignment(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SECURITY_ROLE_ASSIGNMENT, i, str, indent);
		}

		str.append(indent);
		str.append("RunAsRoleAssignment["+this.sizeRunAsRoleAssignment()+"]");	// NOI18N
		for(int i=0; i<this.sizeRunAsRoleAssignment(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getRunAsRoleAssignment(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RUN_AS_ROLE_ASSIGNMENT, i, str, indent);
		}

		str.append(indent);
		str.append("ResourceDescription["+this.sizeResourceDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("ResourceEnvDescription["+this.sizeResourceEnvDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceEnvDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceEnvDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_ENV_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("EjbReferenceDescription["+this.sizeEjbReferenceDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeEjbReferenceDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjbReferenceDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EJB_REFERENCE_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("ServiceReferenceDescription["+this.sizeServiceReferenceDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeServiceReferenceDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getServiceReferenceDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SERVICE_REFERENCE_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("MessageDestinationDescriptor["+this.sizeMessageDestinationDescriptor()+"]");	// NOI18N
		for(int i=0; i<this.sizeMessageDestinationDescriptor(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageDestinationDescriptor(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MESSAGE_DESTINATION_DESCRIPTOR, i, str, indent);
		}

		str.append(indent);
		str.append("SessionDescriptor["+this.sizeSessionDescriptor()+"]");	// NOI18N
		for(int i=0; i<this.sizeSessionDescriptor(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSessionDescriptor(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SESSION_DESCRIPTOR, i, str, indent);
		}

		str.append(indent);
		str.append("JspDescriptor["+this.sizeJspDescriptor()+"]");	// NOI18N
		for(int i=0; i<this.sizeJspDescriptor(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJspDescriptor(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JSP_DESCRIPTOR, i, str, indent);
		}

		str.append(indent);
		str.append("AuthFilter["+this.sizeAuthFilter()+"]");	// NOI18N
		for(int i=0; i<this.sizeAuthFilter(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getAuthFilter(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(AUTH_FILTER, i, str, indent);
		}

		str.append(indent);
		str.append("ContainerDescriptor["+this.sizeContainerDescriptor()+"]");	// NOI18N
		for(int i=0; i<this.sizeContainerDescriptor(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getContainerDescriptor(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONTAINER_DESCRIPTOR, i, str, indent);
		}

		str.append(indent);
		str.append("CharsetParams["+this.sizeCharsetParams()+"]");	// NOI18N
		for(int i=0; i<this.sizeCharsetParams(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCharsetParams(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CHARSET_PARAMS, i, str, indent);
		}

		str.append(indent);
		str.append("VirtualDirectoryMapping["+this.sizeVirtualDirectoryMapping()+"]");	// NOI18N
		for(int i=0; i<this.sizeVirtualDirectoryMapping(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getVirtualDirectoryMapping(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(VIRTUAL_DIRECTORY_MAPPING, i, str, indent);
		}

		str.append(indent);
		str.append("UrlMatchMap["+this.sizeUrlMatchMap()+"]");	// NOI18N
		for(int i=0; i<this.sizeUrlMatchMap(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getUrlMatchMap(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(URL_MATCH_MAP, i, str, indent);
		}

		str.append(indent);
		str.append("SecurityPermission["+this.sizeSecurityPermission()+"]");	// NOI18N
		for(int i=0; i<this.sizeSecurityPermission(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityPermission(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SECURITY_PERMISSION, i, str, indent);
		}

		str.append(indent);
		str.append("ContextRoot["+this.sizeContextRoot()+"]");	// NOI18N
		for(int i=0; i<this.sizeContextRoot(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getContextRoot(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CONTEXT_ROOT, i, str, indent);
		}

		str.append(indent);
		str.append("WlDispatchPolicy["+this.sizeWlDispatchPolicy()+"]");	// NOI18N
		for(int i=0; i<this.sizeWlDispatchPolicy(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getWlDispatchPolicy(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(WL_DISPATCH_POLICY, i, str, indent);
		}

		str.append(indent);
		str.append("ServletDescriptor["+this.sizeServletDescriptor()+"]");	// NOI18N
		for(int i=0; i<this.sizeServletDescriptor(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getServletDescriptor(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SERVLET_DESCRIPTOR, i, str, indent);
		}

		str.append(indent);
		str.append("WorkManager["+this.sizeWorkManager()+"]");	// NOI18N
		for(int i=0; i<this.sizeWorkManager(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWorkManager(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WORK_MANAGER, i, str, indent);
		}

		str.append(indent);
		str.append("Logging["+this.sizeLogging()+"]");	// NOI18N
		for(int i=0; i<this.sizeLogging(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getLogging(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LOGGING, i, str, indent);
		}

		str.append(indent);
		str.append("LibraryRef["+this.sizeLibraryRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeLibraryRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getLibraryRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LIBRARY_REF, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WeblogicWebApp\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

