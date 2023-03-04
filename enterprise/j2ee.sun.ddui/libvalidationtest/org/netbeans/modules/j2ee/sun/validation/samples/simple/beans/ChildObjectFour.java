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
 *	This generated bean class ChildObjectFour matches the schema element child-object-four
 *
 */

package org.netbeans.modules.j2ee.sun.validation.samples.simple.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ChildObjectFour extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String PROPERTY_ONE = "PropertyOne";	// NOI18N
	static public final String PROPERTY_TWO = "PropertyTwo";	// NOI18N
	static public final String PROPERTY_THREE = "PropertyThree";	// NOI18N
	static public final String PROPERTY_FOUR = "PropertyFour";	// NOI18N

	public ChildObjectFour() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ChildObjectFour(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("property-one", 	// NOI18N
			PROPERTY_ONE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("property-two", 	// NOI18N
			PROPERTY_TWO, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("property-three", 	// NOI18N
			PROPERTY_THREE, 
			Common.TYPE_1_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("property-four", 	// NOI18N
			PROPERTY_FOUR, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
				
	}

	// This attribute is mandatory
	public void setPropertyOne(String value) {
		this.setValue(PROPERTY_ONE, value);
	}

	//
	public String getPropertyOne() {
		return (String)this.getValue(PROPERTY_ONE);
	}

	// This attribute is optional
	public void setPropertyTwo(String value) {
		this.setValue(PROPERTY_TWO, value);
	}

	//
	public String getPropertyTwo() {
		return (String)this.getValue(PROPERTY_TWO);
	}

	// This attribute is an array containing at least one element
	public void setPropertyThree(int index, String value) {
		this.setValue(PROPERTY_THREE, index, value);
	}

	//
	public String getPropertyThree(int index) {
		return (String)this.getValue(PROPERTY_THREE, index);
	}

	// This attribute is an array containing at least one element
	public void setPropertyThree(String[] value) {
		this.setValue(PROPERTY_THREE, value);
	}

	//
	public String[] getPropertyThree() {
		return (String[])this.getValues(PROPERTY_THREE);
	}

	// Return the number of properties
	public int sizePropertyThree() {
		return this.size(PROPERTY_THREE);
	}

	// Add a new element returning its index in the list
	public int addPropertyThree(String value) {
		return this.addValue(PROPERTY_THREE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePropertyThree(String value) {
		return this.removeValue(PROPERTY_THREE, value);
	}

	// This attribute is an array, possibly empty
	public void setPropertyFour(int index, String value) {
		this.setValue(PROPERTY_FOUR, index, value);
	}

	//
	public String getPropertyFour(int index) {
		return (String)this.getValue(PROPERTY_FOUR, index);
	}

	// This attribute is an array, possibly empty
	public void setPropertyFour(String[] value) {
		this.setValue(PROPERTY_FOUR, value);
	}

	//
	public String[] getPropertyFour() {
		return (String[])this.getValues(PROPERTY_FOUR);
	}

	// Return the number of properties
	public int sizePropertyFour() {
		return this.size(PROPERTY_FOUR);
	}

	// Add a new element returning its index in the list
	public int addPropertyFour(String value) {
		return this.addValue(PROPERTY_FOUR, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePropertyFour(String value) {
		return this.removeValue(PROPERTY_FOUR, value);
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
		// Validating property propertyOne
		if (getPropertyOne() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPropertyOne() == null", "propertyOne", this);	// NOI18N
		}
		// Validating property propertyTwo
		if (getPropertyTwo() != null) {
		}
		// Validating property propertyThree
		if (sizePropertyThree() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizePropertyThree() == 0", "propertyThree", this);	// NOI18N
		}
		for (int _index = 0; _index < sizePropertyThree(); ++_index) {
			String element = getPropertyThree(_index);
			if (element != null) {
			}
		}
		// Validating property propertyFour
		for (int _index = 0; _index < sizePropertyFour(); ++_index) {
			String element = getPropertyFour(_index);
			if (element != null) {
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("PropertyOne");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPropertyOne();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PROPERTY_ONE, 0, str, indent);

		str.append(indent);
		str.append("PropertyTwo");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPropertyTwo();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PROPERTY_TWO, 0, str, indent);

		str.append(indent);
		str.append("PropertyThree["+this.sizePropertyThree()+"]");	// NOI18N
		for(int i=0; i<this.sizePropertyThree(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getPropertyThree(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PROPERTY_THREE, i, str, indent);
		}

		str.append(indent);
		str.append("PropertyFour["+this.sizePropertyFour()+"]");	// NOI18N
		for(int i=0; i<this.sizePropertyFour(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getPropertyFour(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PROPERTY_FOUR, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ChildObjectFour\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : sample.dtd
    Created on : May 8, 2003, 11:18 AM
    Author     : Rajeshwar Patil
    Description:
        Purpose of the document follows.
-->

<!--
-->
<!ELEMENT root-element (non-zero-length-property, number-property+,
    boolean-property*, range-property?, enumeration-property,
    object-one, object-two?, object-three?, object-four,
    object-five*, object-six+, object-seven*,object-eight*)>

<!--
-->
<!ELEMENT object-one (property-one, property-two?)>


<!--
-->
<!ELEMENT object-two (property-one, property-two)>


<!--
-->
<!ELEMENT object-three (property-one, property-two?,
    property-three+, property-four*)>


<!--
-->
<!ELEMENT object-four (property-one?, property-two?)>


<!--
-->
<!ELEMENT object-five (property-one, property-two?)>


<!--
-->
<!ELEMENT object-six (property-one?, property-two?)>


<!--
-->
<!ELEMENT object-seven (property-one, property-two)>


<!--
-->
<!ELEMENT object-eight (property-one, property-two?, child-object-one,
    child-object-two?, child-object-three*, child-object-four+)>


<!--
-->
<!ELEMENT child-object-one (property-one, property-two?)>


<!--
-->
<!ELEMENT child-object-two (property-one, property-two)>


<!--
-->
<!ELEMENT child-object-three (property-one?, property-two?)>


<!--
-->
<!ELEMENT child-object-four (property-one, property-two?,
    property-three+, property-four*)>


<!ELEMENT boolean-property (#PCDATA)>
<!ELEMENT number-property (#PCDATA)>
<!ELEMENT non-zero-length-property (#PCDATA)>
<!ELEMENT range-property (#PCDATA)>
<!ELEMENT enumeration-property (#PCDATA)>
<!ELEMENT property-one (#PCDATA)>
<!ELEMENT property-two (#PCDATA)>
<!ELEMENT property-three (#PCDATA)>
<!ELEMENT property-four (#PCDATA)>

*/
