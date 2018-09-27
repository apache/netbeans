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
 *	This generated bean class ServiceReferenceDescriptionType matches the schema element 'service-reference-descriptionType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:52 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ServiceReferenceDescriptionType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String SERVICE_REF_NAME = "ServiceRefName";	// NOI18N
	static public final String WSDL_URL = "WsdlUrl";	// NOI18N
	static public final String WSDLURLJ2EEID = "WsdlUrlJ2eeId";	// NOI18N
	static public final String CALL_PROPERTY = "CallProperty";	// NOI18N
	static public final String PORT_INFO = "PortInfo";	// NOI18N

	public ServiceReferenceDescriptionType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ServiceReferenceDescriptionType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("service-ref-name", 	// NOI18N
			SERVICE_REF_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("wsdl-url", 	// NOI18N
			WSDL_URL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(WSDL_URL, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("call-property", 	// NOI18N
			CALL_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PropertyNamevalueType.class);
		this.createProperty("port-info", 	// NOI18N
			PORT_INFO, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PortInfoType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		setAttributeValue(ID, value);
	}

	//
	public java.lang.String getId() {
		return getAttributeValue(ID);
	}

	// This attribute is mandatory
	public void setServiceRefName(java.lang.String value) {
		this.setValue(SERVICE_REF_NAME, value);
	}

	//
	public java.lang.String getServiceRefName() {
		return (java.lang.String)this.getValue(SERVICE_REF_NAME);
	}

	// This attribute is optional
	public void setWsdlUrl(java.lang.String value) {
		this.setValue(WSDL_URL, value);
	}

	//
	public java.lang.String getWsdlUrl() {
		return (java.lang.String)this.getValue(WSDL_URL);
	}

	// This attribute is optional
	public void setWsdlUrlJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(WSDL_URL) == 0) {
			setValue(WSDL_URL, "");
		}
		setAttributeValue(WSDL_URL, "J2eeId", value);
	}

	//
	public java.lang.String getWsdlUrlJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(WSDL_URL) == 0) {
			return null;
		} else {
			return getAttributeValue(WSDL_URL, "J2eeId");
		}
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
	public int addCallProperty(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.PropertyNamevalueType value) {
		int positionOfNewItem = this.addValue(CALL_PROPERTY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCallProperty(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.PropertyNamevalueType value) {
		return this.removeValue(CALL_PROPERTY, value);
	}

	// This attribute is an array, possibly empty
	public void setPortInfo(int index, PortInfoType value) {
		this.setValue(PORT_INFO, index, value);
	}

	//
	public PortInfoType getPortInfo(int index) {
		return (PortInfoType)this.getValue(PORT_INFO, index);
	}

	// Return the number of properties
	public int sizePortInfo() {
		return this.size(PORT_INFO);
	}

	// This attribute is an array, possibly empty
	public void setPortInfo(PortInfoType[] value) {
		this.setValue(PORT_INFO, value);
	}

	//
	public PortInfoType[] getPortInfo() {
		return (PortInfoType[])this.getValues(PORT_INFO);
	}

	// Add a new element returning its index in the list
	public int addPortInfo(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.PortInfoType value) {
		int positionOfNewItem = this.addValue(PORT_INFO, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePortInfo(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.PortInfoType value) {
		return this.removeValue(PORT_INFO, value);
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
	public PortInfoType newPortInfoType() {
		return new PortInfoType();
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
		// Validating property id
		if (getId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "id", this);	// NOI18N
			}
		}
		// Validating property serviceRefName
		if (getServiceRefName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getServiceRefName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "serviceRefName", this);	// NOI18N
		}
		// Validating property wsdlUrl
		if (getWsdlUrl() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getWsdlUrl() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "wsdlUrl", this);	// NOI18N
			}
		}
		// Validating property wsdlUrlJ2eeId
		if (getWsdlUrlJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getWsdlUrlJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "wsdlUrlJ2eeId", this);	// NOI18N
			}
		}
		// Validating property callProperty
		for (int _index = 0; _index < sizeCallProperty(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.PropertyNamevalueType element = getCallProperty(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property portInfo
		for (int _index = 0; _index < sizePortInfo(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.PortInfoType element = getPortInfo(_index);
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
		str.append("ServiceRefName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getServiceRefName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVICE_REF_NAME, 0, str, indent);

		str.append(indent);
		str.append("WsdlUrl");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getWsdlUrl();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(WSDL_URL, 0, str, indent);

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
		str.append("PortInfo["+this.sizePortInfo()+"]");	// NOI18N
		for(int i=0; i<this.sizePortInfo(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getPortInfo(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PORT_INFO, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ServiceReferenceDescriptionType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

