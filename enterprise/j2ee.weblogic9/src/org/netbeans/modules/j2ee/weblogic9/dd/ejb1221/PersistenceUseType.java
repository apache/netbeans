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
 *	This generated bean class PersistenceUseType matches the schema element 'persistence-useType'.
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

public class PersistenceUseType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String TYPE_IDENTIFIER = "TypeIdentifier";	// NOI18N
	static public final String TYPEIDENTIFIERID = "TypeIdentifierId";	// NOI18N
	static public final String TYPE_VERSION = "TypeVersion";	// NOI18N
	static public final String TYPEVERSIONID = "TypeVersionId";	// NOI18N
	static public final String TYPE_STORAGE = "TypeStorage";	// NOI18N
	static public final String TYPESTORAGEID = "TypeStorageId";	// NOI18N

	public PersistenceUseType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PersistenceUseType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("type-identifier", 	// NOI18N
			TYPE_IDENTIFIER, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(TYPE_IDENTIFIER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("type-version", 	// NOI18N
			TYPE_VERSION, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(TYPE_VERSION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("type-storage", 	// NOI18N
			TYPE_STORAGE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(TYPE_STORAGE, "id", "Id", 
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
	public void setTypeIdentifier(java.lang.String value) {
		this.setValue(TYPE_IDENTIFIER, value);
	}

	//
	public java.lang.String getTypeIdentifier() {
		return (java.lang.String)this.getValue(TYPE_IDENTIFIER);
	}

	// This attribute is optional
	public void setTypeIdentifierId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TYPE_IDENTIFIER) == 0) {
			setValue(TYPE_IDENTIFIER, "");
		}
		setAttributeValue(TYPE_IDENTIFIER, "Id", value);
	}

	//
	public java.lang.String getTypeIdentifierId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TYPE_IDENTIFIER) == 0) {
			return null;
		} else {
			return getAttributeValue(TYPE_IDENTIFIER, "Id");
		}
	}

	// This attribute is mandatory
	public void setTypeVersion(java.lang.String value) {
		this.setValue(TYPE_VERSION, value);
	}

	//
	public java.lang.String getTypeVersion() {
		return (java.lang.String)this.getValue(TYPE_VERSION);
	}

	// This attribute is optional
	public void setTypeVersionId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TYPE_VERSION) == 0) {
			setValue(TYPE_VERSION, "");
		}
		setAttributeValue(TYPE_VERSION, "Id", value);
	}

	//
	public java.lang.String getTypeVersionId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TYPE_VERSION) == 0) {
			return null;
		} else {
			return getAttributeValue(TYPE_VERSION, "Id");
		}
	}

	// This attribute is mandatory
	public void setTypeStorage(java.lang.String value) {
		this.setValue(TYPE_STORAGE, value);
	}

	//
	public java.lang.String getTypeStorage() {
		return (java.lang.String)this.getValue(TYPE_STORAGE);
	}

	// This attribute is optional
	public void setTypeStorageId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TYPE_STORAGE) == 0) {
			setValue(TYPE_STORAGE, "");
		}
		setAttributeValue(TYPE_STORAGE, "Id", value);
	}

	//
	public java.lang.String getTypeStorageId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TYPE_STORAGE) == 0) {
			return null;
		} else {
			return getAttributeValue(TYPE_STORAGE, "Id");
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
		// Validating property typeIdentifier
		if (getTypeIdentifier() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTypeIdentifier() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "typeIdentifier", this);	// NOI18N
		}
		// Validating property typeIdentifierId
		if (getTypeIdentifierId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTypeIdentifierId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "typeIdentifierId", this);	// NOI18N
			}
		}
		// Validating property typeVersion
		if (getTypeVersion() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTypeVersion() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "typeVersion", this);	// NOI18N
		}
		// Validating property typeVersionId
		if (getTypeVersionId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTypeVersionId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "typeVersionId", this);	// NOI18N
			}
		}
		// Validating property typeStorage
		if (getTypeStorage() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTypeStorage() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "typeStorage", this);	// NOI18N
		}
		// Validating property typeStorageId
		if (getTypeStorageId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTypeStorageId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "typeStorageId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("TypeIdentifier");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTypeIdentifier();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TYPE_IDENTIFIER, 0, str, indent);

		str.append(indent);
		str.append("TypeVersion");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTypeVersion();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TYPE_VERSION, 0, str, indent);

		str.append(indent);
		str.append("TypeStorage");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTypeStorage();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TYPE_STORAGE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PersistenceUseType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

