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
 *	This generated bean class Constraints matches the schema element constraints
 *
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	constraints : Constraints
 *		check-info : CheckInfo[0,n]
 *			name : String
 *			classname : String
 *			arguments : Arguments?
 *				argument : Argument[1,n]
 *					name : String
 *					type : String?
 *
 */

package org.netbeans.modules.j2ee.sun.validation.constraints.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class Constraints extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String CHECK_INFO = "CheckInfo";	// NOI18N

	public Constraints() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public Constraints(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("constraints");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "constraints"));
		}
		Node n = GraphManager.getElementNode("constraints", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "constraints", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public Constraints(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("constraints", "Constraints",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, Constraints.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("check-info", 	// NOI18N
			CHECK_INFO, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CheckInfo.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is an array, possibly empty
	public void setCheckInfo(int index, CheckInfo value) {
		this.setValue(CHECK_INFO, index, value);
	}

	//
	public CheckInfo getCheckInfo(int index) {
		return (CheckInfo)this.getValue(CHECK_INFO, index);
	}

	// This attribute is an array, possibly empty
	public void setCheckInfo(CheckInfo[] value) {
		this.setValue(CHECK_INFO, value);
	}

	//
	public CheckInfo[] getCheckInfo() {
		return (CheckInfo[])this.getValues(CHECK_INFO);
	}

	// Return the number of properties
	public int sizeCheckInfo() {
		return this.size(CHECK_INFO);
	}

	// Add a new element returning its index in the list
	public int addCheckInfo(org.netbeans.modules.j2ee.sun.validation.constraints.data.CheckInfo value) {
		return this.addValue(CHECK_INFO, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCheckInfo(org.netbeans.modules.j2ee.sun.validation.constraints.data.CheckInfo value) {
		return this.removeValue(CHECK_INFO, value);
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
	public static Constraints createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new Constraints(doc, Common.NO_DEFAULT_VALUES);
	}

	public static Constraints createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static Constraints createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static Constraints createGraph() {
		try {
			return new Constraints();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property checkInfo
		for (int _index = 0; _index < sizeCheckInfo(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.constraints.data.CheckInfo element = getCheckInfo(_index);
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
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("CheckInfo["+this.sizeCheckInfo()+"]");	// NOI18N
		for(int i=0; i<this.sizeCheckInfo(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCheckInfo(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CHECK_INFO, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Constraints\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for for constraints xml.
  constraints.xml is used to specify provide information of the
  Constraints to the Validation framework.
 
  $Revision$
-->


<!--
This is the root element.
-->
<!ELEMENT constraints (check-info*)>


<!--
This represents an information, about a particular Constraint.
Provides information of a Constraint represented by corresponding
<check> element in validation.xml.
Sub element <name> is used to link this element with the
corresponding <check> element in validation.xml.
-->
<!ELEMENT check-info (name, classname, arguments?)>


<!--
This element represents information of a Constraint class arguments.
Number of sub elements, <argument> should match with the number
of <parameter> sub elements, of corresponding <arguments> element
in validation.xml
-->
<!ELEMENT arguments (argument+)>


<!--
This element represents information of a single Constraint class
argument.
Sub elements <name> should match with the <name> sub element of
corresponding <parameter> element in constraints.xml
-->
<!ELEMENT argument (name, type?)>


<!--
Used in two elements <check-info> and <argument>
In <check-info>, it represents a Constraint name and is the linking
element between <check> element in validation.xml and <check-info>
element in constraints.xml.
In <argument>, it represents argument name and is the linking element
between <parameter> element in validation.xml and <argument> element
in constraints.xml.
-->
<!ELEMENT name (#PCDATA)>


<!--
This element represents Constraint class name.
Constraint class should provide the constructor with no arguments.
Constraint class should also provide the set* methods for all the
required arguments.
Constraint class is always created using default constructor and
then the arguments are set using set* methods.
-->
<!ELEMENT classname (#PCDATA)>


<!--
This element represents the type of an argument.
If not specified, it defaults to java.lang.String
-->
<!ELEMENT type (#PCDATA)>

*/
