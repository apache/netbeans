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
 *	This generated bean class ConnectionPropertiesType matches the schema element 'connection-propertiesType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:50 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ConnectionPropertiesType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String USER_NAME = "UserName";	// NOI18N
	static public final String PASSWORD = "Password";	// NOI18N
	static public final String URL = "Url";	// NOI18N
	static public final String DRIVER_CLASS_NAME = "DriverClassName";	// NOI18N
	static public final String CONNECTION_PARAMS = "ConnectionParams";	// NOI18N

	public ConnectionPropertiesType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ConnectionPropertiesType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("user-name", 	// NOI18N
			USER_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("password", 	// NOI18N
			PASSWORD, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("url", 	// NOI18N
			URL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("driver-class-name", 	// NOI18N
			DRIVER_CLASS_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("connection-params", 	// NOI18N
			CONNECTION_PARAMS, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectionParamsType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setUserName(java.lang.String value) {
		this.setValue(USER_NAME, value);
	}

	//
	public java.lang.String getUserName() {
		return (java.lang.String)this.getValue(USER_NAME);
	}

	// This attribute is optional
	public void setPassword(java.lang.String value) {
		this.setValue(PASSWORD, value);
	}

	//
	public java.lang.String getPassword() {
		return (java.lang.String)this.getValue(PASSWORD);
	}

	// This attribute is optional
	public void setUrl(java.lang.String value) {
		this.setValue(URL, value);
	}

	//
	public java.lang.String getUrl() {
		return (java.lang.String)this.getValue(URL);
	}

	// This attribute is optional
	public void setDriverClassName(java.lang.String value) {
		this.setValue(DRIVER_CLASS_NAME, value);
	}

	//
	public java.lang.String getDriverClassName() {
		return (java.lang.String)this.getValue(DRIVER_CLASS_NAME);
	}

	// This attribute is an array, possibly empty
	public void setConnectionParams(int index, ConnectionParamsType value) {
		this.setValue(CONNECTION_PARAMS, index, value);
	}

	//
	public ConnectionParamsType getConnectionParams(int index) {
		return (ConnectionParamsType)this.getValue(CONNECTION_PARAMS, index);
	}

	// Return the number of properties
	public int sizeConnectionParams() {
		return this.size(CONNECTION_PARAMS);
	}

	// This attribute is an array, possibly empty
	public void setConnectionParams(ConnectionParamsType[] value) {
		this.setValue(CONNECTION_PARAMS, value);
	}

	//
	public ConnectionParamsType[] getConnectionParams() {
		return (ConnectionParamsType[])this.getValues(CONNECTION_PARAMS);
	}

	// Add a new element returning its index in the list
	public int addConnectionParams(org.netbeans.modules.j2ee.weblogic9.dd.ear90.ConnectionParamsType value) {
		int positionOfNewItem = this.addValue(CONNECTION_PARAMS, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeConnectionParams(org.netbeans.modules.j2ee.weblogic9.dd.ear90.ConnectionParamsType value) {
		return this.removeValue(CONNECTION_PARAMS, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectionParamsType newConnectionParamsType() {
		return new ConnectionParamsType();
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
		// Validating property userName
		// Validating property password
		// Validating property url
		// Validating property driverClassName
		// Validating property connectionParams
		for (int _index = 0; _index < sizeConnectionParams(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear90.ConnectionParamsType element = getConnectionParams(_index);
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
		str.append("UserName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUserName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(USER_NAME, 0, str, indent);

		str.append(indent);
		str.append("Password");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPassword();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PASSWORD, 0, str, indent);

		str.append(indent);
		str.append("Url");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUrl();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(URL, 0, str, indent);

		str.append(indent);
		str.append("DriverClassName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDriverClassName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DRIVER_CLASS_NAME, 0, str, indent);

		str.append(indent);
		str.append("ConnectionParams["+this.sizeConnectionParams()+"]");	// NOI18N
		for(int i=0; i<this.sizeConnectionParams(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectionParams(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONNECTION_PARAMS, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ConnectionPropertiesType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

