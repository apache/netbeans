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
 *	This generated bean class SafLoginContextType matches the schema element 'saf-login-context-type'.
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

public class SafLoginContextType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String LOGINURL = "LoginURL";	// NOI18N
	static public final String USERNAME = "Username";	// NOI18N
	static public final String PASSWORD_ENCRYPTED = "PasswordEncrypted";	// NOI18N

	public SafLoginContextType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SafLoginContextType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("loginURL", 	// NOI18N
			LOGINURL, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("username", 	// NOI18N
			USERNAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("password-encrypted", 	// NOI18N
			PASSWORD_ENCRYPTED, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setLoginURL(java.lang.String value) {
		this.setValue(LOGINURL, value);
	}

	//
	public java.lang.String getLoginURL() {
		return (java.lang.String)this.getValue(LOGINURL);
	}

	// This attribute is optional
	public void setUsername(java.lang.String value) {
		this.setValue(USERNAME, value);
	}

	//
	public java.lang.String getUsername() {
		return (java.lang.String)this.getValue(USERNAME);
	}

	// This attribute is optional
	public void setPasswordEncrypted(java.lang.String value) {
		this.setValue(PASSWORD_ENCRYPTED, value);
	}

	//
	public java.lang.String getPasswordEncrypted() {
		return (java.lang.String)this.getValue(PASSWORD_ENCRYPTED);
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
		// Validating property loginURL
		if (getLoginURL() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getLoginURL() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "loginURL", this);	// NOI18N
		}
		// Validating property username
		// Validating property passwordEncrypted
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("LoginURL");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLoginURL();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOGINURL, 0, str, indent);

		str.append(indent);
		str.append("Username");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUsername();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(USERNAME, 0, str, indent);

		str.append(indent);
		str.append("PasswordEncrypted");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPasswordEncrypted();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PASSWORD_ENCRYPTED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SafLoginContextType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

