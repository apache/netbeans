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
 *	This generated bean class FlowControlParamsType matches the schema element 'flow-control-params-type'.
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

public class FlowControlParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String FLOW_MINIMUM = "FlowMinimum";	// NOI18N
	static public final String FLOW_MAXIMUM = "FlowMaximum";	// NOI18N
	static public final String FLOW_INTERVAL = "FlowInterval";	// NOI18N
	static public final String FLOW_STEPS = "FlowSteps";	// NOI18N
	static public final String FLOW_CONTROL_ENABLED = "FlowControlEnabled";	// NOI18N
	static public final String ONE_WAY_SEND_MODE = "OneWaySendMode";	// NOI18N
	static public final String ONE_WAY_SEND_WINDOW_SIZE = "OneWaySendWindowSize";	// NOI18N

	public FlowControlParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public FlowControlParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(7);
		this.createProperty("flow-minimum", 	// NOI18N
			FLOW_MINIMUM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("flow-maximum", 	// NOI18N
			FLOW_MAXIMUM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("flow-interval", 	// NOI18N
			FLOW_INTERVAL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("flow-steps", 	// NOI18N
			FLOW_STEPS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("flow-control-enabled", 	// NOI18N
			FLOW_CONTROL_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("one-way-send-mode", 	// NOI18N
			ONE_WAY_SEND_MODE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("one-way-send-window-size", 	// NOI18N
			ONE_WAY_SEND_WINDOW_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setFlowMinimum(int value) {
		this.setValue(FLOW_MINIMUM, java.lang.Integer.valueOf(value));
	}

	//
	public int getFlowMinimum() {
		Integer ret = (Integer)this.getValue(FLOW_MINIMUM);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"FLOW_MINIMUM", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setFlowMaximum(int value) {
		this.setValue(FLOW_MAXIMUM, java.lang.Integer.valueOf(value));
	}

	//
	public int getFlowMaximum() {
		Integer ret = (Integer)this.getValue(FLOW_MAXIMUM);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"FLOW_MAXIMUM", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setFlowInterval(int value) {
		this.setValue(FLOW_INTERVAL, java.lang.Integer.valueOf(value));
	}

	//
	public int getFlowInterval() {
		Integer ret = (Integer)this.getValue(FLOW_INTERVAL);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"FLOW_INTERVAL", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setFlowSteps(int value) {
		this.setValue(FLOW_STEPS, java.lang.Integer.valueOf(value));
	}

	//
	public int getFlowSteps() {
		Integer ret = (Integer)this.getValue(FLOW_STEPS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"FLOW_STEPS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setFlowControlEnabled(boolean value) {
		this.setValue(FLOW_CONTROL_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isFlowControlEnabled() {
		Boolean ret = (Boolean)this.getValue(FLOW_CONTROL_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setOneWaySendMode(java.lang.String value) {
		this.setValue(ONE_WAY_SEND_MODE, value);
	}

	//
	public java.lang.String getOneWaySendMode() {
		return (java.lang.String)this.getValue(ONE_WAY_SEND_MODE);
	}

	// This attribute is optional
	public void setOneWaySendWindowSize(int value) {
		this.setValue(ONE_WAY_SEND_WINDOW_SIZE, java.lang.Integer.valueOf(value));
	}

	//
	public int getOneWaySendWindowSize() {
		Integer ret = (Integer)this.getValue(ONE_WAY_SEND_WINDOW_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"ONE_WAY_SEND_WINDOW_SIZE", "int"}));
		return ((java.lang.Integer)ret).intValue();
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
		// Validating property flowMinimum
		// Validating property flowMaximum
		// Validating property flowInterval
		// Validating property flowSteps
		// Validating property flowControlEnabled
		// Validating property oneWaySendMode
		if (getOneWaySendMode() != null) {
			final java.lang.String[] enumRestrictionOneWaySendMode = {"enabled", "disabled", "topicOnly"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionOneWaySendMode.length; ++_index2) {
				if (enumRestrictionOneWaySendMode[_index2].equals(getOneWaySendMode())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getOneWaySendMode() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "oneWaySendMode", this);	// NOI18N
			}
		}
		// Validating property oneWaySendWindowSize
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(FLOW_MINIMUM) != null) {
			str.append(indent);
			str.append("FlowMinimum");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getFlowMinimum());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(FLOW_MINIMUM, 0, str, indent);
		}

		if (this.getValue(FLOW_MAXIMUM) != null) {
			str.append(indent);
			str.append("FlowMaximum");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getFlowMaximum());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(FLOW_MAXIMUM, 0, str, indent);
		}

		if (this.getValue(FLOW_INTERVAL) != null) {
			str.append(indent);
			str.append("FlowInterval");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getFlowInterval());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(FLOW_INTERVAL, 0, str, indent);
		}

		if (this.getValue(FLOW_STEPS) != null) {
			str.append(indent);
			str.append("FlowSteps");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getFlowSteps());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(FLOW_STEPS, 0, str, indent);
		}

		str.append(indent);
		str.append("FlowControlEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isFlowControlEnabled()?"true":"false"));
		this.dumpAttributes(FLOW_CONTROL_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("OneWaySendMode");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getOneWaySendMode();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ONE_WAY_SEND_MODE, 0, str, indent);

		if (this.getValue(ONE_WAY_SEND_WINDOW_SIZE) != null) {
			str.append(indent);
			str.append("OneWaySendWindowSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getOneWaySendWindowSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(ONE_WAY_SEND_WINDOW_SIZE, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("FlowControlParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

