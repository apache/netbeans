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
 *	This generated bean class ApplicationAdminModeTriggerType matches the schema element 'application-admin-mode-triggerType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:44 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ApplicationAdminModeTriggerType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String MAX_STUCK_THREAD_TIME = "MaxStuckThreadTime";	// NOI18N
	static public final String MAXSTUCKTHREADTIMEJ2EEID = "MaxStuckThreadTimeJ2eeId";	// NOI18N
	static public final String MAXSTUCKTHREADTIMEFAIRSHAREJ2EEID2 = "MaxStuckThreadTimeFairShareJ2eeId2";	// NOI18N
	static public final String STUCK_THREAD_COUNT = "StuckThreadCount";	// NOI18N
	static public final String STUCKTHREADCOUNTJ2EEID = "StuckThreadCountJ2eeId";	// NOI18N
	static public final String STUCKTHREADCOUNTFAIRSHAREJ2EEID2 = "StuckThreadCountFairShareJ2eeId2";	// NOI18N

	public ApplicationAdminModeTriggerType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ApplicationAdminModeTriggerType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("max-stuck-thread-time", 	// NOI18N
			MAX_STUCK_THREAD_TIME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(MAX_STUCK_THREAD_TIME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MAX_STUCK_THREAD_TIME, "j2ee:id", "FairShareJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stuck-thread-count", 	// NOI18N
			STUCK_THREAD_COUNT, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(STUCK_THREAD_COUNT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(STUCK_THREAD_COUNT, "j2ee:id", "FairShareJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
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
	public void setMaxStuckThreadTime(java.math.BigInteger value) {
		this.setValue(MAX_STUCK_THREAD_TIME, value);
	}

	//
	public java.math.BigInteger getMaxStuckThreadTime() {
		return (java.math.BigInteger)this.getValue(MAX_STUCK_THREAD_TIME);
	}

	// This attribute is optional
	public void setMaxStuckThreadTimeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			setValue(MAX_STUCK_THREAD_TIME, "");
		}
		setAttributeValue(MAX_STUCK_THREAD_TIME, "J2eeId", value);
	}

	//
	public java.lang.String getMaxStuckThreadTimeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_STUCK_THREAD_TIME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setMaxStuckThreadTimeFairShareJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			setValue(MAX_STUCK_THREAD_TIME, "");
		}
		setAttributeValue(MAX_STUCK_THREAD_TIME, "FairShareJ2eeId2", value);
	}

	//
	public java.lang.String getMaxStuckThreadTimeFairShareJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_STUCK_THREAD_TIME, "FairShareJ2eeId2");
		}
	}

	// This attribute is mandatory
	public void setStuckThreadCount(java.math.BigInteger value) {
		this.setValue(STUCK_THREAD_COUNT, value);
	}

	//
	public java.math.BigInteger getStuckThreadCount() {
		return (java.math.BigInteger)this.getValue(STUCK_THREAD_COUNT);
	}

	// This attribute is optional
	public void setStuckThreadCountJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			setValue(MAX_STUCK_THREAD_TIME, "");
		}
		setAttributeValue(MAX_STUCK_THREAD_TIME, "J2eeId", value);
	}

	//
	public java.lang.String getStuckThreadCountJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_STUCK_THREAD_TIME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setStuckThreadCountFairShareJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			setValue(MAX_STUCK_THREAD_TIME, "");
		}
		setAttributeValue(MAX_STUCK_THREAD_TIME, "FairShareJ2eeId2", value);
	}

	//
	public java.lang.String getStuckThreadCountFairShareJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_STUCK_THREAD_TIME) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_STUCK_THREAD_TIME, "FairShareJ2eeId2");
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
		// Validating property id
		if (getId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "id", this);	// NOI18N
			}
		}
		// Validating property maxStuckThreadTime
		// Validating property maxStuckThreadTimeJ2eeId
		if (getMaxStuckThreadTimeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxStuckThreadTimeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxStuckThreadTimeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property maxStuckThreadTimeFairShareJ2eeId2
		if (getMaxStuckThreadTimeFairShareJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxStuckThreadTimeFairShareJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxStuckThreadTimeFairShareJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property stuckThreadCount
		if (getStuckThreadCount() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getStuckThreadCount() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "stuckThreadCount", this);	// NOI18N
		}
		// Validating property stuckThreadCountJ2eeId
		if (getStuckThreadCountJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStuckThreadCountJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "stuckThreadCountJ2eeId", this);	// NOI18N
			}
		}
		// Validating property stuckThreadCountFairShareJ2eeId2
		if (getStuckThreadCountFairShareJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStuckThreadCountFairShareJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "stuckThreadCountFairShareJ2eeId2", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("MaxStuckThreadTime");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMaxStuckThreadTime();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MAX_STUCK_THREAD_TIME, 0, str, indent);

		str.append(indent);
		str.append("StuckThreadCount");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getStuckThreadCount();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(STUCK_THREAD_COUNT, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ApplicationAdminModeTriggerType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

