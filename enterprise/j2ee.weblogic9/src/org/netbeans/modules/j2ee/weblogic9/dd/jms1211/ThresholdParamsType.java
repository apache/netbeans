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
 *	This generated bean class ThresholdParamsType matches the schema element 'threshold-params-type'.
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

public class ThresholdParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String BYTES_HIGH = "BytesHigh";	// NOI18N
	static public final String BYTES_LOW = "BytesLow";	// NOI18N
	static public final String MESSAGES_HIGH = "MessagesHigh";	// NOI18N
	static public final String MESSAGES_LOW = "MessagesLow";	// NOI18N

	public ThresholdParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ThresholdParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("bytes-high", 	// NOI18N
			BYTES_HIGH, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("bytes-low", 	// NOI18N
			BYTES_LOW, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("messages-high", 	// NOI18N
			MESSAGES_HIGH, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("messages-low", 	// NOI18N
			MESSAGES_LOW, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setBytesHigh(long value) {
		this.setValue(BYTES_HIGH, java.lang.Long.valueOf(value));
	}

	//
	public long getBytesHigh() {
		Long ret = (Long)this.getValue(BYTES_HIGH);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"BYTES_HIGH", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setBytesLow(long value) {
		this.setValue(BYTES_LOW, java.lang.Long.valueOf(value));
	}

	//
	public long getBytesLow() {
		Long ret = (Long)this.getValue(BYTES_LOW);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"BYTES_LOW", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMessagesHigh(long value) {
		this.setValue(MESSAGES_HIGH, java.lang.Long.valueOf(value));
	}

	//
	public long getMessagesHigh() {
		Long ret = (Long)this.getValue(MESSAGES_HIGH);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MESSAGES_HIGH", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMessagesLow(long value) {
		this.setValue(MESSAGES_LOW, java.lang.Long.valueOf(value));
	}

	//
	public long getMessagesLow() {
		Long ret = (Long)this.getValue(MESSAGES_LOW);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MESSAGES_LOW", "long"}));
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
		// Validating property bytesHigh
		// Validating property bytesLow
		// Validating property messagesHigh
		// Validating property messagesLow
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(BYTES_HIGH) != null) {
			str.append(indent);
			str.append("BytesHigh");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getBytesHigh());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(BYTES_HIGH, 0, str, indent);
		}

		if (this.getValue(BYTES_LOW) != null) {
			str.append(indent);
			str.append("BytesLow");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getBytesLow());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(BYTES_LOW, 0, str, indent);
		}

		if (this.getValue(MESSAGES_HIGH) != null) {
			str.append(indent);
			str.append("MessagesHigh");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMessagesHigh());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MESSAGES_HIGH, 0, str, indent);
		}

		if (this.getValue(MESSAGES_LOW) != null) {
			str.append(indent);
			str.append("MessagesLow");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMessagesLow());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MESSAGES_LOW, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ThresholdParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

