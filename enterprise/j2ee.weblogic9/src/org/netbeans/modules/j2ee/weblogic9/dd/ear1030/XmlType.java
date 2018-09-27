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
 *	This generated bean class XmlType matches the schema element 'xmlType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:43 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class XmlType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String PARSER_FACTORY = "ParserFactory";	// NOI18N
	static public final String ENTITY_MAPPING = "EntityMapping";	// NOI18N

	public XmlType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public XmlType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("parser-factory", 	// NOI18N
			PARSER_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ParserFactoryType.class);
		this.createProperty("entity-mapping", 	// NOI18N
			ENTITY_MAPPING, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EntityMappingType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setParserFactory(ParserFactoryType value) {
		this.setValue(PARSER_FACTORY, value);
	}

	//
	public ParserFactoryType getParserFactory() {
		return (ParserFactoryType)this.getValue(PARSER_FACTORY);
	}

	// This attribute is an array, possibly empty
	public void setEntityMapping(int index, EntityMappingType value) {
		this.setValue(ENTITY_MAPPING, index, value);
	}

	//
	public EntityMappingType getEntityMapping(int index) {
		return (EntityMappingType)this.getValue(ENTITY_MAPPING, index);
	}

	// Return the number of properties
	public int sizeEntityMapping() {
		return this.size(ENTITY_MAPPING);
	}

	// This attribute is an array, possibly empty
	public void setEntityMapping(EntityMappingType[] value) {
		this.setValue(ENTITY_MAPPING, value);
	}

	//
	public EntityMappingType[] getEntityMapping() {
		return (EntityMappingType[])this.getValues(ENTITY_MAPPING);
	}

	// Add a new element returning its index in the list
	public int addEntityMapping(org.netbeans.modules.j2ee.weblogic9.dd.ear1030.EntityMappingType value) {
		int positionOfNewItem = this.addValue(ENTITY_MAPPING, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEntityMapping(org.netbeans.modules.j2ee.weblogic9.dd.ear1030.EntityMappingType value) {
		return this.removeValue(ENTITY_MAPPING, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ParserFactoryType newParserFactoryType() {
		return new ParserFactoryType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EntityMappingType newEntityMappingType() {
		return new EntityMappingType();
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
		// Validating property parserFactory
		if (getParserFactory() != null) {
			getParserFactory().validate();
		}
		// Validating property entityMapping
		for (int _index = 0; _index < sizeEntityMapping(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1030.EntityMappingType element = getEntityMapping(_index);
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
		str.append("ParserFactory");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getParserFactory();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PARSER_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("EntityMapping["+this.sizeEntityMapping()+"]");	// NOI18N
		for(int i=0; i<this.sizeEntityMapping(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getEntityMapping(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ENTITY_MAPPING, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("XmlType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

