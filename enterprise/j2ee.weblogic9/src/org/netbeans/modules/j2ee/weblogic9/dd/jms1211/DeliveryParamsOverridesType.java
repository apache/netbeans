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
 *	This generated bean class DeliveryParamsOverridesType matches the schema element 'delivery-params-overrides-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:27:00 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class DeliveryParamsOverridesType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String DELIVERY_MODE = "DeliveryMode";	// NOI18N
	static public final String TIME_TO_DELIVER = "TimeToDeliver";	// NOI18N
	static public final String TIME_TO_LIVE = "TimeToLive";	// NOI18N
	static public final String PRIORITY = "Priority";	// NOI18N
	static public final String REDELIVERY_DELAY = "RedeliveryDelay";	// NOI18N

	public DeliveryParamsOverridesType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public DeliveryParamsOverridesType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("delivery-mode", 	// NOI18N
			DELIVERY_MODE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("time-to-deliver", 	// NOI18N
			TIME_TO_DELIVER, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("time-to-live", 	// NOI18N
			TIME_TO_LIVE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("priority", 	// NOI18N
			PRIORITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("redelivery-delay", 	// NOI18N
			REDELIVERY_DELAY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setDeliveryMode(java.lang.String value) {
		this.setValue(DELIVERY_MODE, value);
	}

	//
	public java.lang.String getDeliveryMode() {
		return (java.lang.String)this.getValue(DELIVERY_MODE);
	}

	// This attribute is optional
	public void setTimeToDeliver(java.lang.String value) {
		this.setValue(TIME_TO_DELIVER, value);
	}

	//
	public java.lang.String getTimeToDeliver() {
		return (java.lang.String)this.getValue(TIME_TO_DELIVER);
	}

	// This attribute is optional
	public void setTimeToLive(long value) {
		this.setValue(TIME_TO_LIVE, java.lang.Long.valueOf(value));
	}

	//
	public long getTimeToLive() {
		Long ret = (Long)this.getValue(TIME_TO_LIVE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"TIME_TO_LIVE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setPriority(int value) {
		this.setValue(PRIORITY, java.lang.Integer.valueOf(value));
	}

	//
	public int getPriority() {
		Integer ret = (Integer)this.getValue(PRIORITY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"PRIORITY", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setRedeliveryDelay(long value) {
		this.setValue(REDELIVERY_DELAY, java.lang.Long.valueOf(value));
	}

	//
	public long getRedeliveryDelay() {
		Long ret = (Long)this.getValue(REDELIVERY_DELAY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"REDELIVERY_DELAY", "long"}));
		return ((java.lang.Long)ret).longValue();
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
		// Validating property deliveryMode
		if (getDeliveryMode() != null) {
			final java.lang.String[] enumRestrictionDeliveryMode = {"Persistent", "Non-Persistent", "No-Delivery"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionDeliveryMode.length; ++_index2) {
				if (enumRestrictionDeliveryMode[_index2].equals(getDeliveryMode())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDeliveryMode() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "deliveryMode", this);	// NOI18N
			}
		}
		// Validating property timeToDeliver
		// Validating property timeToLive
		// Validating property priority
		// Validating property redeliveryDelay
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("DeliveryMode");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDeliveryMode();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DELIVERY_MODE, 0, str, indent);

		str.append(indent);
		str.append("TimeToDeliver");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTimeToDeliver();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TIME_TO_DELIVER, 0, str, indent);

		if (this.getValue(TIME_TO_LIVE) != null) {
			str.append(indent);
			str.append("TimeToLive");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getTimeToLive());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(TIME_TO_LIVE, 0, str, indent);
		}

		if (this.getValue(PRIORITY) != null) {
			str.append(indent);
			str.append("Priority");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getPriority());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PRIORITY, 0, str, indent);
		}

		if (this.getValue(REDELIVERY_DELAY) != null) {
			str.append(indent);
			str.append("RedeliveryDelay");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getRedeliveryDelay());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(REDELIVERY_DELAY, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DeliveryParamsOverridesType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

