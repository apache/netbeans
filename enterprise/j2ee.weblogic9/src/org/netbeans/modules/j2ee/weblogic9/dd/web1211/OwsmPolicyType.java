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
 *	This generated bean class OwsmPolicyType matches the schema element 'owsm-policyType'.
 *  The root bean class is WeblogicWebApp
 *
 *	===============================================================
 *	
 *	                  Specifies the owsm security policy configuration for this web service port component.
 *	          
 *	===============================================================
 *	Generated on Tue Jul 25 03:27:04 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class OwsmPolicyType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String URI = "Uri";	// NOI18N
	static public final String URIJ2EEID = "UriJ2eeId";	// NOI18N
	static public final String STATUS = "Status";	// NOI18N
	static public final String STATUSJ2EEID = "StatusJ2eeId";	// NOI18N
	static public final String CATEGORY = "Category";	// NOI18N
	static public final String CATEGORYJ2EEID = "CategoryJ2eeId";	// NOI18N
	static public final String SECURITY_CONFIGURATION_PROPERTY  = "SecurityConfigurationProperty";	// NOI18N

	public OwsmPolicyType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public OwsmPolicyType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("uri", 	// NOI18N
			URI, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(URI, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("status", 	// NOI18N
			STATUS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(STATUS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("category", 	// NOI18N
			CATEGORY, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CATEGORY, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("security-configuration-property ", 	// NOI18N
			SECURITY_CONFIGURATION_PROPERTY , 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PropertyNamevalueType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setUri(java.lang.String value) {
		this.setValue(URI, value);
	}

	//
	public java.lang.String getUri() {
		return (java.lang.String)this.getValue(URI);
	}

	// This attribute is optional
	public void setUriJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(URI) == 0) {
			setValue(URI, "");
		}
		setAttributeValue(URI, "J2eeId", value);
	}

	//
	public java.lang.String getUriJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(URI) == 0) {
			return null;
		} else {
			return getAttributeValue(URI, "J2eeId");
		}
	}

	// This attribute is optional
	public void setStatus(java.lang.String value) {
		this.setValue(STATUS, value);
	}

	//
	public java.lang.String getStatus() {
		return (java.lang.String)this.getValue(STATUS);
	}

	// This attribute is optional
	public void setStatusJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(URI) == 0) {
			setValue(URI, "");
		}
		setAttributeValue(URI, "J2eeId", value);
	}

	//
	public java.lang.String getStatusJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(URI) == 0) {
			return null;
		} else {
			return getAttributeValue(URI, "J2eeId");
		}
	}

	// This attribute is mandatory
	public void setCategory(java.lang.String value) {
		this.setValue(CATEGORY, value);
	}

	//
	public java.lang.String getCategory() {
		return (java.lang.String)this.getValue(CATEGORY);
	}

	// This attribute is optional
	public void setCategoryJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(URI) == 0) {
			setValue(URI, "");
		}
		setAttributeValue(URI, "J2eeId", value);
	}

	//
	public java.lang.String getCategoryJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(URI) == 0) {
			return null;
		} else {
			return getAttributeValue(URI, "J2eeId");
		}
	}

	// This attribute is an array, possibly empty
	public void setSecurityConfigurationProperty(int index, PropertyNamevalueType value) {
		this.setValue(SECURITY_CONFIGURATION_PROPERTY , index, value);
	}

	//
	public PropertyNamevalueType getSecurityConfigurationProperty(int index) {
		return (PropertyNamevalueType)this.getValue(SECURITY_CONFIGURATION_PROPERTY , index);
	}

	// Return the number of properties
	public int sizeSecurityConfigurationProperty() {
		return this.size(SECURITY_CONFIGURATION_PROPERTY );
	}

	// This attribute is an array, possibly empty
	public void setSecurityConfigurationProperty(PropertyNamevalueType[] value) {
		this.setValue(SECURITY_CONFIGURATION_PROPERTY , value);
	}

	//
	public PropertyNamevalueType[] getSecurityConfigurationProperty() {
		return (PropertyNamevalueType[])this.getValues(SECURITY_CONFIGURATION_PROPERTY );
	}

	// Add a new element returning its index in the list
	public int addSecurityConfigurationProperty(org.netbeans.modules.j2ee.weblogic9.dd.web1211.PropertyNamevalueType value) {
		int positionOfNewItem = this.addValue(SECURITY_CONFIGURATION_PROPERTY , value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSecurityConfigurationProperty(org.netbeans.modules.j2ee.weblogic9.dd.web1211.PropertyNamevalueType value) {
		return this.removeValue(SECURITY_CONFIGURATION_PROPERTY , value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PropertyNamevalueType newPropertyNamevalueType() {
		return new PropertyNamevalueType();
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
		// Validating property uri
		if (getUri() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getUri() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "uri", this);	// NOI18N
		}
		// has whitespace restriction
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getUri() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "uri", this);	// NOI18N
		}
		// Validating property uriJ2eeId
		if (getUriJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getUriJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "uriJ2eeId", this);	// NOI18N
			}
		}
		// Validating property status
		if (getStatus() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStatus() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "status", this);	// NOI18N
			}
		}
		// Validating property statusJ2eeId
		if (getStatusJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStatusJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "statusJ2eeId", this);	// NOI18N
			}
		}
		// Validating property category
		if (getCategory() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getCategory() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "category", this);	// NOI18N
		}
		// has whitespace restriction
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getCategory() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "category", this);	// NOI18N
		}
		// Validating property categoryJ2eeId
		if (getCategoryJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCategoryJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "categoryJ2eeId", this);	// NOI18N
			}
		}
		// Validating property securityConfigurationProperty
		for (int _index = 0; _index < sizeSecurityConfigurationProperty(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.web1211.PropertyNamevalueType element = getSecurityConfigurationProperty(_index);
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
		str.append("Uri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUri();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(URI, 0, str, indent);

		str.append(indent);
		str.append("Status");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getStatus();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(STATUS, 0, str, indent);

		str.append(indent);
		str.append("Category");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCategory();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CATEGORY, 0, str, indent);

		str.append(indent);
		str.append("SecurityConfigurationProperty["+this.sizeSecurityConfigurationProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeSecurityConfigurationProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityConfigurationProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SECURITY_CONFIGURATION_PROPERTY , i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("OwsmPolicyType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

