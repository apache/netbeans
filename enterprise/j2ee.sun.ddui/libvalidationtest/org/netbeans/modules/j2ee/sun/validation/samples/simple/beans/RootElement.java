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
 *	This generated bean class RootElement matches the schema element root-element
 *
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	root-element : RootElement
 *		non-zero-length-property : String
 *		number-property : String[1,n]
 *		boolean-property : String[0,n]
 *		range-property : String?
 *		enumeration-property : String
 *		object-one : ObjectOne
 *			property-one : String
 *			property-two : String?
 *		object-two : ObjectTwo?
 *			property-one : String
 *			property-two : String
 *		object-three : ObjectThree?
 *			property-one : String
 *			property-two : String?
 *			property-three : String[1,n]
 *			property-four : String[0,n]
 *		object-four : ObjectFour
 *			property-one : String?
 *			property-two : String?
 *		object-five : ObjectFive[0,n]
 *			property-one : String
 *			property-two : String?
 *		object-six : ObjectSix[1,n]
 *			property-one : String?
 *			property-two : String?
 *		object-seven : ObjectSeven[0,n]
 *			property-one : String
 *			property-two : String
 *		object-eight : ObjectEight[0,n]
 *			property-one : String
 *			property-two : String?
 *			child-object-one : ChildObjectOne
 *				property-one : String
 *				property-two : String?
 *			child-object-two : ChildObjectTwo?
 *				property-one : String
 *				property-two : String
 *			child-object-three : ChildObjectThree[0,n]
 *				property-one : String?
 *				property-two : String?
 *			child-object-four : ChildObjectFour[1,n]
 *				property-one : String
 *				property-two : String?
 *				property-three : String[1,n]
 *				property-four : String[0,n]
 *
 */

package org.netbeans.modules.j2ee.sun.validation.samples.simple.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class RootElement extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String NON_ZERO_LENGTH_PROPERTY = "NonZeroLengthProperty";	// NOI18N
	static public final String NUMBER_PROPERTY = "NumberProperty";	// NOI18N
	static public final String BOOLEAN_PROPERTY = "BooleanProperty";	// NOI18N
	static public final String RANGE_PROPERTY = "RangeProperty";	// NOI18N
	static public final String ENUMERATION_PROPERTY = "EnumerationProperty";	// NOI18N
	static public final String OBJECT_ONE = "ObjectOne";	// NOI18N
	static public final String OBJECT_TWO = "ObjectTwo";	// NOI18N
	static public final String OBJECT_THREE = "ObjectThree";	// NOI18N
	static public final String OBJECT_FOUR = "ObjectFour";	// NOI18N
	static public final String OBJECT_FIVE = "ObjectFive";	// NOI18N
	static public final String OBJECT_SIX = "ObjectSix";	// NOI18N
	static public final String OBJECT_SEVEN = "ObjectSeven";	// NOI18N
	static public final String OBJECT_EIGHT = "ObjectEight";	// NOI18N

	public RootElement() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public RootElement(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("root-element");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "root-element"));
		}
		Node n = GraphManager.getElementNode("root-element", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "root-element", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public RootElement(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("root-element", "RootElement",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, RootElement.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("non-zero-length-property", 	// NOI18N
			NON_ZERO_LENGTH_PROPERTY, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("number-property", 	// NOI18N
			NUMBER_PROPERTY, 
			Common.TYPE_1_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("boolean-property", 	// NOI18N
			BOOLEAN_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("range-property", 	// NOI18N
			RANGE_PROPERTY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("enumeration-property", 	// NOI18N
			ENUMERATION_PROPERTY, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("object-one", 	// NOI18N
			OBJECT_ONE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectOne.class);
		this.createProperty("object-two", 	// NOI18N
			OBJECT_TWO, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectTwo.class);
		this.createProperty("object-three", 	// NOI18N
			OBJECT_THREE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectThree.class);
		this.createProperty("object-four", 	// NOI18N
			OBJECT_FOUR, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectFour.class);
		this.createProperty("object-five", 	// NOI18N
			OBJECT_FIVE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectFive.class);
		this.createProperty("object-six", 	// NOI18N
			OBJECT_SIX, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectSix.class);
		this.createProperty("object-seven", 	// NOI18N
			OBJECT_SEVEN, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectSeven.class);
		this.createProperty("object-eight", 	// NOI18N
			OBJECT_EIGHT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ObjectEight.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
					
	}

	// This attribute is mandatory
	public void setNonZeroLengthProperty(String value) {
		this.setValue(NON_ZERO_LENGTH_PROPERTY, value);
	}

	//
	public String getNonZeroLengthProperty() {
		return (String)this.getValue(NON_ZERO_LENGTH_PROPERTY);
	}

	// This attribute is an array containing at least one element
	public void setNumberProperty(int index, String value) {
		this.setValue(NUMBER_PROPERTY, index, value);
	}

	//
	public String getNumberProperty(int index) {
		return (String)this.getValue(NUMBER_PROPERTY, index);
	}

	// This attribute is an array containing at least one element
	public void setNumberProperty(String[] value) {
		this.setValue(NUMBER_PROPERTY, value);
	}

	//
	public String[] getNumberProperty() {
		return (String[])this.getValues(NUMBER_PROPERTY);
	}

	// Return the number of properties
	public int sizeNumberProperty() {
		return this.size(NUMBER_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addNumberProperty(String value) {
		return this.addValue(NUMBER_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeNumberProperty(String value) {
		return this.removeValue(NUMBER_PROPERTY, value);
	}

	// This attribute is an array, possibly empty
	public void setBooleanProperty(int index, String value) {
		this.setValue(BOOLEAN_PROPERTY, index, value);
	}

	//
	public String getBooleanProperty(int index) {
		return (String)this.getValue(BOOLEAN_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setBooleanProperty(String[] value) {
		this.setValue(BOOLEAN_PROPERTY, value);
	}

	//
	public String[] getBooleanProperty() {
		return (String[])this.getValues(BOOLEAN_PROPERTY);
	}

	// Return the number of properties
	public int sizeBooleanProperty() {
		return this.size(BOOLEAN_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addBooleanProperty(String value) {
		return this.addValue(BOOLEAN_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeBooleanProperty(String value) {
		return this.removeValue(BOOLEAN_PROPERTY, value);
	}

	// This attribute is optional
	public void setRangeProperty(String value) {
		this.setValue(RANGE_PROPERTY, value);
	}

	//
	public String getRangeProperty() {
		return (String)this.getValue(RANGE_PROPERTY);
	}

	// This attribute is mandatory
	public void setEnumerationProperty(String value) {
		this.setValue(ENUMERATION_PROPERTY, value);
	}

	//
	public String getEnumerationProperty() {
		return (String)this.getValue(ENUMERATION_PROPERTY);
	}

	// This attribute is mandatory
	public void setObjectOne(ObjectOne value) {
		this.setValue(OBJECT_ONE, value);
	}

	//
	public ObjectOne getObjectOne() {
		return (ObjectOne)this.getValue(OBJECT_ONE);
	}

	// This attribute is optional
	public void setObjectTwo(ObjectTwo value) {
		this.setValue(OBJECT_TWO, value);
	}

	//
	public ObjectTwo getObjectTwo() {
		return (ObjectTwo)this.getValue(OBJECT_TWO);
	}

	// This attribute is optional
	public void setObjectThree(ObjectThree value) {
		this.setValue(OBJECT_THREE, value);
	}

	//
	public ObjectThree getObjectThree() {
		return (ObjectThree)this.getValue(OBJECT_THREE);
	}

	// This attribute is mandatory
	public void setObjectFour(ObjectFour value) {
		this.setValue(OBJECT_FOUR, value);
	}

	//
	public ObjectFour getObjectFour() {
		return (ObjectFour)this.getValue(OBJECT_FOUR);
	}

	// This attribute is an array, possibly empty
	public void setObjectFive(int index, ObjectFive value) {
		this.setValue(OBJECT_FIVE, index, value);
	}

	//
	public ObjectFive getObjectFive(int index) {
		return (ObjectFive)this.getValue(OBJECT_FIVE, index);
	}

	// This attribute is an array, possibly empty
	public void setObjectFive(ObjectFive[] value) {
		this.setValue(OBJECT_FIVE, value);
	}

	//
	public ObjectFive[] getObjectFive() {
		return (ObjectFive[])this.getValues(OBJECT_FIVE);
	}

	// Return the number of properties
	public int sizeObjectFive() {
		return this.size(OBJECT_FIVE);
	}

	// Add a new element returning its index in the list
	public int addObjectFive(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectFive value) {
		return this.addValue(OBJECT_FIVE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeObjectFive(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectFive value) {
		return this.removeValue(OBJECT_FIVE, value);
	}

	// This attribute is an array containing at least one element
	public void setObjectSix(int index, ObjectSix value) {
		this.setValue(OBJECT_SIX, index, value);
	}

	//
	public ObjectSix getObjectSix(int index) {
		return (ObjectSix)this.getValue(OBJECT_SIX, index);
	}

	// This attribute is an array containing at least one element
	public void setObjectSix(ObjectSix[] value) {
		this.setValue(OBJECT_SIX, value);
	}

	//
	public ObjectSix[] getObjectSix() {
		return (ObjectSix[])this.getValues(OBJECT_SIX);
	}

	// Return the number of properties
	public int sizeObjectSix() {
		return this.size(OBJECT_SIX);
	}

	// Add a new element returning its index in the list
	public int addObjectSix(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectSix value) {
		return this.addValue(OBJECT_SIX, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeObjectSix(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectSix value) {
		return this.removeValue(OBJECT_SIX, value);
	}

	// This attribute is an array, possibly empty
	public void setObjectSeven(int index, ObjectSeven value) {
		this.setValue(OBJECT_SEVEN, index, value);
	}

	//
	public ObjectSeven getObjectSeven(int index) {
		return (ObjectSeven)this.getValue(OBJECT_SEVEN, index);
	}

	// This attribute is an array, possibly empty
	public void setObjectSeven(ObjectSeven[] value) {
		this.setValue(OBJECT_SEVEN, value);
	}

	//
	public ObjectSeven[] getObjectSeven() {
		return (ObjectSeven[])this.getValues(OBJECT_SEVEN);
	}

	// Return the number of properties
	public int sizeObjectSeven() {
		return this.size(OBJECT_SEVEN);
	}

	// Add a new element returning its index in the list
	public int addObjectSeven(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectSeven value) {
		return this.addValue(OBJECT_SEVEN, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeObjectSeven(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectSeven value) {
		return this.removeValue(OBJECT_SEVEN, value);
	}

	// This attribute is an array, possibly empty
	public void setObjectEight(int index, ObjectEight value) {
		this.setValue(OBJECT_EIGHT, index, value);
	}

	//
	public ObjectEight getObjectEight(int index) {
		return (ObjectEight)this.getValue(OBJECT_EIGHT, index);
	}

	// This attribute is an array, possibly empty
	public void setObjectEight(ObjectEight[] value) {
		this.setValue(OBJECT_EIGHT, value);
	}

	//
	public ObjectEight[] getObjectEight() {
		return (ObjectEight[])this.getValues(OBJECT_EIGHT);
	}

	// Return the number of properties
	public int sizeObjectEight() {
		return this.size(OBJECT_EIGHT);
	}

	// Add a new element returning its index in the list
	public int addObjectEight(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectEight value) {
		return this.addValue(OBJECT_EIGHT, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeObjectEight(org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectEight value) {
		return this.removeValue(OBJECT_EIGHT, value);
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
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static RootElement createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new RootElement(doc, Common.NO_DEFAULT_VALUES);
	}

	public static RootElement createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static RootElement createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static RootElement createGraph() {
		try {
			return new RootElement();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property nonZeroLengthProperty
		if (getNonZeroLengthProperty() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getNonZeroLengthProperty() == null", "nonZeroLengthProperty", this);	// NOI18N
		}
		// Validating property numberProperty
		if (sizeNumberProperty() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeNumberProperty() == 0", "numberProperty", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeNumberProperty(); ++_index) {
			String element = getNumberProperty(_index);
			if (element != null) {
			}
		}
		// Validating property booleanProperty
		for (int _index = 0; _index < sizeBooleanProperty(); ++_index) {
			String element = getBooleanProperty(_index);
			if (element != null) {
			}
		}
		// Validating property rangeProperty
		if (getRangeProperty() != null) {
		}
		// Validating property enumerationProperty
		if (getEnumerationProperty() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getEnumerationProperty() == null", "enumerationProperty", this);	// NOI18N
		}
		// Validating property objectOne
		if (getObjectOne() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getObjectOne() == null", "objectOne", this);	// NOI18N
		}
		getObjectOne().validate();
		// Validating property objectTwo
		if (getObjectTwo() != null) {
			getObjectTwo().validate();
		}
		// Validating property objectThree
		if (getObjectThree() != null) {
			getObjectThree().validate();
		}
		// Validating property objectFour
		if (getObjectFour() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getObjectFour() == null", "objectFour", this);	// NOI18N
		}
		getObjectFour().validate();
		// Validating property objectFive
		for (int _index = 0; _index < sizeObjectFive(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectFive element = getObjectFive(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property objectSix
		if (sizeObjectSix() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeObjectSix() == 0", "objectSix", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeObjectSix(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectSix element = getObjectSix(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property objectSeven
		for (int _index = 0; _index < sizeObjectSeven(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectSeven element = getObjectSeven(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property objectEight
		for (int _index = 0; _index < sizeObjectEight(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.ObjectEight element = getObjectEight(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		String str = baos.toString();;
		// System.out.println("str='"+str+"'");
		out.writeUTF(str);
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			init(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
			String strDocument = in.readUTF();
			// System.out.println("strDocument='"+strDocument+"'");
			ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("NonZeroLengthProperty");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getNonZeroLengthProperty();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NON_ZERO_LENGTH_PROPERTY, 0, str, indent);

		str.append(indent);
		str.append("NumberProperty["+this.sizeNumberProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeNumberProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getNumberProperty(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(NUMBER_PROPERTY, i, str, indent);
		}

		str.append(indent);
		str.append("BooleanProperty["+this.sizeBooleanProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeBooleanProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getBooleanProperty(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(BOOLEAN_PROPERTY, i, str, indent);
		}

		str.append(indent);
		str.append("RangeProperty");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getRangeProperty();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RANGE_PROPERTY, 0, str, indent);

		str.append(indent);
		str.append("EnumerationProperty");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getEnumerationProperty();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENUMERATION_PROPERTY, 0, str, indent);

		str.append(indent);
		str.append("ObjectOne");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectOne();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(OBJECT_ONE, 0, str, indent);

		str.append(indent);
		str.append("ObjectTwo");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectTwo();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(OBJECT_TWO, 0, str, indent);

		str.append(indent);
		str.append("ObjectThree");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectThree();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(OBJECT_THREE, 0, str, indent);

		str.append(indent);
		str.append("ObjectFour");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectFour();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(OBJECT_FOUR, 0, str, indent);

		str.append(indent);
		str.append("ObjectFive["+this.sizeObjectFive()+"]");	// NOI18N
		for(int i=0; i<this.sizeObjectFive(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectFive(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(OBJECT_FIVE, i, str, indent);
		}

		str.append(indent);
		str.append("ObjectSix["+this.sizeObjectSix()+"]");	// NOI18N
		for(int i=0; i<this.sizeObjectSix(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectSix(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(OBJECT_SIX, i, str, indent);
		}

		str.append(indent);
		str.append("ObjectSeven["+this.sizeObjectSeven()+"]");	// NOI18N
		for(int i=0; i<this.sizeObjectSeven(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectSeven(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(OBJECT_SEVEN, i, str, indent);
		}

		str.append(indent);
		str.append("ObjectEight["+this.sizeObjectEight()+"]");	// NOI18N
		for(int i=0; i<this.sizeObjectEight(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getObjectEight(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(OBJECT_EIGHT, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("RootElement\n");	// NOI18N
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
