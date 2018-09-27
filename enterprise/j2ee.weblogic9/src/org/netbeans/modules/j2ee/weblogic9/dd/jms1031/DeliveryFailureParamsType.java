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
 *	This generated bean class DeliveryFailureParamsType matches the schema element 'delivery-failure-params-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:59 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class DeliveryFailureParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ERROR_DESTINATION = "ErrorDestination";	// NOI18N
	static public final String REDELIVERY_LIMIT = "RedeliveryLimit";	// NOI18N
	static public final String EXPIRATION_POLICY = "ExpirationPolicy";	// NOI18N
	static public final String EXPIRATION_LOGGING_POLICY = "ExpirationLoggingPolicy";	// NOI18N

	public DeliveryFailureParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public DeliveryFailureParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("error-destination", 	// NOI18N
			ERROR_DESTINATION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("redelivery-limit", 	// NOI18N
			REDELIVERY_LIMIT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("expiration-policy", 	// NOI18N
			EXPIRATION_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("expiration-logging-policy", 	// NOI18N
			EXPIRATION_LOGGING_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setErrorDestination(java.lang.String value) {
		this.setValue(ERROR_DESTINATION, value);
	}

	//
	public java.lang.String getErrorDestination() {
		return (java.lang.String)this.getValue(ERROR_DESTINATION);
	}

	// This attribute is optional
	public void setRedeliveryLimit(int value) {
		this.setValue(REDELIVERY_LIMIT, java.lang.Integer.valueOf(value));
	}

	//
	public int getRedeliveryLimit() {
		Integer ret = (Integer)this.getValue(REDELIVERY_LIMIT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"REDELIVERY_LIMIT", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setExpirationPolicy(java.lang.String value) {
		this.setValue(EXPIRATION_POLICY, value);
	}

	//
	public java.lang.String getExpirationPolicy() {
		return (java.lang.String)this.getValue(EXPIRATION_POLICY);
	}

	// This attribute is optional
	public void setExpirationLoggingPolicy(java.lang.String value) {
		this.setValue(EXPIRATION_LOGGING_POLICY, value);
	}

	//
	public java.lang.String getExpirationLoggingPolicy() {
		return (java.lang.String)this.getValue(EXPIRATION_LOGGING_POLICY);
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
		// Validating property errorDestination
		// Validating property redeliveryLimit
		// Validating property expirationPolicy
		if (getExpirationPolicy() != null) {
			final java.lang.String[] enumRestrictionExpirationPolicy = {"Discard", "Log", "Redirect"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionExpirationPolicy.length; ++_index2) {
				if (enumRestrictionExpirationPolicy[_index2].equals(getExpirationPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getExpirationPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "expirationPolicy", this);	// NOI18N
			}
		}
		// Validating property expirationLoggingPolicy
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ErrorDestination");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getErrorDestination();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ERROR_DESTINATION, 0, str, indent);

		if (this.getValue(REDELIVERY_LIMIT) != null) {
			str.append(indent);
			str.append("RedeliveryLimit");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getRedeliveryLimit());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(REDELIVERY_LIMIT, 0, str, indent);
		}

		str.append(indent);
		str.append("ExpirationPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getExpirationPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(EXPIRATION_POLICY, 0, str, indent);

		str.append(indent);
		str.append("ExpirationLoggingPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getExpirationLoggingPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(EXPIRATION_LOGGING_POLICY, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DeliveryFailureParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

