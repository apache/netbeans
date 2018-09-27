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
 *	This generated bean class SecurityRoleAssignmentType matches the schema element 'security-role-assignmentType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:57 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SecurityRoleAssignmentType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String ROLE_NAME = "RoleName";	// NOI18N
	static public final String PRINCIPAL_NAME = "PrincipalName";	// NOI18N
	static public final String PRINCIPALNAMEJ2EEID = "PrincipalNameJ2eeId";	// NOI18N
	static public final String EXTERNALLY_DEFINED = "ExternallyDefined";	// NOI18N

	public SecurityRoleAssignmentType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SecurityRoleAssignmentType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("role-name", 	// NOI18N
			ROLE_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("principal-name", 	// NOI18N
			PRINCIPAL_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PRINCIPAL_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("externally-defined", 	// NOI18N
			EXTERNALLY_DEFINED, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EmptyType.class);
		this.createAttribute(EXTERNALLY_DEFINED, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
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
	public void setRoleName(java.lang.String value) {
		this.setValue(ROLE_NAME, value);
	}

	//
	public java.lang.String getRoleName() {
		return (java.lang.String)this.getValue(ROLE_NAME);
	}

	// This attribute is an array containing at least one element
	public void setPrincipalName(int index, java.lang.String value) {
		this.setValue(PRINCIPAL_NAME, index, value);
	}

	//
	public java.lang.String getPrincipalName(int index) {
		return (java.lang.String)this.getValue(PRINCIPAL_NAME, index);
	}

	// Return the number of properties
	public int sizePrincipalName() {
		return this.size(PRINCIPAL_NAME);
	}

	// This attribute is an array containing at least one element
	public void setPrincipalName(java.lang.String[] value) {
		this.setValue(PRINCIPAL_NAME, value);
		if (value != null && value.length > 0) {
			// It's a mutually exclusive property.
			setExternallyDefined(null);
		}
	}

	//
	public java.lang.String[] getPrincipalName() {
		return (java.lang.String[])this.getValues(PRINCIPAL_NAME);
	}

	// Add a new element returning its index in the list
	public int addPrincipalName(java.lang.String value) {
		int positionOfNewItem = this.addValue(PRINCIPAL_NAME, value);
		if (positionOfNewItem == 0) {
			// It's a mutually exclusive property.
			setExternallyDefined(null);
		}
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePrincipalName(java.lang.String value) {
		return this.removeValue(PRINCIPAL_NAME, value);
	}

	// This attribute is an array, possibly empty
	public void setPrincipalNameJ2eeId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PRINCIPAL_NAME) == 0) {
			addValue(PRINCIPAL_NAME, "");
		}
		setAttributeValue(PRINCIPAL_NAME, index, "J2eeId", value);
	}

	//
	public java.lang.String getPrincipalNameJ2eeId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(PRINCIPAL_NAME, index, "J2eeId");
		}
	}

	// Return the number of properties
	public int sizePrincipalNameJ2eeId() {
		return this.size(PRINCIPAL_NAME);
	}

	// This attribute is mandatory
	public void setExternallyDefined(EmptyType value) {
		this.setValue(EXTERNALLY_DEFINED, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setPrincipalName(null);
		}
	}

	//
	public EmptyType getExternallyDefined() {
		return (EmptyType)this.getValue(EXTERNALLY_DEFINED);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EmptyType newEmptyType() {
		return new EmptyType();
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
		// Validating property roleName
		if (getRoleName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRoleName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "roleName", this);	// NOI18N
		}
		// Validating property principalName
		for (int _index = 0; _index < sizePrincipalName(); ++_index) {
			java.lang.String element = getPrincipalName(_index);
			if (element != null) {
				// has whitespace restriction
				if (restrictionFailure) {
					throw new org.netbeans.modules.schema2beans.ValidateException("element whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "principalName", this);	// NOI18N
				}
			}
		}
		if (sizePrincipalName() > 0) {
			if (getExternallyDefined() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: PrincipalName and ExternallyDefined", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "ExternallyDefined", this);	// NOI18N
			}
		}
		// Validating property principalNameJ2eeId
		// Validating property externallyDefined
		if (getExternallyDefined() != null) {
			getExternallyDefined().validate();
		}
		if (getExternallyDefined() != null) {
			if (sizePrincipalName() > 0) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: ExternallyDefined and PrincipalName", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "PrincipalName", this);	// NOI18N
			}
		}
		if (getExternallyDefined() == null && sizePrincipalName() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getExternallyDefined() == null && sizePrincipalName() == 0", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "ExternallyDefined", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("RoleName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRoleName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ROLE_NAME, 0, str, indent);

		str.append(indent);
		str.append("PrincipalName["+this.sizePrincipalName()+"]");	// NOI18N
		for(int i=0; i<this.sizePrincipalName(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getPrincipalName(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PRINCIPAL_NAME, i, str, indent);
		}

		str.append(indent);
		str.append("ExternallyDefined");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getExternallyDefined();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(EXTERNALLY_DEFINED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SecurityRoleAssignmentType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

