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
 *	This generated bean class FastSwapType matches the schema element 'fast-swapType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:48 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class FastSwapType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ENABLED = "Enabled";	// NOI18N
	static public final String REFRESH_INTERVAL = "RefreshInterval";	// NOI18N
	static public final String REFRESHINTERVALJ2EEID = "RefreshIntervalJ2eeId";	// NOI18N
	static public final String REFRESHINTERVALFAIRSHAREJ2EEID2 = "RefreshIntervalFairShareJ2eeId2";	// NOI18N
	static public final String REDEFINITION_TASK_LIMIT = "RedefinitionTaskLimit";	// NOI18N
	static public final String REDEFINITIONTASKLIMITJ2EEID = "RedefinitionTaskLimitJ2eeId";	// NOI18N
	static public final String REDEFINITIONTASKLIMITFAIRSHAREJ2EEID2 = "RedefinitionTaskLimitFairShareJ2eeId2";	// NOI18N

	public FastSwapType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public FastSwapType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("enabled", 	// NOI18N
			ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("refresh-interval", 	// NOI18N
			REFRESH_INTERVAL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(REFRESH_INTERVAL, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(REFRESH_INTERVAL, "j2ee:id", "FairShareJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("redefinition-task-limit", 	// NOI18N
			REDEFINITION_TASK_LIMIT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(REDEFINITION_TASK_LIMIT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(REDEFINITION_TASK_LIMIT, "j2ee:id", "FairShareJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setEnabled(boolean value) {
		this.setValue(ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isEnabled() {
		Boolean ret = (Boolean)this.getValue(ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRefreshInterval(java.math.BigInteger value) {
		this.setValue(REFRESH_INTERVAL, value);
	}

	//
	public java.math.BigInteger getRefreshInterval() {
		return (java.math.BigInteger)this.getValue(REFRESH_INTERVAL);
	}

	// This attribute is optional
	public void setRefreshIntervalJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REFRESH_INTERVAL) == 0) {
			setValue(REFRESH_INTERVAL, "");
		}
		setAttributeValue(REFRESH_INTERVAL, "J2eeId", value);
	}

	//
	public java.lang.String getRefreshIntervalJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REFRESH_INTERVAL) == 0) {
			return null;
		} else {
			return getAttributeValue(REFRESH_INTERVAL, "J2eeId");
		}
	}

	// This attribute is optional
	public void setRefreshIntervalFairShareJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REFRESH_INTERVAL) == 0) {
			setValue(REFRESH_INTERVAL, "");
		}
		setAttributeValue(REFRESH_INTERVAL, "FairShareJ2eeId2", value);
	}

	//
	public java.lang.String getRefreshIntervalFairShareJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REFRESH_INTERVAL) == 0) {
			return null;
		} else {
			return getAttributeValue(REFRESH_INTERVAL, "FairShareJ2eeId2");
		}
	}

	// This attribute is optional
	public void setRedefinitionTaskLimit(java.math.BigInteger value) {
		this.setValue(REDEFINITION_TASK_LIMIT, value);
	}

	//
	public java.math.BigInteger getRedefinitionTaskLimit() {
		return (java.math.BigInteger)this.getValue(REDEFINITION_TASK_LIMIT);
	}

	// This attribute is optional
	public void setRedefinitionTaskLimitJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REFRESH_INTERVAL) == 0) {
			setValue(REFRESH_INTERVAL, "");
		}
		setAttributeValue(REFRESH_INTERVAL, "J2eeId", value);
	}

	//
	public java.lang.String getRedefinitionTaskLimitJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REFRESH_INTERVAL) == 0) {
			return null;
		} else {
			return getAttributeValue(REFRESH_INTERVAL, "J2eeId");
		}
	}

	// This attribute is optional
	public void setRedefinitionTaskLimitFairShareJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REFRESH_INTERVAL) == 0) {
			setValue(REFRESH_INTERVAL, "");
		}
		setAttributeValue(REFRESH_INTERVAL, "FairShareJ2eeId2", value);
	}

	//
	public java.lang.String getRedefinitionTaskLimitFairShareJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REFRESH_INTERVAL) == 0) {
			return null;
		} else {
			return getAttributeValue(REFRESH_INTERVAL, "FairShareJ2eeId2");
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
		// Validating property enabled
		{
			boolean patternPassed = false;
			if ((isEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "enabled", this);	// NOI18N
		}
		// Validating property refreshInterval
		// Validating property refreshIntervalJ2eeId
		if (getRefreshIntervalJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRefreshIntervalJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "refreshIntervalJ2eeId", this);	// NOI18N
			}
		}
		// Validating property refreshIntervalFairShareJ2eeId2
		if (getRefreshIntervalFairShareJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRefreshIntervalFairShareJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "refreshIntervalFairShareJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property redefinitionTaskLimit
		// Validating property redefinitionTaskLimitJ2eeId
		if (getRedefinitionTaskLimitJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRedefinitionTaskLimitJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "redefinitionTaskLimitJ2eeId", this);	// NOI18N
			}
		}
		// Validating property redefinitionTaskLimitFairShareJ2eeId2
		if (getRedefinitionTaskLimitFairShareJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRedefinitionTaskLimitFairShareJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "redefinitionTaskLimitFairShareJ2eeId2", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Enabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isEnabled()?"true":"false"));
		this.dumpAttributes(ENABLED, 0, str, indent);

		str.append(indent);
		str.append("RefreshInterval");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRefreshInterval();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REFRESH_INTERVAL, 0, str, indent);

		str.append(indent);
		str.append("RedefinitionTaskLimit");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRedefinitionTaskLimit();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REDEFINITION_TASK_LIMIT, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("FastSwapType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

