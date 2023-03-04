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
 *	This generated bean class ModElement matches the schema element 'mod-element'.
 *  The root bean class is Transform
 *
 *	Generated on Sat Aug 13 01:27:36 GMT-08:00 2005
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.dd.impl.transform;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ModElement extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String NAME = "Name";	// NOI18N
	static public final String MOD_ATTRIBUTE = "ModAttribute";	// NOI18N
	static public final String SUB_ELEMENT = "SubElement";	// NOI18N

	public ModElement() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ModElement(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("mod-attribute", 	// NOI18N
			MOD_ATTRIBUTE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ModAttribute.class);
		this.createProperty("sub-element", 	// NOI18N
			SUB_ELEMENT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SubElement.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(String value) {
		this.setValue(NAME, value);
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is an array, possibly empty
	public void setModAttribute(int index, ModAttribute value) {
		this.setValue(MOD_ATTRIBUTE, index, value);
	}

	//
	public ModAttribute getModAttribute(int index) {
		return (ModAttribute)this.getValue(MOD_ATTRIBUTE, index);
	}

	// Return the number of properties
	public int sizeModAttribute() {
		return this.size(MOD_ATTRIBUTE);
	}

	// This attribute is an array, possibly empty
	public void setModAttribute(ModAttribute[] value) {
		this.setValue(MOD_ATTRIBUTE, value);
	}

	//
	public ModAttribute[] getModAttribute() {
		return (ModAttribute[])this.getValues(MOD_ATTRIBUTE);
	}

	// Add a new element returning its index in the list
	public int addModAttribute(ModAttribute value) {
		int positionOfNewItem = this.addValue(MOD_ATTRIBUTE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeModAttribute(ModAttribute value) {
		return this.removeValue(MOD_ATTRIBUTE, value);
	}

	// This attribute is an array, possibly empty
	public void setSubElement(int index, SubElement value) {
		this.setValue(SUB_ELEMENT, index, value);
	}

	//
	public SubElement getSubElement(int index) {
		return (SubElement)this.getValue(SUB_ELEMENT, index);
	}

	// Return the number of properties
	public int sizeSubElement() {
		return this.size(SUB_ELEMENT);
	}

	// This attribute is an array, possibly empty
	public void setSubElement(SubElement[] value) {
		this.setValue(SUB_ELEMENT, value);
	}

	//
	public SubElement[] getSubElement() {
		return (SubElement[])this.getValues(SUB_ELEMENT);
	}

	// Add a new element returning its index in the list
	public int addSubElement(SubElement value) {
		int positionOfNewItem = this.addValue(SUB_ELEMENT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSubElement(SubElement value) {
		return this.removeValue(SUB_ELEMENT, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ModAttribute newModAttribute() {
		return new ModAttribute();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SubElement newSubElement() {
		return new SubElement();
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
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property modAttribute
		for (int _index = 0; _index < sizeModAttribute(); ++_index) {
			ModAttribute element = getModAttribute(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property subElement
		for (int _index = 0; _index < sizeSubElement(); ++_index) {
			SubElement element = getSubElement(_index);
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
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("ModAttribute["+this.sizeModAttribute()+"]");	// NOI18N
		for(int i=0; i<this.sizeModAttribute(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getModAttribute(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MOD_ATTRIBUTE, i, str, indent);
		}

		str.append(indent);
		str.append("SubElement["+this.sizeSubElement()+"]");	// NOI18N
		for(int i=0; i<this.sizeSubElement(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSubElement(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SUB_ELEMENT, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ModElement\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:


<!--- Put your DTDDoc comment here. -->
<!ELEMENT transform (xmltype)*>

<!--- xmltype : target version of the server xml -->
<!ELEMENT xmltype (name, mod-element*)>

<!--- Put your DTDDoc comment here. -->
<!ELEMENT name (#PCDATA)>

<!--- mod-element : element that contains sub-elements and attributes that need to be removed 
      ie. sub-elements and attributes that were added in a later version of server xml -->
<!ELEMENT mod-element (name, mod-attribute*, sub-element*)>

<!--- sub-element : sub-elements that need to be removed -->
<!ELEMENT sub-element (name)>

<!--- new-attribute : attribute that need to be removed -->
<!ELEMENT mod-attribute (name)>


*/
