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
 *	This generated bean class XaParamsType matches the schema element 'xa-paramsType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:47 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class XaParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String DEBUG_LEVEL = "DebugLevel";	// NOI18N
	static public final String KEEP_CONN_UNTIL_TX_COMPLETE_ENABLED = "KeepConnUntilTxCompleteEnabled";	// NOI18N
	static public final String END_ONLY_ONCE_ENABLED = "EndOnlyOnceEnabled";	// NOI18N
	static public final String RECOVER_ONLY_ONCE_ENABLED = "RecoverOnlyOnceEnabled";	// NOI18N
	static public final String TX_CONTEXT_ON_CLOSE_NEEDED = "TxContextOnCloseNeeded";	// NOI18N
	static public final String NEW_CONN_FOR_COMMIT_ENABLED = "NewConnForCommitEnabled";	// NOI18N
	static public final String PREPARED_STATEMENT_CACHE_SIZE = "PreparedStatementCacheSize";	// NOI18N
	static public final String KEEP_LOGICAL_CONN_OPEN_ON_RELEASE = "KeepLogicalConnOpenOnRelease";	// NOI18N
	static public final String LOCAL_TRANSACTION_SUPPORTED = "LocalTransactionSupported";	// NOI18N
	static public final String RESOURCE_HEALTH_MONITORING_ENABLED = "ResourceHealthMonitoringEnabled";	// NOI18N
	static public final String XA_SET_TRANSACTION_TIMEOUT = "XaSetTransactionTimeout";	// NOI18N
	static public final String XA_TRANSACTION_TIMEOUT = "XaTransactionTimeout";	// NOI18N
	static public final String ROLLBACK_LOCALTX_UPON_CONNCLOSE = "RollbackLocaltxUponConnclose";	// NOI18N

	public XaParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public XaParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(13);
		this.createProperty("debug-level", 	// NOI18N
			DEBUG_LEVEL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("keep-conn-until-tx-complete-enabled", 	// NOI18N
			KEEP_CONN_UNTIL_TX_COMPLETE_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("end-only-once-enabled", 	// NOI18N
			END_ONLY_ONCE_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("recover-only-once-enabled", 	// NOI18N
			RECOVER_ONLY_ONCE_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("tx-context-on-close-needed", 	// NOI18N
			TX_CONTEXT_ON_CLOSE_NEEDED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("new-conn-for-commit-enabled", 	// NOI18N
			NEW_CONN_FOR_COMMIT_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("prepared-statement-cache-size", 	// NOI18N
			PREPARED_STATEMENT_CACHE_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("keep-logical-conn-open-on-release", 	// NOI18N
			KEEP_LOGICAL_CONN_OPEN_ON_RELEASE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("local-transaction-supported", 	// NOI18N
			LOCAL_TRANSACTION_SUPPORTED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("resource-health-monitoring-enabled", 	// NOI18N
			RESOURCE_HEALTH_MONITORING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("xa-set-transaction-timeout", 	// NOI18N
			XA_SET_TRANSACTION_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("xa-transaction-timeout", 	// NOI18N
			XA_TRANSACTION_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("rollback-localtx-upon-connclose", 	// NOI18N
			ROLLBACK_LOCALTX_UPON_CONNCLOSE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setDebugLevel(int value) {
		this.setValue(DEBUG_LEVEL, java.lang.Integer.valueOf(value));
	}

	//
	public int getDebugLevel() {
		Integer ret = (Integer)this.getValue(DEBUG_LEVEL);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"DEBUG_LEVEL", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setKeepConnUntilTxCompleteEnabled(boolean value) {
		this.setValue(KEEP_CONN_UNTIL_TX_COMPLETE_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isKeepConnUntilTxCompleteEnabled() {
		Boolean ret = (Boolean)this.getValue(KEEP_CONN_UNTIL_TX_COMPLETE_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setEndOnlyOnceEnabled(boolean value) {
		this.setValue(END_ONLY_ONCE_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isEndOnlyOnceEnabled() {
		Boolean ret = (Boolean)this.getValue(END_ONLY_ONCE_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRecoverOnlyOnceEnabled(boolean value) {
		this.setValue(RECOVER_ONLY_ONCE_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRecoverOnlyOnceEnabled() {
		Boolean ret = (Boolean)this.getValue(RECOVER_ONLY_ONCE_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setTxContextOnCloseNeeded(boolean value) {
		this.setValue(TX_CONTEXT_ON_CLOSE_NEEDED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isTxContextOnCloseNeeded() {
		Boolean ret = (Boolean)this.getValue(TX_CONTEXT_ON_CLOSE_NEEDED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setNewConnForCommitEnabled(boolean value) {
		this.setValue(NEW_CONN_FOR_COMMIT_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isNewConnForCommitEnabled() {
		Boolean ret = (Boolean)this.getValue(NEW_CONN_FOR_COMMIT_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPreparedStatementCacheSize(int value) {
		this.setValue(PREPARED_STATEMENT_CACHE_SIZE, java.lang.Integer.valueOf(value));
	}

	//
	public int getPreparedStatementCacheSize() {
		Integer ret = (Integer)this.getValue(PREPARED_STATEMENT_CACHE_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"PREPARED_STATEMENT_CACHE_SIZE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setKeepLogicalConnOpenOnRelease(boolean value) {
		this.setValue(KEEP_LOGICAL_CONN_OPEN_ON_RELEASE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isKeepLogicalConnOpenOnRelease() {
		Boolean ret = (Boolean)this.getValue(KEEP_LOGICAL_CONN_OPEN_ON_RELEASE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setLocalTransactionSupported(boolean value) {
		this.setValue(LOCAL_TRANSACTION_SUPPORTED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isLocalTransactionSupported() {
		Boolean ret = (Boolean)this.getValue(LOCAL_TRANSACTION_SUPPORTED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setResourceHealthMonitoringEnabled(boolean value) {
		this.setValue(RESOURCE_HEALTH_MONITORING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isResourceHealthMonitoringEnabled() {
		Boolean ret = (Boolean)this.getValue(RESOURCE_HEALTH_MONITORING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setXaSetTransactionTimeout(boolean value) {
		this.setValue(XA_SET_TRANSACTION_TIMEOUT, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isXaSetTransactionTimeout() {
		Boolean ret = (Boolean)this.getValue(XA_SET_TRANSACTION_TIMEOUT);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setXaTransactionTimeout(int value) {
		this.setValue(XA_TRANSACTION_TIMEOUT, java.lang.Integer.valueOf(value));
	}

	//
	public int getXaTransactionTimeout() {
		Integer ret = (Integer)this.getValue(XA_TRANSACTION_TIMEOUT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"XA_TRANSACTION_TIMEOUT", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setRollbackLocaltxUponConnclose(boolean value) {
		this.setValue(ROLLBACK_LOCALTX_UPON_CONNCLOSE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRollbackLocaltxUponConnclose() {
		Boolean ret = (Boolean)this.getValue(ROLLBACK_LOCALTX_UPON_CONNCLOSE);
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
		// Validating property debugLevel
		// Validating property keepConnUntilTxCompleteEnabled
		{
			boolean patternPassed = false;
			if ((isKeepConnUntilTxCompleteEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isKeepConnUntilTxCompleteEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "keepConnUntilTxCompleteEnabled", this);	// NOI18N
		}
		// Validating property endOnlyOnceEnabled
		{
			boolean patternPassed = false;
			if ((isEndOnlyOnceEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isEndOnlyOnceEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "endOnlyOnceEnabled", this);	// NOI18N
		}
		// Validating property recoverOnlyOnceEnabled
		{
			boolean patternPassed = false;
			if ((isRecoverOnlyOnceEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRecoverOnlyOnceEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "recoverOnlyOnceEnabled", this);	// NOI18N
		}
		// Validating property txContextOnCloseNeeded
		{
			boolean patternPassed = false;
			if ((isTxContextOnCloseNeeded() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isTxContextOnCloseNeeded()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "txContextOnCloseNeeded", this);	// NOI18N
		}
		// Validating property newConnForCommitEnabled
		{
			boolean patternPassed = false;
			if ((isNewConnForCommitEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isNewConnForCommitEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "newConnForCommitEnabled", this);	// NOI18N
		}
		// Validating property preparedStatementCacheSize
		// Validating property keepLogicalConnOpenOnRelease
		{
			boolean patternPassed = false;
			if ((isKeepLogicalConnOpenOnRelease() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isKeepLogicalConnOpenOnRelease()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "keepLogicalConnOpenOnRelease", this);	// NOI18N
		}
		// Validating property localTransactionSupported
		{
			boolean patternPassed = false;
			if ((isLocalTransactionSupported() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isLocalTransactionSupported()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "localTransactionSupported", this);	// NOI18N
		}
		// Validating property resourceHealthMonitoringEnabled
		{
			boolean patternPassed = false;
			if ((isResourceHealthMonitoringEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isResourceHealthMonitoringEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "resourceHealthMonitoringEnabled", this);	// NOI18N
		}
		// Validating property xaSetTransactionTimeout
		{
			boolean patternPassed = false;
			if ((isXaSetTransactionTimeout() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isXaSetTransactionTimeout()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaSetTransactionTimeout", this);	// NOI18N
		}
		// Validating property xaTransactionTimeout
		// Validating property rollbackLocaltxUponConnclose
		{
			boolean patternPassed = false;
			if ((isRollbackLocaltxUponConnclose() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRollbackLocaltxUponConnclose()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rollbackLocaltxUponConnclose", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(DEBUG_LEVEL) != null) {
			str.append(indent);
			str.append("DebugLevel");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getDebugLevel());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DEBUG_LEVEL, 0, str, indent);
		}

		str.append(indent);
		str.append("KeepConnUntilTxCompleteEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isKeepConnUntilTxCompleteEnabled()?"true":"false"));
		this.dumpAttributes(KEEP_CONN_UNTIL_TX_COMPLETE_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("EndOnlyOnceEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isEndOnlyOnceEnabled()?"true":"false"));
		this.dumpAttributes(END_ONLY_ONCE_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("RecoverOnlyOnceEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRecoverOnlyOnceEnabled()?"true":"false"));
		this.dumpAttributes(RECOVER_ONLY_ONCE_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("TxContextOnCloseNeeded");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isTxContextOnCloseNeeded()?"true":"false"));
		this.dumpAttributes(TX_CONTEXT_ON_CLOSE_NEEDED, 0, str, indent);

		str.append(indent);
		str.append("NewConnForCommitEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isNewConnForCommitEnabled()?"true":"false"));
		this.dumpAttributes(NEW_CONN_FOR_COMMIT_ENABLED, 0, str, indent);

		if (this.getValue(PREPARED_STATEMENT_CACHE_SIZE) != null) {
			str.append(indent);
			str.append("PreparedStatementCacheSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getPreparedStatementCacheSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PREPARED_STATEMENT_CACHE_SIZE, 0, str, indent);
		}

		str.append(indent);
		str.append("KeepLogicalConnOpenOnRelease");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isKeepLogicalConnOpenOnRelease()?"true":"false"));
		this.dumpAttributes(KEEP_LOGICAL_CONN_OPEN_ON_RELEASE, 0, str, indent);

		str.append(indent);
		str.append("LocalTransactionSupported");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isLocalTransactionSupported()?"true":"false"));
		this.dumpAttributes(LOCAL_TRANSACTION_SUPPORTED, 0, str, indent);

		str.append(indent);
		str.append("ResourceHealthMonitoringEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isResourceHealthMonitoringEnabled()?"true":"false"));
		this.dumpAttributes(RESOURCE_HEALTH_MONITORING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("XaSetTransactionTimeout");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isXaSetTransactionTimeout()?"true":"false"));
		this.dumpAttributes(XA_SET_TRANSACTION_TIMEOUT, 0, str, indent);

		if (this.getValue(XA_TRANSACTION_TIMEOUT) != null) {
			str.append(indent);
			str.append("XaTransactionTimeout");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getXaTransactionTimeout());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(XA_TRANSACTION_TIMEOUT, 0, str, indent);
		}

		str.append(indent);
		str.append("RollbackLocaltxUponConnclose");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRollbackLocaltxUponConnclose()?"true":"false"));
		this.dumpAttributes(ROLLBACK_LOCALTX_UPON_CONNCLOSE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("XaParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

