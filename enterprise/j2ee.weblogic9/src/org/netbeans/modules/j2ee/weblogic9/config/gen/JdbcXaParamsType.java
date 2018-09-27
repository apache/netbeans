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
 *	This generated bean class JdbcXaParamsType matches the schema element 'jdbc-xa-paramsType'.
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

public class JdbcXaParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String KEEP_XA_CONN_TILL_TX_COMPLETE = "KeepXaConnTillTxComplete";	// NOI18N
	static public final String NEED_TX_CTX_ON_CLOSE = "NeedTxCtxOnClose";	// NOI18N
	static public final String XA_END_ONLY_ONCE = "XaEndOnlyOnce";	// NOI18N
	static public final String NEW_XA_CONN_FOR_COMMIT = "NewXaConnForCommit";	// NOI18N
	static public final String KEEP_LOGICAL_CONN_OPEN_ON_RELEASE = "KeepLogicalConnOpenOnRelease";	// NOI18N
	static public final String RESOURCE_HEALTH_MONITORING = "ResourceHealthMonitoring";	// NOI18N
	static public final String RECOVER_ONLY_ONCE = "RecoverOnlyOnce";	// NOI18N
	static public final String XA_SET_TRANSACTION_TIMEOUT = "XaSetTransactionTimeout";	// NOI18N
	static public final String XA_TRANSACTION_TIMEOUT = "XaTransactionTimeout";	// NOI18N
	static public final String XATRANSACTIONTIMEOUTJ2EEID = "XaTransactionTimeoutJ2eeId";	// NOI18N
	static public final String ROLLBACK_LOCAL_TX_UPON_CONN_CLOSE = "RollbackLocalTxUponConnClose";	// NOI18N
	static public final String XA_RETRY_DURATION_SECONDS = "XaRetryDurationSeconds";	// NOI18N
	static public final String XARETRYDURATIONSECONDSJ2EEID = "XaRetryDurationSecondsJ2eeId";	// NOI18N
	static public final String XA_RETRY_INTERVAL_SECONDS = "XaRetryIntervalSeconds";	// NOI18N
	static public final String XARETRYINTERVALSECONDSJ2EEID = "XaRetryIntervalSecondsJ2eeId";	// NOI18N

	public JdbcXaParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JdbcXaParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(12);
		this.createProperty("keep-xa-conn-till-tx-complete", 	// NOI18N
			KEEP_XA_CONN_TILL_TX_COMPLETE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("need-tx-ctx-on-close", 	// NOI18N
			NEED_TX_CTX_ON_CLOSE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("xa-end-only-once", 	// NOI18N
			XA_END_ONLY_ONCE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("new-xa-conn-for-commit", 	// NOI18N
			NEW_XA_CONN_FOR_COMMIT, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("keep-logical-conn-open-on-release", 	// NOI18N
			KEEP_LOGICAL_CONN_OPEN_ON_RELEASE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("resource-health-monitoring", 	// NOI18N
			RESOURCE_HEALTH_MONITORING, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("recover-only-once", 	// NOI18N
			RECOVER_ONLY_ONCE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("xa-set-transaction-timeout", 	// NOI18N
			XA_SET_TRANSACTION_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("xa-transaction-timeout", 	// NOI18N
			XA_TRANSACTION_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(XA_TRANSACTION_TIMEOUT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("rollback-local-tx-upon-conn-close", 	// NOI18N
			ROLLBACK_LOCAL_TX_UPON_CONN_CLOSE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("xa-retry-duration-seconds", 	// NOI18N
			XA_RETRY_DURATION_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(XA_RETRY_DURATION_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("xa-retry-interval-seconds", 	// NOI18N
			XA_RETRY_INTERVAL_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(XA_RETRY_INTERVAL_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setKeepXaConnTillTxComplete(boolean value) {
		this.setValue(KEEP_XA_CONN_TILL_TX_COMPLETE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isKeepXaConnTillTxComplete() {
		Boolean ret = (Boolean)this.getValue(KEEP_XA_CONN_TILL_TX_COMPLETE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setNeedTxCtxOnClose(boolean value) {
		this.setValue(NEED_TX_CTX_ON_CLOSE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isNeedTxCtxOnClose() {
		Boolean ret = (Boolean)this.getValue(NEED_TX_CTX_ON_CLOSE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setXaEndOnlyOnce(boolean value) {
		this.setValue(XA_END_ONLY_ONCE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isXaEndOnlyOnce() {
		Boolean ret = (Boolean)this.getValue(XA_END_ONLY_ONCE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setNewXaConnForCommit(boolean value) {
		this.setValue(NEW_XA_CONN_FOR_COMMIT, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isNewXaConnForCommit() {
		Boolean ret = (Boolean)this.getValue(NEW_XA_CONN_FOR_COMMIT);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
	public void setResourceHealthMonitoring(boolean value) {
		this.setValue(RESOURCE_HEALTH_MONITORING, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isResourceHealthMonitoring() {
		Boolean ret = (Boolean)this.getValue(RESOURCE_HEALTH_MONITORING);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRecoverOnlyOnce(boolean value) {
		this.setValue(RECOVER_ONLY_ONCE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRecoverOnlyOnce() {
		Boolean ret = (Boolean)this.getValue(RECOVER_ONLY_ONCE);
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
	public void setXaTransactionTimeout(long value) {
		this.setValue(XA_TRANSACTION_TIMEOUT, java.lang.Long.valueOf(value));
	}

	//
	public long getXaTransactionTimeout() {
		Long ret = (Long)this.getValue(XA_TRANSACTION_TIMEOUT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"XA_TRANSACTION_TIMEOUT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setXaTransactionTimeoutJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(XA_TRANSACTION_TIMEOUT) == 0) {
			setValue(XA_TRANSACTION_TIMEOUT, "");
		}
		setAttributeValue(XA_TRANSACTION_TIMEOUT, "J2eeId", value);
	}

	//
	public java.lang.String getXaTransactionTimeoutJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(XA_TRANSACTION_TIMEOUT) == 0) {
			return null;
		} else {
			return getAttributeValue(XA_TRANSACTION_TIMEOUT, "J2eeId");
		}
	}

	// This attribute is optional
	public void setRollbackLocalTxUponConnClose(boolean value) {
		this.setValue(ROLLBACK_LOCAL_TX_UPON_CONN_CLOSE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRollbackLocalTxUponConnClose() {
		Boolean ret = (Boolean)this.getValue(ROLLBACK_LOCAL_TX_UPON_CONN_CLOSE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setXaRetryDurationSeconds(long value) {
		this.setValue(XA_RETRY_DURATION_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getXaRetryDurationSeconds() {
		Long ret = (Long)this.getValue(XA_RETRY_DURATION_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"XA_RETRY_DURATION_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setXaRetryDurationSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(XA_TRANSACTION_TIMEOUT) == 0) {
			setValue(XA_TRANSACTION_TIMEOUT, "");
		}
		setAttributeValue(XA_TRANSACTION_TIMEOUT, "J2eeId", value);
	}

	//
	public java.lang.String getXaRetryDurationSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(XA_TRANSACTION_TIMEOUT) == 0) {
			return null;
		} else {
			return getAttributeValue(XA_TRANSACTION_TIMEOUT, "J2eeId");
		}
	}

	// This attribute is optional
	public void setXaRetryIntervalSeconds(long value) {
		this.setValue(XA_RETRY_INTERVAL_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getXaRetryIntervalSeconds() {
		Long ret = (Long)this.getValue(XA_RETRY_INTERVAL_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"XA_RETRY_INTERVAL_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setXaRetryIntervalSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(XA_TRANSACTION_TIMEOUT) == 0) {
			setValue(XA_TRANSACTION_TIMEOUT, "");
		}
		setAttributeValue(XA_TRANSACTION_TIMEOUT, "J2eeId", value);
	}

	//
	public java.lang.String getXaRetryIntervalSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(XA_TRANSACTION_TIMEOUT) == 0) {
			return null;
		} else {
			return getAttributeValue(XA_TRANSACTION_TIMEOUT, "J2eeId");
		}
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
		// Validating property keepXaConnTillTxComplete
		{
			boolean patternPassed = false;
			if ((isKeepXaConnTillTxComplete() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isKeepXaConnTillTxComplete()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "keepXaConnTillTxComplete", this);	// NOI18N
		}
		// Validating property needTxCtxOnClose
		{
			boolean patternPassed = false;
			if ((isNeedTxCtxOnClose() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isNeedTxCtxOnClose()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "needTxCtxOnClose", this);	// NOI18N
		}
		// Validating property xaEndOnlyOnce
		{
			boolean patternPassed = false;
			if ((isXaEndOnlyOnce() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isXaEndOnlyOnce()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaEndOnlyOnce", this);	// NOI18N
		}
		// Validating property newXaConnForCommit
		{
			boolean patternPassed = false;
			if ((isNewXaConnForCommit() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isNewXaConnForCommit()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "newXaConnForCommit", this);	// NOI18N
		}
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
		// Validating property resourceHealthMonitoring
		{
			boolean patternPassed = false;
			if ((isResourceHealthMonitoring() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isResourceHealthMonitoring()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "resourceHealthMonitoring", this);	// NOI18N
		}
		// Validating property recoverOnlyOnce
		{
			boolean patternPassed = false;
			if ((isRecoverOnlyOnce() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRecoverOnlyOnce()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "recoverOnlyOnce", this);	// NOI18N
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
		if (getXaTransactionTimeout() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getXaTransactionTimeout() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaTransactionTimeout", this);	// NOI18N
		}
		// Validating property xaTransactionTimeoutJ2eeId
		if (getXaTransactionTimeoutJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getXaTransactionTimeoutJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaTransactionTimeoutJ2eeId", this);	// NOI18N
			}
		}
		// Validating property rollbackLocalTxUponConnClose
		{
			boolean patternPassed = false;
			if ((isRollbackLocalTxUponConnClose() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRollbackLocalTxUponConnClose()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rollbackLocalTxUponConnClose", this);	// NOI18N
		}
		// Validating property xaRetryDurationSeconds
		if (getXaRetryDurationSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getXaRetryDurationSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaRetryDurationSeconds", this);	// NOI18N
		}
		// Validating property xaRetryDurationSecondsJ2eeId
		if (getXaRetryDurationSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getXaRetryDurationSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaRetryDurationSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property xaRetryIntervalSeconds
		if (getXaRetryIntervalSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getXaRetryIntervalSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaRetryIntervalSeconds", this);	// NOI18N
		}
		// Validating property xaRetryIntervalSecondsJ2eeId
		if (getXaRetryIntervalSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getXaRetryIntervalSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "xaRetryIntervalSecondsJ2eeId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("KeepXaConnTillTxComplete");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isKeepXaConnTillTxComplete()?"true":"false"));
		this.dumpAttributes(KEEP_XA_CONN_TILL_TX_COMPLETE, 0, str, indent);

		str.append(indent);
		str.append("NeedTxCtxOnClose");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isNeedTxCtxOnClose()?"true":"false"));
		this.dumpAttributes(NEED_TX_CTX_ON_CLOSE, 0, str, indent);

		str.append(indent);
		str.append("XaEndOnlyOnce");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isXaEndOnlyOnce()?"true":"false"));
		this.dumpAttributes(XA_END_ONLY_ONCE, 0, str, indent);

		str.append(indent);
		str.append("NewXaConnForCommit");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isNewXaConnForCommit()?"true":"false"));
		this.dumpAttributes(NEW_XA_CONN_FOR_COMMIT, 0, str, indent);

		str.append(indent);
		str.append("KeepLogicalConnOpenOnRelease");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isKeepLogicalConnOpenOnRelease()?"true":"false"));
		this.dumpAttributes(KEEP_LOGICAL_CONN_OPEN_ON_RELEASE, 0, str, indent);

		str.append(indent);
		str.append("ResourceHealthMonitoring");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isResourceHealthMonitoring()?"true":"false"));
		this.dumpAttributes(RESOURCE_HEALTH_MONITORING, 0, str, indent);

		str.append(indent);
		str.append("RecoverOnlyOnce");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRecoverOnlyOnce()?"true":"false"));
		this.dumpAttributes(RECOVER_ONLY_ONCE, 0, str, indent);

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
		str.append("RollbackLocalTxUponConnClose");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRollbackLocalTxUponConnClose()?"true":"false"));
		this.dumpAttributes(ROLLBACK_LOCAL_TX_UPON_CONN_CLOSE, 0, str, indent);

		if (this.getValue(XA_RETRY_DURATION_SECONDS) != null) {
			str.append(indent);
			str.append("XaRetryDurationSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getXaRetryDurationSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(XA_RETRY_DURATION_SECONDS, 0, str, indent);
		}

		if (this.getValue(XA_RETRY_INTERVAL_SECONDS) != null) {
			str.append(indent);
			str.append("XaRetryIntervalSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getXaRetryIntervalSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(XA_RETRY_INTERVAL_SECONDS, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JdbcXaParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

