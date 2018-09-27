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
 *	This generated bean class ModuleOverrideType matches the schema element 'module-overrideType'.
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

public class ModuleOverrideType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String MODULE_NAME = "ModuleName";	// NOI18N
	static public final String MODULE_TYPE = "ModuleType";	// NOI18N
	static public final String MODULE_DESCRIPTOR = "ModuleDescriptor";	// NOI18N

	public ModuleOverrideType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ModuleOverrideType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("module-name", 	// NOI18N
			MODULE_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("module-type", 	// NOI18N
			MODULE_TYPE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("module-descriptor", 	// NOI18N
			MODULE_DESCRIPTOR, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ModuleDescriptorType.class);
		this.createAttribute(MODULE_DESCRIPTOR, "external", "External", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, "false");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setModuleName(java.lang.String value) {
		this.setValue(MODULE_NAME, value);
	}

	//
	public java.lang.String getModuleName() {
		return (java.lang.String)this.getValue(MODULE_NAME);
	}

	// This attribute is mandatory
	public void setModuleType(java.lang.String value) {
		this.setValue(MODULE_TYPE, value);
	}

	//
	public java.lang.String getModuleType() {
		return (java.lang.String)this.getValue(MODULE_TYPE);
	}

	// This attribute is an array, possibly empty
	public void setModuleDescriptor(int index, ModuleDescriptorType value) {
		this.setValue(MODULE_DESCRIPTOR, index, value);
	}

	//
	public ModuleDescriptorType getModuleDescriptor(int index) {
		return (ModuleDescriptorType)this.getValue(MODULE_DESCRIPTOR, index);
	}

	// Return the number of properties
	public int sizeModuleDescriptor() {
		return this.size(MODULE_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setModuleDescriptor(ModuleDescriptorType[] value) {
		this.setValue(MODULE_DESCRIPTOR, value);
	}

	//
	public ModuleDescriptorType[] getModuleDescriptor() {
		return (ModuleDescriptorType[])this.getValues(MODULE_DESCRIPTOR);
	}

	// Add a new element returning its index in the list
	public int addModuleDescriptor(org.netbeans.modules.j2ee.weblogic9.config.gen.ModuleDescriptorType value) {
		int positionOfNewItem = this.addValue(MODULE_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeModuleDescriptor(org.netbeans.modules.j2ee.weblogic9.config.gen.ModuleDescriptorType value) {
		return this.removeValue(MODULE_DESCRIPTOR, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ModuleDescriptorType newModuleDescriptorType() {
		return new ModuleDescriptorType();
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
		// Validating property moduleName
		if (getModuleName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getModuleName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "moduleName", this);	// NOI18N
		}
		// Validating property moduleType
		if (getModuleType() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getModuleType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "moduleType", this);	// NOI18N
		}
		// Validating property moduleDescriptor
		for (int _index = 0; _index < sizeModuleDescriptor(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.config.gen.ModuleDescriptorType element = getModuleDescriptor(_index);
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
		str.append("ModuleName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getModuleName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MODULE_NAME, 0, str, indent);

		str.append(indent);
		str.append("ModuleType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getModuleType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MODULE_TYPE, 0, str, indent);

		str.append(indent);
		str.append("ModuleDescriptor["+this.sizeModuleDescriptor()+"]");	// NOI18N
		for(int i=0; i<this.sizeModuleDescriptor(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getModuleDescriptor(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MODULE_DESCRIPTOR, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ModuleOverrideType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

