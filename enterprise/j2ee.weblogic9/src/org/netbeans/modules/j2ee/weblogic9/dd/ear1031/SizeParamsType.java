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
 *	This generated bean class SizeParamsType matches the schema element 'size-paramsType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:45 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SizeParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String INITIAL_CAPACITY = "InitialCapacity";	// NOI18N
	static public final String MAX_CAPACITY = "MaxCapacity";	// NOI18N
	static public final String CAPACITY_INCREMENT = "CapacityIncrement";	// NOI18N
	static public final String SHRINKING_ENABLED = "ShrinkingEnabled";	// NOI18N
	static public final String SHRINK_PERIOD_MINUTES = "ShrinkPeriodMinutes";	// NOI18N
	static public final String SHRINK_FREQUENCY_SECONDS = "ShrinkFrequencySeconds";	// NOI18N
	static public final String HIGHEST_NUM_WAITERS = "HighestNumWaiters";	// NOI18N
	static public final String HIGHEST_NUM_UNAVAILABLE = "HighestNumUnavailable";	// NOI18N

	public SizeParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SizeParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(8);
		this.createProperty("initial-capacity", 	// NOI18N
			INITIAL_CAPACITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("max-capacity", 	// NOI18N
			MAX_CAPACITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("capacity-increment", 	// NOI18N
			CAPACITY_INCREMENT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("shrinking-enabled", 	// NOI18N
			SHRINKING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("shrink-period-minutes", 	// NOI18N
			SHRINK_PERIOD_MINUTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("shrink-frequency-seconds", 	// NOI18N
			SHRINK_FREQUENCY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("highest-num-waiters", 	// NOI18N
			HIGHEST_NUM_WAITERS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("highest-num-unavailable", 	// NOI18N
			HIGHEST_NUM_UNAVAILABLE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setInitialCapacity(int value) {
		this.setValue(INITIAL_CAPACITY, java.lang.Integer.valueOf(value));
	}

	//
	public int getInitialCapacity() {
		Integer ret = (Integer)this.getValue(INITIAL_CAPACITY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"INITIAL_CAPACITY", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setMaxCapacity(int value) {
		this.setValue(MAX_CAPACITY, java.lang.Integer.valueOf(value));
	}

	//
	public int getMaxCapacity() {
		Integer ret = (Integer)this.getValue(MAX_CAPACITY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_CAPACITY", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setCapacityIncrement(int value) {
		this.setValue(CAPACITY_INCREMENT, java.lang.Integer.valueOf(value));
	}

	//
	public int getCapacityIncrement() {
		Integer ret = (Integer)this.getValue(CAPACITY_INCREMENT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CAPACITY_INCREMENT", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setShrinkingEnabled(boolean value) {
		this.setValue(SHRINKING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isShrinkingEnabled() {
		Boolean ret = (Boolean)this.getValue(SHRINKING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setShrinkPeriodMinutes(int value) {
		this.setValue(SHRINK_PERIOD_MINUTES, java.lang.Integer.valueOf(value));
	}

	//
	public int getShrinkPeriodMinutes() {
		Integer ret = (Integer)this.getValue(SHRINK_PERIOD_MINUTES);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"SHRINK_PERIOD_MINUTES", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setShrinkFrequencySeconds(int value) {
		this.setValue(SHRINK_FREQUENCY_SECONDS, java.lang.Integer.valueOf(value));
	}

	//
	public int getShrinkFrequencySeconds() {
		Integer ret = (Integer)this.getValue(SHRINK_FREQUENCY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"SHRINK_FREQUENCY_SECONDS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setHighestNumWaiters(int value) {
		this.setValue(HIGHEST_NUM_WAITERS, java.lang.Integer.valueOf(value));
	}

	//
	public int getHighestNumWaiters() {
		Integer ret = (Integer)this.getValue(HIGHEST_NUM_WAITERS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"HIGHEST_NUM_WAITERS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setHighestNumUnavailable(int value) {
		this.setValue(HIGHEST_NUM_UNAVAILABLE, java.lang.Integer.valueOf(value));
	}

	//
	public int getHighestNumUnavailable() {
		Integer ret = (Integer)this.getValue(HIGHEST_NUM_UNAVAILABLE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"HIGHEST_NUM_UNAVAILABLE", "int"}));
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
		// Validating property initialCapacity
		// Validating property maxCapacity
		// Validating property capacityIncrement
		// Validating property shrinkingEnabled
		{
			boolean patternPassed = false;
			if ((isShrinkingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isShrinkingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "shrinkingEnabled", this);	// NOI18N
		}
		// Validating property shrinkPeriodMinutes
		// Validating property shrinkFrequencySeconds
		// Validating property highestNumWaiters
		// Validating property highestNumUnavailable
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(INITIAL_CAPACITY) != null) {
			str.append(indent);
			str.append("InitialCapacity");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getInitialCapacity());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(INITIAL_CAPACITY, 0, str, indent);
		}

		if (this.getValue(MAX_CAPACITY) != null) {
			str.append(indent);
			str.append("MaxCapacity");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxCapacity());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_CAPACITY, 0, str, indent);
		}

		if (this.getValue(CAPACITY_INCREMENT) != null) {
			str.append(indent);
			str.append("CapacityIncrement");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getCapacityIncrement());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CAPACITY_INCREMENT, 0, str, indent);
		}

		str.append(indent);
		str.append("ShrinkingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isShrinkingEnabled()?"true":"false"));
		this.dumpAttributes(SHRINKING_ENABLED, 0, str, indent);

		if (this.getValue(SHRINK_PERIOD_MINUTES) != null) {
			str.append(indent);
			str.append("ShrinkPeriodMinutes");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getShrinkPeriodMinutes());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SHRINK_PERIOD_MINUTES, 0, str, indent);
		}

		if (this.getValue(SHRINK_FREQUENCY_SECONDS) != null) {
			str.append(indent);
			str.append("ShrinkFrequencySeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getShrinkFrequencySeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SHRINK_FREQUENCY_SECONDS, 0, str, indent);
		}

		if (this.getValue(HIGHEST_NUM_WAITERS) != null) {
			str.append(indent);
			str.append("HighestNumWaiters");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getHighestNumWaiters());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(HIGHEST_NUM_WAITERS, 0, str, indent);
		}

		if (this.getValue(HIGHEST_NUM_UNAVAILABLE) != null) {
			str.append(indent);
			str.append("HighestNumUnavailable");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getHighestNumUnavailable());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(HIGHEST_NUM_UNAVAILABLE, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SizeParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

