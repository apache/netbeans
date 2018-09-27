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
 *	This generated bean class ListenerType matches the schema element 'listenerType'.
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

public class ListenerType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String LISTENER_CLASS = "ListenerClass";	// NOI18N
	static public final String LISTENER_URI = "ListenerUri";	// NOI18N
	static public final String RUN_AS_PRINCIPAL_NAME = "RunAsPrincipalName";	// NOI18N

	public ListenerType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ListenerType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("listener-class", 	// NOI18N
			LISTENER_CLASS, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("listener-uri", 	// NOI18N
			LISTENER_URI, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("run-as-principal-name", 	// NOI18N
			RUN_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setListenerClass(java.lang.String value) {
		this.setValue(LISTENER_CLASS, value);
	}

	//
	public java.lang.String getListenerClass() {
		return (java.lang.String)this.getValue(LISTENER_CLASS);
	}

	// This attribute is optional
	public void setListenerUri(java.lang.String value) {
		this.setValue(LISTENER_URI, value);
	}

	//
	public java.lang.String getListenerUri() {
		return (java.lang.String)this.getValue(LISTENER_URI);
	}

	// This attribute is optional
	public void setRunAsPrincipalName(java.lang.String value) {
		this.setValue(RUN_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getRunAsPrincipalName() {
		return (java.lang.String)this.getValue(RUN_AS_PRINCIPAL_NAME);
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
		// Validating property listenerClass
		if (getListenerClass() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getListenerClass() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "listenerClass", this);	// NOI18N
		}
		// Validating property listenerUri
		// Validating property runAsPrincipalName
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ListenerClass");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getListenerClass();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LISTENER_CLASS, 0, str, indent);

		str.append(indent);
		str.append("ListenerUri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getListenerUri();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LISTENER_URI, 0, str, indent);

		str.append(indent);
		str.append("RunAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRunAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RUN_AS_PRINCIPAL_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ListenerType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

