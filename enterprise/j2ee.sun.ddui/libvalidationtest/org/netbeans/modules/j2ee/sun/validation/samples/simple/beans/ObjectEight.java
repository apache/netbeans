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
 *	This generated bean class ObjectEight matches the schema element object-eight
 *
 */

package org.netbeans.modules.j2ee.sun.validation.samples.simple.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ObjectEight extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String PROPERTY_ONE = "PropertyOne";	// NOI18N
	static public final String PROPERTY_TWO = "PropertyTwo";	// NOI18N
	static public final String CHILD_OBJECT_ONE = "ChildObjectOne";	// NOI18N
	static public final String CHILD_OBJECT_TWO = "ChildObjectTwo";	// NOI18N
	static public final String CHILD_OBJECT_THREE = "ChildObjectThree";	// NOI18N
	static public final String CHILD_OBJECT_FOUR = "ChildObjectFour";	// NOI18N

	public ObjectEight() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ObjectEight(int options)
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
		this.createProperty("child-object-one", 	// NOI18N
			CHILD_OBJECT_ONE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ChildObjectOne.class);
		this.createProperty("child-object-two", 	// NOI18N
			CHILD_OBJECT_TWO, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ChildObjectTwo.class);
		this.createProperty("child-object-three", 	// NOI18N
			CHILD_OBJECT_THREE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ChildObjectThree.class);
		this.createProperty("child-object-four", 	// NOI18N
			CHILD_OBJECT_FOUR, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ChildObjectFour.class);
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

	// This attribute is mandatory
	public void setChildObjectOne(ChildObjectOne value) {
		this.setValue(CHILD_OBJECT_ONE, value);
	}

	//
	public ChildObjectOne getChildObjectOne() {
		return (ChildObjectOne)this.getValue(CHILD_OBJECT_ONE);
	}

	// This attribute is optional
	public void setChildObjectTwo(ChildObjectTwo value) {
		this.setValue(CHILD_OBJECT_TWO, value);
	}

	//
	public ChildObjectTwo getChildObjectTwo() {
		return (ChildObjectTwo)this.getValue(CHILD_OBJECT_TWO);
	}

	// This attribute is an array, possibly empty
	public void setChildObjectThree(int index, ChildObjectThree value) {
		this.setValue(CHILD_OBJECT_THREE, index, value);
	}

	//
	public ChildObjectThree getChildObjectThree(int index) {
		return (ChildObjectThree)this.getValue(CHILD_OBJECT_THREE, index);
	}

	// This attribute is an array, possibly empty
	public void setChildObjectThree(ChildObjectThree[] value) {
		this.setValue(CHILD_OBJECT_THREE, value);
	}

	//
	public ChildObjectThree[] getChildObjectThree() {
		return (ChildObjectThree[])this.getValues(CHILD_OBJECT_THREE);
	}

	// Return the number of properties
	public int sizeChildObjectThree() {
		return this.size(CHILD_OBJECT_THREE);
	}

	// Add a new element returning its index in the list
	public int addChildObjectThree(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ChildObjectThree value) {
		return this.addValue(CHILD_OBJECT_THREE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeChildObjectThree(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ChildObjectThree value) {
		return this.removeValue(CHILD_OBJECT_THREE, value);
	}

	// This attribute is an array containing at least one element
	public void setChildObjectFour(int index, ChildObjectFour value) {
		this.setValue(CHILD_OBJECT_FOUR, index, value);
	}

	//
	public ChildObjectFour getChildObjectFour(int index) {
		return (ChildObjectFour)this.getValue(CHILD_OBJECT_FOUR, index);
	}

	// This attribute is an array containing at least one element
	public void setChildObjectFour(ChildObjectFour[] value) {
		this.setValue(CHILD_OBJECT_FOUR, value);
	}

	//
	public ChildObjectFour[] getChildObjectFour() {
		return (ChildObjectFour[])this.getValues(CHILD_OBJECT_FOUR);
	}

	// Return the number of properties
	public int sizeChildObjectFour() {
		return this.size(CHILD_OBJECT_FOUR);
	}

	// Add a new element returning its index in the list
	public int addChildObjectFour(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ChildObjectFour value) {
		return this.addValue(CHILD_OBJECT_FOUR, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeChildObjectFour(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ChildObjectFour value) {
		return this.removeValue(CHILD_OBJECT_FOUR, value);
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
		// Validating property childObjectOne
		if (getChildObjectOne() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getChildObjectOne() == null", "childObjectOne", this);	// NOI18N
		}
		getChildObjectOne().validate();
		// Validating property childObjectTwo
		if (getChildObjectTwo() != null) {
			getChildObjectTwo().validate();
		}
		// Validating property childObjectThree
		for (int _index = 0; _index < sizeChildObjectThree(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ChildObjectThree element = getChildObjectThree(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property childObjectFour
		if (sizeChildObjectFour() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeChildObjectFour() == 0", "childObjectFour", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeChildObjectFour(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ChildObjectFour element = getChildObjectFour(_index);
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
		str.append("ChildObjectOne");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getChildObjectOne();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CHILD_OBJECT_ONE, 0, str, indent);

		str.append(indent);
		str.append("ChildObjectTwo");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getChildObjectTwo();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CHILD_OBJECT_TWO, 0, str, indent);

		str.append(indent);
		str.append("ChildObjectThree["+this.sizeChildObjectThree()+"]");	// NOI18N
		for(int i=0; i<this.sizeChildObjectThree(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getChildObjectThree(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CHILD_OBJECT_THREE, i, str, indent);
		}

		str.append(indent);
		str.append("ChildObjectFour["+this.sizeChildObjectFour()+"]");	// NOI18N
		for(int i=0; i<this.sizeChildObjectFour(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getChildObjectFour(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CHILD_OBJECT_FOUR, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ObjectEight\n");	// NOI18N
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
