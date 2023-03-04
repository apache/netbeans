/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 *	This generated bean class FileEntry matches the schema element 'file-entry'.
 *  The root bean class is DeploymentPlan
 *
 *	Generated on Wed Sep 29 16:29:52 PDT 2004
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.plan;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class FileEntry extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);

	static public final String NAME = "Name";	// NOI18N
	static public final String CONTENT = "Content";	// NOI18N
	static public final String URI = "Uri";	// NOI18N

	public FileEntry() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public FileEntry(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("name", 	// NOI18N
			NAME, Common.TYPE_VETOABLE |
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("content", 	// NOI18N
			CONTENT, Common.TYPE_VETOABLE |
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("uri", 	// NOI18N
			URI, Common.TYPE_VETOABLE |
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(String value) throws java.beans.PropertyVetoException {
		try
		{
			this.setValue(NAME, value);
		}
		catch(BaseProperty.VetoException ve)
		{
			throw ve.getPropertyVetoException();
		}
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is mandatory
	public void setContent(String value) throws java.beans.PropertyVetoException {
		try
		{
			this.setValue(CONTENT, value);
		}
		catch(BaseProperty.VetoException ve)
		{
			throw ve.getPropertyVetoException();
		}
	}

	//
	public String getContent() {
		return (String)this.getValue(CONTENT);
	}

	// This attribute is optional
	public void setUri(String value) throws java.beans.PropertyVetoException {
		try
		{
			this.setValue(URI, value);
		}
		catch(BaseProperty.VetoException ve)
		{
			throw ve.getPropertyVetoException();
		}
	}

	//
	public String getUri() {
		return (String)this.getValue(URI);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	//
	public void addVetoableChangeListener(String n, java.beans.VetoableChangeListener l) {
		BeanProp p = this.beanProp(n);
		if (p != null)
			p.addVCListener(l);
	}

	//
	public void removeVetoableChangeListener(String n, java.beans.VetoableChangeListener l) {
		BeanProp p = this.beanProp(n);
		if (p != null)
			p.removeVCListener(l);
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
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
		str.append("Content");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getContent();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONTENT, 0, str, indent);

		str.append(indent);
		str.append("Uri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUri();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(URI, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("FileEntry\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : deployment-plan.dtd
    Created on : April 7, 2003, 2:33 PM
    Author     : vkraemer
    Description:
        Purpose of the document follows.
-->

<!ELEMENT deployment-plan (file-entry*) >

<!ELEMENT file-entry (name, content, uri?) >

<!ELEMENT name (#PCDATA) >
<!ELEMENT content (#PCDATA) >
<!ELEMENT uri (#PCDATA) >

*/

