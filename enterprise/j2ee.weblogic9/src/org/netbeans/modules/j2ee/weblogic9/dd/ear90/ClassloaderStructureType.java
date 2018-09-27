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
 *	This generated bean class ClassloaderStructureType matches the schema element 'classloader-structureType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:49 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ClassloaderStructureType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String MODULE_REF = "ModuleRef";	// NOI18N
	static public final String CLASSLOADER_STRUCTURE = "ClassloaderStructure";	// NOI18N

	public ClassloaderStructureType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ClassloaderStructureType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("module-ref", 	// NOI18N
			MODULE_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ModuleRefType.class);
		this.createProperty("classloader-structure", 	// NOI18N
			CLASSLOADER_STRUCTURE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ClassloaderStructureType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setModuleRef(int index, ModuleRefType value) {
		this.setValue(MODULE_REF, index, value);
	}

	//
	public ModuleRefType getModuleRef(int index) {
		return (ModuleRefType)this.getValue(MODULE_REF, index);
	}

	// Return the number of properties
	public int sizeModuleRef() {
		return this.size(MODULE_REF);
	}

	// This attribute is an array, possibly empty
	public void setModuleRef(ModuleRefType[] value) {
		this.setValue(MODULE_REF, value);
	}

	//
	public ModuleRefType[] getModuleRef() {
		return (ModuleRefType[])this.getValues(MODULE_REF);
	}

	// Add a new element returning its index in the list
	public int addModuleRef(org.netbeans.modules.j2ee.weblogic9.dd.ear90.ModuleRefType value) {
		int positionOfNewItem = this.addValue(MODULE_REF, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeModuleRef(org.netbeans.modules.j2ee.weblogic9.dd.ear90.ModuleRefType value) {
		return this.removeValue(MODULE_REF, value);
	}

	// This attribute is an array, possibly empty
	public void setClassloaderStructure(int index, ClassloaderStructureType value) {
		this.setValue(CLASSLOADER_STRUCTURE, index, value);
	}

	//
	public ClassloaderStructureType getClassloaderStructure(int index) {
		return (ClassloaderStructureType)this.getValue(CLASSLOADER_STRUCTURE, index);
	}

	// Return the number of properties
	public int sizeClassloaderStructure() {
		return this.size(CLASSLOADER_STRUCTURE);
	}

	// This attribute is an array, possibly empty
	public void setClassloaderStructure(ClassloaderStructureType[] value) {
		this.setValue(CLASSLOADER_STRUCTURE, value);
	}

	//
	public ClassloaderStructureType[] getClassloaderStructure() {
		return (ClassloaderStructureType[])this.getValues(CLASSLOADER_STRUCTURE);
	}

	// Add a new element returning its index in the list
	public int addClassloaderStructure(org.netbeans.modules.j2ee.weblogic9.dd.ear90.ClassloaderStructureType value) {
		int positionOfNewItem = this.addValue(CLASSLOADER_STRUCTURE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeClassloaderStructure(org.netbeans.modules.j2ee.weblogic9.dd.ear90.ClassloaderStructureType value) {
		return this.removeValue(CLASSLOADER_STRUCTURE, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ModuleRefType newModuleRefType() {
		return new ModuleRefType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ClassloaderStructureType newClassloaderStructureType() {
		return new ClassloaderStructureType();
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
		// Validating property moduleRef
		for (int _index = 0; _index < sizeModuleRef(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear90.ModuleRefType element = getModuleRef(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property classloaderStructure
		for (int _index = 0; _index < sizeClassloaderStructure(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear90.ClassloaderStructureType element = getClassloaderStructure(_index);
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
		str.append("ModuleRef["+this.sizeModuleRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeModuleRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getModuleRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MODULE_REF, i, str, indent);
		}

		str.append(indent);
		str.append("ClassloaderStructure["+this.sizeClassloaderStructure()+"]");	// NOI18N
		for(int i=0; i<this.sizeClassloaderStructure(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getClassloaderStructure(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CLASSLOADER_STRUCTURE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ClassloaderStructureType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

