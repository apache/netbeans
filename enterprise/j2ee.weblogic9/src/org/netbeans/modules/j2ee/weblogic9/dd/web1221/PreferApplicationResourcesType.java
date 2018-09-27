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
 *	This generated bean class PreferApplicationResourcesType matches the schema element 'prefer-application-resourcesType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:05 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PreferApplicationResourcesType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String RESOURCE_NAME = "ResourceName";	// NOI18N

	public PreferApplicationResourcesType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PreferApplicationResourcesType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("resource-name", 	// NOI18N
			RESOURCE_NAME, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setResourceName(int index, java.lang.String value) {
		this.setValue(RESOURCE_NAME, index, value);
	}

	//
	public java.lang.String getResourceName(int index) {
		return (java.lang.String)this.getValue(RESOURCE_NAME, index);
	}

	// Return the number of properties
	public int sizeResourceName() {
		return this.size(RESOURCE_NAME);
	}

	// This attribute is an array, possibly empty
	public void setResourceName(java.lang.String[] value) {
		this.setValue(RESOURCE_NAME, value);
	}

	//
	public java.lang.String[] getResourceName() {
		return (java.lang.String[])this.getValues(RESOURCE_NAME);
	}

	// Add a new element returning its index in the list
	public int addResourceName(java.lang.String value) {
		int positionOfNewItem = this.addValue(RESOURCE_NAME, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceName(java.lang.String value) {
		return this.removeValue(RESOURCE_NAME, value);
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
		// Validating property resourceName
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ResourceName["+this.sizeResourceName()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceName(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getResourceName(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(RESOURCE_NAME, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PreferApplicationResourcesType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

