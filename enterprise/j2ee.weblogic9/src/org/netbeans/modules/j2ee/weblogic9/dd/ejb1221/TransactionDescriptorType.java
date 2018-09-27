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
 *	This generated bean class TransactionDescriptorType matches the schema element 'transaction-descriptorType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:56 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class TransactionDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String TRANS_TIMEOUT_SECONDS = "TransTimeoutSeconds";	// NOI18N
	static public final String TRANSTIMEOUTSECONDSJ2EEID = "TransTimeoutSecondsJ2eeId";	// NOI18N
	static public final String TRANSTIMEOUTSECONDSREMOTECLIENTTIMEOUTJ2EEID2 = "TransTimeoutSecondsRemoteClientTimeoutJ2eeId2";	// NOI18N

	public TransactionDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public TransactionDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("trans-timeout-seconds", 	// NOI18N
			TRANS_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(TRANS_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(TRANS_TIMEOUT_SECONDS, "j2ee:id", "RemoteClientTimeoutJ2eeId2", 
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
	public void setTransTimeoutSeconds(long value) {
		this.setValue(TRANS_TIMEOUT_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getTransTimeoutSeconds() {
		Long ret = (Long)this.getValue(TRANS_TIMEOUT_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"TRANS_TIMEOUT_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setTransTimeoutSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TRANS_TIMEOUT_SECONDS) == 0) {
			setValue(TRANS_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(TRANS_TIMEOUT_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getTransTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TRANS_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(TRANS_TIMEOUT_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setTransTimeoutSecondsRemoteClientTimeoutJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TRANS_TIMEOUT_SECONDS) == 0) {
			setValue(TRANS_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(TRANS_TIMEOUT_SECONDS, "RemoteClientTimeoutJ2eeId2", value);
	}

	//
	public java.lang.String getTransTimeoutSecondsRemoteClientTimeoutJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TRANS_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(TRANS_TIMEOUT_SECONDS, "RemoteClientTimeoutJ2eeId2");
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
		// Validating property transTimeoutSeconds
		if (getTransTimeoutSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTransTimeoutSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "transTimeoutSeconds", this);	// NOI18N
		}
		// Validating property transTimeoutSecondsJ2eeId
		if (getTransTimeoutSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTransTimeoutSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "transTimeoutSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property transTimeoutSecondsRemoteClientTimeoutJ2eeId2
		if (getTransTimeoutSecondsRemoteClientTimeoutJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTransTimeoutSecondsRemoteClientTimeoutJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "transTimeoutSecondsRemoteClientTimeoutJ2eeId2", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(TRANS_TIMEOUT_SECONDS) != null) {
			str.append(indent);
			str.append("TransTimeoutSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getTransTimeoutSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(TRANS_TIMEOUT_SECONDS, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("TransactionDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

