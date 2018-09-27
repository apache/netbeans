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
 *	This generated bean class ForeignServerType matches the schema element 'foreign-server-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:58 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ForeignServerType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1031.TargetableType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String SUB_DEPLOYMENT_NAME = "SubDeploymentName";	// NOI18N
	static public final String DEFAULT_TARGETING_ENABLED = "DefaultTargetingEnabled";	// NOI18N
	static public final String FOREIGN_DESTINATION = "ForeignDestination";	// NOI18N
	static public final String FOREIGN_CONNECTION_FACTORY = "ForeignConnectionFactory";	// NOI18N
	static public final String INITIAL_CONTEXT_FACTORY = "InitialContextFactory";	// NOI18N
	static public final String CONNECTION_URL = "ConnectionUrl";	// NOI18N
	static public final String JNDI_PROPERTIES_CREDENTIAL_ENCRYPTED = "JndiPropertiesCredentialEncrypted";	// NOI18N
	static public final String JNDI_PROPERTY = "JndiProperty";	// NOI18N

	public ForeignServerType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ForeignServerType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(9);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("sub-deployment-name", 	// NOI18N
			SUB_DEPLOYMENT_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("default-targeting-enabled", 	// NOI18N
			DEFAULT_TARGETING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("foreign-destination", 	// NOI18N
			FOREIGN_DESTINATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ForeignDestinationType.class);
		this.createAttribute(FOREIGN_DESTINATION, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("foreign-connection-factory", 	// NOI18N
			FOREIGN_CONNECTION_FACTORY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ForeignConnectionFactoryType.class);
		this.createAttribute(FOREIGN_CONNECTION_FACTORY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("initial-context-factory", 	// NOI18N
			INITIAL_CONTEXT_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("connection-url", 	// NOI18N
			CONNECTION_URL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jndi-properties-credential-encrypted", 	// NOI18N
			JNDI_PROPERTIES_CREDENTIAL_ENCRYPTED, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jndi-property", 	// NOI18N
			JNDI_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PropertyType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		setAttributeValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return getAttributeValue(NAME);
	}

	// This attribute is optional
	public void setNotes(java.lang.String value) {
		this.setValue(NOTES, value);
	}

	//
	public java.lang.String getNotes() {
		return (java.lang.String)this.getValue(NOTES);
	}

	// This attribute is optional
	public void setSubDeploymentName(java.lang.String value) {
		this.setValue(SUB_DEPLOYMENT_NAME, value);
	}

	//
	public java.lang.String getSubDeploymentName() {
		return (java.lang.String)this.getValue(SUB_DEPLOYMENT_NAME);
	}

	// This attribute is optional
	public void setDefaultTargetingEnabled(boolean value) {
		this.setValue(DEFAULT_TARGETING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDefaultTargetingEnabled() {
		Boolean ret = (Boolean)this.getValue(DEFAULT_TARGETING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is an array, possibly empty
	public void setForeignDestination(int index, ForeignDestinationType value) {
		this.setValue(FOREIGN_DESTINATION, index, value);
	}

	//
	public ForeignDestinationType getForeignDestination(int index) {
		return (ForeignDestinationType)this.getValue(FOREIGN_DESTINATION, index);
	}

	// Return the number of properties
	public int sizeForeignDestination() {
		return this.size(FOREIGN_DESTINATION);
	}

	// This attribute is an array, possibly empty
	public void setForeignDestination(ForeignDestinationType[] value) {
		this.setValue(FOREIGN_DESTINATION, value);
	}

	//
	public ForeignDestinationType[] getForeignDestination() {
		return (ForeignDestinationType[])this.getValues(FOREIGN_DESTINATION);
	}

	// Add a new element returning its index in the list
	public int addForeignDestination(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignDestinationType value) {
		int positionOfNewItem = this.addValue(FOREIGN_DESTINATION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeForeignDestination(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignDestinationType value) {
		return this.removeValue(FOREIGN_DESTINATION, value);
	}

	// This attribute is an array, possibly empty
	public void setForeignConnectionFactory(int index, ForeignConnectionFactoryType value) {
		this.setValue(FOREIGN_CONNECTION_FACTORY, index, value);
	}

	//
	public ForeignConnectionFactoryType getForeignConnectionFactory(int index) {
		return (ForeignConnectionFactoryType)this.getValue(FOREIGN_CONNECTION_FACTORY, index);
	}

	// Return the number of properties
	public int sizeForeignConnectionFactory() {
		return this.size(FOREIGN_CONNECTION_FACTORY);
	}

	// This attribute is an array, possibly empty
	public void setForeignConnectionFactory(ForeignConnectionFactoryType[] value) {
		this.setValue(FOREIGN_CONNECTION_FACTORY, value);
	}

	//
	public ForeignConnectionFactoryType[] getForeignConnectionFactory() {
		return (ForeignConnectionFactoryType[])this.getValues(FOREIGN_CONNECTION_FACTORY);
	}

	// Add a new element returning its index in the list
	public int addForeignConnectionFactory(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignConnectionFactoryType value) {
		int positionOfNewItem = this.addValue(FOREIGN_CONNECTION_FACTORY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeForeignConnectionFactory(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignConnectionFactoryType value) {
		return this.removeValue(FOREIGN_CONNECTION_FACTORY, value);
	}

	// This attribute is optional
	public void setInitialContextFactory(java.lang.String value) {
		this.setValue(INITIAL_CONTEXT_FACTORY, value);
	}

	//
	public java.lang.String getInitialContextFactory() {
		return (java.lang.String)this.getValue(INITIAL_CONTEXT_FACTORY);
	}

	// This attribute is optional
	public void setConnectionUrl(java.lang.String value) {
		this.setValue(CONNECTION_URL, value);
	}

	//
	public java.lang.String getConnectionUrl() {
		return (java.lang.String)this.getValue(CONNECTION_URL);
	}

	// This attribute is optional
	public void setJndiPropertiesCredentialEncrypted(java.lang.String value) {
		this.setValue(JNDI_PROPERTIES_CREDENTIAL_ENCRYPTED, value);
	}

	//
	public java.lang.String getJndiPropertiesCredentialEncrypted() {
		return (java.lang.String)this.getValue(JNDI_PROPERTIES_CREDENTIAL_ENCRYPTED);
	}

	// This attribute is an array, possibly empty
	public void setJndiProperty(int index, PropertyType value) {
		this.setValue(JNDI_PROPERTY, index, value);
	}

	//
	public PropertyType getJndiProperty(int index) {
		return (PropertyType)this.getValue(JNDI_PROPERTY, index);
	}

	// Return the number of properties
	public int sizeJndiProperty() {
		return this.size(JNDI_PROPERTY);
	}

	// This attribute is an array, possibly empty
	public void setJndiProperty(PropertyType[] value) {
		this.setValue(JNDI_PROPERTY, value);
	}

	//
	public PropertyType[] getJndiProperty() {
		return (PropertyType[])this.getValues(JNDI_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addJndiProperty(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.PropertyType value) {
		int positionOfNewItem = this.addValue(JNDI_PROPERTY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJndiProperty(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.PropertyType value) {
		return this.removeValue(JNDI_PROPERTY, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ForeignDestinationType newForeignDestinationType() {
		return new ForeignDestinationType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ForeignConnectionFactoryType newForeignConnectionFactoryType() {
		return new ForeignConnectionFactoryType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PropertyType newPropertyType() {
		return new PropertyType();
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
		// Validating property notes
		// Validating property subDeploymentName
		// Validating property defaultTargetingEnabled
		// Validating property foreignDestination
		for (int _index = 0; _index < sizeForeignDestination(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignDestinationType element = getForeignDestination(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property foreignConnectionFactory
		for (int _index = 0; _index < sizeForeignConnectionFactory(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignConnectionFactoryType element = getForeignConnectionFactory(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property initialContextFactory
		// Validating property connectionUrl
		// Validating property jndiPropertiesCredentialEncrypted
		// Validating property jndiProperty
		for (int _index = 0; _index < sizeJndiProperty(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.PropertyType element = getJndiProperty(_index);
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
		str.append("Notes");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNotes();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NOTES, 0, str, indent);

		str.append(indent);
		str.append("SubDeploymentName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSubDeploymentName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SUB_DEPLOYMENT_NAME, 0, str, indent);

		str.append(indent);
		str.append("DefaultTargetingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDefaultTargetingEnabled()?"true":"false"));
		this.dumpAttributes(DEFAULT_TARGETING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("ForeignDestination["+this.sizeForeignDestination()+"]");	// NOI18N
		for(int i=0; i<this.sizeForeignDestination(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getForeignDestination(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(FOREIGN_DESTINATION, i, str, indent);
		}

		str.append(indent);
		str.append("ForeignConnectionFactory["+this.sizeForeignConnectionFactory()+"]");	// NOI18N
		for(int i=0; i<this.sizeForeignConnectionFactory(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getForeignConnectionFactory(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(FOREIGN_CONNECTION_FACTORY, i, str, indent);
		}

		str.append(indent);
		str.append("InitialContextFactory");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getInitialContextFactory();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INITIAL_CONTEXT_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("ConnectionUrl");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConnectionUrl();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONNECTION_URL, 0, str, indent);

		str.append(indent);
		str.append("JndiPropertiesCredentialEncrypted");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJndiPropertiesCredentialEncrypted();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JNDI_PROPERTIES_CREDENTIAL_ENCRYPTED, 0, str, indent);

		str.append(indent);
		str.append("JndiProperty["+this.sizeJndiProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeJndiProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJndiProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JNDI_PROPERTY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ForeignServerType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

