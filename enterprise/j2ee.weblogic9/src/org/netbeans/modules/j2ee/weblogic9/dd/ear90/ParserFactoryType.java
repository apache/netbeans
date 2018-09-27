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
 *	This generated bean class ParserFactoryType matches the schema element 'parser-factoryType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:50 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ParserFactoryType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String SAXPARSER_FACTORY = "SaxparserFactory";	// NOI18N
	static public final String DOCUMENT_BUILDER_FACTORY = "DocumentBuilderFactory";	// NOI18N
	static public final String TRANSFORMER_FACTORY = "TransformerFactory";	// NOI18N

	public ParserFactoryType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ParserFactoryType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("saxparser-factory", 	// NOI18N
			SAXPARSER_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("document-builder-factory", 	// NOI18N
			DOCUMENT_BUILDER_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("transformer-factory", 	// NOI18N
			TRANSFORMER_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setSaxparserFactory(java.lang.String value) {
		this.setValue(SAXPARSER_FACTORY, value);
	}

	//
	public java.lang.String getSaxparserFactory() {
		return (java.lang.String)this.getValue(SAXPARSER_FACTORY);
	}

	// This attribute is optional
	public void setDocumentBuilderFactory(java.lang.String value) {
		this.setValue(DOCUMENT_BUILDER_FACTORY, value);
	}

	//
	public java.lang.String getDocumentBuilderFactory() {
		return (java.lang.String)this.getValue(DOCUMENT_BUILDER_FACTORY);
	}

	// This attribute is optional
	public void setTransformerFactory(java.lang.String value) {
		this.setValue(TRANSFORMER_FACTORY, value);
	}

	//
	public java.lang.String getTransformerFactory() {
		return (java.lang.String)this.getValue(TRANSFORMER_FACTORY);
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
		// Validating property saxparserFactory
		// Validating property documentBuilderFactory
		// Validating property transformerFactory
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("SaxparserFactory");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSaxparserFactory();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SAXPARSER_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("DocumentBuilderFactory");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDocumentBuilderFactory();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DOCUMENT_BUILDER_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("TransformerFactory");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTransformerFactory();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TRANSFORMER_FACTORY, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ParserFactoryType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

