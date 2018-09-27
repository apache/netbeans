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
 *	This generated bean class LibraryRefType matches the schema element 'library-refType'.
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

public class LibraryRefType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String LIBRARY_NAME = "LibraryName";	// NOI18N
	static public final String SPECIFICATION_VERSION = "SpecificationVersion";	// NOI18N
	static public final String IMPLEMENTATION_VERSION = "ImplementationVersion";	// NOI18N
	static public final String EXACT_MATCH = "ExactMatch";	// NOI18N
	static public final String CONTEXT_ROOT = "ContextRoot";	// NOI18N

	public LibraryRefType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public LibraryRefType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("library-name", 	// NOI18N
			LIBRARY_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("specification-version", 	// NOI18N
			SPECIFICATION_VERSION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("implementation-version", 	// NOI18N
			IMPLEMENTATION_VERSION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("exact-match", 	// NOI18N
			EXACT_MATCH, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("context-root", 	// NOI18N
			CONTEXT_ROOT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setLibraryName(java.lang.String value) {
		this.setValue(LIBRARY_NAME, value);
	}

	//
	public java.lang.String getLibraryName() {
		return (java.lang.String)this.getValue(LIBRARY_NAME);
	}

	// This attribute is optional
	public void setSpecificationVersion(java.lang.String value) {
		this.setValue(SPECIFICATION_VERSION, value);
	}

	//
	public java.lang.String getSpecificationVersion() {
		return (java.lang.String)this.getValue(SPECIFICATION_VERSION);
	}

	// This attribute is optional
	public void setImplementationVersion(java.lang.String value) {
		this.setValue(IMPLEMENTATION_VERSION, value);
	}

	//
	public java.lang.String getImplementationVersion() {
		return (java.lang.String)this.getValue(IMPLEMENTATION_VERSION);
	}

	// This attribute is optional
	public void setExactMatch(boolean value) {
		this.setValue(EXACT_MATCH, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isExactMatch() {
		Boolean ret = (Boolean)this.getValue(EXACT_MATCH);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setContextRoot(java.lang.String value) {
		this.setValue(CONTEXT_ROOT, value);
	}

	//
	public java.lang.String getContextRoot() {
		return (java.lang.String)this.getValue(CONTEXT_ROOT);
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
		// Validating property libraryName
		if (getLibraryName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getLibraryName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "libraryName", this);	// NOI18N
		}
		// Validating property specificationVersion
		// Validating property implementationVersion
		// Validating property exactMatch
		// Validating property contextRoot
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("LibraryName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLibraryName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LIBRARY_NAME, 0, str, indent);

		str.append(indent);
		str.append("SpecificationVersion");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSpecificationVersion();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SPECIFICATION_VERSION, 0, str, indent);

		str.append(indent);
		str.append("ImplementationVersion");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getImplementationVersion();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(IMPLEMENTATION_VERSION, 0, str, indent);

		str.append(indent);
		str.append("ExactMatch");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isExactMatch()?"true":"false"));
		this.dumpAttributes(EXACT_MATCH, 0, str, indent);

		str.append(indent);
		str.append("ContextRoot");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getContextRoot();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONTEXT_ROOT, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("LibraryRefType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

