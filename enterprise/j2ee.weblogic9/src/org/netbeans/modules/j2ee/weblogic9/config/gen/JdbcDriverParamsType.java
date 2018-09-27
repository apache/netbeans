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
 *	This generated bean class JdbcDriverParamsType matches the schema element 'jdbc-driver-paramsType'.
 *  The root bean class is JdbcDataSource
 *
 *	Generated on Tue Jul 25 03:27:07 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.config.gen;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class JdbcDriverParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String URL = "Url";	// NOI18N
	static public final String DRIVER_NAME = "DriverName";	// NOI18N
	static public final String PROPERTIES = "Properties";	// NOI18N
	static public final String PASSWORD_ENCRYPTED = "PasswordEncrypted";	// NOI18N
	static public final String USE_XA_DATA_SOURCE_INTERFACE = "UseXaDataSourceInterface";	// NOI18N
	static public final String USE_PASSWORD_INDIRECTION = "UsePasswordIndirection";	// NOI18N

	public JdbcDriverParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JdbcDriverParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("url", 	// NOI18N
			URL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("driver-name", 	// NOI18N
			DRIVER_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("properties", 	// NOI18N
			PROPERTIES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcPropertiesType.class);
		this.createProperty("password-encrypted", 	// NOI18N
			PASSWORD_ENCRYPTED, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("use-xa-data-source-interface", 	// NOI18N
			USE_XA_DATA_SOURCE_INTERFACE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("use-password-indirection", 	// NOI18N
			USE_PASSWORD_INDIRECTION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

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
	public void setDriverName(java.lang.String value) {
		this.setValue(DRIVER_NAME, value);
	}

	//
	public java.lang.String getDriverName() {
		return (java.lang.String)this.getValue(DRIVER_NAME);
	}

	// This attribute is optional
	public void setProperties(JdbcPropertiesType value) {
		this.setValue(PROPERTIES, value);
	}

	//
	public JdbcPropertiesType getProperties() {
		return (JdbcPropertiesType)this.getValue(PROPERTIES);
	}

	// This attribute is optional
	public void setPasswordEncrypted(java.lang.String value) {
		this.setValue(PASSWORD_ENCRYPTED, value);
	}

	//
	public java.lang.String getPasswordEncrypted() {
		return (java.lang.String)this.getValue(PASSWORD_ENCRYPTED);
	}

	// This attribute is optional
	public void setUseXaDataSourceInterface(boolean value) {
		this.setValue(USE_XA_DATA_SOURCE_INTERFACE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isUseXaDataSourceInterface() {
		Boolean ret = (Boolean)this.getValue(USE_XA_DATA_SOURCE_INTERFACE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setUsePasswordIndirection(boolean value) {
		this.setValue(USE_PASSWORD_INDIRECTION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isUsePasswordIndirection() {
		Boolean ret = (Boolean)this.getValue(USE_PASSWORD_INDIRECTION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcPropertiesType newJdbcPropertiesType() {
		return new JdbcPropertiesType();
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
		// Validating property url
		// Validating property driverName
		// Validating property properties
		if (getProperties() != null) {
			getProperties().validate();
		}
		// Validating property passwordEncrypted
		// Validating property useXaDataSourceInterface
		{
			boolean patternPassed = false;
			if ((isUseXaDataSourceInterface() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isUseXaDataSourceInterface()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "useXaDataSourceInterface", this);	// NOI18N
		}
		// Validating property usePasswordIndirection
		{
			boolean patternPassed = false;
			if ((isUsePasswordIndirection() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isUsePasswordIndirection()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "usePasswordIndirection", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Url");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUrl();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(URL, 0, str, indent);

		str.append(indent);
		str.append("DriverName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDriverName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DRIVER_NAME, 0, str, indent);

		str.append(indent);
		str.append("Properties");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getProperties();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PROPERTIES, 0, str, indent);

		str.append(indent);
		str.append("PasswordEncrypted");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPasswordEncrypted();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PASSWORD_ENCRYPTED, 0, str, indent);

		str.append(indent);
		str.append("UseXaDataSourceInterface");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isUseXaDataSourceInterface()?"true":"false"));
		this.dumpAttributes(USE_XA_DATA_SOURCE_INTERFACE, 0, str, indent);

		str.append(indent);
		str.append("UsePasswordIndirection");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isUsePasswordIndirection()?"true":"false"));
		this.dumpAttributes(USE_PASSWORD_INDIRECTION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JdbcDriverParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

