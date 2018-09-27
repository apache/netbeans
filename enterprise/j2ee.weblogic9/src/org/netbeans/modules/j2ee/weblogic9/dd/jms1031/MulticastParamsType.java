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
 *	This generated bean class MulticastParamsType matches the schema element 'multicast-params-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:59 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class MulticastParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String MULTICAST_ADDRESS = "MulticastAddress";	// NOI18N
	static public final String MULTICAST_PORT = "MulticastPort";	// NOI18N
	static public final String MULTICAST_TIME_TO_LIVE = "MulticastTimeToLive";	// NOI18N

	public MulticastParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MulticastParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("multicast-address", 	// NOI18N
			MULTICAST_ADDRESS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("multicast-port", 	// NOI18N
			MULTICAST_PORT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("multicast-time-to-live", 	// NOI18N
			MULTICAST_TIME_TO_LIVE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setMulticastAddress(java.lang.String value) {
		this.setValue(MULTICAST_ADDRESS, value);
	}

	//
	public java.lang.String getMulticastAddress() {
		return (java.lang.String)this.getValue(MULTICAST_ADDRESS);
	}

	// This attribute is optional
	public void setMulticastPort(int value) {
		this.setValue(MULTICAST_PORT, java.lang.Integer.valueOf(value));
	}

	//
	public int getMulticastPort() {
		Integer ret = (Integer)this.getValue(MULTICAST_PORT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MULTICAST_PORT", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setMulticastTimeToLive(int value) {
		this.setValue(MULTICAST_TIME_TO_LIVE, java.lang.Integer.valueOf(value));
	}

	//
	public int getMulticastTimeToLive() {
		Integer ret = (Integer)this.getValue(MULTICAST_TIME_TO_LIVE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MULTICAST_TIME_TO_LIVE", "int"}));
		return ((java.lang.Integer)ret).intValue();
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
		// Validating property multicastAddress
		// Validating property multicastPort
		// Validating property multicastTimeToLive
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("MulticastAddress");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMulticastAddress();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MULTICAST_ADDRESS, 0, str, indent);

		if (this.getValue(MULTICAST_PORT) != null) {
			str.append(indent);
			str.append("MulticastPort");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMulticastPort());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MULTICAST_PORT, 0, str, indent);
		}

		if (this.getValue(MULTICAST_TIME_TO_LIVE) != null) {
			str.append(indent);
			str.append("MulticastTimeToLive");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMulticastTimeToLive());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MULTICAST_TIME_TO_LIVE, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MulticastParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

