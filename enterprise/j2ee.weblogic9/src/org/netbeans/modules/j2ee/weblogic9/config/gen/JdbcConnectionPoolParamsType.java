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
 *	This generated bean class JdbcConnectionPoolParamsType matches the schema element 'jdbc-connection-pool-paramsType'.
 *  The root bean class is JdbcDataSource
 *
 *	Generated on Tue Jul 25 03:27:07 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.config.gen;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class JdbcConnectionPoolParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String INITIAL_CAPACITY = "InitialCapacity";	// NOI18N
	static public final String INITIALCAPACITYJ2EEID = "InitialCapacityJ2eeId";	// NOI18N
	static public final String MAX_CAPACITY = "MaxCapacity";	// NOI18N
	static public final String MAXCAPACITYJ2EEID = "MaxCapacityJ2eeId";	// NOI18N
	static public final String CAPACITY_INCREMENT = "CapacityIncrement";	// NOI18N
	static public final String CAPACITYINCREMENTJ2EEID = "CapacityIncrementJ2eeId";	// NOI18N
	static public final String SHRINK_FREQUENCY_SECONDS = "ShrinkFrequencySeconds";	// NOI18N
	static public final String SHRINKFREQUENCYSECONDSJ2EEID = "ShrinkFrequencySecondsJ2eeId";	// NOI18N
	static public final String HIGHEST_NUM_WAITERS = "HighestNumWaiters";	// NOI18N
	static public final String HIGHESTNUMWAITERSJ2EEID = "HighestNumWaitersJ2eeId";	// NOI18N
	static public final String CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS = "ConnectionCreationRetryFrequencySeconds";	// NOI18N
	static public final String CONNECTIONCREATIONRETRYFREQUENCYSECONDSJ2EEID = "ConnectionCreationRetryFrequencySecondsJ2eeId";	// NOI18N
	static public final String CONNECTION_RESERVE_TIMEOUT_SECONDS = "ConnectionReserveTimeoutSeconds";	// NOI18N
	static public final String CONNECTIONRESERVETIMEOUTSECONDSJ2EEID = "ConnectionReserveTimeoutSecondsJ2eeId";	// NOI18N
	static public final String TEST_FREQUENCY_SECONDS = "TestFrequencySeconds";	// NOI18N
	static public final String TESTFREQUENCYSECONDSJ2EEID = "TestFrequencySecondsJ2eeId";	// NOI18N
	static public final String TEST_CONNECTIONS_ON_RESERVE = "TestConnectionsOnReserve";	// NOI18N
	static public final String PROFILE_HARVEST_FREQUENCY_SECONDS = "ProfileHarvestFrequencySeconds";	// NOI18N
	static public final String PROFILEHARVESTFREQUENCYSECONDSJ2EEID = "ProfileHarvestFrequencySecondsJ2eeId";	// NOI18N
	static public final String IGNORE_IN_USE_CONNECTIONS_ENABLED = "IgnoreInUseConnectionsEnabled";	// NOI18N
	static public final String INACTIVE_CONNECTION_TIMEOUT_SECONDS = "InactiveConnectionTimeoutSeconds";	// NOI18N
	static public final String INACTIVECONNECTIONTIMEOUTSECONDSJ2EEID = "InactiveConnectionTimeoutSecondsJ2eeId";	// NOI18N
	static public final String TEST_TABLE_NAME = "TestTableName";	// NOI18N
	static public final String LOGIN_DELAY_SECONDS = "LoginDelaySeconds";	// NOI18N
	static public final String LOGINDELAYSECONDSJ2EEID = "LoginDelaySecondsJ2eeId";	// NOI18N
	static public final String INIT_SQL = "InitSql";	// NOI18N
	static public final String STATEMENT_CACHE_SIZE = "StatementCacheSize";	// NOI18N
	static public final String STATEMENTCACHESIZEJ2EEID = "StatementCacheSizeJ2eeId";	// NOI18N
	static public final String STATEMENT_CACHE_TYPE = "StatementCacheType";	// NOI18N
	static public final String REMOVE_INFECTED_CONNECTIONS = "RemoveInfectedConnections";	// NOI18N
	static public final String SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION = "SecondsToTrustAnIdlePoolConnection";	// NOI18N
	static public final String SECONDSTOTRUSTANIDLEPOOLCONNECTIONJ2EEID = "SecondsToTrustAnIdlePoolConnectionJ2eeId";	// NOI18N
	static public final String STATEMENT_TIMEOUT = "StatementTimeout";	// NOI18N
	static public final String STATEMENTTIMEOUTJ2EEID = "StatementTimeoutJ2eeId";	// NOI18N
	static public final String PROFILE_TYPE = "ProfileType";	// NOI18N
	static public final String PROFILETYPEJ2EEID = "ProfileTypeJ2eeId";	// NOI18N
	static public final String JDBC_XA_DEBUG_LEVEL = "JdbcXaDebugLevel";	// NOI18N
	static public final String JDBCXADEBUGLEVELJ2EEID = "JdbcXaDebugLevelJ2eeId";	// NOI18N
	static public final String CREDENTIAL_MAPPING_ENABLED = "CredentialMappingEnabled";	// NOI18N
	static public final String DRIVER_INTERCEPTOR = "DriverInterceptor";	// NOI18N
	static public final String PINNED_TO_THREAD = "PinnedToThread";	// NOI18N
	static public final String IDENTITY_BASED_CONNECTION_POOLING_ENABLED = "IdentityBasedConnectionPoolingEnabled";	// NOI18N

	public JdbcConnectionPoolParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JdbcConnectionPoolParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(26);
		this.createProperty("initial-capacity", 	// NOI18N
			INITIAL_CAPACITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(INITIAL_CAPACITY, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("max-capacity", 	// NOI18N
			MAX_CAPACITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(MAX_CAPACITY, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("capacity-increment", 	// NOI18N
			CAPACITY_INCREMENT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(CAPACITY_INCREMENT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("shrink-frequency-seconds", 	// NOI18N
			SHRINK_FREQUENCY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(SHRINK_FREQUENCY_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("highest-num-waiters", 	// NOI18N
			HIGHEST_NUM_WAITERS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(HIGHEST_NUM_WAITERS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("connection-creation-retry-frequency-seconds", 	// NOI18N
			CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("connection-reserve-timeout-seconds", 	// NOI18N
			CONNECTION_RESERVE_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(CONNECTION_RESERVE_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("test-frequency-seconds", 	// NOI18N
			TEST_FREQUENCY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(TEST_FREQUENCY_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("test-connections-on-reserve", 	// NOI18N
			TEST_CONNECTIONS_ON_RESERVE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("profile-harvest-frequency-seconds", 	// NOI18N
			PROFILE_HARVEST_FREQUENCY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(PROFILE_HARVEST_FREQUENCY_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("ignore-in-use-connections-enabled", 	// NOI18N
			IGNORE_IN_USE_CONNECTIONS_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("inactive-connection-timeout-seconds", 	// NOI18N
			INACTIVE_CONNECTION_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(INACTIVE_CONNECTION_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("test-table-name", 	// NOI18N
			TEST_TABLE_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("login-delay-seconds", 	// NOI18N
			LOGIN_DELAY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(LOGIN_DELAY_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("init-sql", 	// NOI18N
			INIT_SQL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("statement-cache-size", 	// NOI18N
			STATEMENT_CACHE_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(STATEMENT_CACHE_SIZE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("statement-cache-type", 	// NOI18N
			STATEMENT_CACHE_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("remove-infected-connections", 	// NOI18N
			REMOVE_INFECTED_CONNECTIONS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("seconds-to-trust-an-idle-pool-connection", 	// NOI18N
			SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("statement-timeout", 	// NOI18N
			STATEMENT_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(STATEMENT_TIMEOUT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("profile-type", 	// NOI18N
			PROFILE_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(PROFILE_TYPE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("jdbc-xa-debug-level", 	// NOI18N
			JDBC_XA_DEBUG_LEVEL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(JDBC_XA_DEBUG_LEVEL, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("credential-mapping-enabled", 	// NOI18N
			CREDENTIAL_MAPPING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("driver-interceptor", 	// NOI18N
			DRIVER_INTERCEPTOR, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("pinned-to-thread", 	// NOI18N
			PINNED_TO_THREAD, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("identity-based-connection-pooling-enabled", 	// NOI18N
			IDENTITY_BASED_CONNECTION_POOLING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setInitialCapacity(long value) {
		this.setValue(INITIAL_CAPACITY, java.lang.Long.valueOf(value));
	}

	//
	public long getInitialCapacity() {
		Long ret = (Long)this.getValue(INITIAL_CAPACITY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"INITIAL_CAPACITY", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setInitialCapacityJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getInitialCapacityJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setMaxCapacity(long value) {
		this.setValue(MAX_CAPACITY, java.lang.Long.valueOf(value));
	}

	//
	public long getMaxCapacity() {
		Long ret = (Long)this.getValue(MAX_CAPACITY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_CAPACITY", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMaxCapacityJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_CAPACITY) == 0) {
			setValue(MAX_CAPACITY, "");
		}
		setAttributeValue(MAX_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getMaxCapacityJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setCapacityIncrement(long value) {
		this.setValue(CAPACITY_INCREMENT, java.lang.Long.valueOf(value));
	}

	//
	public long getCapacityIncrement() {
		Long ret = (Long)this.getValue(CAPACITY_INCREMENT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CAPACITY_INCREMENT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setCapacityIncrementJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_CAPACITY) == 0) {
			setValue(MAX_CAPACITY, "");
		}
		setAttributeValue(MAX_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getCapacityIncrementJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setShrinkFrequencySeconds(long value) {
		this.setValue(SHRINK_FREQUENCY_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getShrinkFrequencySeconds() {
		Long ret = (Long)this.getValue(SHRINK_FREQUENCY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"SHRINK_FREQUENCY_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setShrinkFrequencySecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getShrinkFrequencySecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setHighestNumWaiters(long value) {
		this.setValue(HIGHEST_NUM_WAITERS, java.lang.Long.valueOf(value));
	}

	//
	public long getHighestNumWaiters() {
		Long ret = (Long)this.getValue(HIGHEST_NUM_WAITERS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"HIGHEST_NUM_WAITERS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setHighestNumWaitersJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getHighestNumWaitersJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setConnectionCreationRetryFrequencySeconds(long value) {
		this.setValue(CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getConnectionCreationRetryFrequencySeconds() {
		Long ret = (Long)this.getValue(CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setConnectionCreationRetryFrequencySecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getConnectionCreationRetryFrequencySecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setConnectionReserveTimeoutSeconds(java.math.BigInteger value) {
		this.setValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, value);
	}

	//
	public java.math.BigInteger getConnectionReserveTimeoutSeconds() {
		return (java.math.BigInteger)this.getValue(CONNECTION_RESERVE_TIMEOUT_SECONDS);
	}

	// This attribute is optional
	public void setConnectionReserveTimeoutSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONNECTION_RESERVE_TIMEOUT_SECONDS) == 0) {
			setValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getConnectionReserveTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONNECTION_RESERVE_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setTestFrequencySeconds(long value) {
		this.setValue(TEST_FREQUENCY_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getTestFrequencySeconds() {
		Long ret = (Long)this.getValue(TEST_FREQUENCY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"TEST_FREQUENCY_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setTestFrequencySecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getTestFrequencySecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setTestConnectionsOnReserve(boolean value) {
		this.setValue(TEST_CONNECTIONS_ON_RESERVE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isTestConnectionsOnReserve() {
		Boolean ret = (Boolean)this.getValue(TEST_CONNECTIONS_ON_RESERVE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setProfileHarvestFrequencySeconds(long value) {
		this.setValue(PROFILE_HARVEST_FREQUENCY_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getProfileHarvestFrequencySeconds() {
		Long ret = (Long)this.getValue(PROFILE_HARVEST_FREQUENCY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"PROFILE_HARVEST_FREQUENCY_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setProfileHarvestFrequencySecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getProfileHarvestFrequencySecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setIgnoreInUseConnectionsEnabled(boolean value) {
		this.setValue(IGNORE_IN_USE_CONNECTIONS_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isIgnoreInUseConnectionsEnabled() {
		Boolean ret = (Boolean)this.getValue(IGNORE_IN_USE_CONNECTIONS_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setInactiveConnectionTimeoutSeconds(long value) {
		this.setValue(INACTIVE_CONNECTION_TIMEOUT_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getInactiveConnectionTimeoutSeconds() {
		Long ret = (Long)this.getValue(INACTIVE_CONNECTION_TIMEOUT_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"INACTIVE_CONNECTION_TIMEOUT_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setInactiveConnectionTimeoutSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getInactiveConnectionTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setTestTableName(java.lang.String value) {
		this.setValue(TEST_TABLE_NAME, value);
	}

	//
	public java.lang.String getTestTableName() {
		return (java.lang.String)this.getValue(TEST_TABLE_NAME);
	}

	// This attribute is optional
	public void setLoginDelaySeconds(long value) {
		this.setValue(LOGIN_DELAY_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getLoginDelaySeconds() {
		Long ret = (Long)this.getValue(LOGIN_DELAY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"LOGIN_DELAY_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setLoginDelaySecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getLoginDelaySecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setInitSql(java.lang.String value) {
		this.setValue(INIT_SQL, value);
	}

	//
	public java.lang.String getInitSql() {
		return (java.lang.String)this.getValue(INIT_SQL);
	}

	// This attribute is optional
	public void setStatementCacheSize(long value) {
		this.setValue(STATEMENT_CACHE_SIZE, java.lang.Long.valueOf(value));
	}

	//
	public long getStatementCacheSize() {
		Long ret = (Long)this.getValue(STATEMENT_CACHE_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"STATEMENT_CACHE_SIZE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setStatementCacheSizeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getStatementCacheSizeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setStatementCacheType(java.lang.String value) {
		this.setValue(STATEMENT_CACHE_TYPE, value);
	}

	//
	public java.lang.String getStatementCacheType() {
		return (java.lang.String)this.getValue(STATEMENT_CACHE_TYPE);
	}

	// This attribute is optional
	public void setRemoveInfectedConnections(boolean value) {
		this.setValue(REMOVE_INFECTED_CONNECTIONS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRemoveInfectedConnections() {
		Boolean ret = (Boolean)this.getValue(REMOVE_INFECTED_CONNECTIONS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setSecondsToTrustAnIdlePoolConnection(long value) {
		this.setValue(SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION, java.lang.Long.valueOf(value));
	}

	//
	public long getSecondsToTrustAnIdlePoolConnection() {
		Long ret = (Long)this.getValue(SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setSecondsToTrustAnIdlePoolConnectionJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getSecondsToTrustAnIdlePoolConnectionJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setStatementTimeout(java.math.BigInteger value) {
		this.setValue(STATEMENT_TIMEOUT, value);
	}

	//
	public java.math.BigInteger getStatementTimeout() {
		return (java.math.BigInteger)this.getValue(STATEMENT_TIMEOUT);
	}

	// This attribute is optional
	public void setStatementTimeoutJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONNECTION_RESERVE_TIMEOUT_SECONDS) == 0) {
			setValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getStatementTimeoutJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONNECTION_RESERVE_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setProfileType(long value) {
		this.setValue(PROFILE_TYPE, java.lang.Long.valueOf(value));
	}

	//
	public long getProfileType() {
		Long ret = (Long)this.getValue(PROFILE_TYPE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"PROFILE_TYPE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setProfileTypeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CAPACITY) == 0) {
			setValue(INITIAL_CAPACITY, "");
		}
		setAttributeValue(INITIAL_CAPACITY, "J2eeId", value);
	}

	//
	public java.lang.String getProfileTypeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CAPACITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CAPACITY, "J2eeId");
		}
	}

	// This attribute is optional
	public void setJdbcXaDebugLevel(java.math.BigInteger value) {
		this.setValue(JDBC_XA_DEBUG_LEVEL, value);
	}

	//
	public java.math.BigInteger getJdbcXaDebugLevel() {
		return (java.math.BigInteger)this.getValue(JDBC_XA_DEBUG_LEVEL);
	}

	// This attribute is optional
	public void setJdbcXaDebugLevelJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONNECTION_RESERVE_TIMEOUT_SECONDS) == 0) {
			setValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getJdbcXaDebugLevelJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONNECTION_RESERVE_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setCredentialMappingEnabled(boolean value) {
		this.setValue(CREDENTIAL_MAPPING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCredentialMappingEnabled() {
		Boolean ret = (Boolean)this.getValue(CREDENTIAL_MAPPING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setDriverInterceptor(java.lang.String value) {
		this.setValue(DRIVER_INTERCEPTOR, value);
	}

	//
	public java.lang.String getDriverInterceptor() {
		return (java.lang.String)this.getValue(DRIVER_INTERCEPTOR);
	}

	// This attribute is optional
	public void setPinnedToThread(boolean value) {
		this.setValue(PINNED_TO_THREAD, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPinnedToThread() {
		Boolean ret = (Boolean)this.getValue(PINNED_TO_THREAD);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setIdentityBasedConnectionPoolingEnabled(boolean value) {
		this.setValue(IDENTITY_BASED_CONNECTION_POOLING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isIdentityBasedConnectionPoolingEnabled() {
		Boolean ret = (Boolean)this.getValue(IDENTITY_BASED_CONNECTION_POOLING_ENABLED);
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
		// Validating property initialCapacity
		if (getInitialCapacity() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getInitialCapacity() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initialCapacity", this);	// NOI18N
		}
		// Validating property initialCapacityJ2eeId
		if (getInitialCapacityJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInitialCapacityJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initialCapacityJ2eeId", this);	// NOI18N
			}
		}
		// Validating property maxCapacity
		if (getMaxCapacity() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMaxCapacity() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxCapacity", this);	// NOI18N
		}
		// Validating property maxCapacityJ2eeId
		if (getMaxCapacityJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxCapacityJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxCapacityJ2eeId", this);	// NOI18N
			}
		}
		// Validating property capacityIncrement
		if (getCapacityIncrement() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getCapacityIncrement() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "capacityIncrement", this);	// NOI18N
		}
		// Validating property capacityIncrementJ2eeId
		if (getCapacityIncrementJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCapacityIncrementJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "capacityIncrementJ2eeId", this);	// NOI18N
			}
		}
		// Validating property shrinkFrequencySeconds
		if (getShrinkFrequencySeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getShrinkFrequencySeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "shrinkFrequencySeconds", this);	// NOI18N
		}
		// Validating property shrinkFrequencySecondsJ2eeId
		if (getShrinkFrequencySecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getShrinkFrequencySecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "shrinkFrequencySecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property highestNumWaiters
		if (getHighestNumWaiters() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getHighestNumWaiters() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "highestNumWaiters", this);	// NOI18N
		}
		// Validating property highestNumWaitersJ2eeId
		if (getHighestNumWaitersJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getHighestNumWaitersJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "highestNumWaitersJ2eeId", this);	// NOI18N
			}
		}
		// Validating property connectionCreationRetryFrequencySeconds
		if (getConnectionCreationRetryFrequencySeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getConnectionCreationRetryFrequencySeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "connectionCreationRetryFrequencySeconds", this);	// NOI18N
		}
		// Validating property connectionCreationRetryFrequencySecondsJ2eeId
		if (getConnectionCreationRetryFrequencySecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getConnectionCreationRetryFrequencySecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "connectionCreationRetryFrequencySecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property connectionReserveTimeoutSeconds
		// Validating property connectionReserveTimeoutSecondsJ2eeId
		if (getConnectionReserveTimeoutSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getConnectionReserveTimeoutSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "connectionReserveTimeoutSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property testFrequencySeconds
		if (getTestFrequencySeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTestFrequencySeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "testFrequencySeconds", this);	// NOI18N
		}
		// Validating property testFrequencySecondsJ2eeId
		if (getTestFrequencySecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTestFrequencySecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "testFrequencySecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property testConnectionsOnReserve
		{
			boolean patternPassed = false;
			if ((isTestConnectionsOnReserve() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isTestConnectionsOnReserve()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "testConnectionsOnReserve", this);	// NOI18N
		}
		// Validating property profileHarvestFrequencySeconds
		if (getProfileHarvestFrequencySeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getProfileHarvestFrequencySeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "profileHarvestFrequencySeconds", this);	// NOI18N
		}
		// Validating property profileHarvestFrequencySecondsJ2eeId
		if (getProfileHarvestFrequencySecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getProfileHarvestFrequencySecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "profileHarvestFrequencySecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property ignoreInUseConnectionsEnabled
		{
			boolean patternPassed = false;
			if ((isIgnoreInUseConnectionsEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isIgnoreInUseConnectionsEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "ignoreInUseConnectionsEnabled", this);	// NOI18N
		}
		// Validating property inactiveConnectionTimeoutSeconds
		if (getInactiveConnectionTimeoutSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getInactiveConnectionTimeoutSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "inactiveConnectionTimeoutSeconds", this);	// NOI18N
		}
		// Validating property inactiveConnectionTimeoutSecondsJ2eeId
		if (getInactiveConnectionTimeoutSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInactiveConnectionTimeoutSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "inactiveConnectionTimeoutSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property testTableName
		// Validating property loginDelaySeconds
		if (getLoginDelaySeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getLoginDelaySeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "loginDelaySeconds", this);	// NOI18N
		}
		// Validating property loginDelaySecondsJ2eeId
		if (getLoginDelaySecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getLoginDelaySecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "loginDelaySecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property initSql
		// Validating property statementCacheSize
		if (getStatementCacheSize() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getStatementCacheSize() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "statementCacheSize", this);	// NOI18N
		}
		// Validating property statementCacheSizeJ2eeId
		if (getStatementCacheSizeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStatementCacheSizeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "statementCacheSizeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property statementCacheType
		if (getStatementCacheType() != null) {
			final java.lang.String[] enumRestrictionStatementCacheType = {"LRU", "FIXED"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionStatementCacheType.length; ++_index2) {
				if (enumRestrictionStatementCacheType[_index2].equals(getStatementCacheType())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStatementCacheType() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "statementCacheType", this);	// NOI18N
			}
		}
		// Validating property removeInfectedConnections
		{
			boolean patternPassed = false;
			if ((isRemoveInfectedConnections() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRemoveInfectedConnections()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "removeInfectedConnections", this);	// NOI18N
		}
		// Validating property secondsToTrustAnIdlePoolConnection
		if (getSecondsToTrustAnIdlePoolConnection() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getSecondsToTrustAnIdlePoolConnection() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "secondsToTrustAnIdlePoolConnection", this);	// NOI18N
		}
		// Validating property secondsToTrustAnIdlePoolConnectionJ2eeId
		if (getSecondsToTrustAnIdlePoolConnectionJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSecondsToTrustAnIdlePoolConnectionJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "secondsToTrustAnIdlePoolConnectionJ2eeId", this);	// NOI18N
			}
		}
		// Validating property statementTimeout
		// Validating property statementTimeoutJ2eeId
		if (getStatementTimeoutJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStatementTimeoutJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "statementTimeoutJ2eeId", this);	// NOI18N
			}
		}
		// Validating property profileType
		if (getProfileType() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getProfileType() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "profileType", this);	// NOI18N
		}
		// Validating property profileTypeJ2eeId
		if (getProfileTypeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getProfileTypeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "profileTypeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property jdbcXaDebugLevel
		// Validating property jdbcXaDebugLevelJ2eeId
		if (getJdbcXaDebugLevelJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJdbcXaDebugLevelJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jdbcXaDebugLevelJ2eeId", this);	// NOI18N
			}
		}
		// Validating property credentialMappingEnabled
		{
			boolean patternPassed = false;
			if ((isCredentialMappingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCredentialMappingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "credentialMappingEnabled", this);	// NOI18N
		}
		// Validating property driverInterceptor
		// Validating property pinnedToThread
		{
			boolean patternPassed = false;
			if ((isPinnedToThread() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isPinnedToThread()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "pinnedToThread", this);	// NOI18N
		}
		// Validating property identityBasedConnectionPoolingEnabled
		{
			boolean patternPassed = false;
			if ((isIdentityBasedConnectionPoolingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isIdentityBasedConnectionPoolingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "identityBasedConnectionPoolingEnabled", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(INITIAL_CAPACITY) != null) {
			str.append(indent);
			str.append("InitialCapacity");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getInitialCapacity());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(INITIAL_CAPACITY, 0, str, indent);
		}

		if (this.getValue(MAX_CAPACITY) != null) {
			str.append(indent);
			str.append("MaxCapacity");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxCapacity());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_CAPACITY, 0, str, indent);
		}

		if (this.getValue(CAPACITY_INCREMENT) != null) {
			str.append(indent);
			str.append("CapacityIncrement");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getCapacityIncrement());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CAPACITY_INCREMENT, 0, str, indent);
		}

		if (this.getValue(SHRINK_FREQUENCY_SECONDS) != null) {
			str.append(indent);
			str.append("ShrinkFrequencySeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getShrinkFrequencySeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SHRINK_FREQUENCY_SECONDS, 0, str, indent);
		}

		if (this.getValue(HIGHEST_NUM_WAITERS) != null) {
			str.append(indent);
			str.append("HighestNumWaiters");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getHighestNumWaiters());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(HIGHEST_NUM_WAITERS, 0, str, indent);
		}

		if (this.getValue(CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS) != null) {
			str.append(indent);
			str.append("ConnectionCreationRetryFrequencySeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getConnectionCreationRetryFrequencySeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("ConnectionReserveTimeoutSeconds");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConnectionReserveTimeoutSeconds();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONNECTION_RESERVE_TIMEOUT_SECONDS, 0, str, indent);

		if (this.getValue(TEST_FREQUENCY_SECONDS) != null) {
			str.append(indent);
			str.append("TestFrequencySeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getTestFrequencySeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(TEST_FREQUENCY_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("TestConnectionsOnReserve");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isTestConnectionsOnReserve()?"true":"false"));
		this.dumpAttributes(TEST_CONNECTIONS_ON_RESERVE, 0, str, indent);

		if (this.getValue(PROFILE_HARVEST_FREQUENCY_SECONDS) != null) {
			str.append(indent);
			str.append("ProfileHarvestFrequencySeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getProfileHarvestFrequencySeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PROFILE_HARVEST_FREQUENCY_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("IgnoreInUseConnectionsEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isIgnoreInUseConnectionsEnabled()?"true":"false"));
		this.dumpAttributes(IGNORE_IN_USE_CONNECTIONS_ENABLED, 0, str, indent);

		if (this.getValue(INACTIVE_CONNECTION_TIMEOUT_SECONDS) != null) {
			str.append(indent);
			str.append("InactiveConnectionTimeoutSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getInactiveConnectionTimeoutSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(INACTIVE_CONNECTION_TIMEOUT_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("TestTableName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTestTableName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TEST_TABLE_NAME, 0, str, indent);

		if (this.getValue(LOGIN_DELAY_SECONDS) != null) {
			str.append(indent);
			str.append("LoginDelaySeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getLoginDelaySeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(LOGIN_DELAY_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("InitSql");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getInitSql();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INIT_SQL, 0, str, indent);

		if (this.getValue(STATEMENT_CACHE_SIZE) != null) {
			str.append(indent);
			str.append("StatementCacheSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getStatementCacheSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(STATEMENT_CACHE_SIZE, 0, str, indent);
		}

		str.append(indent);
		str.append("StatementCacheType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getStatementCacheType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(STATEMENT_CACHE_TYPE, 0, str, indent);

		str.append(indent);
		str.append("RemoveInfectedConnections");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRemoveInfectedConnections()?"true":"false"));
		this.dumpAttributes(REMOVE_INFECTED_CONNECTIONS, 0, str, indent);

		if (this.getValue(SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION) != null) {
			str.append(indent);
			str.append("SecondsToTrustAnIdlePoolConnection");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getSecondsToTrustAnIdlePoolConnection());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SECONDS_TO_TRUST_AN_IDLE_POOL_CONNECTION, 0, str, indent);
		}

		str.append(indent);
		str.append("StatementTimeout");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getStatementTimeout();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(STATEMENT_TIMEOUT, 0, str, indent);

		if (this.getValue(PROFILE_TYPE) != null) {
			str.append(indent);
			str.append("ProfileType");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getProfileType());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PROFILE_TYPE, 0, str, indent);
		}

		str.append(indent);
		str.append("JdbcXaDebugLevel");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJdbcXaDebugLevel();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JDBC_XA_DEBUG_LEVEL, 0, str, indent);

		str.append(indent);
		str.append("CredentialMappingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCredentialMappingEnabled()?"true":"false"));
		this.dumpAttributes(CREDENTIAL_MAPPING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("DriverInterceptor");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDriverInterceptor();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DRIVER_INTERCEPTOR, 0, str, indent);

		str.append(indent);
		str.append("PinnedToThread");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPinnedToThread()?"true":"false"));
		this.dumpAttributes(PINNED_TO_THREAD, 0, str, indent);

		str.append(indent);
		str.append("IdentityBasedConnectionPoolingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isIdentityBasedConnectionPoolingEnabled()?"true":"false"));
		this.dumpAttributes(IDENTITY_BASED_CONNECTION_POOLING_ENABLED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JdbcConnectionPoolParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

