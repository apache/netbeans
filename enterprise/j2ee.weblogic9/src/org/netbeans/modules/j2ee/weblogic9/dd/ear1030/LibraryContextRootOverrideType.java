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
 *	This generated bean class LibraryContextRootOverrideType matches the schema element 'library-context-root-overrideType'.
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

public class LibraryContextRootOverrideType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
	static public final String OVERRIDE_VALUE = "OverrideValue";	// NOI18N

	public LibraryContextRootOverrideType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public LibraryContextRootOverrideType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("context-root", 	// NOI18N
			CONTEXT_ROOT, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("override-value", 	// NOI18N
			OVERRIDE_VALUE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setContextRoot(java.lang.String value) {
		this.setValue(CONTEXT_ROOT, value);
	}

	//
	public java.lang.String getContextRoot() {
		return (java.lang.String)this.getValue(CONTEXT_ROOT);
	}

	// This attribute is mandatory
	public void setOverrideValue(java.lang.String value) {
		this.setValue(OVERRIDE_VALUE, value);
	}

	//
	public java.lang.String getOverrideValue() {
		return (java.lang.String)this.getValue(OVERRIDE_VALUE);
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
		// Validating property contextRoot
		if (getContextRoot() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getContextRoot() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "contextRoot", this);	// NOI18N
		}
		// Validating property overrideValue
		if (getOverrideValue() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getOverrideValue() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "overrideValue", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ContextRoot");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getContextRoot();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONTEXT_ROOT, 0, str, indent);

		str.append(indent);
		str.append("OverrideValue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getOverrideValue();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(OVERRIDE_VALUE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("LibraryContextRootOverrideType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

