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
 *	This generated bean class DeploymentPlan matches the schema element 'deployment-plan'.
 *
 *	Generated on Tue Jul 25 03:27:07 PDT 2017
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the following bean graph:
 *
 *	deploymentPlan <deployment-plan> : DeploymentPlan
 *		[attr: global-variables CDATA #IMPLIED false : boolean]
 *		description <description> : java.lang.String[0,1]
 *		applicationName <application-name> : java.lang.String
 *		version <version> : java.lang.String[0,1]
 *		variableDefinition <variable-definition> : VariableDefinitionType[0,1]
 *			variable <variable> : VariableType[0,n]
 *				name <name> : java.lang.String
 *				value <value> : java.lang.String[0,1]
 *				description <description> : java.lang.String[0,1]
 *		moduleOverride <module-override> : ModuleOverrideType[0,n]
 *			moduleName <module-name> : java.lang.String
 *			moduleType <module-type> : java.lang.String
 *			moduleDescriptor <module-descriptor> : ModuleDescriptorType[0,n]
 *				[attr: external CDATA #IMPLIED false : boolean]
 *				rootElement <root-element> : java.lang.String
 *				uri <uri> : java.lang.String
 *				variableAssignment <variable-assignment> : VariableAssignmentType[0,n]
 *					description <description> : java.lang.String[0,1]
 *					name <name> : java.lang.String
 *					xpath <xpath> : java.lang.String
 *					operation <operation> : java.lang.String[0,1] 	[enumeration (add), enumeration (remove), enumeration (replace)]
 *				hashCode <hash-code> : java.lang.String[0,1]
 *		configRoot <config-root> : java.lang.String[0,1]
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.config.gen;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class DeploymentPlan extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	private static final String SERIALIZATION_HELPER_CHARSET = "UTF-8";	// NOI18N

	static public final String GLOBALVARIABLES = "GlobalVariables";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String APPLICATION_NAME = "ApplicationName";	// NOI18N
	static public final String VERSION = "Version";	// NOI18N
	static public final String VARIABLE_DEFINITION = "VariableDefinition";	// NOI18N
	static public final String MODULE_OVERRIDE = "ModuleOverride";	// NOI18N
	static public final String CONFIG_ROOT = "ConfigRoot";	// NOI18N

	public DeploymentPlan() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public DeploymentPlan(org.w3c.dom.Node doc, int options) {
		this(Common.NO_DEFAULT_VALUES);
		try {
			initFromNode(doc, options);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
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
		initPropertyTables(6);
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("application-name", 	// NOI18N
			APPLICATION_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("version", 	// NOI18N
			VERSION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("variable-definition", 	// NOI18N
			VARIABLE_DEFINITION, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			VariableDefinitionType.class);
		this.createProperty("module-override", 	// NOI18N
			MODULE_OVERRIDE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ModuleOverrideType.class);
		this.createProperty("config-root", 	// NOI18N
			CONFIG_ROOT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute("global-variables", "GlobalVariables", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, "false");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {
		setDefaultNamespace("http://www.bea.com/ns/weblogic/90");
		if ((options & Common.USE_DEFAULT_VALUES) == Common.USE_DEFAULT_VALUES) {
			setGlobalVariables(false);
		}

	}

	// This attribute is mandatory
	public void setGlobalVariables(boolean value) {
		setAttributeValue(GLOBALVARIABLES, ""+value);
	}

	//
	public boolean isGlobalVariables() {
		return (getAttributeValue(GLOBALVARIABLES) == null) ? false : ("true".equalsIgnoreCase(getAttributeValue(GLOBALVARIABLES)) || "1".equals(getAttributeValue(GLOBALVARIABLES)));
	}

	// This attribute is optional
	public void setDescription(java.lang.String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public java.lang.String getDescription() {
		return (java.lang.String)this.getValue(DESCRIPTION);
	}

	// This attribute is mandatory
	public void setApplicationName(java.lang.String value) {
		this.setValue(APPLICATION_NAME, value);
	}

	//
	public java.lang.String getApplicationName() {
		return (java.lang.String)this.getValue(APPLICATION_NAME);
	}

	// This attribute is optional
	public void setVersion(java.lang.String value) {
		this.setValue(VERSION, value);
	}

	//
	public java.lang.String getVersion() {
		return (java.lang.String)this.getValue(VERSION);
	}

	// This attribute is optional
	public void setVariableDefinition(VariableDefinitionType value) {
		this.setValue(VARIABLE_DEFINITION, value);
	}

	//
	public VariableDefinitionType getVariableDefinition() {
		return (VariableDefinitionType)this.getValue(VARIABLE_DEFINITION);
	}

	// This attribute is an array, possibly empty
	public void setModuleOverride(int index, ModuleOverrideType value) {
		this.setValue(MODULE_OVERRIDE, index, value);
	}

	//
	public ModuleOverrideType getModuleOverride(int index) {
		return (ModuleOverrideType)this.getValue(MODULE_OVERRIDE, index);
	}

	// Return the number of properties
	public int sizeModuleOverride() {
		return this.size(MODULE_OVERRIDE);
	}

	// This attribute is an array, possibly empty
	public void setModuleOverride(ModuleOverrideType[] value) {
		this.setValue(MODULE_OVERRIDE, value);
	}

	//
	public ModuleOverrideType[] getModuleOverride() {
		return (ModuleOverrideType[])this.getValues(MODULE_OVERRIDE);
	}

	// Add a new element returning its index in the list
	public int addModuleOverride(org.netbeans.modules.j2ee.weblogic9.config.gen.ModuleOverrideType value) {
		int positionOfNewItem = this.addValue(MODULE_OVERRIDE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeModuleOverride(org.netbeans.modules.j2ee.weblogic9.config.gen.ModuleOverrideType value) {
		return this.removeValue(MODULE_OVERRIDE, value);
	}

	// This attribute is optional
	public void setConfigRoot(java.lang.String value) {
		this.setValue(CONFIG_ROOT, value);
	}

	//
	public java.lang.String getConfigRoot() {
		return (java.lang.String)this.getValue(CONFIG_ROOT);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public VariableDefinitionType newVariableDefinitionType() {
		return new VariableDefinitionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ModuleOverrideType newModuleOverrideType() {
		return new ModuleOverrideType();
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
	public static DeploymentPlan createGraph(org.w3c.dom.Node doc) {
		return new DeploymentPlan(doc, Common.NO_DEFAULT_VALUES);
	}

	public static DeploymentPlan createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static DeploymentPlan createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static DeploymentPlan createGraph(java.io.InputStream in, boolean validate) {
		try {
			Document doc = GraphManager.createXmlDocument(in, validate);
			return createGraph(doc);
		}
		catch (Exception t) {
			throw new RuntimeException(Common.getMessage(
				"DOMGraphCreateFailed_msg",
				t));
		}
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static DeploymentPlan createGraph() {
		return new DeploymentPlan();
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property globalVariables
		// Validating property description
		// Validating property applicationName
		if (getApplicationName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getApplicationName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "applicationName", this);	// NOI18N
		}
		// Validating property version
		// Validating property variableDefinition
		if (getVariableDefinition() != null) {
			getVariableDefinition().validate();
		}
		// Validating property moduleOverride
		for (int _index = 0; _index < sizeModuleOverride(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.config.gen.ModuleOverrideType element = getModuleOverride(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property configRoot
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		out.defaultWriteObject();
		final int MAX_SIZE = 0XFFFF;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			write(baos, SERIALIZATION_HELPER_CHARSET);
			final byte [] array = baos.toByteArray();
			final int numStrings = array.length / MAX_SIZE;
			final int leftover = array.length % MAX_SIZE;
			out.writeInt(numStrings + (0 == leftover ? 0 : 1));
			out.writeInt(MAX_SIZE);
			int offset = 0;
			for (int i = 0; i < numStrings; i++){
				out.writeUTF(new String(array, offset, MAX_SIZE, SERIALIZATION_HELPER_CHARSET));
				offset += MAX_SIZE;
			}
			if (leftover > 0){
				final int count = array.length - offset;
				out.writeUTF(new String(array, offset, count, SERIALIZATION_HELPER_CHARSET));
			}
		}
		catch (Schema2BeansException ex){
			throw new Schema2BeansRuntimeException(ex);
		}
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			in.defaultReadObject();
			init(comparators, runtimeVersion);
			// init(comparators, new GenBeans.Version(1, 0, 8))
			final int numStrings = in.readInt();
			final int max_size = in.readInt();
			final StringBuilder sb = new StringBuilder(numStrings * max_size);
			for (int i = 0; i < numStrings; i++){
				sb.append(in.readUTF());
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(SERIALIZATION_HELPER_CHARSET));
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e){
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
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("ApplicationName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getApplicationName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(APPLICATION_NAME, 0, str, indent);

		str.append(indent);
		str.append("Version");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getVersion();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(VERSION, 0, str, indent);

		str.append(indent);
		str.append("VariableDefinition");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getVariableDefinition();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(VARIABLE_DEFINITION, 0, str, indent);

		str.append(indent);
		str.append("ModuleOverride["+this.sizeModuleOverride()+"]");	// NOI18N
		for(int i=0; i<this.sizeModuleOverride(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getModuleOverride(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MODULE_OVERRIDE, i, str, indent);
		}

		str.append(indent);
		str.append("ConfigRoot");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConfigRoot();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONFIG_ROOT, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DeploymentPlan\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

