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
 *	This generated bean class SecurityType matches the schema element 'securityType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:44 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SecurityType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String REALM_NAME = "RealmName";	// NOI18N
	static public final String SECURITY_ROLE_ASSIGNMENT = "SecurityRoleAssignment";	// NOI18N

	public SecurityType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SecurityType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("realm-name", 	// NOI18N
			REALM_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("security-role-assignment", 	// NOI18N
			SECURITY_ROLE_ASSIGNMENT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ApplicationSecurityRoleAssignmentType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setRealmName(java.lang.String value) {
		this.setValue(REALM_NAME, value);
	}

	//
	public java.lang.String getRealmName() {
		return (java.lang.String)this.getValue(REALM_NAME);
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleAssignment(int index, ApplicationSecurityRoleAssignmentType value) {
		this.setValue(SECURITY_ROLE_ASSIGNMENT, index, value);
	}

	//
	public ApplicationSecurityRoleAssignmentType getSecurityRoleAssignment(int index) {
		return (ApplicationSecurityRoleAssignmentType)this.getValue(SECURITY_ROLE_ASSIGNMENT, index);
	}

	// Return the number of properties
	public int sizeSecurityRoleAssignment() {
		return this.size(SECURITY_ROLE_ASSIGNMENT);
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleAssignment(ApplicationSecurityRoleAssignmentType[] value) {
		this.setValue(SECURITY_ROLE_ASSIGNMENT, value);
	}

	//
	public ApplicationSecurityRoleAssignmentType[] getSecurityRoleAssignment() {
		return (ApplicationSecurityRoleAssignmentType[])this.getValues(SECURITY_ROLE_ASSIGNMENT);
	}

	// Add a new element returning its index in the list
	public int addSecurityRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.ear1030.ApplicationSecurityRoleAssignmentType value) {
		int positionOfNewItem = this.addValue(SECURITY_ROLE_ASSIGNMENT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSecurityRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.ear1030.ApplicationSecurityRoleAssignmentType value) {
		return this.removeValue(SECURITY_ROLE_ASSIGNMENT, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ApplicationSecurityRoleAssignmentType newApplicationSecurityRoleAssignmentType() {
		return new ApplicationSecurityRoleAssignmentType();
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
		// Validating property realmName
		// Validating property securityRoleAssignment
		for (int _index = 0; _index < sizeSecurityRoleAssignment(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1030.ApplicationSecurityRoleAssignmentType element = getSecurityRoleAssignment(_index);
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
		str.append("RealmName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRealmName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REALM_NAME, 0, str, indent);

		str.append(indent);
		str.append("SecurityRoleAssignment["+this.sizeSecurityRoleAssignment()+"]");	// NOI18N
		for(int i=0; i<this.sizeSecurityRoleAssignment(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityRoleAssignment(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SECURITY_ROLE_ASSIGNMENT, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SecurityType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

