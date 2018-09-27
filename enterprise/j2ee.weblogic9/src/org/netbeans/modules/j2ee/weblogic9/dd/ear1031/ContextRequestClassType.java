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
 *	This generated bean class ContextRequestClassType matches the schema element 'context-request-classType'.
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

public class ContextRequestClassType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String NAME = "Name";	// NOI18N
	static public final String NAMEJ2EEID = "NameJ2eeId";	// NOI18N
	static public final String NAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "NameComponentFactoryClassNameJ2eeId2";	// NOI18N
	static public final String CONTEXT_CASE = "ContextCase";	// NOI18N

	public ContextRequestClassType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ContextRequestClassType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("context-case", 	// NOI18N
			CONTEXT_CASE, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ContextCaseType.class);
		this.createAttribute(CONTEXT_CASE, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		setAttributeValue(ID, value);
	}

	//
	public java.lang.String getId() {
		return getAttributeValue(ID);
	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		this.setValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return (java.lang.String)this.getValue(NAME);
	}

	// This attribute is optional
	public void setNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NAME) == 0) {
			setValue(NAME, "");
		}
		setAttributeValue(NAME, "J2eeId", value);
	}

	//
	public java.lang.String getNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NAME) == 0) {
			setValue(NAME, "");
		}
		setAttributeValue(NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(NAME, "ComponentFactoryClassNameJ2eeId2");
		}
	}

	// This attribute is an array containing at least one element
	public void setContextCase(int index, ContextCaseType value) {
		this.setValue(CONTEXT_CASE, index, value);
	}

	//
	public ContextCaseType getContextCase(int index) {
		return (ContextCaseType)this.getValue(CONTEXT_CASE, index);
	}

	// Return the number of properties
	public int sizeContextCase() {
		return this.size(CONTEXT_CASE);
	}

	// This attribute is an array containing at least one element
	public void setContextCase(ContextCaseType[] value) {
		this.setValue(CONTEXT_CASE, value);
	}

	//
	public ContextCaseType[] getContextCase() {
		return (ContextCaseType[])this.getValues(CONTEXT_CASE);
	}

	// Add a new element returning its index in the list
	public int addContextCase(org.netbeans.modules.j2ee.weblogic9.dd.ear1031.ContextCaseType value) {
		int positionOfNewItem = this.addValue(CONTEXT_CASE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeContextCase(org.netbeans.modules.j2ee.weblogic9.dd.ear1031.ContextCaseType value) {
		return this.removeValue(CONTEXT_CASE, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ContextCaseType newContextCaseType() {
		return new ContextCaseType();
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
		// Validating property id
		if (getId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "id", this);	// NOI18N
			}
		}
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property nameJ2eeId
		if (getNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "nameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property nameComponentFactoryClassNameJ2eeId2
		if (getNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "nameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property contextCase
		if (sizeContextCase() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeContextCase() == 0", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "contextCase", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeContextCase(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1031.ContextCaseType element = getContextCase(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("ContextCase["+this.sizeContextCase()+"]");	// NOI18N
		for(int i=0; i<this.sizeContextCase(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getContextCase(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONTEXT_CASE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ContextRequestClassType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

