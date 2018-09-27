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
 *	This generated bean class ShareableType matches the schema element 'shareableType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:48 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ShareableType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String DIR = "Dir";	// NOI18N
	static public final String INCLUDE = "Include";	// NOI18N
	static public final String EXCLUDE = "Exclude";	// NOI18N

	public ShareableType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ShareableType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("include", 	// NOI18N
			INCLUDE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("exclude", 	// NOI18N
			EXCLUDE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setDir(java.lang.String value) {
		setAttributeValue(DIR, value);
	}

	//
	public java.lang.String getDir() {
		return getAttributeValue(DIR);
	}

	// This attribute is an array, possibly empty
	public void setInclude(int index, java.lang.String value) {
		this.setValue(INCLUDE, index, value);
	}

	//
	public java.lang.String getInclude(int index) {
		return (java.lang.String)this.getValue(INCLUDE, index);
	}

	// Return the number of properties
	public int sizeInclude() {
		return this.size(INCLUDE);
	}

	// This attribute is an array, possibly empty
	public void setInclude(java.lang.String[] value) {
		this.setValue(INCLUDE, value);
	}

	//
	public java.lang.String[] getInclude() {
		return (java.lang.String[])this.getValues(INCLUDE);
	}

	// Add a new element returning its index in the list
	public int addInclude(java.lang.String value) {
		int positionOfNewItem = this.addValue(INCLUDE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeInclude(java.lang.String value) {
		return this.removeValue(INCLUDE, value);
	}

	// This attribute is an array, possibly empty
	public void setExclude(int index, java.lang.String value) {
		this.setValue(EXCLUDE, index, value);
	}

	//
	public java.lang.String getExclude(int index) {
		return (java.lang.String)this.getValue(EXCLUDE, index);
	}

	// Return the number of properties
	public int sizeExclude() {
		return this.size(EXCLUDE);
	}

	// This attribute is an array, possibly empty
	public void setExclude(java.lang.String[] value) {
		this.setValue(EXCLUDE, value);
	}

	//
	public java.lang.String[] getExclude() {
		return (java.lang.String[])this.getValues(EXCLUDE);
	}

	// Add a new element returning its index in the list
	public int addExclude(java.lang.String value) {
		int positionOfNewItem = this.addValue(EXCLUDE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeExclude(java.lang.String value) {
		return this.removeValue(EXCLUDE, value);
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
		// Validating property dir
		// Validating property include
		// Validating property exclude
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Include["+this.sizeInclude()+"]");	// NOI18N
		for(int i=0; i<this.sizeInclude(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getInclude(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(INCLUDE, i, str, indent);
		}

		str.append(indent);
		str.append("Exclude["+this.sizeExclude()+"]");	// NOI18N
		for(int i=0; i<this.sizeExclude(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getExclude(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(EXCLUDE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ShareableType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

