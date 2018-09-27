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
 *	This generated bean class DistributedDestinationType matches the schema element 'distributed-destination-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:58 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class DistributedDestinationType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1031.NamedEntityType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String JNDI_NAME = "JndiName";	// NOI18N
	static public final String LOCAL_JNDI_NAME = "LocalJndiName";	// NOI18N
	static public final String LOAD_BALANCING_POLICY = "LoadBalancingPolicy";	// NOI18N
	static public final String UNIT_OF_ORDER_ROUTING = "UnitOfOrderRouting";	// NOI18N
	static public final String SAF_EXPORT_POLICY = "SafExportPolicy";	// NOI18N

	public DistributedDestinationType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	protected DistributedDestinationType(Vector comparators, Version runtimeVersion){

		super(comparators, runtimeVersion);
	}
	public DistributedDestinationType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jndi-name", 	// NOI18N
			JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("local-jndi-name", 	// NOI18N
			LOCAL_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("load-balancing-policy", 	// NOI18N
			LOAD_BALANCING_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("unit-of-order-routing", 	// NOI18N
			UNIT_OF_ORDER_ROUTING, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("saf-export-policy", 	// NOI18N
			SAF_EXPORT_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		setAttributeValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return getAttributeValue(NAME);
	}

	// This attribute is optional
	public void setNotes(java.lang.String value) {
		this.setValue(NOTES, value);
	}

	//
	public java.lang.String getNotes() {
		return (java.lang.String)this.getValue(NOTES);
	}

	// This attribute is optional
	public void setJndiName(java.lang.String value) {
		this.setValue(JNDI_NAME, value);
	}

	//
	public java.lang.String getJndiName() {
		return (java.lang.String)this.getValue(JNDI_NAME);
	}

	// This attribute is optional
	public void setLocalJndiName(java.lang.String value) {
		this.setValue(LOCAL_JNDI_NAME, value);
	}

	//
	public java.lang.String getLocalJndiName() {
		return (java.lang.String)this.getValue(LOCAL_JNDI_NAME);
	}

	// This attribute is optional
	public void setLoadBalancingPolicy(java.lang.String value) {
		this.setValue(LOAD_BALANCING_POLICY, value);
	}

	//
	public java.lang.String getLoadBalancingPolicy() {
		return (java.lang.String)this.getValue(LOAD_BALANCING_POLICY);
	}

	// This attribute is optional
	public void setUnitOfOrderRouting(java.lang.String value) {
		this.setValue(UNIT_OF_ORDER_ROUTING, value);
	}

	//
	public java.lang.String getUnitOfOrderRouting() {
		return (java.lang.String)this.getValue(UNIT_OF_ORDER_ROUTING);
	}

	// This attribute is optional
	public void setSafExportPolicy(java.lang.String value) {
		this.setValue(SAF_EXPORT_POLICY, value);
	}

	//
	public java.lang.String getSafExportPolicy() {
		return (java.lang.String)this.getValue(SAF_EXPORT_POLICY);
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
		// Validating property notes
		// Validating property jndiName
		// Validating property localJndiName
		// Validating property loadBalancingPolicy
		// Validating property unitOfOrderRouting
		if (getUnitOfOrderRouting() != null) {
			final java.lang.String[] enumRestrictionUnitOfOrderRouting = {"Hash", "PathService"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionUnitOfOrderRouting.length; ++_index2) {
				if (enumRestrictionUnitOfOrderRouting[_index2].equals(getUnitOfOrderRouting())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getUnitOfOrderRouting() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "unitOfOrderRouting", this);	// NOI18N
			}
		}
		// Validating property safExportPolicy
		if (getSafExportPolicy() != null) {
			final java.lang.String[] enumRestrictionSafExportPolicy = {"All", "None"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionSafExportPolicy.length; ++_index2) {
				if (enumRestrictionSafExportPolicy[_index2].equals(getSafExportPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSafExportPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "safExportPolicy", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Notes");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNotes();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NOTES, 0, str, indent);

		str.append(indent);
		str.append("JndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("LocalJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLocalJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOCAL_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("LoadBalancingPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLoadBalancingPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOAD_BALANCING_POLICY, 0, str, indent);

		str.append(indent);
		str.append("UnitOfOrderRouting");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUnitOfOrderRouting();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(UNIT_OF_ORDER_ROUTING, 0, str, indent);

		str.append(indent);
		str.append("SafExportPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSafExportPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SAF_EXPORT_POLICY, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DistributedDestinationType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

