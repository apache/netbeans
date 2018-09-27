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
 *	This generated bean class JdbcConnectionPoolType matches the schema element 'jdbc-connection-poolType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:46 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class JdbcConnectionPoolType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String DATA_SOURCE_JNDI_NAME = "DataSourceJndiName";	// NOI18N
	static public final String CONNECTION_FACTORY = "ConnectionFactory";	// NOI18N
	static public final String POOL_PARAMS = "PoolParams";	// NOI18N
	static public final String DRIVER_PARAMS = "DriverParams";	// NOI18N
	static public final String ACL_NAME = "AclName";	// NOI18N

	public JdbcConnectionPoolType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JdbcConnectionPoolType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("data-source-jndi-name", 	// NOI18N
			DATA_SOURCE_JNDI_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("connection-factory", 	// NOI18N
			CONNECTION_FACTORY, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectionFactoryType.class);
		this.createProperty("pool-params", 	// NOI18N
			POOL_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ApplicationPoolParamsType.class);
		this.createProperty("driver-params", 	// NOI18N
			DRIVER_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DriverParamsType.class);
		this.createProperty("acl-name", 	// NOI18N
			ACL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setDataSourceJndiName(java.lang.String value) {
		this.setValue(DATA_SOURCE_JNDI_NAME, value);
	}

	//
	public java.lang.String getDataSourceJndiName() {
		return (java.lang.String)this.getValue(DATA_SOURCE_JNDI_NAME);
	}

	// This attribute is mandatory
	public void setConnectionFactory(ConnectionFactoryType value) {
		this.setValue(CONNECTION_FACTORY, value);
	}

	//
	public ConnectionFactoryType getConnectionFactory() {
		return (ConnectionFactoryType)this.getValue(CONNECTION_FACTORY);
	}

	// This attribute is optional
	public void setPoolParams(ApplicationPoolParamsType value) {
		this.setValue(POOL_PARAMS, value);
	}

	//
	public ApplicationPoolParamsType getPoolParams() {
		return (ApplicationPoolParamsType)this.getValue(POOL_PARAMS);
	}

	// This attribute is optional
	public void setDriverParams(DriverParamsType value) {
		this.setValue(DRIVER_PARAMS, value);
	}

	//
	public DriverParamsType getDriverParams() {
		return (DriverParamsType)this.getValue(DRIVER_PARAMS);
	}

	// This attribute is optional
	public void setAclName(java.lang.String value) {
		this.setValue(ACL_NAME, value);
	}

	//
	public java.lang.String getAclName() {
		return (java.lang.String)this.getValue(ACL_NAME);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectionFactoryType newConnectionFactoryType() {
		return new ConnectionFactoryType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ApplicationPoolParamsType newApplicationPoolParamsType() {
		return new ApplicationPoolParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DriverParamsType newDriverParamsType() {
		return new DriverParamsType();
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
		// Validating property dataSourceJndiName
		if (getDataSourceJndiName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getDataSourceJndiName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "dataSourceJndiName", this);	// NOI18N
		}
		// Validating property connectionFactory
		if (getConnectionFactory() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getConnectionFactory() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "connectionFactory", this);	// NOI18N
		}
		getConnectionFactory().validate();
		// Validating property poolParams
		if (getPoolParams() != null) {
			getPoolParams().validate();
		}
		// Validating property driverParams
		if (getDriverParams() != null) {
			getDriverParams().validate();
		}
		// Validating property aclName
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("DataSourceJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDataSourceJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DATA_SOURCE_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("ConnectionFactory");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectionFactory();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CONNECTION_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("PoolParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPoolParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(POOL_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("DriverParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getDriverParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(DRIVER_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("AclName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getAclName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ACL_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JdbcConnectionPoolType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

