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
 *	This generated bean class ContainerDescriptorType matches the schema element 'container-descriptorType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:05 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ContainerDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String REFERER_VALIDATION = "RefererValidation";	// NOI18N
	static public final String CHECK_AUTH_ON_FORWARD = "CheckAuthOnForward";	// NOI18N
	static public final String FILTER_DISPATCHED_REQUESTS_ENABLED = "FilterDispatchedRequestsEnabled";	// NOI18N
	static public final String REDIRECT_CONTENT_TYPE = "RedirectContentType";	// NOI18N
	static public final String REDIRECTCONTENTTYPEID = "RedirectContentTypeId";	// NOI18N
	static public final String REDIRECT_CONTENT = "RedirectContent";	// NOI18N
	static public final String REDIRECTCONTENTID = "RedirectContentId";	// NOI18N
	static public final String REDIRECT_WITH_ABSOLUTE_URL = "RedirectWithAbsoluteUrl";	// NOI18N
	static public final String INDEX_DIRECTORY_ENABLED = "IndexDirectoryEnabled";	// NOI18N
	static public final String INDEX_DIRECTORY_SORT_BY = "IndexDirectorySortBy";	// NOI18N
	static public final String INDEXDIRECTORYSORTBYID = "IndexDirectorySortById";	// NOI18N
	static public final String SERVLET_RELOAD_CHECK_SECS = "ServletReloadCheckSecs";	// NOI18N
	static public final String SERVLETRELOADCHECKSECSJ2EEID = "ServletReloadCheckSecsJ2eeId";	// NOI18N
	static public final String RESOURCE_RELOAD_CHECK_SECS = "ResourceReloadCheckSecs";	// NOI18N
	static public final String RESOURCERELOADCHECKSECSJ2EEID = "ResourceReloadCheckSecsJ2eeId";	// NOI18N
	static public final String SINGLE_THREADED_SERVLET_POOL_SIZE = "SingleThreadedServletPoolSize";	// NOI18N
	static public final String SINGLETHREADEDSERVLETPOOLSIZEJ2EEID = "SingleThreadedServletPoolSizeJ2eeId";	// NOI18N
	static public final String SESSION_MONITORING_ENABLED = "SessionMonitoringEnabled";	// NOI18N
	static public final String SAVE_SESSIONS_ENABLED = "SaveSessionsEnabled";	// NOI18N
	static public final String PREFER_WEB_INF_CLASSES = "PreferWebInfClasses";	// NOI18N
	static public final String PREFER_APPLICATION_PACKAGES = "PreferApplicationPackages";	// NOI18N
	static public final String PREFER_APPLICATION_RESOURCES = "PreferApplicationResources";	// NOI18N
	static public final String DEFAULT_MIME_TYPE = "DefaultMimeType";	// NOI18N
	static public final String DEFAULTMIMETYPEID = "DefaultMimeTypeId";	// NOI18N
	static public final String CLIENT_CERT_PROXY_ENABLED = "ClientCertProxyEnabled";	// NOI18N
	static public final String RELOGIN_ENABLED = "ReloginEnabled";	// NOI18N
	static public final String ALLOW_ALL_ROLES = "AllowAllRoles";	// NOI18N
	static public final String NATIVE_IO_ENABLED = "NativeIoEnabled";	// NOI18N
	static public final String MINIMUM_NATIVE_FILE_SIZE = "MinimumNativeFileSize";	// NOI18N
	static public final String DISABLE_IMPLICIT_SERVLET_MAPPINGS = "DisableImplicitServletMappings";	// NOI18N
	static public final String TEMP_DIR = "TempDir";	// NOI18N
	static public final String OPTIMISTIC_SERIALIZATION = "OptimisticSerialization";	// NOI18N
	static public final String RETAIN_ORIGINAL_URL = "RetainOriginalUrl";	// NOI18N
	static public final String SHOW_ARCHIVED_REAL_PATH_ENABLED = "ShowArchivedRealPathEnabled";	// NOI18N
	static public final String REQUIRE_ADMIN_TRAFFIC = "RequireAdminTraffic";	// NOI18N
	static public final String ACCESS_LOGGING_DISABLED = "AccessLoggingDisabled";	// NOI18N
	static public final String PREFER_FORWARD_QUERY_STRING = "PreferForwardQueryString";	// NOI18N
	static public final String FAIL_DEPLOY_ON_FILTER_INIT_ERROR = "FailDeployOnFilterInitError";	// NOI18N
	static public final String SEND_PERMANENT_REDIRECTS = "SendPermanentRedirects";	// NOI18N
	static public final String CONTAINER_INITIALIZER_ENABLED = "ContainerInitializerEnabled";	// NOI18N
	static public final String LANGTAG_REVISION = "LangtagRevision";	// NOI18N
	static public final String GZIP_COMPRESSION = "GzipCompression";	// NOI18N

	public ContainerDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ContainerDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(35);
		this.createProperty("referer-validation", 	// NOI18N
			REFERER_VALIDATION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("check-auth-on-forward", 	// NOI18N
			CHECK_AUTH_ON_FORWARD, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EmptyType.class);
		this.createAttribute(CHECK_AUTH_ON_FORWARD, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("filter-dispatched-requests-enabled", 	// NOI18N
			FILTER_DISPATCHED_REQUESTS_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("redirect-content-type", 	// NOI18N
			REDIRECT_CONTENT_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(REDIRECT_CONTENT_TYPE, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("redirect-content", 	// NOI18N
			REDIRECT_CONTENT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(REDIRECT_CONTENT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("redirect-with-absolute-url", 	// NOI18N
			REDIRECT_WITH_ABSOLUTE_URL, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("index-directory-enabled", 	// NOI18N
			INDEX_DIRECTORY_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("index-directory-sort-by", 	// NOI18N
			INDEX_DIRECTORY_SORT_BY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(INDEX_DIRECTORY_SORT_BY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("servlet-reload-check-secs", 	// NOI18N
			SERVLET_RELOAD_CHECK_SECS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(SERVLET_RELOAD_CHECK_SECS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("resource-reload-check-secs", 	// NOI18N
			RESOURCE_RELOAD_CHECK_SECS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(RESOURCE_RELOAD_CHECK_SECS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("single-threaded-servlet-pool-size", 	// NOI18N
			SINGLE_THREADED_SERVLET_POOL_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(SINGLE_THREADED_SERVLET_POOL_SIZE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("session-monitoring-enabled", 	// NOI18N
			SESSION_MONITORING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("save-sessions-enabled", 	// NOI18N
			SAVE_SESSIONS_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("prefer-web-inf-classes", 	// NOI18N
			PREFER_WEB_INF_CLASSES, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("prefer-application-packages", 	// NOI18N
			PREFER_APPLICATION_PACKAGES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PreferApplicationPackagesType.class);
		this.createProperty("prefer-application-resources", 	// NOI18N
			PREFER_APPLICATION_RESOURCES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PreferApplicationResourcesType.class);
		this.createProperty("default-mime-type", 	// NOI18N
			DEFAULT_MIME_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DEFAULT_MIME_TYPE, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("client-cert-proxy-enabled", 	// NOI18N
			CLIENT_CERT_PROXY_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("relogin-enabled", 	// NOI18N
			RELOGIN_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("allow-all-roles", 	// NOI18N
			ALLOW_ALL_ROLES, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("native-io-enabled", 	// NOI18N
			NATIVE_IO_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("minimum-native-file-size", 	// NOI18N
			MINIMUM_NATIVE_FILE_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("disable-implicit-servlet-mappings", 	// NOI18N
			DISABLE_IMPLICIT_SERVLET_MAPPINGS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("temp-dir", 	// NOI18N
			TEMP_DIR, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("optimistic-serialization", 	// NOI18N
			OPTIMISTIC_SERIALIZATION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("retain-original-url", 	// NOI18N
			RETAIN_ORIGINAL_URL, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("show-archived-real-path-enabled", 	// NOI18N
			SHOW_ARCHIVED_REAL_PATH_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("require-admin-traffic", 	// NOI18N
			REQUIRE_ADMIN_TRAFFIC, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("access-logging-disabled", 	// NOI18N
			ACCESS_LOGGING_DISABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("prefer-forward-query-string", 	// NOI18N
			PREFER_FORWARD_QUERY_STRING, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("fail-deploy-on-filter-init-error", 	// NOI18N
			FAIL_DEPLOY_ON_FILTER_INIT_ERROR, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("send-permanent-redirects", 	// NOI18N
			SEND_PERMANENT_REDIRECTS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("container-initializer-enabled", 	// NOI18N
			CONTAINER_INITIALIZER_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("langtag-revision", 	// NOI18N
			LANGTAG_REVISION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("gzip-compression", 	// NOI18N
			GZIP_COMPRESSION, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			GzipCompressionType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		setAttributeValue(ID, value);
	}

	//
	public java.lang.String getId() {
		return getAttributeValue(ID);
	}

	// This attribute is optional
	public void setRefererValidation(java.lang.String value) {
		this.setValue(REFERER_VALIDATION, value);
	}

	//
	public java.lang.String getRefererValidation() {
		return (java.lang.String)this.getValue(REFERER_VALIDATION);
	}

	// This attribute is optional
	public void setCheckAuthOnForward(EmptyType value) {
		this.setValue(CHECK_AUTH_ON_FORWARD, value);
	}

	//
	public EmptyType getCheckAuthOnForward() {
		return (EmptyType)this.getValue(CHECK_AUTH_ON_FORWARD);
	}

	// This attribute is optional
	public void setFilterDispatchedRequestsEnabled(boolean value) {
		this.setValue(FILTER_DISPATCHED_REQUESTS_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isFilterDispatchedRequestsEnabled() {
		Boolean ret = (Boolean)this.getValue(FILTER_DISPATCHED_REQUESTS_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRedirectContentType(java.lang.String value) {
		this.setValue(REDIRECT_CONTENT_TYPE, value);
	}

	//
	public java.lang.String getRedirectContentType() {
		return (java.lang.String)this.getValue(REDIRECT_CONTENT_TYPE);
	}

	// This attribute is optional
	public void setRedirectContentTypeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REDIRECT_CONTENT_TYPE) == 0) {
			setValue(REDIRECT_CONTENT_TYPE, "");
		}
		setAttributeValue(REDIRECT_CONTENT_TYPE, "Id", value);
	}

	//
	public java.lang.String getRedirectContentTypeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REDIRECT_CONTENT_TYPE) == 0) {
			return null;
		} else {
			return getAttributeValue(REDIRECT_CONTENT_TYPE, "Id");
		}
	}

	// This attribute is optional
	public void setRedirectContent(java.lang.String value) {
		this.setValue(REDIRECT_CONTENT, value);
	}

	//
	public java.lang.String getRedirectContent() {
		return (java.lang.String)this.getValue(REDIRECT_CONTENT);
	}

	// This attribute is optional
	public void setRedirectContentId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REDIRECT_CONTENT) == 0) {
			setValue(REDIRECT_CONTENT, "");
		}
		setAttributeValue(REDIRECT_CONTENT, "Id", value);
	}

	//
	public java.lang.String getRedirectContentId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REDIRECT_CONTENT) == 0) {
			return null;
		} else {
			return getAttributeValue(REDIRECT_CONTENT, "Id");
		}
	}

	// This attribute is optional
	public void setRedirectWithAbsoluteUrl(boolean value) {
		this.setValue(REDIRECT_WITH_ABSOLUTE_URL, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRedirectWithAbsoluteUrl() {
		Boolean ret = (Boolean)this.getValue(REDIRECT_WITH_ABSOLUTE_URL);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setIndexDirectoryEnabled(boolean value) {
		this.setValue(INDEX_DIRECTORY_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isIndexDirectoryEnabled() {
		Boolean ret = (Boolean)this.getValue(INDEX_DIRECTORY_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setIndexDirectorySortBy(java.lang.String value) {
		this.setValue(INDEX_DIRECTORY_SORT_BY, value);
	}

	//
	public java.lang.String getIndexDirectorySortBy() {
		return (java.lang.String)this.getValue(INDEX_DIRECTORY_SORT_BY);
	}

	// This attribute is optional
	public void setIndexDirectorySortById(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INDEX_DIRECTORY_SORT_BY) == 0) {
			setValue(INDEX_DIRECTORY_SORT_BY, "");
		}
		setAttributeValue(INDEX_DIRECTORY_SORT_BY, "Id", value);
	}

	//
	public java.lang.String getIndexDirectorySortById() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INDEX_DIRECTORY_SORT_BY) == 0) {
			return null;
		} else {
			return getAttributeValue(INDEX_DIRECTORY_SORT_BY, "Id");
		}
	}

	// This attribute is optional
	public void setServletReloadCheckSecs(java.math.BigInteger value) {
		this.setValue(SERVLET_RELOAD_CHECK_SECS, value);
	}

	//
	public java.math.BigInteger getServletReloadCheckSecs() {
		return (java.math.BigInteger)this.getValue(SERVLET_RELOAD_CHECK_SECS);
	}

	// This attribute is optional
	public void setServletReloadCheckSecsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(SERVLET_RELOAD_CHECK_SECS) == 0) {
			setValue(SERVLET_RELOAD_CHECK_SECS, "");
		}
		setAttributeValue(SERVLET_RELOAD_CHECK_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getServletReloadCheckSecsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(SERVLET_RELOAD_CHECK_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(SERVLET_RELOAD_CHECK_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setResourceReloadCheckSecs(java.math.BigInteger value) {
		this.setValue(RESOURCE_RELOAD_CHECK_SECS, value);
	}

	//
	public java.math.BigInteger getResourceReloadCheckSecs() {
		return (java.math.BigInteger)this.getValue(RESOURCE_RELOAD_CHECK_SECS);
	}

	// This attribute is optional
	public void setResourceReloadCheckSecsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(SERVLET_RELOAD_CHECK_SECS) == 0) {
			setValue(SERVLET_RELOAD_CHECK_SECS, "");
		}
		setAttributeValue(SERVLET_RELOAD_CHECK_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getResourceReloadCheckSecsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(SERVLET_RELOAD_CHECK_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(SERVLET_RELOAD_CHECK_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setSingleThreadedServletPoolSize(long value) {
		this.setValue(SINGLE_THREADED_SERVLET_POOL_SIZE, java.lang.Long.valueOf(value));
	}

	//
	public long getSingleThreadedServletPoolSize() {
		Long ret = (Long)this.getValue(SINGLE_THREADED_SERVLET_POOL_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"SINGLE_THREADED_SERVLET_POOL_SIZE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setSingleThreadedServletPoolSizeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(SINGLE_THREADED_SERVLET_POOL_SIZE) == 0) {
			setValue(SINGLE_THREADED_SERVLET_POOL_SIZE, "");
		}
		setAttributeValue(SINGLE_THREADED_SERVLET_POOL_SIZE, "J2eeId", value);
	}

	//
	public java.lang.String getSingleThreadedServletPoolSizeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(SINGLE_THREADED_SERVLET_POOL_SIZE) == 0) {
			return null;
		} else {
			return getAttributeValue(SINGLE_THREADED_SERVLET_POOL_SIZE, "J2eeId");
		}
	}

	// This attribute is optional
	public void setSessionMonitoringEnabled(boolean value) {
		this.setValue(SESSION_MONITORING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isSessionMonitoringEnabled() {
		Boolean ret = (Boolean)this.getValue(SESSION_MONITORING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setSaveSessionsEnabled(boolean value) {
		this.setValue(SAVE_SESSIONS_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isSaveSessionsEnabled() {
		Boolean ret = (Boolean)this.getValue(SAVE_SESSIONS_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPreferWebInfClasses(boolean value) {
		this.setValue(PREFER_WEB_INF_CLASSES, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPreferWebInfClasses() {
		Boolean ret = (Boolean)this.getValue(PREFER_WEB_INF_CLASSES);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPreferApplicationPackages(PreferApplicationPackagesType value) {
		this.setValue(PREFER_APPLICATION_PACKAGES, value);
	}

	//
	public PreferApplicationPackagesType getPreferApplicationPackages() {
		return (PreferApplicationPackagesType)this.getValue(PREFER_APPLICATION_PACKAGES);
	}

	// This attribute is optional
	public void setPreferApplicationResources(PreferApplicationResourcesType value) {
		this.setValue(PREFER_APPLICATION_RESOURCES, value);
	}

	//
	public PreferApplicationResourcesType getPreferApplicationResources() {
		return (PreferApplicationResourcesType)this.getValue(PREFER_APPLICATION_RESOURCES);
	}

	// This attribute is optional
	public void setDefaultMimeType(java.lang.String value) {
		this.setValue(DEFAULT_MIME_TYPE, value);
	}

	//
	public java.lang.String getDefaultMimeType() {
		return (java.lang.String)this.getValue(DEFAULT_MIME_TYPE);
	}

	// This attribute is optional
	public void setDefaultMimeTypeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DEFAULT_MIME_TYPE) == 0) {
			setValue(DEFAULT_MIME_TYPE, "");
		}
		setAttributeValue(DEFAULT_MIME_TYPE, "Id", value);
	}

	//
	public java.lang.String getDefaultMimeTypeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DEFAULT_MIME_TYPE) == 0) {
			return null;
		} else {
			return getAttributeValue(DEFAULT_MIME_TYPE, "Id");
		}
	}

	// This attribute is optional
	public void setClientCertProxyEnabled(boolean value) {
		this.setValue(CLIENT_CERT_PROXY_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isClientCertProxyEnabled() {
		Boolean ret = (Boolean)this.getValue(CLIENT_CERT_PROXY_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setReloginEnabled(boolean value) {
		this.setValue(RELOGIN_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isReloginEnabled() {
		Boolean ret = (Boolean)this.getValue(RELOGIN_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setAllowAllRoles(boolean value) {
		this.setValue(ALLOW_ALL_ROLES, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isAllowAllRoles() {
		Boolean ret = (Boolean)this.getValue(ALLOW_ALL_ROLES);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setNativeIoEnabled(boolean value) {
		this.setValue(NATIVE_IO_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isNativeIoEnabled() {
		Boolean ret = (Boolean)this.getValue(NATIVE_IO_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setMinimumNativeFileSize(long value) {
		this.setValue(MINIMUM_NATIVE_FILE_SIZE, java.lang.Long.valueOf(value));
	}

	//
	public long getMinimumNativeFileSize() {
		Long ret = (Long)this.getValue(MINIMUM_NATIVE_FILE_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MINIMUM_NATIVE_FILE_SIZE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setDisableImplicitServletMappings(boolean value) {
		this.setValue(DISABLE_IMPLICIT_SERVLET_MAPPINGS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDisableImplicitServletMappings() {
		Boolean ret = (Boolean)this.getValue(DISABLE_IMPLICIT_SERVLET_MAPPINGS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setTempDir(java.lang.String value) {
		this.setValue(TEMP_DIR, value);
	}

	//
	public java.lang.String getTempDir() {
		return (java.lang.String)this.getValue(TEMP_DIR);
	}

	// This attribute is optional
	public void setOptimisticSerialization(boolean value) {
		this.setValue(OPTIMISTIC_SERIALIZATION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isOptimisticSerialization() {
		Boolean ret = (Boolean)this.getValue(OPTIMISTIC_SERIALIZATION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRetainOriginalUrl(boolean value) {
		this.setValue(RETAIN_ORIGINAL_URL, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRetainOriginalUrl() {
		Boolean ret = (Boolean)this.getValue(RETAIN_ORIGINAL_URL);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setShowArchivedRealPathEnabled(boolean value) {
		this.setValue(SHOW_ARCHIVED_REAL_PATH_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isShowArchivedRealPathEnabled() {
		Boolean ret = (Boolean)this.getValue(SHOW_ARCHIVED_REAL_PATH_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRequireAdminTraffic(boolean value) {
		this.setValue(REQUIRE_ADMIN_TRAFFIC, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRequireAdminTraffic() {
		Boolean ret = (Boolean)this.getValue(REQUIRE_ADMIN_TRAFFIC);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setAccessLoggingDisabled(boolean value) {
		this.setValue(ACCESS_LOGGING_DISABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isAccessLoggingDisabled() {
		Boolean ret = (Boolean)this.getValue(ACCESS_LOGGING_DISABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPreferForwardQueryString(boolean value) {
		this.setValue(PREFER_FORWARD_QUERY_STRING, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPreferForwardQueryString() {
		Boolean ret = (Boolean)this.getValue(PREFER_FORWARD_QUERY_STRING);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setFailDeployOnFilterInitError(boolean value) {
		this.setValue(FAIL_DEPLOY_ON_FILTER_INIT_ERROR, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isFailDeployOnFilterInitError() {
		Boolean ret = (Boolean)this.getValue(FAIL_DEPLOY_ON_FILTER_INIT_ERROR);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setSendPermanentRedirects(boolean value) {
		this.setValue(SEND_PERMANENT_REDIRECTS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isSendPermanentRedirects() {
		Boolean ret = (Boolean)this.getValue(SEND_PERMANENT_REDIRECTS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setContainerInitializerEnabled(boolean value) {
		this.setValue(CONTAINER_INITIALIZER_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isContainerInitializerEnabled() {
		Boolean ret = (Boolean)this.getValue(CONTAINER_INITIALIZER_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setLangtagRevision(java.lang.String value) {
		this.setValue(LANGTAG_REVISION, value);
	}

	//
	public java.lang.String getLangtagRevision() {
		return (java.lang.String)this.getValue(LANGTAG_REVISION);
	}

	// This attribute is optional
	public void setGzipCompression(GzipCompressionType value) {
		this.setValue(GZIP_COMPRESSION, value);
	}

	//
	public GzipCompressionType getGzipCompression() {
		return (GzipCompressionType)this.getValue(GZIP_COMPRESSION);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EmptyType newEmptyType() {
		return new EmptyType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PreferApplicationPackagesType newPreferApplicationPackagesType() {
		return new PreferApplicationPackagesType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PreferApplicationResourcesType newPreferApplicationResourcesType() {
		return new PreferApplicationResourcesType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public GzipCompressionType newGzipCompressionType() {
		return new GzipCompressionType();
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
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
		// Validating property refererValidation
		if (getRefererValidation() != null) {
			final java.lang.String[] enumRestrictionRefererValidation = {"NONE", "LENIENT", "STRICT"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionRefererValidation.length; ++_index2) {
				if (enumRestrictionRefererValidation[_index2].equals(getRefererValidation())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRefererValidation() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "refererValidation", this);	// NOI18N
			}
		}
		// Validating property checkAuthOnForward
		if (getCheckAuthOnForward() != null) {
			getCheckAuthOnForward().validate();
		}
		// Validating property filterDispatchedRequestsEnabled
		{
			boolean patternPassed = false;
			if ((isFilterDispatchedRequestsEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isFilterDispatchedRequestsEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "filterDispatchedRequestsEnabled", this);	// NOI18N
		}
		// Validating property redirectContentType
		// Validating property redirectContentTypeId
		if (getRedirectContentTypeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRedirectContentTypeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "redirectContentTypeId", this);	// NOI18N
			}
		}
		// Validating property redirectContent
		// Validating property redirectContentId
		if (getRedirectContentId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRedirectContentId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "redirectContentId", this);	// NOI18N
			}
		}
		// Validating property redirectWithAbsoluteUrl
		{
			boolean patternPassed = false;
			if ((isRedirectWithAbsoluteUrl() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRedirectWithAbsoluteUrl()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "redirectWithAbsoluteUrl", this);	// NOI18N
		}
		// Validating property indexDirectoryEnabled
		{
			boolean patternPassed = false;
			if ((isIndexDirectoryEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isIndexDirectoryEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "indexDirectoryEnabled", this);	// NOI18N
		}
		// Validating property indexDirectorySortBy
		// Validating property indexDirectorySortById
		if (getIndexDirectorySortById() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIndexDirectorySortById() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "indexDirectorySortById", this);	// NOI18N
			}
		}
		// Validating property servletReloadCheckSecs
		// Validating property servletReloadCheckSecsJ2eeId
		if (getServletReloadCheckSecsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getServletReloadCheckSecsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "servletReloadCheckSecsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property resourceReloadCheckSecs
		// Validating property resourceReloadCheckSecsJ2eeId
		if (getResourceReloadCheckSecsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getResourceReloadCheckSecsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "resourceReloadCheckSecsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property singleThreadedServletPoolSize
		if (getSingleThreadedServletPoolSize() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getSingleThreadedServletPoolSize() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "singleThreadedServletPoolSize", this);	// NOI18N
		}
		// Validating property singleThreadedServletPoolSizeJ2eeId
		if (getSingleThreadedServletPoolSizeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSingleThreadedServletPoolSizeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "singleThreadedServletPoolSizeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property sessionMonitoringEnabled
		{
			boolean patternPassed = false;
			if ((isSessionMonitoringEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isSessionMonitoringEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "sessionMonitoringEnabled", this);	// NOI18N
		}
		// Validating property saveSessionsEnabled
		{
			boolean patternPassed = false;
			if ((isSaveSessionsEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isSaveSessionsEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "saveSessionsEnabled", this);	// NOI18N
		}
		// Validating property preferWebInfClasses
		{
			boolean patternPassed = false;
			if ((isPreferWebInfClasses() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isPreferWebInfClasses()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "preferWebInfClasses", this);	// NOI18N
		}
		// Validating property preferApplicationPackages
		if (getPreferApplicationPackages() != null) {
			getPreferApplicationPackages().validate();
		}
		// Validating property preferApplicationResources
		if (getPreferApplicationResources() != null) {
			getPreferApplicationResources().validate();
		}
		// Validating property defaultMimeType
		// Validating property defaultMimeTypeId
		if (getDefaultMimeTypeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDefaultMimeTypeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "defaultMimeTypeId", this);	// NOI18N
			}
		}
		// Validating property clientCertProxyEnabled
		{
			boolean patternPassed = false;
			if ((isClientCertProxyEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isClientCertProxyEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "clientCertProxyEnabled", this);	// NOI18N
		}
		// Validating property reloginEnabled
		{
			boolean patternPassed = false;
			if ((isReloginEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isReloginEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "reloginEnabled", this);	// NOI18N
		}
		// Validating property allowAllRoles
		{
			boolean patternPassed = false;
			if ((isAllowAllRoles() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isAllowAllRoles()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "allowAllRoles", this);	// NOI18N
		}
		// Validating property nativeIoEnabled
		{
			boolean patternPassed = false;
			if ((isNativeIoEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isNativeIoEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "nativeIoEnabled", this);	// NOI18N
		}
		// Validating property minimumNativeFileSize
		// Validating property disableImplicitServletMappings
		{
			boolean patternPassed = false;
			if ((isDisableImplicitServletMappings() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isDisableImplicitServletMappings()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "disableImplicitServletMappings", this);	// NOI18N
		}
		// Validating property tempDir
		// Validating property optimisticSerialization
		{
			boolean patternPassed = false;
			if ((isOptimisticSerialization() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isOptimisticSerialization()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "optimisticSerialization", this);	// NOI18N
		}
		// Validating property retainOriginalUrl
		{
			boolean patternPassed = false;
			if ((isRetainOriginalUrl() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRetainOriginalUrl()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "retainOriginalUrl", this);	// NOI18N
		}
		// Validating property showArchivedRealPathEnabled
		{
			boolean patternPassed = false;
			if ((isShowArchivedRealPathEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isShowArchivedRealPathEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "showArchivedRealPathEnabled", this);	// NOI18N
		}
		// Validating property requireAdminTraffic
		{
			boolean patternPassed = false;
			if ((isRequireAdminTraffic() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRequireAdminTraffic()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "requireAdminTraffic", this);	// NOI18N
		}
		// Validating property accessLoggingDisabled
		{
			boolean patternPassed = false;
			if ((isAccessLoggingDisabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isAccessLoggingDisabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "accessLoggingDisabled", this);	// NOI18N
		}
		// Validating property preferForwardQueryString
		{
			boolean patternPassed = false;
			if ((isPreferForwardQueryString() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isPreferForwardQueryString()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "preferForwardQueryString", this);	// NOI18N
		}
		// Validating property failDeployOnFilterInitError
		{
			boolean patternPassed = false;
			if ((isFailDeployOnFilterInitError() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isFailDeployOnFilterInitError()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "failDeployOnFilterInitError", this);	// NOI18N
		}
		// Validating property sendPermanentRedirects
		{
			boolean patternPassed = false;
			if ((isSendPermanentRedirects() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isSendPermanentRedirects()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "sendPermanentRedirects", this);	// NOI18N
		}
		// Validating property containerInitializerEnabled
		{
			boolean patternPassed = false;
			if ((isContainerInitializerEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isContainerInitializerEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "containerInitializerEnabled", this);	// NOI18N
		}
		// Validating property langtagRevision
		// Validating property gzipCompression
		if (getGzipCompression() != null) {
			getGzipCompression().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("RefererValidation");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRefererValidation();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REFERER_VALIDATION, 0, str, indent);

		str.append(indent);
		str.append("CheckAuthOnForward");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCheckAuthOnForward();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CHECK_AUTH_ON_FORWARD, 0, str, indent);

		str.append(indent);
		str.append("FilterDispatchedRequestsEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isFilterDispatchedRequestsEnabled()?"true":"false"));
		this.dumpAttributes(FILTER_DISPATCHED_REQUESTS_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("RedirectContentType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRedirectContentType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REDIRECT_CONTENT_TYPE, 0, str, indent);

		str.append(indent);
		str.append("RedirectContent");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRedirectContent();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REDIRECT_CONTENT, 0, str, indent);

		str.append(indent);
		str.append("RedirectWithAbsoluteUrl");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRedirectWithAbsoluteUrl()?"true":"false"));
		this.dumpAttributes(REDIRECT_WITH_ABSOLUTE_URL, 0, str, indent);

		str.append(indent);
		str.append("IndexDirectoryEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isIndexDirectoryEnabled()?"true":"false"));
		this.dumpAttributes(INDEX_DIRECTORY_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("IndexDirectorySortBy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getIndexDirectorySortBy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INDEX_DIRECTORY_SORT_BY, 0, str, indent);

		str.append(indent);
		str.append("ServletReloadCheckSecs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getServletReloadCheckSecs();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVLET_RELOAD_CHECK_SECS, 0, str, indent);

		str.append(indent);
		str.append("ResourceReloadCheckSecs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getResourceReloadCheckSecs();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RESOURCE_RELOAD_CHECK_SECS, 0, str, indent);

		if (this.getValue(SINGLE_THREADED_SERVLET_POOL_SIZE) != null) {
			str.append(indent);
			str.append("SingleThreadedServletPoolSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getSingleThreadedServletPoolSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SINGLE_THREADED_SERVLET_POOL_SIZE, 0, str, indent);
		}

		str.append(indent);
		str.append("SessionMonitoringEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isSessionMonitoringEnabled()?"true":"false"));
		this.dumpAttributes(SESSION_MONITORING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("SaveSessionsEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isSaveSessionsEnabled()?"true":"false"));
		this.dumpAttributes(SAVE_SESSIONS_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("PreferWebInfClasses");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPreferWebInfClasses()?"true":"false"));
		this.dumpAttributes(PREFER_WEB_INF_CLASSES, 0, str, indent);

		str.append(indent);
		str.append("PreferApplicationPackages");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPreferApplicationPackages();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PREFER_APPLICATION_PACKAGES, 0, str, indent);

		str.append(indent);
		str.append("PreferApplicationResources");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPreferApplicationResources();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PREFER_APPLICATION_RESOURCES, 0, str, indent);

		str.append(indent);
		str.append("DefaultMimeType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDefaultMimeType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DEFAULT_MIME_TYPE, 0, str, indent);

		str.append(indent);
		str.append("ClientCertProxyEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isClientCertProxyEnabled()?"true":"false"));
		this.dumpAttributes(CLIENT_CERT_PROXY_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("ReloginEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isReloginEnabled()?"true":"false"));
		this.dumpAttributes(RELOGIN_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("AllowAllRoles");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isAllowAllRoles()?"true":"false"));
		this.dumpAttributes(ALLOW_ALL_ROLES, 0, str, indent);

		str.append(indent);
		str.append("NativeIoEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isNativeIoEnabled()?"true":"false"));
		this.dumpAttributes(NATIVE_IO_ENABLED, 0, str, indent);

		if (this.getValue(MINIMUM_NATIVE_FILE_SIZE) != null) {
			str.append(indent);
			str.append("MinimumNativeFileSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMinimumNativeFileSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MINIMUM_NATIVE_FILE_SIZE, 0, str, indent);
		}

		str.append(indent);
		str.append("DisableImplicitServletMappings");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDisableImplicitServletMappings()?"true":"false"));
		this.dumpAttributes(DISABLE_IMPLICIT_SERVLET_MAPPINGS, 0, str, indent);

		str.append(indent);
		str.append("TempDir");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTempDir();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TEMP_DIR, 0, str, indent);

		str.append(indent);
		str.append("OptimisticSerialization");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isOptimisticSerialization()?"true":"false"));
		this.dumpAttributes(OPTIMISTIC_SERIALIZATION, 0, str, indent);

		str.append(indent);
		str.append("RetainOriginalUrl");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRetainOriginalUrl()?"true":"false"));
		this.dumpAttributes(RETAIN_ORIGINAL_URL, 0, str, indent);

		str.append(indent);
		str.append("ShowArchivedRealPathEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isShowArchivedRealPathEnabled()?"true":"false"));
		this.dumpAttributes(SHOW_ARCHIVED_REAL_PATH_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("RequireAdminTraffic");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRequireAdminTraffic()?"true":"false"));
		this.dumpAttributes(REQUIRE_ADMIN_TRAFFIC, 0, str, indent);

		str.append(indent);
		str.append("AccessLoggingDisabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isAccessLoggingDisabled()?"true":"false"));
		this.dumpAttributes(ACCESS_LOGGING_DISABLED, 0, str, indent);

		str.append(indent);
		str.append("PreferForwardQueryString");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPreferForwardQueryString()?"true":"false"));
		this.dumpAttributes(PREFER_FORWARD_QUERY_STRING, 0, str, indent);

		str.append(indent);
		str.append("FailDeployOnFilterInitError");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isFailDeployOnFilterInitError()?"true":"false"));
		this.dumpAttributes(FAIL_DEPLOY_ON_FILTER_INIT_ERROR, 0, str, indent);

		str.append(indent);
		str.append("SendPermanentRedirects");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isSendPermanentRedirects()?"true":"false"));
		this.dumpAttributes(SEND_PERMANENT_REDIRECTS, 0, str, indent);

		str.append(indent);
		str.append("ContainerInitializerEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isContainerInitializerEnabled()?"true":"false"));
		this.dumpAttributes(CONTAINER_INITIALIZER_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("LangtagRevision");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLangtagRevision();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LANGTAG_REVISION, 0, str, indent);

		str.append(indent);
		str.append("GzipCompression");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getGzipCompression();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(GZIP_COMPRESSION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ContainerDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

