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
 *	This generated bean class PropertyNamevalueType matches the schema element 'property-namevalueType'.
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

public class PropertyNamevalueType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NAMEJ2EEID = "NameJ2eeId";	// NOI18N
	static public final String NAMEDESTINATIONRESOURCELINKJ2EEID2 = "NameDestinationResourceLinkJ2eeId2";	// NOI18N
	static public final String VALUE = "Value";	// NOI18N
	static public final String VALUEJ2EEID = "ValueJ2eeId";	// NOI18N
	static public final String VALUEDESTINATIONRESOURCELINKJ2EEID2 = "ValueDestinationResourceLinkJ2eeId2";	// NOI18N

	public PropertyNamevalueType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PropertyNamevalueType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(NAME, "j2ee:id", "DestinationResourceLinkJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("value", 	// NOI18N
			VALUE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(VALUE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(VALUE, "j2ee:id", "DestinationResourceLinkJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		this.setValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return (java.lang.String)this.getValue(NAME);
	}

	// This attribute is optional
	public void setNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NAME) == 0) {
			setValue(NAME, "");
		}
		setAttributeValue(NAME, "J2eeId", value);
	}

	//
	public java.lang.String getNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setNameDestinationResourceLinkJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NAME) == 0) {
			setValue(NAME, "");
		}
		setAttributeValue(NAME, "DestinationResourceLinkJ2eeId2", value);
	}

	//
	public java.lang.String getNameDestinationResourceLinkJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(NAME, "DestinationResourceLinkJ2eeId2");
		}
	}

	// This attribute is mandatory
	public void setValue(java.lang.String value) {
		this.setValue(VALUE, value);
	}

	//
	public java.lang.String getValue() {
		return (java.lang.String)this.getValue(VALUE);
	}

	// This attribute is optional
	public void setValueJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NAME) == 0) {
			setValue(NAME, "");
		}
		setAttributeValue(NAME, "J2eeId", value);
	}

	//
	public java.lang.String getValueJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setValueDestinationResourceLinkJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NAME) == 0) {
			setValue(NAME, "");
		}
		setAttributeValue(NAME, "DestinationResourceLinkJ2eeId2", value);
	}

	//
	public java.lang.String getValueDestinationResourceLinkJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(NAME, "DestinationResourceLinkJ2eeId2");
		}
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
		// has whitespace restriction
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "name", this);	// NOI18N
		}
		// Validating property nameJ2eeId
		if (getNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "nameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property nameDestinationResourceLinkJ2eeId2
		if (getNameDestinationResourceLinkJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getNameDestinationResourceLinkJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "nameDestinationResourceLinkJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property value
		if (getValue() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getValue() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "value", this);	// NOI18N
		}
		// has whitespace restriction
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getValue() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "value", this);	// NOI18N
		}
		// Validating property valueJ2eeId
		if (getValueJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getValueJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "valueJ2eeId", this);	// NOI18N
			}
		}
		// Validating property valueDestinationResourceLinkJ2eeId2
		if (getValueDestinationResourceLinkJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getValueDestinationResourceLinkJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "valueDestinationResourceLinkJ2eeId2", this);	// NOI18N
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
		str.append("Value");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getValue();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(VALUE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PropertyNamevalueType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

