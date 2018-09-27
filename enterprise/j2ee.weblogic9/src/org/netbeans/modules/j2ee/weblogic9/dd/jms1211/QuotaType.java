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
 *	This generated bean class QuotaType matches the schema element 'quota-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:59 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class QuotaType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1211.NamedEntityType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String ID = "Id";	// NOI18N
	static public final String BYTES_MAXIMUM = "BytesMaximum";	// NOI18N
	static public final String MESSAGES_MAXIMUM = "MessagesMaximum";	// NOI18N
	static public final String POLICY = "Policy";	// NOI18N
	static public final String SHARED = "Shared";	// NOI18N

	public QuotaType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public QuotaType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("id", 	// NOI18N
			ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("bytes-maximum", 	// NOI18N
			BYTES_MAXIMUM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("messages-maximum", 	// NOI18N
			MESSAGES_MAXIMUM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("policy", 	// NOI18N
			POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("shared", 	// NOI18N
			SHARED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		setAttributeValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return getAttributeValue(NAME);
	}

	// This attribute is optional
	public void setNotes(java.lang.String value) {
		this.setValue(NOTES, value);
	}

	//
	public java.lang.String getNotes() {
		return (java.lang.String)this.getValue(NOTES);
	}

	// This attribute is optional
	public void setId(long value) {
		this.setValue(ID, java.lang.Long.valueOf(value));
	}

	//
	public long getId() {
		Long ret = (Long)this.getValue(ID);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"ID", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setBytesMaximum(long value) {
		this.setValue(BYTES_MAXIMUM, java.lang.Long.valueOf(value));
	}

	//
	public long getBytesMaximum() {
		Long ret = (Long)this.getValue(BYTES_MAXIMUM);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"BYTES_MAXIMUM", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMessagesMaximum(long value) {
		this.setValue(MESSAGES_MAXIMUM, java.lang.Long.valueOf(value));
	}

	//
	public long getMessagesMaximum() {
		Long ret = (Long)this.getValue(MESSAGES_MAXIMUM);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MESSAGES_MAXIMUM", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setPolicy(java.lang.String value) {
		this.setValue(POLICY, value);
	}

	//
	public java.lang.String getPolicy() {
		return (java.lang.String)this.getValue(POLICY);
	}

	// This attribute is optional
	public void setShared(boolean value) {
		this.setValue(SHARED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isShared() {
		Boolean ret = (Boolean)this.getValue(SHARED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property notes
		// Validating property id
		// Validating property bytesMaximum
		// Validating property messagesMaximum
		// Validating property policy
		if (getPolicy() != null) {
			final java.lang.String[] enumRestrictionPolicy = {"FIFO", "Preemptive"};
			restrictionFailure = true;
			for (int _index2 = 0; _index2 < enumRestrictionPolicy.length; 
				++_index2) {
				if (enumRestrictionPolicy[_index2].equals(getPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "policy", this);	// NOI18N
			}
		}
		// Validating property shared
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Notes");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNotes();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NOTES, 0, str, indent);

		if (this.getValue(ID) != null) {
			str.append(indent);
			str.append("Id");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getId());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(ID, 0, str, indent);
		}

		if (this.getValue(BYTES_MAXIMUM) != null) {
			str.append(indent);
			str.append("BytesMaximum");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getBytesMaximum());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(BYTES_MAXIMUM, 0, str, indent);
		}

		if (this.getValue(MESSAGES_MAXIMUM) != null) {
			str.append(indent);
			str.append("MessagesMaximum");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMessagesMaximum());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MESSAGES_MAXIMUM, 0, str, indent);
		}

		str.append(indent);
		str.append("Policy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(POLICY, 0, str, indent);

		str.append(indent);
		str.append("Shared");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isShared()?"true":"false"));
		this.dumpAttributes(SHARED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("QuotaType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

