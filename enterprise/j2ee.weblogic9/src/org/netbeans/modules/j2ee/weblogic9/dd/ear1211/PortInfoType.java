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
 *	This generated bean class PortInfoType matches the schema element 'port-infoType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:47 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PortInfoType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String PORT_NAME = "PortName";	// NOI18N
	static public final String PORTNAMEJ2EEID = "PortNameJ2eeId";	// NOI18N
	static public final String PORTNAMEDESTINATIONRESOURCELINKJ2EEID2 = "PortNameDestinationResourceLinkJ2eeId2";	// NOI18N
	static public final String STUB_PROPERTY = "StubProperty";	// NOI18N
	static public final String CALL_PROPERTY = "CallProperty";	// NOI18N
	static public final String WSAT_CONFIG = "WsatConfig";	// NOI18N
	static public final String OWSM_POLICY = "OwsmPolicy";	// NOI18N
	static public final String OPERATION = "Operation";	// NOI18N

	public PortInfoType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PortInfoType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("port-name", 	// NOI18N
			PORT_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PORT_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(PORT_NAME, "j2ee:id", "DestinationResourceLinkJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stub-property", 	// NOI18N
			STUB_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PropertyNamevalueType.class);
		this.createProperty("call-property", 	// NOI18N
			CALL_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PropertyNamevalueType.class);
		this.createProperty("wsat-config", 	// NOI18N
			WSAT_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WsatConfigType.class);
		this.createProperty("owsm-policy", 	// NOI18N
			OWSM_POLICY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			OwsmPolicyType.class);
		this.createProperty("operation", 	// NOI18N
			OPERATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			OperationInfoType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setPortName(java.lang.String value) {
		this.setValue(PORT_NAME, value);
	}

	//
	public java.lang.String getPortName() {
		return (java.lang.String)this.getValue(PORT_NAME);
	}

	// This attribute is optional
	public void setPortNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PORT_NAME) == 0) {
			setValue(PORT_NAME, "");
		}
		setAttributeValue(PORT_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getPortNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PORT_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(PORT_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPortNameDestinationResourceLinkJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PORT_NAME) == 0) {
			setValue(PORT_NAME, "");
		}
		setAttributeValue(PORT_NAME, "DestinationResourceLinkJ2eeId2", value);
	}

	//
	public java.lang.String getPortNameDestinationResourceLinkJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PORT_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(PORT_NAME, "DestinationResourceLinkJ2eeId2");
		}
	}

	// This attribute is an array, possibly empty
	public void setStubProperty(int index, PropertyNamevalueType value) {
		this.setValue(STUB_PROPERTY, index, value);
	}

	//
	public PropertyNamevalueType getStubProperty(int index) {
		return (PropertyNamevalueType)this.getValue(STUB_PROPERTY, index);
	}

	// Return the number of properties
	public int sizeStubProperty() {
		return this.size(STUB_PROPERTY);
	}

	// This attribute is an array, possibly empty
	public void setStubProperty(PropertyNamevalueType[] value) {
		this.setValue(STUB_PROPERTY, value);
	}

	//
	public PropertyNamevalueType[] getStubProperty() {
		return (PropertyNamevalueType[])this.getValues(STUB_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addStubProperty(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.PropertyNamevalueType value) {
		int positionOfNewItem = this.addValue(STUB_PROPERTY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeStubProperty(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.PropertyNamevalueType value) {
		return this.removeValue(STUB_PROPERTY, value);
	}

	// This attribute is an array, possibly empty
	public void setCallProperty(int index, PropertyNamevalueType value) {
		this.setValue(CALL_PROPERTY, index, value);
	}

	//
	public PropertyNamevalueType getCallProperty(int index) {
		return (PropertyNamevalueType)this.getValue(CALL_PROPERTY, index);
	}

	// Return the number of properties
	public int sizeCallProperty() {
		return this.size(CALL_PROPERTY);
	}

	// This attribute is an array, possibly empty
	public void setCallProperty(PropertyNamevalueType[] value) {
		this.setValue(CALL_PROPERTY, value);
	}

	//
	public PropertyNamevalueType[] getCallProperty() {
		return (PropertyNamevalueType[])this.getValues(CALL_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addCallProperty(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.PropertyNamevalueType value) {
		int positionOfNewItem = this.addValue(CALL_PROPERTY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCallProperty(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.PropertyNamevalueType value) {
		return this.removeValue(CALL_PROPERTY, value);
	}

	// This attribute is optional
	public void setWsatConfig(WsatConfigType value) {
		this.setValue(WSAT_CONFIG, value);
	}

	//
	public WsatConfigType getWsatConfig() {
		return (WsatConfigType)this.getValue(WSAT_CONFIG);
	}

	// This attribute is an array, possibly empty
	public void setOwsmPolicy(int index, OwsmPolicyType value) {
		this.setValue(OWSM_POLICY, index, value);
	}

	//
	public OwsmPolicyType getOwsmPolicy(int index) {
		return (OwsmPolicyType)this.getValue(OWSM_POLICY, index);
	}

	// Return the number of properties
	public int sizeOwsmPolicy() {
		return this.size(OWSM_POLICY);
	}

	// This attribute is an array, possibly empty
	public void setOwsmPolicy(OwsmPolicyType[] value) {
		this.setValue(OWSM_POLICY, value);
	}

	//
	public OwsmPolicyType[] getOwsmPolicy() {
		return (OwsmPolicyType[])this.getValues(OWSM_POLICY);
	}

	// Add a new element returning its index in the list
	public int addOwsmPolicy(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.OwsmPolicyType value) {
		int positionOfNewItem = this.addValue(OWSM_POLICY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeOwsmPolicy(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.OwsmPolicyType value) {
		return this.removeValue(OWSM_POLICY, value);
	}

	// This attribute is an array, possibly empty
	public void setOperation(int index, OperationInfoType value) {
		this.setValue(OPERATION, index, value);
	}

	//
	public OperationInfoType getOperation(int index) {
		return (OperationInfoType)this.getValue(OPERATION, index);
	}

	// Return the number of properties
	public int sizeOperation() {
		return this.size(OPERATION);
	}

	// This attribute is an array, possibly empty
	public void setOperation(OperationInfoType[] value) {
		this.setValue(OPERATION, value);
	}

	//
	public OperationInfoType[] getOperation() {
		return (OperationInfoType[])this.getValues(OPERATION);
	}

	// Add a new element returning its index in the list
	public int addOperation(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.OperationInfoType value) {
		int positionOfNewItem = this.addValue(OPERATION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeOperation(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.OperationInfoType value) {
		return this.removeValue(OPERATION, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PropertyNamevalueType newPropertyNamevalueType() {
		return new PropertyNamevalueType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WsatConfigType newWsatConfigType() {
		return new WsatConfigType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public OwsmPolicyType newOwsmPolicyType() {
		return new OwsmPolicyType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public OperationInfoType newOperationInfoType() {
		return new OperationInfoType();
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
		// Validating property portName
		if (getPortName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPortName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "portName", this);	// NOI18N
		}
		// has whitespace restriction
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPortName() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "portName", this);	// NOI18N
		}
		// Validating property portNameJ2eeId
		if (getPortNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPortNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "portNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property portNameDestinationResourceLinkJ2eeId2
		if (getPortNameDestinationResourceLinkJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPortNameDestinationResourceLinkJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "portNameDestinationResourceLinkJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property stubProperty
		for (int _index = 0; _index < sizeStubProperty(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.PropertyNamevalueType element = getStubProperty(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property callProperty
		for (int _index = 0; _index < sizeCallProperty(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.PropertyNamevalueType element = getCallProperty(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property wsatConfig
		if (getWsatConfig() != null) {
			getWsatConfig().validate();
		}
		// Validating property owsmPolicy
		for (int _index = 0; _index < sizeOwsmPolicy(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.OwsmPolicyType element = getOwsmPolicy(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property operation
		for (int _index = 0; _index < sizeOperation(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.OperationInfoType element = getOperation(_index);
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
		str.append("PortName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPortName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PORT_NAME, 0, str, indent);

		str.append(indent);
		str.append("StubProperty["+this.sizeStubProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeStubProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getStubProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(STUB_PROPERTY, i, str, indent);
		}

		str.append(indent);
		str.append("CallProperty["+this.sizeCallProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeCallProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCallProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CALL_PROPERTY, i, str, indent);
		}

		str.append(indent);
		str.append("WsatConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getWsatConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(WSAT_CONFIG, 0, str, indent);

		str.append(indent);
		str.append("OwsmPolicy["+this.sizeOwsmPolicy()+"]");	// NOI18N
		for(int i=0; i<this.sizeOwsmPolicy(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getOwsmPolicy(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(OWSM_POLICY, i, str, indent);
		}

		str.append(indent);
		str.append("Operation["+this.sizeOperation()+"]");	// NOI18N
		for(int i=0; i<this.sizeOperation(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getOperation(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(OPERATION, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PortInfoType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

