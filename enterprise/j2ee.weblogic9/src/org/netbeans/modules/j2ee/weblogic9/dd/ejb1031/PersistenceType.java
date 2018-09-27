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
 *	This generated bean class PersistenceType matches the schema element 'persistenceType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:53 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PersistenceType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String IS_MODIFIED_METHOD_NAME = "IsModifiedMethodName";	// NOI18N
	static public final String ISMODIFIEDMETHODNAMEID = "IsModifiedMethodNameId";	// NOI18N
	static public final String DELAY_UPDATES_UNTIL_END_OF_TX = "DelayUpdatesUntilEndOfTx";	// NOI18N
	static public final String FINDERS_LOAD_BEAN = "FindersLoadBean";	// NOI18N
	static public final String PERSISTENCE_USE = "PersistenceUse";	// NOI18N

	public PersistenceType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PersistenceType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("is-modified-method-name", 	// NOI18N
			IS_MODIFIED_METHOD_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(IS_MODIFIED_METHOD_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("delay-updates-until-end-of-tx", 	// NOI18N
			DELAY_UPDATES_UNTIL_END_OF_TX, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("finders-load-bean", 	// NOI18N
			FINDERS_LOAD_BEAN, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("persistence-use", 	// NOI18N
			PERSISTENCE_USE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PersistenceUseType.class);
		this.createAttribute(PERSISTENCE_USE, "id", "Id", 
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
	public void setIsModifiedMethodName(java.lang.String value) {
		this.setValue(IS_MODIFIED_METHOD_NAME, value);
	}

	//
	public java.lang.String getIsModifiedMethodName() {
		return (java.lang.String)this.getValue(IS_MODIFIED_METHOD_NAME);
	}

	// This attribute is optional
	public void setIsModifiedMethodNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IS_MODIFIED_METHOD_NAME) == 0) {
			setValue(IS_MODIFIED_METHOD_NAME, "");
		}
		setAttributeValue(IS_MODIFIED_METHOD_NAME, "Id", value);
	}

	//
	public java.lang.String getIsModifiedMethodNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IS_MODIFIED_METHOD_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(IS_MODIFIED_METHOD_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setDelayUpdatesUntilEndOfTx(boolean value) {
		this.setValue(DELAY_UPDATES_UNTIL_END_OF_TX, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDelayUpdatesUntilEndOfTx() {
		Boolean ret = (Boolean)this.getValue(DELAY_UPDATES_UNTIL_END_OF_TX);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setFindersLoadBean(boolean value) {
		this.setValue(FINDERS_LOAD_BEAN, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isFindersLoadBean() {
		Boolean ret = (Boolean)this.getValue(FINDERS_LOAD_BEAN);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPersistenceUse(PersistenceUseType value) {
		this.setValue(PERSISTENCE_USE, value);
	}

	//
	public PersistenceUseType getPersistenceUse() {
		return (PersistenceUseType)this.getValue(PERSISTENCE_USE);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PersistenceUseType newPersistenceUseType() {
		return new PersistenceUseType();
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
		// Validating property isModifiedMethodName
		// Validating property isModifiedMethodNameId
		if (getIsModifiedMethodNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIsModifiedMethodNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "isModifiedMethodNameId", this);	// NOI18N
			}
		}
		// Validating property delayUpdatesUntilEndOfTx
		{
			boolean patternPassed = false;
			if ((isDelayUpdatesUntilEndOfTx() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isDelayUpdatesUntilEndOfTx()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "delayUpdatesUntilEndOfTx", this);	// NOI18N
		}
		// Validating property findersLoadBean
		{
			boolean patternPassed = false;
			if ((isFindersLoadBean() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isFindersLoadBean()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "findersLoadBean", this);	// NOI18N
		}
		// Validating property persistenceUse
		if (getPersistenceUse() != null) {
			getPersistenceUse().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("IsModifiedMethodName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getIsModifiedMethodName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(IS_MODIFIED_METHOD_NAME, 0, str, indent);

		str.append(indent);
		str.append("DelayUpdatesUntilEndOfTx");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDelayUpdatesUntilEndOfTx()?"true":"false"));
		this.dumpAttributes(DELAY_UPDATES_UNTIL_END_OF_TX, 0, str, indent);

		str.append(indent);
		str.append("FindersLoadBean");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isFindersLoadBean()?"true":"false"));
		this.dumpAttributes(FINDERS_LOAD_BEAN, 0, str, indent);

		str.append(indent);
		str.append("PersistenceUse");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPersistenceUse();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PERSISTENCE_USE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PersistenceType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

