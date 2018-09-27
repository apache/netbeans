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
 *	This generated bean class ModuleDescriptorType matches the schema element 'module-descriptorType'.
 *  The root bean class is DeploymentPlan
 *
 *	Generated on Tue Jul 25 03:27:07 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.config.gen;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ModuleDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String EXTERNAL = "External";	// NOI18N
	static public final String ROOT_ELEMENT = "RootElement";	// NOI18N
	static public final String URI = "Uri";	// NOI18N
	static public final String VARIABLE_ASSIGNMENT = "VariableAssignment";	// NOI18N
	static public final String HASH_CODE = "HashCode";	// NOI18N

	public ModuleDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ModuleDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("root-element", 	// NOI18N
			ROOT_ELEMENT, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("uri", 	// NOI18N
			URI, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("variable-assignment", 	// NOI18N
			VARIABLE_ASSIGNMENT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			VariableAssignmentType.class);
		this.createProperty("hash-code", 	// NOI18N
			HASH_CODE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {
		if ((options & Common.USE_DEFAULT_VALUES) == Common.USE_DEFAULT_VALUES) {
			setExternal(false);
		}

	}

	// This attribute is mandatory
	public void setExternal(boolean value) {
		setAttributeValue(EXTERNAL, ""+value);
	}

	//
	public boolean isExternal() {
		return (getAttributeValue(EXTERNAL) == null) ? false : ("true".equalsIgnoreCase(getAttributeValue(EXTERNAL)) || "1".equals(getAttributeValue(EXTERNAL)));
	}

	// This attribute is mandatory
	public void setRootElement(java.lang.String value) {
		this.setValue(ROOT_ELEMENT, value);
	}

	//
	public java.lang.String getRootElement() {
		return (java.lang.String)this.getValue(ROOT_ELEMENT);
	}

	// This attribute is mandatory
	public void setUri(java.lang.String value) {
		this.setValue(URI, value);
	}

	//
	public java.lang.String getUri() {
		return (java.lang.String)this.getValue(URI);
	}

	// This attribute is an array, possibly empty
	public void setVariableAssignment(int index, VariableAssignmentType value) {
		this.setValue(VARIABLE_ASSIGNMENT, index, value);
	}

	//
	public VariableAssignmentType getVariableAssignment(int index) {
		return (VariableAssignmentType)this.getValue(VARIABLE_ASSIGNMENT, index);
	}

	// Return the number of properties
	public int sizeVariableAssignment() {
		return this.size(VARIABLE_ASSIGNMENT);
	}

	// This attribute is an array, possibly empty
	public void setVariableAssignment(VariableAssignmentType[] value) {
		this.setValue(VARIABLE_ASSIGNMENT, value);
	}

	//
	public VariableAssignmentType[] getVariableAssignment() {
		return (VariableAssignmentType[])this.getValues(VARIABLE_ASSIGNMENT);
	}

	// Add a new element returning its index in the list
	public int addVariableAssignment(org.netbeans.modules.j2ee.weblogic9.config.gen.VariableAssignmentType value) {
		int positionOfNewItem = this.addValue(VARIABLE_ASSIGNMENT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeVariableAssignment(org.netbeans.modules.j2ee.weblogic9.config.gen.VariableAssignmentType value) {
		return this.removeValue(VARIABLE_ASSIGNMENT, value);
	}

	// This attribute is optional
	public void setHashCode(java.lang.String value) {
		this.setValue(HASH_CODE, value);
	}

	//
	public java.lang.String getHashCode() {
		return (java.lang.String)this.getValue(HASH_CODE);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public VariableAssignmentType newVariableAssignmentType() {
		return new VariableAssignmentType();
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
		// Validating property external
		// Validating property rootElement
		if (getRootElement() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRootElement() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "rootElement", this);	// NOI18N
		}
		// Validating property uri
		if (getUri() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getUri() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "uri", this);	// NOI18N
		}
		// Validating property variableAssignment
		for (int _index = 0; _index < sizeVariableAssignment(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.config.gen.VariableAssignmentType element = getVariableAssignment(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property hashCode
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("RootElement");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRootElement();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ROOT_ELEMENT, 0, str, indent);

		str.append(indent);
		str.append("Uri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUri();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(URI, 0, str, indent);

		str.append(indent);
		str.append("VariableAssignment["+this.sizeVariableAssignment()+"]");	// NOI18N
		for(int i=0; i<this.sizeVariableAssignment(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getVariableAssignment(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(VARIABLE_ASSIGNMENT, i, str, indent);
		}

		str.append(indent);
		str.append("HashCode");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getHashCode();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(HASH_CODE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ModuleDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

