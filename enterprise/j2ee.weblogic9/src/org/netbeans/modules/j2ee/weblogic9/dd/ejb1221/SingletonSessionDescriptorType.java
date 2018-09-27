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
 *	This generated bean class SingletonSessionDescriptorType matches the schema element 'singleton-session-descriptorType'.
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

public class SingletonSessionDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String TIMER_DESCRIPTOR = "TimerDescriptor";	// NOI18N
	static public final String SINGLETON_CLUSTERING = "SingletonClustering";	// NOI18N

	public SingletonSessionDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SingletonSessionDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("timer-descriptor", 	// NOI18N
			TIMER_DESCRIPTOR, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TimerDescriptorType.class);
		this.createAttribute(TIMER_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("singleton-clustering", 	// NOI18N
			SINGLETON_CLUSTERING, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SingletonClusteringType.class);
		this.createAttribute(SINGLETON_CLUSTERING, "id", "Id", 
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
	public void setTimerDescriptor(TimerDescriptorType value) {
		this.setValue(TIMER_DESCRIPTOR, value);
	}

	//
	public TimerDescriptorType getTimerDescriptor() {
		return (TimerDescriptorType)this.getValue(TIMER_DESCRIPTOR);
	}

	// This attribute is optional
	public void setSingletonClustering(SingletonClusteringType value) {
		this.setValue(SINGLETON_CLUSTERING, value);
	}

	//
	public SingletonClusteringType getSingletonClustering() {
		return (SingletonClusteringType)this.getValue(SINGLETON_CLUSTERING);
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
	public SingletonClusteringType newSingletonClusteringType() {
		return new SingletonClusteringType();
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
		// Validating property timerDescriptor
		if (getTimerDescriptor() != null) {
			getTimerDescriptor().validate();
		}
		// Validating property singletonClustering
		if (getSingletonClustering() != null) {
			getSingletonClustering().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("TimerDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTimerDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TIMER_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("SingletonClustering");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSingletonClustering();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SINGLETON_CLUSTERING, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SingletonSessionDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

