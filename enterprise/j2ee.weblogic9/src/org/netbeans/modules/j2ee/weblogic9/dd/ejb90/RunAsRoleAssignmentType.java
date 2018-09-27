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
 *	This generated bean class RunAsRoleAssignmentType matches the schema element 'run-as-role-assignmentType'.
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

public class RunAsRoleAssignmentType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String ROLE_NAME = "RoleName";	// NOI18N
	static public final String RUN_AS_PRINCIPAL_NAME = "RunAsPrincipalName";	// NOI18N
	static public final String RUNASPRINCIPALNAMEJ2EEID = "RunAsPrincipalNameJ2eeId";	// NOI18N

	public RunAsRoleAssignmentType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public RunAsRoleAssignmentType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("role-name", 	// NOI18N
			ROLE_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("run-as-principal-name", 	// NOI18N
			RUN_AS_PRINCIPAL_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(RUN_AS_PRINCIPAL_NAME, "j2ee:id", "J2eeId", 
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

	// This attribute is mandatory
	public void setRunAsPrincipalName(java.lang.String value) {
		this.setValue(RUN_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getRunAsPrincipalName() {
		return (java.lang.String)this.getValue(RUN_AS_PRINCIPAL_NAME);
	}

	// This attribute is optional
	public void setRunAsPrincipalNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(RUN_AS_PRINCIPAL_NAME) == 0) {
			setValue(RUN_AS_PRINCIPAL_NAME, "");
		}
		setAttributeValue(RUN_AS_PRINCIPAL_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getRunAsPrincipalNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(RUN_AS_PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(RUN_AS_PRINCIPAL_NAME, "J2eeId");
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
		// Validating property runAsPrincipalName
		if (getRunAsPrincipalName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRunAsPrincipalName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "runAsPrincipalName", this);	// NOI18N
		}
		// has whitespace restriction
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRunAsPrincipalName() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "runAsPrincipalName", this);	// NOI18N
		}
		// Validating property runAsPrincipalNameJ2eeId
		if (getRunAsPrincipalNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRunAsPrincipalNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "runAsPrincipalNameJ2eeId", this);	// NOI18N
			}
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
		str.append("RunAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRunAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RUN_AS_PRINCIPAL_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("RunAsRoleAssignmentType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

