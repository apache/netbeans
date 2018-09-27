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
 *	This generated bean class MessageLoggingParamsType matches the schema element 'message-logging-params-type'.
 *  The root bean class is WeblogicJms
 *
 *	===============================================================
 *	This element was introduced in the 9.0.1 version of 
 *	WebLogic Server.  It should not be used in instance documents that may be 
 *	consumed by prior WebLogic Server versions
 *	
 *	===============================================================
 *	Generated on Tue Jul 25 03:26:59 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class MessageLoggingParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String MESSAGE_LOGGING_ENABLED = "MessageLoggingEnabled";	// NOI18N
	static public final String MESSAGE_LOGGING_FORMAT = "MessageLoggingFormat";	// NOI18N

	public MessageLoggingParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MessageLoggingParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("message-logging-enabled", 	// NOI18N
			MESSAGE_LOGGING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("message-logging-format", 	// NOI18N
			MESSAGE_LOGGING_FORMAT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setMessageLoggingEnabled(boolean value) {
		this.setValue(MESSAGE_LOGGING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isMessageLoggingEnabled() {
		Boolean ret = (Boolean)this.getValue(MESSAGE_LOGGING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setMessageLoggingFormat(java.lang.String value) {
		this.setValue(MESSAGE_LOGGING_FORMAT, value);
	}

	//
	public java.lang.String getMessageLoggingFormat() {
		return (java.lang.String)this.getValue(MESSAGE_LOGGING_FORMAT);
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
		// Validating property messageLoggingEnabled
		// Validating property messageLoggingFormat
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("MessageLoggingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isMessageLoggingEnabled()?"true":"false"));
		this.dumpAttributes(MESSAGE_LOGGING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("MessageLoggingFormat");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMessageLoggingFormat();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MESSAGE_LOGGING_FORMAT, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MessageLoggingParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

