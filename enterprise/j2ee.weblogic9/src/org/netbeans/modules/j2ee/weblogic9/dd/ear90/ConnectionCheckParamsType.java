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
 *	This generated bean class ConnectionCheckParamsType matches the schema element 'connection-check-paramsType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:50 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ConnectionCheckParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String TABLE_NAME = "TableName";	// NOI18N
	static public final String CHECK_ON_RESERVE_ENABLED = "CheckOnReserveEnabled";	// NOI18N
	static public final String CHECK_ON_RELEASE_ENABLED = "CheckOnReleaseEnabled";	// NOI18N
	static public final String REFRESH_MINUTES = "RefreshMinutes";	// NOI18N
	static public final String CHECK_ON_CREATE_ENABLED = "CheckOnCreateEnabled";	// NOI18N
	static public final String CONNECTION_RESERVE_TIMEOUT_SECONDS = "ConnectionReserveTimeoutSeconds";	// NOI18N
	static public final String CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS = "ConnectionCreationRetryFrequencySeconds";	// NOI18N
	static public final String INACTIVE_CONNECTION_TIMEOUT_SECONDS = "InactiveConnectionTimeoutSeconds";	// NOI18N
	static public final String TEST_FREQUENCY_SECONDS = "TestFrequencySeconds";	// NOI18N
	static public final String INIT_SQL = "InitSql";	// NOI18N

	public ConnectionCheckParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ConnectionCheckParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(10);
		this.createProperty("table-name", 	// NOI18N
			TABLE_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("check-on-reserve-enabled", 	// NOI18N
			CHECK_ON_RESERVE_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("check-on-release-enabled", 	// NOI18N
			CHECK_ON_RELEASE_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("refresh-minutes", 	// NOI18N
			REFRESH_MINUTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("check-on-create-enabled", 	// NOI18N
			CHECK_ON_CREATE_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("connection-reserve-timeout-seconds", 	// NOI18N
			CONNECTION_RESERVE_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("connection-creation-retry-frequency-seconds", 	// NOI18N
			CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("inactive-connection-timeout-seconds", 	// NOI18N
			INACTIVE_CONNECTION_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("test-frequency-seconds", 	// NOI18N
			TEST_FREQUENCY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("init-sql", 	// NOI18N
			INIT_SQL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setTableName(java.lang.String value) {
		this.setValue(TABLE_NAME, value);
	}

	//
	public java.lang.String getTableName() {
		return (java.lang.String)this.getValue(TABLE_NAME);
	}

	// This attribute is optional
	public void setCheckOnReserveEnabled(boolean value) {
		this.setValue(CHECK_ON_RESERVE_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCheckOnReserveEnabled() {
		Boolean ret = (Boolean)this.getValue(CHECK_ON_RESERVE_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setCheckOnReleaseEnabled(boolean value) {
		this.setValue(CHECK_ON_RELEASE_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCheckOnReleaseEnabled() {
		Boolean ret = (Boolean)this.getValue(CHECK_ON_RELEASE_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRefreshMinutes(int value) {
		this.setValue(REFRESH_MINUTES, java.lang.Integer.valueOf(value));
	}

	//
	public int getRefreshMinutes() {
		Integer ret = (Integer)this.getValue(REFRESH_MINUTES);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"REFRESH_MINUTES", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setCheckOnCreateEnabled(boolean value) {
		this.setValue(CHECK_ON_CREATE_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCheckOnCreateEnabled() {
		Boolean ret = (Boolean)this.getValue(CHECK_ON_CREATE_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setConnectionReserveTimeoutSeconds(int value) {
		this.setValue(CONNECTION_RESERVE_TIMEOUT_SECONDS, java.lang.Integer.valueOf(value));
	}

	//
	public int getConnectionReserveTimeoutSeconds() {
		Integer ret = (Integer)this.getValue(CONNECTION_RESERVE_TIMEOUT_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CONNECTION_RESERVE_TIMEOUT_SECONDS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setConnectionCreationRetryFrequencySeconds(int value) {
		this.setValue(CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS, java.lang.Integer.valueOf(value));
	}

	//
	public int getConnectionCreationRetryFrequencySeconds() {
		Integer ret = (Integer)this.getValue(CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setInactiveConnectionTimeoutSeconds(int value) {
		this.setValue(INACTIVE_CONNECTION_TIMEOUT_SECONDS, java.lang.Integer.valueOf(value));
	}

	//
	public int getInactiveConnectionTimeoutSeconds() {
		Integer ret = (Integer)this.getValue(INACTIVE_CONNECTION_TIMEOUT_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"INACTIVE_CONNECTION_TIMEOUT_SECONDS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setTestFrequencySeconds(int value) {
		this.setValue(TEST_FREQUENCY_SECONDS, java.lang.Integer.valueOf(value));
	}

	//
	public int getTestFrequencySeconds() {
		Integer ret = (Integer)this.getValue(TEST_FREQUENCY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"TEST_FREQUENCY_SECONDS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setInitSql(java.lang.String value) {
		this.setValue(INIT_SQL, value);
	}

	//
	public java.lang.String getInitSql() {
		return (java.lang.String)this.getValue(INIT_SQL);
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
		// Validating property tableName
		// Validating property checkOnReserveEnabled
		{
			boolean patternPassed = false;
			if ((isCheckOnReserveEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCheckOnReserveEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "checkOnReserveEnabled", this);	// NOI18N
		}
		// Validating property checkOnReleaseEnabled
		{
			boolean patternPassed = false;
			if ((isCheckOnReleaseEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCheckOnReleaseEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "checkOnReleaseEnabled", this);	// NOI18N
		}
		// Validating property refreshMinutes
		// Validating property checkOnCreateEnabled
		{
			boolean patternPassed = false;
			if ((isCheckOnCreateEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCheckOnCreateEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "checkOnCreateEnabled", this);	// NOI18N
		}
		// Validating property connectionReserveTimeoutSeconds
		// Validating property connectionCreationRetryFrequencySeconds
		// Validating property inactiveConnectionTimeoutSeconds
		// Validating property testFrequencySeconds
		// Validating property initSql
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("TableName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTableName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TABLE_NAME, 0, str, indent);

		str.append(indent);
		str.append("CheckOnReserveEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCheckOnReserveEnabled()?"true":"false"));
		this.dumpAttributes(CHECK_ON_RESERVE_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("CheckOnReleaseEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCheckOnReleaseEnabled()?"true":"false"));
		this.dumpAttributes(CHECK_ON_RELEASE_ENABLED, 0, str, indent);

		if (this.getValue(REFRESH_MINUTES) != null) {
			str.append(indent);
			str.append("RefreshMinutes");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getRefreshMinutes());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(REFRESH_MINUTES, 0, str, indent);
		}

		str.append(indent);
		str.append("CheckOnCreateEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCheckOnCreateEnabled()?"true":"false"));
		this.dumpAttributes(CHECK_ON_CREATE_ENABLED, 0, str, indent);

		if (this.getValue(CONNECTION_RESERVE_TIMEOUT_SECONDS) != null) {
			str.append(indent);
			str.append("ConnectionReserveTimeoutSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getConnectionReserveTimeoutSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CONNECTION_RESERVE_TIMEOUT_SECONDS, 0, str, indent);
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
		str.append("InitSql");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getInitSql();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INIT_SQL, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ConnectionCheckParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

