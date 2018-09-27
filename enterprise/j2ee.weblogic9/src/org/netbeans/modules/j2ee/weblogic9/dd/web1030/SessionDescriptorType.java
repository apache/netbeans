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
 *	This generated bean class SessionDescriptorType matches the schema element 'session-descriptorType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:01 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SessionDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String TIMEOUT_SECS = "TimeoutSecs";	// NOI18N
	static public final String TIMEOUTSECSJ2EEID = "TimeoutSecsJ2eeId";	// NOI18N
	static public final String INVALIDATION_INTERVAL_SECS = "InvalidationIntervalSecs";	// NOI18N
	static public final String INVALIDATIONINTERVALSECSJ2EEID = "InvalidationIntervalSecsJ2eeId";	// NOI18N
	static public final String DEBUG_ENABLED = "DebugEnabled";	// NOI18N
	static public final String ID_LENGTH = "IdLength";	// NOI18N
	static public final String IDLENGTHJ2EEID = "IdLengthJ2eeId";	// NOI18N
	static public final String TRACKING_ENABLED = "TrackingEnabled";	// NOI18N
	static public final String CACHE_SIZE = "CacheSize";	// NOI18N
	static public final String CACHESIZEJ2EEID = "CacheSizeJ2eeId";	// NOI18N
	static public final String MAX_IN_MEMORY_SESSIONS = "MaxInMemorySessions";	// NOI18N
	static public final String MAXINMEMORYSESSIONSJ2EEID = "MaxInMemorySessionsJ2eeId";	// NOI18N
	static public final String COOKIES_ENABLED = "CookiesEnabled";	// NOI18N
	static public final String COOKIE_NAME = "CookieName";	// NOI18N
	static public final String COOKIE_PATH = "CookiePath";	// NOI18N
	static public final String COOKIE_DOMAIN = "CookieDomain";	// NOI18N
	static public final String COOKIE_COMMENT = "CookieComment";	// NOI18N
	static public final String COOKIE_SECURE = "CookieSecure";	// NOI18N
	static public final String COOKIE_MAX_AGE_SECS = "CookieMaxAgeSecs";	// NOI18N
	static public final String COOKIEMAXAGESECSJ2EEID = "CookieMaxAgeSecsJ2eeId";	// NOI18N
	static public final String PERSISTENT_STORE_TYPE = "PersistentStoreType";	// NOI18N
	static public final String PERSISTENT_STORE_COOKIE_NAME = "PersistentStoreCookieName";	// NOI18N
	static public final String PERSISTENT_STORE_DIR = "PersistentStoreDir";	// NOI18N
	static public final String PERSISTENT_STORE_POOL = "PersistentStorePool";	// NOI18N
	static public final String PERSISTENT_DATA_SOURCE_JNDI_NAME = "PersistentDataSourceJndiName";	// NOI18N
	static public final String PERSISTENT_SESSION_FLUSH_INTERVAL = "PersistentSessionFlushInterval";	// NOI18N
	static public final String PERSISTENTSESSIONFLUSHINTERVALJ2EEID = "PersistentSessionFlushIntervalJ2eeId";	// NOI18N
	static public final String PERSISTENT_SESSION_FLUSH_THRESHOLD = "PersistentSessionFlushThreshold";	// NOI18N
	static public final String PERSISTENTSESSIONFLUSHTHRESHOLDJ2EEID = "PersistentSessionFlushThresholdJ2eeId";	// NOI18N
	static public final String PERSISTENT_ASYNC_QUEUE_TIMEOUT = "PersistentAsyncQueueTimeout";	// NOI18N
	static public final String PERSISTENTASYNCQUEUETIMEOUTJ2EEID = "PersistentAsyncQueueTimeoutJ2eeId";	// NOI18N
	static public final String PERSISTENT_STORE_TABLE = "PersistentStoreTable";	// NOI18N
	static public final String JDBC_COLUMN_NAME_MAX_INACTIVE_INTERVAL = "JdbcColumnNameMaxInactiveInterval";	// NOI18N
	static public final String JDBC_CONNECTION_TIMEOUT_SECS = "JdbcConnectionTimeoutSecs";	// NOI18N
	static public final String JDBCCONNECTIONTIMEOUTSECSJ2EEID = "JdbcConnectionTimeoutSecsJ2eeId";	// NOI18N
	static public final String URL_REWRITING_ENABLED = "UrlRewritingEnabled";	// NOI18N
	static public final String HTTP_PROXY_CACHING_OF_COOKIES = "HttpProxyCachingOfCookies";	// NOI18N
	static public final String ENCODE_SESSION_ID_IN_QUERY_PARAMS = "EncodeSessionIdInQueryParams";	// NOI18N
	static public final String MONITORING_ATTRIBUTE_NAME = "MonitoringAttributeName";	// NOI18N
	static public final String SHARING_ENABLED = "SharingEnabled";	// NOI18N

	public SessionDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SessionDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(30);
		this.createProperty("timeout-secs", 	// NOI18N
			TIMEOUT_SECS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(TIMEOUT_SECS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("invalidation-interval-secs", 	// NOI18N
			INVALIDATION_INTERVAL_SECS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(INVALIDATION_INTERVAL_SECS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("debug-enabled", 	// NOI18N
			DEBUG_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("id-length", 	// NOI18N
			ID_LENGTH, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(ID_LENGTH, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("tracking-enabled", 	// NOI18N
			TRACKING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("cache-size", 	// NOI18N
			CACHE_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(CACHE_SIZE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("max-in-memory-sessions", 	// NOI18N
			MAX_IN_MEMORY_SESSIONS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(MAX_IN_MEMORY_SESSIONS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("cookies-enabled", 	// NOI18N
			COOKIES_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("cookie-name", 	// NOI18N
			COOKIE_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("cookie-path", 	// NOI18N
			COOKIE_PATH, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("cookie-domain", 	// NOI18N
			COOKIE_DOMAIN, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("cookie-comment", 	// NOI18N
			COOKIE_COMMENT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("cookie-secure", 	// NOI18N
			COOKIE_SECURE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("cookie-max-age-secs", 	// NOI18N
			COOKIE_MAX_AGE_SECS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(COOKIE_MAX_AGE_SECS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("persistent-store-type", 	// NOI18N
			PERSISTENT_STORE_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("persistent-store-cookie-name", 	// NOI18N
			PERSISTENT_STORE_COOKIE_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("persistent-store-dir", 	// NOI18N
			PERSISTENT_STORE_DIR, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("persistent-store-pool", 	// NOI18N
			PERSISTENT_STORE_POOL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("persistent-data-source-jndi-name", 	// NOI18N
			PERSISTENT_DATA_SOURCE_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("persistent-session-flush-interval", 	// NOI18N
			PERSISTENT_SESSION_FLUSH_INTERVAL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(PERSISTENT_SESSION_FLUSH_INTERVAL, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("persistent-session-flush-threshold", 	// NOI18N
			PERSISTENT_SESSION_FLUSH_THRESHOLD, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(PERSISTENT_SESSION_FLUSH_THRESHOLD, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("persistent-async-queue-timeout", 	// NOI18N
			PERSISTENT_ASYNC_QUEUE_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(PERSISTENT_ASYNC_QUEUE_TIMEOUT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("persistent-store-table", 	// NOI18N
			PERSISTENT_STORE_TABLE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jdbc-column-name-max-inactive-interval", 	// NOI18N
			JDBC_COLUMN_NAME_MAX_INACTIVE_INTERVAL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jdbc-connection-timeout-secs", 	// NOI18N
			JDBC_CONNECTION_TIMEOUT_SECS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(JDBC_CONNECTION_TIMEOUT_SECS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("url-rewriting-enabled", 	// NOI18N
			URL_REWRITING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("http-proxy-caching-of-cookies", 	// NOI18N
			HTTP_PROXY_CACHING_OF_COOKIES, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("encode-session-id-in-query-params", 	// NOI18N
			ENCODE_SESSION_ID_IN_QUERY_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("monitoring-attribute-name", 	// NOI18N
			MONITORING_ATTRIBUTE_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("sharing-enabled", 	// NOI18N
			SHARING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
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
	public void setTimeoutSecs(java.math.BigInteger value) {
		this.setValue(TIMEOUT_SECS, value);
	}

	//
	public java.math.BigInteger getTimeoutSecs() {
		return (java.math.BigInteger)this.getValue(TIMEOUT_SECS);
	}

	// This attribute is optional
	public void setTimeoutSecsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getTimeoutSecsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setInvalidationIntervalSecs(java.math.BigInteger value) {
		this.setValue(INVALIDATION_INTERVAL_SECS, value);
	}

	//
	public java.math.BigInteger getInvalidationIntervalSecs() {
		return (java.math.BigInteger)this.getValue(INVALIDATION_INTERVAL_SECS);
	}

	// This attribute is optional
	public void setInvalidationIntervalSecsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getInvalidationIntervalSecsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setDebugEnabled(boolean value) {
		this.setValue(DEBUG_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDebugEnabled() {
		Boolean ret = (Boolean)this.getValue(DEBUG_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setIdLength(java.math.BigInteger value) {
		this.setValue(ID_LENGTH, value);
	}

	//
	public java.math.BigInteger getIdLength() {
		return (java.math.BigInteger)this.getValue(ID_LENGTH);
	}

	// This attribute is optional
	public void setIdLengthJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getIdLengthJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setTrackingEnabled(boolean value) {
		this.setValue(TRACKING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isTrackingEnabled() {
		Boolean ret = (Boolean)this.getValue(TRACKING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setCacheSize(java.math.BigInteger value) {
		this.setValue(CACHE_SIZE, value);
	}

	//
	public java.math.BigInteger getCacheSize() {
		return (java.math.BigInteger)this.getValue(CACHE_SIZE);
	}

	// This attribute is optional
	public void setCacheSizeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getCacheSizeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setMaxInMemorySessions(java.math.BigInteger value) {
		this.setValue(MAX_IN_MEMORY_SESSIONS, value);
	}

	//
	public java.math.BigInteger getMaxInMemorySessions() {
		return (java.math.BigInteger)this.getValue(MAX_IN_MEMORY_SESSIONS);
	}

	// This attribute is optional
	public void setMaxInMemorySessionsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getMaxInMemorySessionsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setCookiesEnabled(boolean value) {
		this.setValue(COOKIES_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCookiesEnabled() {
		Boolean ret = (Boolean)this.getValue(COOKIES_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setCookieName(java.lang.String value) {
		this.setValue(COOKIE_NAME, value);
	}

	//
	public java.lang.String getCookieName() {
		return (java.lang.String)this.getValue(COOKIE_NAME);
	}

	// This attribute is optional
	public void setCookiePath(java.lang.String value) {
		this.setValue(COOKIE_PATH, value);
	}

	//
	public java.lang.String getCookiePath() {
		return (java.lang.String)this.getValue(COOKIE_PATH);
	}

	// This attribute is optional
	public void setCookieDomain(java.lang.String value) {
		this.setValue(COOKIE_DOMAIN, value);
	}

	//
	public java.lang.String getCookieDomain() {
		return (java.lang.String)this.getValue(COOKIE_DOMAIN);
	}

	// This attribute is optional
	public void setCookieComment(java.lang.String value) {
		this.setValue(COOKIE_COMMENT, value);
	}

	//
	public java.lang.String getCookieComment() {
		return (java.lang.String)this.getValue(COOKIE_COMMENT);
	}

	// This attribute is optional
	public void setCookieSecure(boolean value) {
		this.setValue(COOKIE_SECURE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCookieSecure() {
		Boolean ret = (Boolean)this.getValue(COOKIE_SECURE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setCookieMaxAgeSecs(java.math.BigInteger value) {
		this.setValue(COOKIE_MAX_AGE_SECS, value);
	}

	//
	public java.math.BigInteger getCookieMaxAgeSecs() {
		return (java.math.BigInteger)this.getValue(COOKIE_MAX_AGE_SECS);
	}

	// This attribute is optional
	public void setCookieMaxAgeSecsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getCookieMaxAgeSecsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPersistentStoreType(java.lang.String value) {
		this.setValue(PERSISTENT_STORE_TYPE, value);
	}

	//
	public java.lang.String getPersistentStoreType() {
		return (java.lang.String)this.getValue(PERSISTENT_STORE_TYPE);
	}

	// This attribute is optional
	public void setPersistentStoreCookieName(java.lang.String value) {
		this.setValue(PERSISTENT_STORE_COOKIE_NAME, value);
	}

	//
	public java.lang.String getPersistentStoreCookieName() {
		return (java.lang.String)this.getValue(PERSISTENT_STORE_COOKIE_NAME);
	}

	// This attribute is optional
	public void setPersistentStoreDir(java.lang.String value) {
		this.setValue(PERSISTENT_STORE_DIR, value);
	}

	//
	public java.lang.String getPersistentStoreDir() {
		return (java.lang.String)this.getValue(PERSISTENT_STORE_DIR);
	}

	// This attribute is optional
	public void setPersistentStorePool(java.lang.String value) {
		this.setValue(PERSISTENT_STORE_POOL, value);
	}

	//
	public java.lang.String getPersistentStorePool() {
		return (java.lang.String)this.getValue(PERSISTENT_STORE_POOL);
	}

	// This attribute is optional
	public void setPersistentDataSourceJndiName(java.lang.String value) {
		this.setValue(PERSISTENT_DATA_SOURCE_JNDI_NAME, value);
	}

	//
	public java.lang.String getPersistentDataSourceJndiName() {
		return (java.lang.String)this.getValue(PERSISTENT_DATA_SOURCE_JNDI_NAME);
	}

	// This attribute is optional
	public void setPersistentSessionFlushInterval(java.math.BigInteger value) {
		this.setValue(PERSISTENT_SESSION_FLUSH_INTERVAL, value);
	}

	//
	public java.math.BigInteger getPersistentSessionFlushInterval() {
		return (java.math.BigInteger)this.getValue(PERSISTENT_SESSION_FLUSH_INTERVAL);
	}

	// This attribute is optional
	public void setPersistentSessionFlushIntervalJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getPersistentSessionFlushIntervalJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPersistentSessionFlushThreshold(long value) {
		this.setValue(PERSISTENT_SESSION_FLUSH_THRESHOLD, java.lang.Long.valueOf(value));
	}

	//
	public long getPersistentSessionFlushThreshold() {
		Long ret = (Long)this.getValue(PERSISTENT_SESSION_FLUSH_THRESHOLD);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"PERSISTENT_SESSION_FLUSH_THRESHOLD", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setPersistentSessionFlushThresholdJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PERSISTENT_SESSION_FLUSH_THRESHOLD) == 0) {
			setValue(PERSISTENT_SESSION_FLUSH_THRESHOLD, "");
		}
		setAttributeValue(PERSISTENT_SESSION_FLUSH_THRESHOLD, "J2eeId", value);
	}

	//
	public java.lang.String getPersistentSessionFlushThresholdJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PERSISTENT_SESSION_FLUSH_THRESHOLD) == 0) {
			return null;
		} else {
			return getAttributeValue(PERSISTENT_SESSION_FLUSH_THRESHOLD, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPersistentAsyncQueueTimeout(long value) {
		this.setValue(PERSISTENT_ASYNC_QUEUE_TIMEOUT, java.lang.Long.valueOf(value));
	}

	//
	public long getPersistentAsyncQueueTimeout() {
		Long ret = (Long)this.getValue(PERSISTENT_ASYNC_QUEUE_TIMEOUT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"PERSISTENT_ASYNC_QUEUE_TIMEOUT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setPersistentAsyncQueueTimeoutJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PERSISTENT_SESSION_FLUSH_THRESHOLD) == 0) {
			setValue(PERSISTENT_SESSION_FLUSH_THRESHOLD, "");
		}
		setAttributeValue(PERSISTENT_SESSION_FLUSH_THRESHOLD, "J2eeId", value);
	}

	//
	public java.lang.String getPersistentAsyncQueueTimeoutJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PERSISTENT_SESSION_FLUSH_THRESHOLD) == 0) {
			return null;
		} else {
			return getAttributeValue(PERSISTENT_SESSION_FLUSH_THRESHOLD, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPersistentStoreTable(java.lang.String value) {
		this.setValue(PERSISTENT_STORE_TABLE, value);
	}

	//
	public java.lang.String getPersistentStoreTable() {
		return (java.lang.String)this.getValue(PERSISTENT_STORE_TABLE);
	}

	// This attribute is optional
	public void setJdbcColumnNameMaxInactiveInterval(java.lang.String value) {
		this.setValue(JDBC_COLUMN_NAME_MAX_INACTIVE_INTERVAL, value);
	}

	//
	public java.lang.String getJdbcColumnNameMaxInactiveInterval() {
		return (java.lang.String)this.getValue(JDBC_COLUMN_NAME_MAX_INACTIVE_INTERVAL);
	}

	// This attribute is optional
	public void setJdbcConnectionTimeoutSecs(java.math.BigInteger value) {
		this.setValue(JDBC_CONNECTION_TIMEOUT_SECS, value);
	}

	//
	public java.math.BigInteger getJdbcConnectionTimeoutSecs() {
		return (java.math.BigInteger)this.getValue(JDBC_CONNECTION_TIMEOUT_SECS);
	}

	// This attribute is optional
	public void setJdbcConnectionTimeoutSecsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT_SECS) == 0) {
			setValue(TIMEOUT_SECS, "");
		}
		setAttributeValue(TIMEOUT_SECS, "J2eeId", value);
	}

	//
	public java.lang.String getJdbcConnectionTimeoutSecsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT_SECS) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT_SECS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setUrlRewritingEnabled(boolean value) {
		this.setValue(URL_REWRITING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isUrlRewritingEnabled() {
		Boolean ret = (Boolean)this.getValue(URL_REWRITING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setHttpProxyCachingOfCookies(boolean value) {
		this.setValue(HTTP_PROXY_CACHING_OF_COOKIES, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isHttpProxyCachingOfCookies() {
		Boolean ret = (Boolean)this.getValue(HTTP_PROXY_CACHING_OF_COOKIES);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setEncodeSessionIdInQueryParams(boolean value) {
		this.setValue(ENCODE_SESSION_ID_IN_QUERY_PARAMS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isEncodeSessionIdInQueryParams() {
		Boolean ret = (Boolean)this.getValue(ENCODE_SESSION_ID_IN_QUERY_PARAMS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setMonitoringAttributeName(java.lang.String value) {
		this.setValue(MONITORING_ATTRIBUTE_NAME, value);
	}

	//
	public java.lang.String getMonitoringAttributeName() {
		return (java.lang.String)this.getValue(MONITORING_ATTRIBUTE_NAME);
	}

	// This attribute is optional
	public void setSharingEnabled(boolean value) {
		this.setValue(SHARING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isSharingEnabled() {
		Boolean ret = (Boolean)this.getValue(SHARING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
		// Validating property timeoutSecs
		// Validating property timeoutSecsJ2eeId
		if (getTimeoutSecsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTimeoutSecsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "timeoutSecsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property invalidationIntervalSecs
		// Validating property invalidationIntervalSecsJ2eeId
		if (getInvalidationIntervalSecsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInvalidationIntervalSecsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "invalidationIntervalSecsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property debugEnabled
		{
			boolean patternPassed = false;
			if ((isDebugEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isDebugEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "debugEnabled", this);	// NOI18N
		}
		// Validating property idLength
		// Validating property idLengthJ2eeId
		if (getIdLengthJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIdLengthJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "idLengthJ2eeId", this);	// NOI18N
			}
		}
		// Validating property trackingEnabled
		{
			boolean patternPassed = false;
			if ((isTrackingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isTrackingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "trackingEnabled", this);	// NOI18N
		}
		// Validating property cacheSize
		// Validating property cacheSizeJ2eeId
		if (getCacheSizeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCacheSizeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "cacheSizeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property maxInMemorySessions
		// Validating property maxInMemorySessionsJ2eeId
		if (getMaxInMemorySessionsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxInMemorySessionsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxInMemorySessionsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property cookiesEnabled
		{
			boolean patternPassed = false;
			if ((isCookiesEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCookiesEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "cookiesEnabled", this);	// NOI18N
		}
		// Validating property cookieName
		// Validating property cookiePath
		// Validating property cookieDomain
		// Validating property cookieComment
		// Validating property cookieSecure
		{
			boolean patternPassed = false;
			if ((isCookieSecure() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCookieSecure()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "cookieSecure", this);	// NOI18N
		}
		// Validating property cookieMaxAgeSecs
		// Validating property cookieMaxAgeSecsJ2eeId
		if (getCookieMaxAgeSecsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCookieMaxAgeSecsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "cookieMaxAgeSecsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property persistentStoreType
		// Validating property persistentStoreCookieName
		// Validating property persistentStoreDir
		// Validating property persistentStorePool
		// Validating property persistentDataSourceJndiName
		// Validating property persistentSessionFlushInterval
		// Validating property persistentSessionFlushIntervalJ2eeId
		if (getPersistentSessionFlushIntervalJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentSessionFlushIntervalJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentSessionFlushIntervalJ2eeId", this);	// NOI18N
			}
		}
		// Validating property persistentSessionFlushThreshold
		if (getPersistentSessionFlushThreshold() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentSessionFlushThreshold() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentSessionFlushThreshold", this);	// NOI18N
		}
		// Validating property persistentSessionFlushThresholdJ2eeId
		if (getPersistentSessionFlushThresholdJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentSessionFlushThresholdJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentSessionFlushThresholdJ2eeId", this);	// NOI18N
			}
		}
		// Validating property persistentAsyncQueueTimeout
		if (getPersistentAsyncQueueTimeout() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentAsyncQueueTimeout() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentAsyncQueueTimeout", this);	// NOI18N
		}
		// Validating property persistentAsyncQueueTimeoutJ2eeId
		if (getPersistentAsyncQueueTimeoutJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentAsyncQueueTimeoutJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentAsyncQueueTimeoutJ2eeId", this);	// NOI18N
			}
		}
		// Validating property persistentStoreTable
		// Validating property jdbcColumnNameMaxInactiveInterval
		// Validating property jdbcConnectionTimeoutSecs
		// Validating property jdbcConnectionTimeoutSecsJ2eeId
		if (getJdbcConnectionTimeoutSecsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJdbcConnectionTimeoutSecsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jdbcConnectionTimeoutSecsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property urlRewritingEnabled
		{
			boolean patternPassed = false;
			if ((isUrlRewritingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isUrlRewritingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "urlRewritingEnabled", this);	// NOI18N
		}
		// Validating property httpProxyCachingOfCookies
		{
			boolean patternPassed = false;
			if ((isHttpProxyCachingOfCookies() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isHttpProxyCachingOfCookies()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "httpProxyCachingOfCookies", this);	// NOI18N
		}
		// Validating property encodeSessionIdInQueryParams
		{
			boolean patternPassed = false;
			if ((isEncodeSessionIdInQueryParams() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isEncodeSessionIdInQueryParams()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "encodeSessionIdInQueryParams", this);	// NOI18N
		}
		// Validating property monitoringAttributeName
		// Validating property sharingEnabled
		{
			boolean patternPassed = false;
			if ((isSharingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isSharingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "sharingEnabled", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("TimeoutSecs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTimeoutSecs();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TIMEOUT_SECS, 0, str, indent);

		str.append(indent);
		str.append("InvalidationIntervalSecs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getInvalidationIntervalSecs();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INVALIDATION_INTERVAL_SECS, 0, str, indent);

		str.append(indent);
		str.append("DebugEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDebugEnabled()?"true":"false"));
		this.dumpAttributes(DEBUG_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("IdLength");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getIdLength();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ID_LENGTH, 0, str, indent);

		str.append(indent);
		str.append("TrackingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isTrackingEnabled()?"true":"false"));
		this.dumpAttributes(TRACKING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("CacheSize");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCacheSize();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CACHE_SIZE, 0, str, indent);

		str.append(indent);
		str.append("MaxInMemorySessions");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMaxInMemorySessions();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MAX_IN_MEMORY_SESSIONS, 0, str, indent);

		str.append(indent);
		str.append("CookiesEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCookiesEnabled()?"true":"false"));
		this.dumpAttributes(COOKIES_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("CookieName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCookieName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COOKIE_NAME, 0, str, indent);

		str.append(indent);
		str.append("CookiePath");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCookiePath();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COOKIE_PATH, 0, str, indent);

		str.append(indent);
		str.append("CookieDomain");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCookieDomain();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COOKIE_DOMAIN, 0, str, indent);

		str.append(indent);
		str.append("CookieComment");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCookieComment();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COOKIE_COMMENT, 0, str, indent);

		str.append(indent);
		str.append("CookieSecure");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCookieSecure()?"true":"false"));
		this.dumpAttributes(COOKIE_SECURE, 0, str, indent);

		str.append(indent);
		str.append("CookieMaxAgeSecs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCookieMaxAgeSecs();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COOKIE_MAX_AGE_SECS, 0, str, indent);

		str.append(indent);
		str.append("PersistentStoreType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentStoreType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_STORE_TYPE, 0, str, indent);

		str.append(indent);
		str.append("PersistentStoreCookieName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentStoreCookieName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_STORE_COOKIE_NAME, 0, str, indent);

		str.append(indent);
		str.append("PersistentStoreDir");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentStoreDir();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_STORE_DIR, 0, str, indent);

		str.append(indent);
		str.append("PersistentStorePool");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentStorePool();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_STORE_POOL, 0, str, indent);

		str.append(indent);
		str.append("PersistentDataSourceJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentDataSourceJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_DATA_SOURCE_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("PersistentSessionFlushInterval");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentSessionFlushInterval();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_SESSION_FLUSH_INTERVAL, 0, str, indent);

		if (this.getValue(PERSISTENT_SESSION_FLUSH_THRESHOLD) != null) {
			str.append(indent);
			str.append("PersistentSessionFlushThreshold");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getPersistentSessionFlushThreshold());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PERSISTENT_SESSION_FLUSH_THRESHOLD, 0, str, indent);
		}

		if (this.getValue(PERSISTENT_ASYNC_QUEUE_TIMEOUT) != null) {
			str.append(indent);
			str.append("PersistentAsyncQueueTimeout");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getPersistentAsyncQueueTimeout());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PERSISTENT_ASYNC_QUEUE_TIMEOUT, 0, str, indent);
		}

		str.append(indent);
		str.append("PersistentStoreTable");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentStoreTable();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_STORE_TABLE, 0, str, indent);

		str.append(indent);
		str.append("JdbcColumnNameMaxInactiveInterval");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJdbcColumnNameMaxInactiveInterval();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JDBC_COLUMN_NAME_MAX_INACTIVE_INTERVAL, 0, str, indent);

		str.append(indent);
		str.append("JdbcConnectionTimeoutSecs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJdbcConnectionTimeoutSecs();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JDBC_CONNECTION_TIMEOUT_SECS, 0, str, indent);

		str.append(indent);
		str.append("UrlRewritingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isUrlRewritingEnabled()?"true":"false"));
		this.dumpAttributes(URL_REWRITING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("HttpProxyCachingOfCookies");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isHttpProxyCachingOfCookies()?"true":"false"));
		this.dumpAttributes(HTTP_PROXY_CACHING_OF_COOKIES, 0, str, indent);

		str.append(indent);
		str.append("EncodeSessionIdInQueryParams");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isEncodeSessionIdInQueryParams()?"true":"false"));
		this.dumpAttributes(ENCODE_SESSION_ID_IN_QUERY_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("MonitoringAttributeName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMonitoringAttributeName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MONITORING_ATTRIBUTE_NAME, 0, str, indent);

		str.append(indent);
		str.append("SharingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isSharingEnabled()?"true":"false"));
		this.dumpAttributes(SHARING_ENABLED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SessionDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

