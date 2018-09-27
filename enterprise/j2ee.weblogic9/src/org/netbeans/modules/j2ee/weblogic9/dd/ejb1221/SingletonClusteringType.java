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
 *	This generated bean class SingletonClusteringType matches the schema element 'singleton-clusteringType'.
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

public class SingletonClusteringType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String USE_SERVERSIDE_STUBS = "UseServersideStubs";	// NOI18N
	static public final String SINGLETON_BEAN_IS_CLUSTERABLE = "SingletonBeanIsClusterable";	// NOI18N
	static public final String SINGLETON_BEAN_LOAD_ALGORITHM = "SingletonBeanLoadAlgorithm";	// NOI18N
	static public final String SINGLETONBEANLOADALGORITHMID = "SingletonBeanLoadAlgorithmId";	// NOI18N
	static public final String SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME = "SingletonBeanCallRouterClassName";	// NOI18N
	static public final String SINGLETONBEANCALLROUTERCLASSNAMEID = "SingletonBeanCallRouterClassNameId";	// NOI18N

	public SingletonClusteringType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SingletonClusteringType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("use-serverside-stubs", 	// NOI18N
			USE_SERVERSIDE_STUBS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("singleton-bean-is-clusterable", 	// NOI18N
			SINGLETON_BEAN_IS_CLUSTERABLE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("singleton-bean-load-algorithm", 	// NOI18N
			SINGLETON_BEAN_LOAD_ALGORITHM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(SINGLETON_BEAN_LOAD_ALGORITHM, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("singleton-bean-call-router-class-name", 	// NOI18N
			SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME, "id", "Id", 
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
	public void setUseServersideStubs(boolean value) {
		this.setValue(USE_SERVERSIDE_STUBS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isUseServersideStubs() {
		Boolean ret = (Boolean)this.getValue(USE_SERVERSIDE_STUBS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setSingletonBeanIsClusterable(boolean value) {
		this.setValue(SINGLETON_BEAN_IS_CLUSTERABLE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isSingletonBeanIsClusterable() {
		Boolean ret = (Boolean)this.getValue(SINGLETON_BEAN_IS_CLUSTERABLE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setSingletonBeanLoadAlgorithm(java.lang.String value) {
		this.setValue(SINGLETON_BEAN_LOAD_ALGORITHM, value);
	}

	//
	public java.lang.String getSingletonBeanLoadAlgorithm() {
		return (java.lang.String)this.getValue(SINGLETON_BEAN_LOAD_ALGORITHM);
	}

	// This attribute is optional
	public void setSingletonBeanLoadAlgorithmId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(SINGLETON_BEAN_LOAD_ALGORITHM) == 0) {
			setValue(SINGLETON_BEAN_LOAD_ALGORITHM, "");
		}
		setAttributeValue(SINGLETON_BEAN_LOAD_ALGORITHM, "Id", value);
	}

	//
	public java.lang.String getSingletonBeanLoadAlgorithmId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(SINGLETON_BEAN_LOAD_ALGORITHM) == 0) {
			return null;
		} else {
			return getAttributeValue(SINGLETON_BEAN_LOAD_ALGORITHM, "Id");
		}
	}

	// This attribute is optional
	public void setSingletonBeanCallRouterClassName(java.lang.String value) {
		this.setValue(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME, value);
	}

	//
	public java.lang.String getSingletonBeanCallRouterClassName() {
		return (java.lang.String)this.getValue(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME);
	}

	// This attribute is optional
	public void setSingletonBeanCallRouterClassNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME) == 0) {
			setValue(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME, "");
		}
		setAttributeValue(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME, "Id", value);
	}

	//
	public java.lang.String getSingletonBeanCallRouterClassNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME, "Id");
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
		// Validating property useServersideStubs
		{
			boolean patternPassed = false;
			if ((isUseServersideStubs() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isUseServersideStubs()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "useServersideStubs", this);	// NOI18N
		}
		// Validating property singletonBeanIsClusterable
		{
			boolean patternPassed = false;
			if ((isSingletonBeanIsClusterable() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isSingletonBeanIsClusterable()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "singletonBeanIsClusterable", this);	// NOI18N
		}
		// Validating property singletonBeanLoadAlgorithm
		// Validating property singletonBeanLoadAlgorithmId
		if (getSingletonBeanLoadAlgorithmId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSingletonBeanLoadAlgorithmId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "singletonBeanLoadAlgorithmId", this);	// NOI18N
			}
		}
		// Validating property singletonBeanCallRouterClassName
		// Validating property singletonBeanCallRouterClassNameId
		if (getSingletonBeanCallRouterClassNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSingletonBeanCallRouterClassNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "singletonBeanCallRouterClassNameId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("UseServersideStubs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isUseServersideStubs()?"true":"false"));
		this.dumpAttributes(USE_SERVERSIDE_STUBS, 0, str, indent);

		str.append(indent);
		str.append("SingletonBeanIsClusterable");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isSingletonBeanIsClusterable()?"true":"false"));
		this.dumpAttributes(SINGLETON_BEAN_IS_CLUSTERABLE, 0, str, indent);

		str.append(indent);
		str.append("SingletonBeanLoadAlgorithm");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSingletonBeanLoadAlgorithm();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SINGLETON_BEAN_LOAD_ALGORITHM, 0, str, indent);

		str.append(indent);
		str.append("SingletonBeanCallRouterClassName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSingletonBeanCallRouterClassName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SINGLETON_BEAN_CALL_ROUTER_CLASS_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SingletonClusteringType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

