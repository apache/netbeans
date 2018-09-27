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
 *	This generated bean class SecurityPluginType matches the schema element 'security-pluginType'.
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

public class SecurityPluginType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String PLUGIN_CLASS = "PluginClass";	// NOI18N
	static public final String PLUGINCLASSJ2EEID = "PluginClassJ2eeId";	// NOI18N
	static public final String KEY = "Key";	// NOI18N
	static public final String KEYJ2EEID = "KeyJ2eeId";	// NOI18N

	public SecurityPluginType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SecurityPluginType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("plugin-class", 	// NOI18N
			PLUGIN_CLASS, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PLUGIN_CLASS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("key", 	// NOI18N
			KEY, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(KEY, "j2ee:id", "J2eeId", 
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
	public void setPluginClass(java.lang.String value) {
		this.setValue(PLUGIN_CLASS, value);
	}

	//
	public java.lang.String getPluginClass() {
		return (java.lang.String)this.getValue(PLUGIN_CLASS);
	}

	// This attribute is optional
	public void setPluginClassJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PLUGIN_CLASS) == 0) {
			setValue(PLUGIN_CLASS, "");
		}
		setAttributeValue(PLUGIN_CLASS, "J2eeId", value);
	}

	//
	public java.lang.String getPluginClassJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PLUGIN_CLASS) == 0) {
			return null;
		} else {
			return getAttributeValue(PLUGIN_CLASS, "J2eeId");
		}
	}

	// This attribute is mandatory
	public void setKey(java.lang.String value) {
		this.setValue(KEY, value);
	}

	//
	public java.lang.String getKey() {
		return (java.lang.String)this.getValue(KEY);
	}

	// This attribute is optional
	public void setKeyJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PLUGIN_CLASS) == 0) {
			setValue(PLUGIN_CLASS, "");
		}
		setAttributeValue(PLUGIN_CLASS, "J2eeId", value);
	}

	//
	public java.lang.String getKeyJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PLUGIN_CLASS) == 0) {
			return null;
		} else {
			return getAttributeValue(PLUGIN_CLASS, "J2eeId");
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
		// Validating property pluginClass
		if (getPluginClass() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPluginClass() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "pluginClass", this);	// NOI18N
		}
		// Validating property pluginClassJ2eeId
		if (getPluginClassJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPluginClassJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "pluginClassJ2eeId", this);	// NOI18N
			}
		}
		// Validating property key
		if (getKey() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getKey() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "key", this);	// NOI18N
		}
		// Validating property keyJ2eeId
		if (getKeyJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getKeyJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "keyJ2eeId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("PluginClass");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPluginClass();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PLUGIN_CLASS, 0, str, indent);

		str.append(indent);
		str.append("Key");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getKey();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(KEY, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SecurityPluginType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

