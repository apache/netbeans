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
 *	This generated bean class TimerDescriptorType matches the schema element 'timer-descriptorType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:56 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class TimerDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String PERSISTENT_STORE_LOGICAL_NAME = "PersistentStoreLogicalName";	// NOI18N
	static public final String PERSISTENTSTORELOGICALNAMEJ2EEID = "PersistentStoreLogicalNameJ2eeId";	// NOI18N
	static public final String PERSISTENTSTORELOGICALNAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "PersistentStoreLogicalNameComponentFactoryClassNameJ2eeId2";	// NOI18N

	public TimerDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public TimerDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("persistent-store-logical-name", 	// NOI18N
			PERSISTENT_STORE_LOGICAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PERSISTENT_STORE_LOGICAL_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(PERSISTENT_STORE_LOGICAL_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
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

	// This attribute is optional
	public void setPersistentStoreLogicalName(java.lang.String value) {
		this.setValue(PERSISTENT_STORE_LOGICAL_NAME, value);
	}

	//
	public java.lang.String getPersistentStoreLogicalName() {
		return (java.lang.String)this.getValue(PERSISTENT_STORE_LOGICAL_NAME);
	}

	// This attribute is optional
	public void setPersistentStoreLogicalNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PERSISTENT_STORE_LOGICAL_NAME) == 0) {
			setValue(PERSISTENT_STORE_LOGICAL_NAME, "");
		}
		setAttributeValue(PERSISTENT_STORE_LOGICAL_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getPersistentStoreLogicalNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PERSISTENT_STORE_LOGICAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(PERSISTENT_STORE_LOGICAL_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPersistentStoreLogicalNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PERSISTENT_STORE_LOGICAL_NAME) == 0) {
			setValue(PERSISTENT_STORE_LOGICAL_NAME, "");
		}
		setAttributeValue(PERSISTENT_STORE_LOGICAL_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getPersistentStoreLogicalNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PERSISTENT_STORE_LOGICAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(PERSISTENT_STORE_LOGICAL_NAME, "ComponentFactoryClassNameJ2eeId2");
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
		// Validating property persistentStoreLogicalName
		// Validating property persistentStoreLogicalNameJ2eeId
		if (getPersistentStoreLogicalNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentStoreLogicalNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentStoreLogicalNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property persistentStoreLogicalNameComponentFactoryClassNameJ2eeId2
		if (getPersistentStoreLogicalNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentStoreLogicalNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentStoreLogicalNameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("PersistentStoreLogicalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentStoreLogicalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_STORE_LOGICAL_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("TimerDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

