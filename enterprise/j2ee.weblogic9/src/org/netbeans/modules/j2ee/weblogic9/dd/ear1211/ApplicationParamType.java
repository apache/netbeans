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
 *	This generated bean class ApplicationParamType matches the schema element 'application-paramType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:46 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ApplicationParamType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String PARAM_NAME = "ParamName";	// NOI18N
	static public final String PARAM_VALUE = "ParamValue";	// NOI18N

	public ApplicationParamType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ApplicationParamType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("param-name", 	// NOI18N
			PARAM_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("param-value", 	// NOI18N
			PARAM_VALUE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setDescription(java.lang.String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public java.lang.String getDescription() {
		return (java.lang.String)this.getValue(DESCRIPTION);
	}

	// This attribute is mandatory
	public void setParamName(java.lang.String value) {
		this.setValue(PARAM_NAME, value);
	}

	//
	public java.lang.String getParamName() {
		return (java.lang.String)this.getValue(PARAM_NAME);
	}

	// This attribute is mandatory
	public void setParamValue(java.lang.String value) {
		this.setValue(PARAM_VALUE, value);
	}

	//
	public java.lang.String getParamValue() {
		return (java.lang.String)this.getValue(PARAM_VALUE);
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
		// Validating property description
		// Validating property paramName
		if (getParamName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getParamName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "paramName", this);	// NOI18N
		}
		// Validating property paramValue
		if (getParamValue() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getParamValue() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "paramValue", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("ParamName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getParamName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PARAM_NAME, 0, str, indent);

		str.append(indent);
		str.append("ParamValue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getParamValue();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PARAM_VALUE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ApplicationParamType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

