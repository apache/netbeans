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
 *	This generated bean class FieldValue matches the schema element field-value
 *
 *	Generated on Thu Sep 25 15:18:26 PDT 2003
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class FieldValue extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String DEFAULT_FIELD_VALUE = "DefaultFieldValue";	// NOI18N
	static public final String OPTION_VALUE_PAIR = "OptionValuePair";	// NOI18N

	public FieldValue() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public FieldValue(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("default-field-value", 	// NOI18N
			DEFAULT_FIELD_VALUE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("option-value-pair", 	// NOI18N
			OPTION_VALUE_PAIR, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			OptionValuePair.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
	
	}

	// This attribute is mandatory
	public void setDefaultFieldValue(String value) {
		this.setValue(DEFAULT_FIELD_VALUE, value);
	}

	//
	public String getDefaultFieldValue() {
		return (String)this.getValue(DEFAULT_FIELD_VALUE);
	}

	// This attribute is an array, possibly empty
	public void setOptionValuePair(int index, OptionValuePair value) {
		this.setValue(OPTION_VALUE_PAIR, index, value);
	}

	//
	public OptionValuePair getOptionValuePair(int index) {
		return (OptionValuePair)this.getValue(OPTION_VALUE_PAIR, index);
	}

	// This attribute is an array, possibly empty
	public void setOptionValuePair(OptionValuePair[] value) {
		this.setValue(OPTION_VALUE_PAIR, value);
	}

	//
	public OptionValuePair[] getOptionValuePair() {
		return (OptionValuePair[])this.getValues(OPTION_VALUE_PAIR);
	}

	// Return the number of properties
	public int sizeOptionValuePair() {
		return this.size(OPTION_VALUE_PAIR);
	}

	// Add a new element returning its index in the list
	public int addOptionValuePair(org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair value) {
		return this.addValue(OPTION_VALUE_PAIR, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeOptionValuePair(org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair value) {
		return this.removeValue(OPTION_VALUE_PAIR, value);
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
		// Validating property defaultFieldValue
		if (getDefaultFieldValue() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getDefaultFieldValue() == null", "defaultFieldValue", this);	// NOI18N
		}
		// Validating property optionValuePair
		for (int _index = 0; _index < sizeOptionValuePair(); ++_index) {
			org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair element = getOptionValuePair(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("DefaultFieldValue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getDefaultFieldValue();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DEFAULT_FIELD_VALUE, 0, str, indent);

		str.append(indent);
		str.append("OptionValuePair["+this.sizeOptionValuePair()+"]");	// NOI18N
		for(int i=0; i<this.sizeOptionValuePair(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getOptionValuePair(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(OPTION_VALUE_PAIR, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("FieldValue\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!ELEMENT wizard (name, field-group+)>
<!ELEMENT field-group (name, field+)>
<!ELEMENT field (name, field-value, tag?)>
<!ATTLIST field  field-type                 CDATA     "string"
                 required                   CDATA     "true">
<!ELEMENT field-value (default-field-value, option-value-pair*)>
<!ELEMENT option-value-pair (option-name, conditional-value)>
<!ELEMENT name (#PCDATA)>
<!ELEMENT default-field-value (#PCDATA)>
<!ELEMENT option-name (#PCDATA)>
<!ELEMENT conditional-value (#PCDATA)>
<!ELEMENT tag (tag-item*)>
<!ELEMENT tag-item (#PCDATA)>



*/
