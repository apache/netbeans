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
 *	This generated bean class PreparedStatementType matches the schema element 'prepared-statementType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:50 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PreparedStatementType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String PROFILING_ENABLED = "ProfilingEnabled";	// NOI18N
	static public final String CACHE_PROFILING_THRESHOLD = "CacheProfilingThreshold";	// NOI18N
	static public final String CACHE_SIZE = "CacheSize";	// NOI18N
	static public final String PARAMETER_LOGGING_ENABLED = "ParameterLoggingEnabled";	// NOI18N
	static public final String MAX_PARAMETER_LENGTH = "MaxParameterLength";	// NOI18N
	static public final String CACHE_TYPE = "CacheType";	// NOI18N

	public PreparedStatementType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PreparedStatementType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("profiling-enabled", 	// NOI18N
			PROFILING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("cache-profiling-threshold", 	// NOI18N
			CACHE_PROFILING_THRESHOLD, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("cache-size", 	// NOI18N
			CACHE_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("parameter-logging-enabled", 	// NOI18N
			PARAMETER_LOGGING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("max-parameter-length", 	// NOI18N
			MAX_PARAMETER_LENGTH, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("cache-type", 	// NOI18N
			CACHE_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setProfilingEnabled(boolean value) {
		this.setValue(PROFILING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isProfilingEnabled() {
		Boolean ret = (Boolean)this.getValue(PROFILING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setCacheProfilingThreshold(int value) {
		this.setValue(CACHE_PROFILING_THRESHOLD, java.lang.Integer.valueOf(value));
	}

	//
	public int getCacheProfilingThreshold() {
		Integer ret = (Integer)this.getValue(CACHE_PROFILING_THRESHOLD);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CACHE_PROFILING_THRESHOLD", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setCacheSize(int value) {
		this.setValue(CACHE_SIZE, java.lang.Integer.valueOf(value));
	}

	//
	public int getCacheSize() {
		Integer ret = (Integer)this.getValue(CACHE_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CACHE_SIZE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setParameterLoggingEnabled(boolean value) {
		this.setValue(PARAMETER_LOGGING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isParameterLoggingEnabled() {
		Boolean ret = (Boolean)this.getValue(PARAMETER_LOGGING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setMaxParameterLength(int value) {
		this.setValue(MAX_PARAMETER_LENGTH, java.lang.Integer.valueOf(value));
	}

	//
	public int getMaxParameterLength() {
		Integer ret = (Integer)this.getValue(MAX_PARAMETER_LENGTH);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_PARAMETER_LENGTH", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setCacheType(int value) {
		this.setValue(CACHE_TYPE, java.lang.Integer.valueOf(value));
	}

	//
	public int getCacheType() {
		Integer ret = (Integer)this.getValue(CACHE_TYPE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CACHE_TYPE", "int"}));
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
		// Validating property profilingEnabled
		{
			boolean patternPassed = false;
			if ((isProfilingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isProfilingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "profilingEnabled", this);	// NOI18N
		}
		// Validating property cacheProfilingThreshold
		// Validating property cacheSize
		// Validating property parameterLoggingEnabled
		{
			boolean patternPassed = false;
			if ((isParameterLoggingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isParameterLoggingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "parameterLoggingEnabled", this);	// NOI18N
		}
		// Validating property maxParameterLength
		// Validating property cacheType
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ProfilingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isProfilingEnabled()?"true":"false"));
		this.dumpAttributes(PROFILING_ENABLED, 0, str, indent);

		if (this.getValue(CACHE_PROFILING_THRESHOLD) != null) {
			str.append(indent);
			str.append("CacheProfilingThreshold");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getCacheProfilingThreshold());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CACHE_PROFILING_THRESHOLD, 0, str, indent);
		}

		if (this.getValue(CACHE_SIZE) != null) {
			str.append(indent);
			str.append("CacheSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getCacheSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CACHE_SIZE, 0, str, indent);
		}

		str.append(indent);
		str.append("ParameterLoggingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isParameterLoggingEnabled()?"true":"false"));
		this.dumpAttributes(PARAMETER_LOGGING_ENABLED, 0, str, indent);

		if (this.getValue(MAX_PARAMETER_LENGTH) != null) {
			str.append(indent);
			str.append("MaxParameterLength");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxParameterLength());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_PARAMETER_LENGTH, 0, str, indent);
		}

		if (this.getValue(CACHE_TYPE) != null) {
			str.append(indent);
			str.append("CacheType");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getCacheType());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CACHE_TYPE, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PreparedStatementType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

