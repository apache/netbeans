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
 *	This generated bean class StatelessSessionDescriptorType matches the schema element 'stateless-session-descriptorType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:51 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class StatelessSessionDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String POOL = "Pool";	// NOI18N
	static public final String TIMER_DESCRIPTOR = "TimerDescriptor";	// NOI18N
	static public final String STATELESS_CLUSTERING = "StatelessClustering";	// NOI18N
	static public final String BUSINESS_INTERFACE_JNDI_NAME_MAP = "BusinessInterfaceJndiNameMap";	// NOI18N

	public StatelessSessionDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public StatelessSessionDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("pool", 	// NOI18N
			POOL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PoolType.class);
		this.createAttribute(POOL, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("timer-descriptor", 	// NOI18N
			TIMER_DESCRIPTOR, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TimerDescriptorType.class);
		this.createAttribute(TIMER_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stateless-clustering", 	// NOI18N
			STATELESS_CLUSTERING, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StatelessClusteringType.class);
		this.createAttribute(STATELESS_CLUSTERING, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("business-interface-jndi-name-map", 	// NOI18N
			BUSINESS_INTERFACE_JNDI_NAME_MAP, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			BusinessInterfaceJndiNameMapType.class);
		this.createAttribute(BUSINESS_INTERFACE_JNDI_NAME_MAP, "id", "Id", 
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
	public void setPool(PoolType value) {
		this.setValue(POOL, value);
	}

	//
	public PoolType getPool() {
		return (PoolType)this.getValue(POOL);
	}

	// This attribute is optional
	public void setTimerDescriptor(TimerDescriptorType value) {
		this.setValue(TIMER_DESCRIPTOR, value);
	}

	//
	public TimerDescriptorType getTimerDescriptor() {
		return (TimerDescriptorType)this.getValue(TIMER_DESCRIPTOR);
	}

	// This attribute is optional
	public void setStatelessClustering(StatelessClusteringType value) {
		this.setValue(STATELESS_CLUSTERING, value);
	}

	//
	public StatelessClusteringType getStatelessClustering() {
		return (StatelessClusteringType)this.getValue(STATELESS_CLUSTERING);
	}

	// This attribute is an array, possibly empty
	public void setBusinessInterfaceJndiNameMap(int index, BusinessInterfaceJndiNameMapType value) {
		this.setValue(BUSINESS_INTERFACE_JNDI_NAME_MAP, index, value);
	}

	//
	public BusinessInterfaceJndiNameMapType getBusinessInterfaceJndiNameMap(int index) {
		return (BusinessInterfaceJndiNameMapType)this.getValue(BUSINESS_INTERFACE_JNDI_NAME_MAP, index);
	}

	// Return the number of properties
	public int sizeBusinessInterfaceJndiNameMap() {
		return this.size(BUSINESS_INTERFACE_JNDI_NAME_MAP);
	}

	// This attribute is an array, possibly empty
	public void setBusinessInterfaceJndiNameMap(BusinessInterfaceJndiNameMapType[] value) {
		this.setValue(BUSINESS_INTERFACE_JNDI_NAME_MAP, value);
	}

	//
	public BusinessInterfaceJndiNameMapType[] getBusinessInterfaceJndiNameMap() {
		return (BusinessInterfaceJndiNameMapType[])this.getValues(BUSINESS_INTERFACE_JNDI_NAME_MAP);
	}

	// Add a new element returning its index in the list
	public int addBusinessInterfaceJndiNameMap(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.BusinessInterfaceJndiNameMapType value) {
		int positionOfNewItem = this.addValue(BUSINESS_INTERFACE_JNDI_NAME_MAP, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeBusinessInterfaceJndiNameMap(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.BusinessInterfaceJndiNameMapType value) {
		return this.removeValue(BUSINESS_INTERFACE_JNDI_NAME_MAP, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PoolType newPoolType() {
		return new PoolType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TimerDescriptorType newTimerDescriptorType() {
		return new TimerDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StatelessClusteringType newStatelessClusteringType() {
		return new StatelessClusteringType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public BusinessInterfaceJndiNameMapType newBusinessInterfaceJndiNameMapType() {
		return new BusinessInterfaceJndiNameMapType();
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
		// Validating property pool
		if (getPool() != null) {
			getPool().validate();
		}
		// Validating property timerDescriptor
		if (getTimerDescriptor() != null) {
			getTimerDescriptor().validate();
		}
		// Validating property statelessClustering
		if (getStatelessClustering() != null) {
			getStatelessClustering().validate();
		}
		// Validating property businessInterfaceJndiNameMap
		for (int _index = 0; _index < sizeBusinessInterfaceJndiNameMap(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.BusinessInterfaceJndiNameMapType element = getBusinessInterfaceJndiNameMap(_index);
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
		str.append("Pool");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPool();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(POOL, 0, str, indent);

		str.append(indent);
		str.append("TimerDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTimerDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TIMER_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("StatelessClustering");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getStatelessClustering();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(STATELESS_CLUSTERING, 0, str, indent);

		str.append(indent);
		str.append("BusinessInterfaceJndiNameMap["+this.sizeBusinessInterfaceJndiNameMap()+"]");	// NOI18N
		for(int i=0; i<this.sizeBusinessInterfaceJndiNameMap(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getBusinessInterfaceJndiNameMap(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(BUSINESS_INTERFACE_JNDI_NAME_MAP, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("StatelessSessionDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

