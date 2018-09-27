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
 *	This generated bean class DefaultDeliveryParamsType matches the schema element 'default-delivery-params-type'.
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

public class DefaultDeliveryParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String DEFAULT_DELIVERY_MODE = "DefaultDeliveryMode";	// NOI18N
	static public final String DEFAULT_TIME_TO_DELIVER = "DefaultTimeToDeliver";	// NOI18N
	static public final String DEFAULT_TIME_TO_LIVE = "DefaultTimeToLive";	// NOI18N
	static public final String DEFAULT_PRIORITY = "DefaultPriority";	// NOI18N
	static public final String DEFAULT_REDELIVERY_DELAY = "DefaultRedeliveryDelay";	// NOI18N
	static public final String SEND_TIMEOUT = "SendTimeout";	// NOI18N
	static public final String DEFAULT_COMPRESSION_THRESHOLD = "DefaultCompressionThreshold";	// NOI18N
	static public final String DEFAULT_UNIT_OF_ORDER = "DefaultUnitOfOrder";	// NOI18N

	public DefaultDeliveryParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public DefaultDeliveryParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(8);
		this.createProperty("default-delivery-mode", 	// NOI18N
			DEFAULT_DELIVERY_MODE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("default-time-to-deliver", 	// NOI18N
			DEFAULT_TIME_TO_DELIVER, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("default-time-to-live", 	// NOI18N
			DEFAULT_TIME_TO_LIVE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("default-priority", 	// NOI18N
			DEFAULT_PRIORITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("default-redelivery-delay", 	// NOI18N
			DEFAULT_REDELIVERY_DELAY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("send-timeout", 	// NOI18N
			SEND_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("default-compression-threshold", 	// NOI18N
			DEFAULT_COMPRESSION_THRESHOLD, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("default-unit-of-order", 	// NOI18N
			DEFAULT_UNIT_OF_ORDER, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setDefaultDeliveryMode(java.lang.String value) {
		this.setValue(DEFAULT_DELIVERY_MODE, value);
	}

	//
	public java.lang.String getDefaultDeliveryMode() {
		return (java.lang.String)this.getValue(DEFAULT_DELIVERY_MODE);
	}

	// This attribute is optional
	public void setDefaultTimeToDeliver(java.lang.String value) {
		this.setValue(DEFAULT_TIME_TO_DELIVER, value);
	}

	//
	public java.lang.String getDefaultTimeToDeliver() {
		return (java.lang.String)this.getValue(DEFAULT_TIME_TO_DELIVER);
	}

	// This attribute is optional
	public void setDefaultTimeToLive(long value) {
		this.setValue(DEFAULT_TIME_TO_LIVE, java.lang.Long.valueOf(value));
	}

	//
	public long getDefaultTimeToLive() {
		Long ret = (Long)this.getValue(DEFAULT_TIME_TO_LIVE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"DEFAULT_TIME_TO_LIVE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setDefaultPriority(int value) {
		this.setValue(DEFAULT_PRIORITY, java.lang.Integer.valueOf(value));
	}

	//
	public int getDefaultPriority() {
		Integer ret = (Integer)this.getValue(DEFAULT_PRIORITY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"DEFAULT_PRIORITY", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setDefaultRedeliveryDelay(long value) {
		this.setValue(DEFAULT_REDELIVERY_DELAY, java.lang.Long.valueOf(value));
	}

	//
	public long getDefaultRedeliveryDelay() {
		Long ret = (Long)this.getValue(DEFAULT_REDELIVERY_DELAY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"DEFAULT_REDELIVERY_DELAY", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setSendTimeout(long value) {
		this.setValue(SEND_TIMEOUT, java.lang.Long.valueOf(value));
	}

	//
	public long getSendTimeout() {
		Long ret = (Long)this.getValue(SEND_TIMEOUT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"SEND_TIMEOUT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setDefaultCompressionThreshold(int value) {
		this.setValue(DEFAULT_COMPRESSION_THRESHOLD, java.lang.Integer.valueOf(value));
	}

	//
	public int getDefaultCompressionThreshold() {
		Integer ret = (Integer)this.getValue(DEFAULT_COMPRESSION_THRESHOLD);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"DEFAULT_COMPRESSION_THRESHOLD", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setDefaultUnitOfOrder(java.lang.String value) {
		this.setValue(DEFAULT_UNIT_OF_ORDER, value);
	}

	//
	public java.lang.String getDefaultUnitOfOrder() {
		return (java.lang.String)this.getValue(DEFAULT_UNIT_OF_ORDER);
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
		// Validating property defaultDeliveryMode
		if (getDefaultDeliveryMode() != null) {
			final java.lang.String[] enumRestrictionDefaultDeliveryMode = {"Persistent", "Non-Persistent"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionDefaultDeliveryMode.length; 
				++_index2) {
				if (enumRestrictionDefaultDeliveryMode[_index2].equals(getDefaultDeliveryMode())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDefaultDeliveryMode() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "defaultDeliveryMode", this);	// NOI18N
			}
		}
		// Validating property defaultTimeToDeliver
		// Validating property defaultTimeToLive
		// Validating property defaultPriority
		// Validating property defaultRedeliveryDelay
		// Validating property sendTimeout
		// Validating property defaultCompressionThreshold
		// Validating property defaultUnitOfOrder
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("DefaultDeliveryMode");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDefaultDeliveryMode();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DEFAULT_DELIVERY_MODE, 0, str, indent);

		str.append(indent);
		str.append("DefaultTimeToDeliver");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDefaultTimeToDeliver();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DEFAULT_TIME_TO_DELIVER, 0, str, indent);

		if (this.getValue(DEFAULT_TIME_TO_LIVE) != null) {
			str.append(indent);
			str.append("DefaultTimeToLive");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getDefaultTimeToLive());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DEFAULT_TIME_TO_LIVE, 0, str, indent);
		}

		if (this.getValue(DEFAULT_PRIORITY) != null) {
			str.append(indent);
			str.append("DefaultPriority");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getDefaultPriority());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DEFAULT_PRIORITY, 0, str, indent);
		}

		if (this.getValue(DEFAULT_REDELIVERY_DELAY) != null) {
			str.append(indent);
			str.append("DefaultRedeliveryDelay");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getDefaultRedeliveryDelay());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DEFAULT_REDELIVERY_DELAY, 0, str, indent);
		}

		if (this.getValue(SEND_TIMEOUT) != null) {
			str.append(indent);
			str.append("SendTimeout");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getSendTimeout());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SEND_TIMEOUT, 0, str, indent);
		}

		if (this.getValue(DEFAULT_COMPRESSION_THRESHOLD) != null) {
			str.append(indent);
			str.append("DefaultCompressionThreshold");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getDefaultCompressionThreshold());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DEFAULT_COMPRESSION_THRESHOLD, 0, str, indent);
		}

		str.append(indent);
		str.append("DefaultUnitOfOrder");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDefaultUnitOfOrder();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DEFAULT_UNIT_OF_ORDER, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DefaultDeliveryParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

