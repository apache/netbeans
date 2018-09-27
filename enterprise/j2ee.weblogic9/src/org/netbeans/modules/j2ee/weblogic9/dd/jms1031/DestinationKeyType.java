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
 *	This generated bean class DestinationKeyType matches the schema element 'destination-key-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:58 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class DestinationKeyType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1031.NamedEntityType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String PROPERTY2 = "Property2";	// NOI18N
	static public final String KEY_TYPE = "KeyType";	// NOI18N
	static public final String SORT_ORDER = "SortOrder";	// NOI18N

	public DestinationKeyType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public DestinationKeyType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("property", 	// NOI18N
			PROPERTY2, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("key-type", 	// NOI18N
			KEY_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("sort-order", 	// NOI18N
			SORT_ORDER, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
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
	public void setProperty2(java.lang.String value) {
		this.setValue(PROPERTY2, value);
	}

	//
	public java.lang.String getProperty2() {
		return (java.lang.String)this.getValue(PROPERTY2);
	}

	// This attribute is optional
	public void setKeyType(java.lang.String value) {
		this.setValue(KEY_TYPE, value);
	}

	//
	public java.lang.String getKeyType() {
		return (java.lang.String)this.getValue(KEY_TYPE);
	}

	// This attribute is optional
	public void setSortOrder(java.lang.String value) {
		this.setValue(SORT_ORDER, value);
	}

	//
	public java.lang.String getSortOrder() {
		return (java.lang.String)this.getValue(SORT_ORDER);
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
		// Validating property property2
		// Validating property keyType
		if (getKeyType() != null) {
			final java.lang.String[] enumRestrictionKeyType = {"Boolean", "Byte", "Short", "Int", "Long", "Float", "Double", "String"};
			restrictionFailure = true;
			for (int _index2 = 0; _index2 < enumRestrictionKeyType.length; 
				++_index2) {
				if (enumRestrictionKeyType[_index2].equals(getKeyType())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getKeyType() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "keyType", this);	// NOI18N
			}
		}
		// Validating property sortOrder
		if (getSortOrder() != null) {
			final java.lang.String[] enumRestrictionSortOrder = {"Ascending", "Descending"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionSortOrder.length; ++_index2) {
				if (enumRestrictionSortOrder[_index2].equals(getSortOrder())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSortOrder() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "sortOrder", this);	// NOI18N
			}
		}
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

		str.append(indent);
		str.append("Property2");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getProperty2();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PROPERTY2, 0, str, indent);

		str.append(indent);
		str.append("KeyType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getKeyType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(KEY_TYPE, 0, str, indent);

		str.append(indent);
		str.append("SortOrder");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSortOrder();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SORT_ORDER, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DestinationKeyType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

