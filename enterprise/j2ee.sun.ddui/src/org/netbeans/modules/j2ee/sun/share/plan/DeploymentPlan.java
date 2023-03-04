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
 *	This generated bean class DeploymentPlan matches the schema element 'deployment-plan'.
 *
 *	Generated on Wed Sep 29 16:29:52 PDT 2004
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	deploymentPlan <deployment-plan> : DeploymentPlan
 *		fileEntry <file-entry> : FileEntry[0,n]
 *			name <name> : String
 *			content <content> : String
 *			uri <uri> : String[0,1]
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.plan;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class DeploymentPlan extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);

	static public final String FILE_ENTRY = "FileEntry";	// NOI18N

	public DeploymentPlan() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public DeploymentPlan(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("deployment-plan");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "deployment-plan"));
		}
		Node n = GraphManager.getElementNode("deployment-plan", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "deployment-plan", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public DeploymentPlan(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("deployment-plan", "DeploymentPlan",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, DeploymentPlan.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("file-entry", 	// NOI18N
			FILE_ENTRY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FileEntry.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setFileEntry(int index, FileEntry value) {
		this.setValue(FILE_ENTRY, index, value);
	}

	//
	public FileEntry getFileEntry(int index) {
		return (FileEntry)this.getValue(FILE_ENTRY, index);
	}

	// Return the number of properties
	public int sizeFileEntry() {
		return this.size(FILE_ENTRY);
	}

	// This attribute is an array, possibly empty
	public void setFileEntry(FileEntry[] value) {
		this.setValue(FILE_ENTRY, value);
	}

	//
	public FileEntry[] getFileEntry() {
		return (FileEntry[])this.getValues(FILE_ENTRY);
	}

	// Add a new element returning its index in the list
	public int addFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry value) throws java.beans.PropertyVetoException {
		int positionOfNewItem = this.addValue(FILE_ENTRY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry value) throws java.beans.PropertyVetoException {
		return this.removeValue(FILE_ENTRY, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public FileEntry newFileEntry() {
		return new FileEntry();
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
	public void addVetoableChangeListener(String n, java.beans.VetoableChangeListener l) {
		BeanProp p = this.beanProp(n);
		if (p != null)
			p.addVCListener(l);
	}

	//
	public void removeVetoableChangeListener(String n, java.beans.VetoableChangeListener l) {
		BeanProp p = this.beanProp(n);
		if (p != null)
			p.removeVCListener(l);
	}

	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static DeploymentPlan createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new DeploymentPlan(doc, Common.NO_DEFAULT_VALUES);
	}

	public static DeploymentPlan createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static DeploymentPlan createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static DeploymentPlan createGraph() {
		try {
			return new DeploymentPlan();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
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
			init(comparators, runtimeVersion);
			String strDocument = in.readUTF();
			// System.out.println("strDocument='"+strDocument+"'");
			ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}

	public void _setSchemaLocation(String location) {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
		}
		setAttributeValue("xsi:schemaLocation", location);
	}

	public String _getSchemaLocation() {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
		}
		return getAttributeValue("xsi:schemaLocation");
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("FileEntry["+this.sizeFileEntry()+"]");	// NOI18N
		for(int i=0; i<this.sizeFileEntry(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getFileEntry(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(FILE_ENTRY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DeploymentPlan\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : deployment-plan.dtd
    Created on : April 7, 2003, 2:33 PM
    Author     : vkraemer
    Description:
        Purpose of the document follows.
-->

<!ELEMENT deployment-plan (file-entry*) >

<!ELEMENT file-entry (name, content, uri?) >

<!ELEMENT name (#PCDATA) >
<!ELEMENT content (#PCDATA) >
<!ELEMENT uri (#PCDATA) >

*/

