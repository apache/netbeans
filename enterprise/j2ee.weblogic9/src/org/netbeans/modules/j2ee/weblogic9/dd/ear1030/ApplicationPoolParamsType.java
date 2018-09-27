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
 *	This generated bean class ApplicationPoolParamsType matches the schema element 'application-pool-paramsType'.
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

public class ApplicationPoolParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String SIZE_PARAMS = "SizeParams";	// NOI18N
	static public final String XA_PARAMS = "XaParams";	// NOI18N
	static public final String LOGIN_DELAY_SECONDS = "LoginDelaySeconds";	// NOI18N
	static public final String LEAK_PROFILING_ENABLED = "LeakProfilingEnabled";	// NOI18N
	static public final String CONNECTION_CHECK_PARAMS = "ConnectionCheckParams";	// NOI18N
	static public final String JDBCXA_DEBUG_LEVEL = "JdbcxaDebugLevel";	// NOI18N
	static public final String REMOVE_INFECTED_CONNECTIONS_ENABLED = "RemoveInfectedConnectionsEnabled";	// NOI18N

	public ApplicationPoolParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ApplicationPoolParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(7);
		this.createProperty("size-params", 	// NOI18N
			SIZE_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SizeParamsType.class);
		this.createProperty("xa-params", 	// NOI18N
			XA_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			XaParamsType.class);
		this.createProperty("login-delay-seconds", 	// NOI18N
			LOGIN_DELAY_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("leak-profiling-enabled", 	// NOI18N
			LEAK_PROFILING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("connection-check-params", 	// NOI18N
			CONNECTION_CHECK_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectionCheckParamsType.class);
		this.createProperty("jdbcxa-debug-level", 	// NOI18N
			JDBCXA_DEBUG_LEVEL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("remove-infected-connections-enabled", 	// NOI18N
			REMOVE_INFECTED_CONNECTIONS_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setSizeParams(SizeParamsType value) {
		this.setValue(SIZE_PARAMS, value);
	}

	//
	public SizeParamsType getSizeParams() {
		return (SizeParamsType)this.getValue(SIZE_PARAMS);
	}

	// This attribute is optional
	public void setXaParams(XaParamsType value) {
		this.setValue(XA_PARAMS, value);
	}

	//
	public XaParamsType getXaParams() {
		return (XaParamsType)this.getValue(XA_PARAMS);
	}

	// This attribute is optional
	public void setLoginDelaySeconds(int value) {
		this.setValue(LOGIN_DELAY_SECONDS, java.lang.Integer.valueOf(value));
	}

	//
	public int getLoginDelaySeconds() {
		Integer ret = (Integer)this.getValue(LOGIN_DELAY_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"LOGIN_DELAY_SECONDS", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setLeakProfilingEnabled(boolean value) {
		this.setValue(LEAK_PROFILING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isLeakProfilingEnabled() {
		Boolean ret = (Boolean)this.getValue(LEAK_PROFILING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setConnectionCheckParams(ConnectionCheckParamsType value) {
		this.setValue(CONNECTION_CHECK_PARAMS, value);
	}

	//
	public ConnectionCheckParamsType getConnectionCheckParams() {
		return (ConnectionCheckParamsType)this.getValue(CONNECTION_CHECK_PARAMS);
	}

	// This attribute is optional
	public void setJdbcxaDebugLevel(int value) {
		this.setValue(JDBCXA_DEBUG_LEVEL, java.lang.Integer.valueOf(value));
	}

	//
	public int getJdbcxaDebugLevel() {
		Integer ret = (Integer)this.getValue(JDBCXA_DEBUG_LEVEL);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"JDBCXA_DEBUG_LEVEL", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setRemoveInfectedConnectionsEnabled(boolean value) {
		this.setValue(REMOVE_INFECTED_CONNECTIONS_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRemoveInfectedConnectionsEnabled() {
		Boolean ret = (Boolean)this.getValue(REMOVE_INFECTED_CONNECTIONS_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SizeParamsType newSizeParamsType() {
		return new SizeParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public XaParamsType newXaParamsType() {
		return new XaParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectionCheckParamsType newConnectionCheckParamsType() {
		return new ConnectionCheckParamsType();
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
		// Validating property sizeParams
		if (getSizeParams() != null) {
			getSizeParams().validate();
		}
		// Validating property xaParams
		if (getXaParams() != null) {
			getXaParams().validate();
		}
		// Validating property loginDelaySeconds
		// Validating property leakProfilingEnabled
		{
			boolean patternPassed = false;
			if ((isLeakProfilingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isLeakProfilingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "leakProfilingEnabled", this);	// NOI18N
		}
		// Validating property connectionCheckParams
		if (getConnectionCheckParams() != null) {
			getConnectionCheckParams().validate();
		}
		// Validating property jdbcxaDebugLevel
		// Validating property removeInfectedConnectionsEnabled
		{
			boolean patternPassed = false;
			if ((isRemoveInfectedConnectionsEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRemoveInfectedConnectionsEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "removeInfectedConnectionsEnabled", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("SizeParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSizeParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SIZE_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("XaParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getXaParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(XA_PARAMS, 0, str, indent);

		if (this.getValue(LOGIN_DELAY_SECONDS) != null) {
			str.append(indent);
			str.append("LoginDelaySeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getLoginDelaySeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(LOGIN_DELAY_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("LeakProfilingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isLeakProfilingEnabled()?"true":"false"));
		this.dumpAttributes(LEAK_PROFILING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("ConnectionCheckParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectionCheckParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CONNECTION_CHECK_PARAMS, 0, str, indent);

		if (this.getValue(JDBCXA_DEBUG_LEVEL) != null) {
			str.append(indent);
			str.append("JdbcxaDebugLevel");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getJdbcxaDebugLevel());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(JDBCXA_DEBUG_LEVEL, 0, str, indent);
		}

		str.append(indent);
		str.append("RemoveInfectedConnectionsEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRemoveInfectedConnectionsEnabled()?"true":"false"));
		this.dumpAttributes(REMOVE_INFECTED_CONNECTIONS_ENABLED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ApplicationPoolParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

