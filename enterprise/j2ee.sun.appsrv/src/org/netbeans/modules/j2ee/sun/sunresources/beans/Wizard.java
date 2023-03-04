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
 *	This generated bean class Wizard matches the schema element wizard
 *
 *	Generated on Thu Sep 25 15:18:26 PDT 2003
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	wizard : Wizard
 *		name : String
 *		field-group : FieldGroup[1,n]
 *			name : String
 *			field : Field[1,n]
 *				[attr: field-type CDATA string]
 *				[attr: required CDATA true]
 *				name : String
 *				field-value : FieldValue
 *					default-field-value : String
 *					option-value-pair : OptionValuePair[0,n]
 *						option-name : String
 *						conditional-value : String
 *				tag : Tag?
 *					tag-item : String[0,n]
 *
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class Wizard extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String NAME = "Name";	// NOI18N
	static public final String FIELD_GROUP = "FieldGroup";	// NOI18N

	public Wizard() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public Wizard(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("wizard");	// NOI18N
			if (doc == null){
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "wizard"));
                        }
		}
		Node n = GraphManager.getElementNode("wizard", doc);	// NOI18N
		if (n == null){
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "wizard", doc.getFirstChild().getNodeName()));
                }

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public Wizard(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("wizard", "Wizard",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, Wizard.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("field-group", 	// NOI18N
			FIELD_GROUP, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FieldGroup.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
	
	}

	// This attribute is mandatory
	public void setName(String value) {
		this.setValue(NAME, value);
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is an array containing at least one element
	public void setFieldGroup(int index, FieldGroup value) {
		this.setValue(FIELD_GROUP, index, value);
	}

	//
	public FieldGroup getFieldGroup(int index) {
		return (FieldGroup)this.getValue(FIELD_GROUP, index);
	}

	// This attribute is an array containing at least one element
	public void setFieldGroup(FieldGroup[] value) {
		this.setValue(FIELD_GROUP, value);
	}

	//
	public FieldGroup[] getFieldGroup() {
		return (FieldGroup[])this.getValues(FIELD_GROUP);
	}

	// Return the number of properties
	public int sizeFieldGroup() {
		return this.size(FIELD_GROUP);
	}

	// Add a new element returning its index in the list
	public int addFieldGroup(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup value) {
		return this.addValue(FIELD_GROUP, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeFieldGroup(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup value) {
		return this.removeValue(FIELD_GROUP, value);
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
	public static Wizard createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new Wizard(doc, Common.NO_DEFAULT_VALUES);
	}

	public static Wizard createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static Wizard createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static Wizard createGraph() {
		try {
			return new Wizard();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", "name", this);	// NOI18N
		}
		// Validating property fieldGroup
		if (sizeFieldGroup() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeFieldGroup() == 0", "fieldGroup", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeFieldGroup(); ++_index) {
			org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup element = getFieldGroup(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		String str = baos.toString();
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
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("FieldGroup["+this.sizeFieldGroup()+"]");	// NOI18N
		for(int i=0; i<this.sizeFieldGroup(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getFieldGroup(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(FIELD_GROUP, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Wizard\n");	// NOI18N
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
