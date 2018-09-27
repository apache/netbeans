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
 *	This generated bean class TransactionParamsType matches the schema element 'transaction-params-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:27:00 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class TransactionParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String TRANSACTION_TIMEOUT = "TransactionTimeout";	// NOI18N
	static public final String XA_CONNECTION_FACTORY_ENABLED = "XaConnectionFactoryEnabled";	// NOI18N

	public TransactionParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public TransactionParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("transaction-timeout", 	// NOI18N
			TRANSACTION_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("xa-connection-factory-enabled", 	// NOI18N
			XA_CONNECTION_FACTORY_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setTransactionTimeout(long value) {
		this.setValue(TRANSACTION_TIMEOUT, java.lang.Long.valueOf(value));
	}

	//
	public long getTransactionTimeout() {
		Long ret = (Long)this.getValue(TRANSACTION_TIMEOUT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"TRANSACTION_TIMEOUT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setXaConnectionFactoryEnabled(boolean value) {
		this.setValue(XA_CONNECTION_FACTORY_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isXaConnectionFactoryEnabled() {
		Boolean ret = (Boolean)this.getValue(XA_CONNECTION_FACTORY_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
		// Validating property transactionTimeout
		// Validating property xaConnectionFactoryEnabled
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(TRANSACTION_TIMEOUT) != null) {
			str.append(indent);
			str.append("TransactionTimeout");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getTransactionTimeout());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(TRANSACTION_TIMEOUT, 0, str, indent);
		}

		str.append(indent);
		str.append("XaConnectionFactoryEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isXaConnectionFactoryEnabled()?"true":"false"));
		this.dumpAttributes(XA_CONNECTION_FACTORY_ENABLED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("TransactionParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

