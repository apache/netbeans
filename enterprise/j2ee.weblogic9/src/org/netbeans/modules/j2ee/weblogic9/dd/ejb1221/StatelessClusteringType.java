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
 *	This generated bean class StatelessClusteringType matches the schema element 'stateless-clusteringType'.
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

public class StatelessClusteringType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String HOME_IS_CLUSTERABLE = "HomeIsClusterable";	// NOI18N
	static public final String HOME_LOAD_ALGORITHM = "HomeLoadAlgorithm";	// NOI18N
	static public final String HOMELOADALGORITHMID = "HomeLoadAlgorithmId";	// NOI18N
	static public final String HOME_CALL_ROUTER_CLASS_NAME = "HomeCallRouterClassName";	// NOI18N
	static public final String HOMECALLROUTERCLASSNAMEID = "HomeCallRouterClassNameId";	// NOI18N
	static public final String USE_SERVERSIDE_STUBS = "UseServersideStubs";	// NOI18N
	static public final String STATELESS_BEAN_IS_CLUSTERABLE = "StatelessBeanIsClusterable";	// NOI18N
	static public final String STATELESS_BEAN_LOAD_ALGORITHM = "StatelessBeanLoadAlgorithm";	// NOI18N
	static public final String STATELESSBEANLOADALGORITHMID = "StatelessBeanLoadAlgorithmId";	// NOI18N
	static public final String STATELESS_BEAN_CALL_ROUTER_CLASS_NAME = "StatelessBeanCallRouterClassName";	// NOI18N
	static public final String STATELESSBEANCALLROUTERCLASSNAMEID = "StatelessBeanCallRouterClassNameId";	// NOI18N

	public StatelessClusteringType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public StatelessClusteringType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(7);
		this.createProperty("home-is-clusterable", 	// NOI18N
			HOME_IS_CLUSTERABLE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("home-load-algorithm", 	// NOI18N
			HOME_LOAD_ALGORITHM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(HOME_LOAD_ALGORITHM, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("home-call-router-class-name", 	// NOI18N
			HOME_CALL_ROUTER_CLASS_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(HOME_CALL_ROUTER_CLASS_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("use-serverside-stubs", 	// NOI18N
			USE_SERVERSIDE_STUBS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("stateless-bean-is-clusterable", 	// NOI18N
			STATELESS_BEAN_IS_CLUSTERABLE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("stateless-bean-load-algorithm", 	// NOI18N
			STATELESS_BEAN_LOAD_ALGORITHM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(STATELESS_BEAN_LOAD_ALGORITHM, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stateless-bean-call-router-class-name", 	// NOI18N
			STATELESS_BEAN_CALL_ROUTER_CLASS_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(STATELESS_BEAN_CALL_ROUTER_CLASS_NAME, "id", "Id", 
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
	public void setHomeIsClusterable(boolean value) {
		this.setValue(HOME_IS_CLUSTERABLE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isHomeIsClusterable() {
		Boolean ret = (Boolean)this.getValue(HOME_IS_CLUSTERABLE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setHomeLoadAlgorithm(java.lang.String value) {
		this.setValue(HOME_LOAD_ALGORITHM, value);
	}

	//
	public java.lang.String getHomeLoadAlgorithm() {
		return (java.lang.String)this.getValue(HOME_LOAD_ALGORITHM);
	}

	// This attribute is optional
	public void setHomeLoadAlgorithmId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(HOME_LOAD_ALGORITHM) == 0) {
			setValue(HOME_LOAD_ALGORITHM, "");
		}
		setAttributeValue(HOME_LOAD_ALGORITHM, "Id", value);
	}

	//
	public java.lang.String getHomeLoadAlgorithmId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(HOME_LOAD_ALGORITHM) == 0) {
			return null;
		} else {
			return getAttributeValue(HOME_LOAD_ALGORITHM, "Id");
		}
	}

	// This attribute is optional
	public void setHomeCallRouterClassName(java.lang.String value) {
		this.setValue(HOME_CALL_ROUTER_CLASS_NAME, value);
	}

	//
	public java.lang.String getHomeCallRouterClassName() {
		return (java.lang.String)this.getValue(HOME_CALL_ROUTER_CLASS_NAME);
	}

	// This attribute is optional
	public void setHomeCallRouterClassNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(HOME_CALL_ROUTER_CLASS_NAME) == 0) {
			setValue(HOME_CALL_ROUTER_CLASS_NAME, "");
		}
		setAttributeValue(HOME_CALL_ROUTER_CLASS_NAME, "Id", value);
	}

	//
	public java.lang.String getHomeCallRouterClassNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(HOME_CALL_ROUTER_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(HOME_CALL_ROUTER_CLASS_NAME, "Id");
		}
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
	public void setStatelessBeanIsClusterable(boolean value) {
		this.setValue(STATELESS_BEAN_IS_CLUSTERABLE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isStatelessBeanIsClusterable() {
		Boolean ret = (Boolean)this.getValue(STATELESS_BEAN_IS_CLUSTERABLE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setStatelessBeanLoadAlgorithm(java.lang.String value) {
		this.setValue(STATELESS_BEAN_LOAD_ALGORITHM, value);
	}

	//
	public java.lang.String getStatelessBeanLoadAlgorithm() {
		return (java.lang.String)this.getValue(STATELESS_BEAN_LOAD_ALGORITHM);
	}

	// This attribute is optional
	public void setStatelessBeanLoadAlgorithmId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(HOME_LOAD_ALGORITHM) == 0) {
			setValue(HOME_LOAD_ALGORITHM, "");
		}
		setAttributeValue(HOME_LOAD_ALGORITHM, "Id", value);
	}

	//
	public java.lang.String getStatelessBeanLoadAlgorithmId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(HOME_LOAD_ALGORITHM) == 0) {
			return null;
		} else {
			return getAttributeValue(HOME_LOAD_ALGORITHM, "Id");
		}
	}

	// This attribute is optional
	public void setStatelessBeanCallRouterClassName(java.lang.String value) {
		this.setValue(STATELESS_BEAN_CALL_ROUTER_CLASS_NAME, value);
	}

	//
	public java.lang.String getStatelessBeanCallRouterClassName() {
		return (java.lang.String)this.getValue(STATELESS_BEAN_CALL_ROUTER_CLASS_NAME);
	}

	// This attribute is optional
	public void setStatelessBeanCallRouterClassNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(HOME_CALL_ROUTER_CLASS_NAME) == 0) {
			setValue(HOME_CALL_ROUTER_CLASS_NAME, "");
		}
		setAttributeValue(HOME_CALL_ROUTER_CLASS_NAME, "Id", value);
	}

	//
	public java.lang.String getStatelessBeanCallRouterClassNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(HOME_CALL_ROUTER_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(HOME_CALL_ROUTER_CLASS_NAME, "Id");
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
		// Validating property homeIsClusterable
		{
			boolean patternPassed = false;
			if ((isHomeIsClusterable() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isHomeIsClusterable()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "homeIsClusterable", this);	// NOI18N
		}
		// Validating property homeLoadAlgorithm
		// Validating property homeLoadAlgorithmId
		if (getHomeLoadAlgorithmId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getHomeLoadAlgorithmId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "homeLoadAlgorithmId", this);	// NOI18N
			}
		}
		// Validating property homeCallRouterClassName
		// Validating property homeCallRouterClassNameId
		if (getHomeCallRouterClassNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getHomeCallRouterClassNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "homeCallRouterClassNameId", this);	// NOI18N
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
		// Validating property statelessBeanIsClusterable
		{
			boolean patternPassed = false;
			if ((isStatelessBeanIsClusterable() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isStatelessBeanIsClusterable()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "statelessBeanIsClusterable", this);	// NOI18N
		}
		// Validating property statelessBeanLoadAlgorithm
		// Validating property statelessBeanLoadAlgorithmId
		if (getStatelessBeanLoadAlgorithmId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStatelessBeanLoadAlgorithmId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "statelessBeanLoadAlgorithmId", this);	// NOI18N
			}
		}
		// Validating property statelessBeanCallRouterClassName
		// Validating property statelessBeanCallRouterClassNameId
		if (getStatelessBeanCallRouterClassNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStatelessBeanCallRouterClassNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "statelessBeanCallRouterClassNameId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("HomeIsClusterable");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isHomeIsClusterable()?"true":"false"));
		this.dumpAttributes(HOME_IS_CLUSTERABLE, 0, str, indent);

		str.append(indent);
		str.append("HomeLoadAlgorithm");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getHomeLoadAlgorithm();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(HOME_LOAD_ALGORITHM, 0, str, indent);

		str.append(indent);
		str.append("HomeCallRouterClassName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getHomeCallRouterClassName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(HOME_CALL_ROUTER_CLASS_NAME, 0, str, indent);

		str.append(indent);
		str.append("UseServersideStubs");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isUseServersideStubs()?"true":"false"));
		this.dumpAttributes(USE_SERVERSIDE_STUBS, 0, str, indent);

		str.append(indent);
		str.append("StatelessBeanIsClusterable");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isStatelessBeanIsClusterable()?"true":"false"));
		this.dumpAttributes(STATELESS_BEAN_IS_CLUSTERABLE, 0, str, indent);

		str.append(indent);
		str.append("StatelessBeanLoadAlgorithm");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getStatelessBeanLoadAlgorithm();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(STATELESS_BEAN_LOAD_ALGORITHM, 0, str, indent);

		str.append(indent);
		str.append("StatelessBeanCallRouterClassName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getStatelessBeanCallRouterClassName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(STATELESS_BEAN_CALL_ROUTER_CLASS_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("StatelessClusteringType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

