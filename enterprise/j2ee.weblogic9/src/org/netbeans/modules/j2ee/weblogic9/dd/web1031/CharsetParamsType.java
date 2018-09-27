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
 *	This generated bean class CharsetParamsType matches the schema element 'charset-paramsType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:02 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class CharsetParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String INPUT_CHARSET = "InputCharset";	// NOI18N
	static public final String CHARSET_MAPPING = "CharsetMapping";	// NOI18N

	public CharsetParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public CharsetParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("input-charset", 	// NOI18N
			INPUT_CHARSET, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			InputCharsetType.class);
		this.createAttribute(INPUT_CHARSET, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("charset-mapping", 	// NOI18N
			CHARSET_MAPPING, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CharsetMappingType.class);
		this.createAttribute(CHARSET_MAPPING, "id", "Id", 
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

	// This attribute is an array, possibly empty
	public void setInputCharset(int index, InputCharsetType value) {
		this.setValue(INPUT_CHARSET, index, value);
	}

	//
	public InputCharsetType getInputCharset(int index) {
		return (InputCharsetType)this.getValue(INPUT_CHARSET, index);
	}

	// Return the number of properties
	public int sizeInputCharset() {
		return this.size(INPUT_CHARSET);
	}

	// This attribute is an array, possibly empty
	public void setInputCharset(InputCharsetType[] value) {
		this.setValue(INPUT_CHARSET, value);
	}

	//
	public InputCharsetType[] getInputCharset() {
		return (InputCharsetType[])this.getValues(INPUT_CHARSET);
	}

	// Add a new element returning its index in the list
	public int addInputCharset(org.netbeans.modules.j2ee.weblogic9.dd.web1031.InputCharsetType value) {
		int positionOfNewItem = this.addValue(INPUT_CHARSET, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeInputCharset(org.netbeans.modules.j2ee.weblogic9.dd.web1031.InputCharsetType value) {
		return this.removeValue(INPUT_CHARSET, value);
	}

	// This attribute is an array, possibly empty
	public void setCharsetMapping(int index, CharsetMappingType value) {
		this.setValue(CHARSET_MAPPING, index, value);
	}

	//
	public CharsetMappingType getCharsetMapping(int index) {
		return (CharsetMappingType)this.getValue(CHARSET_MAPPING, index);
	}

	// Return the number of properties
	public int sizeCharsetMapping() {
		return this.size(CHARSET_MAPPING);
	}

	// This attribute is an array, possibly empty
	public void setCharsetMapping(CharsetMappingType[] value) {
		this.setValue(CHARSET_MAPPING, value);
	}

	//
	public CharsetMappingType[] getCharsetMapping() {
		return (CharsetMappingType[])this.getValues(CHARSET_MAPPING);
	}

	// Add a new element returning its index in the list
	public int addCharsetMapping(org.netbeans.modules.j2ee.weblogic9.dd.web1031.CharsetMappingType value) {
		int positionOfNewItem = this.addValue(CHARSET_MAPPING, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCharsetMapping(org.netbeans.modules.j2ee.weblogic9.dd.web1031.CharsetMappingType value) {
		return this.removeValue(CHARSET_MAPPING, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public InputCharsetType newInputCharsetType() {
		return new InputCharsetType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public CharsetMappingType newCharsetMappingType() {
		return new CharsetMappingType();
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
		// Validating property inputCharset
		for (int _index = 0; _index < sizeInputCharset(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web1031.InputCharsetType element = getInputCharset(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property charsetMapping
		for (int _index = 0; _index < sizeCharsetMapping(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web1031.CharsetMappingType element = getCharsetMapping(_index);
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
		str.append("InputCharset["+this.sizeInputCharset()+"]");	// NOI18N
		for(int i=0; i<this.sizeInputCharset(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getInputCharset(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(INPUT_CHARSET, i, str, indent);
		}

		str.append(indent);
		str.append("CharsetMapping["+this.sizeCharsetMapping()+"]");	// NOI18N
		for(int i=0; i<this.sizeCharsetMapping(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCharsetMapping(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CHARSET_MAPPING, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("CharsetParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

