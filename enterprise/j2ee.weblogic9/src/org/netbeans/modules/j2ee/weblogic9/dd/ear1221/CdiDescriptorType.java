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
 *	This generated bean class CdiDescriptorType matches the schema element 'cdi-descriptorType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:48 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class CdiDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String POJO_ANNOTATION_ENABLED = "PojoAnnotationEnabled";	// NOI18N
	static public final String POJOANNOTATIONENABLEDJ2EEID = "PojoAnnotationEnabledJ2eeId";	// NOI18N
	static public final String POJOANNOTATIONENABLEDIGNORESTUCKTHREADSJ2EEID2 = "PojoAnnotationEnabledIgnoreStuckThreadsJ2eeId2";	// NOI18N
	static public final String IMPLICIT_BEAN_DISCOVERY_ENABLED = "ImplicitBeanDiscoveryEnabled";	// NOI18N
	static public final String IMPLICITBEANDISCOVERYENABLEDJ2EEID = "ImplicitBeanDiscoveryEnabledJ2eeId";	// NOI18N
	static public final String IMPLICITBEANDISCOVERYENABLEDIGNORESTUCKTHREADSJ2EEID2 = "ImplicitBeanDiscoveryEnabledIgnoreStuckThreadsJ2eeId2";	// NOI18N

	public CdiDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public CdiDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("pojo-annotation-enabled", 	// NOI18N
			POJO_ANNOTATION_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(POJO_ANNOTATION_ENABLED, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(POJO_ANNOTATION_ENABLED, "j2ee:id", "IgnoreStuckThreadsJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("implicit-bean-discovery-enabled", 	// NOI18N
			IMPLICIT_BEAN_DISCOVERY_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(IMPLICIT_BEAN_DISCOVERY_ENABLED, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(IMPLICIT_BEAN_DISCOVERY_ENABLED, "j2ee:id", "IgnoreStuckThreadsJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setPojoAnnotationEnabled(boolean value) {
		this.setValue(POJO_ANNOTATION_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPojoAnnotationEnabled() {
		Boolean ret = (Boolean)this.getValue(POJO_ANNOTATION_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPojoAnnotationEnabledJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			setValue(POJO_ANNOTATION_ENABLED, "");
		}
		setAttributeValue(POJO_ANNOTATION_ENABLED, "J2eeId", value);
	}

	//
	public java.lang.String getPojoAnnotationEnabledJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			return null;
		} else {
			return getAttributeValue(POJO_ANNOTATION_ENABLED, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPojoAnnotationEnabledIgnoreStuckThreadsJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			setValue(POJO_ANNOTATION_ENABLED, "");
		}
		setAttributeValue(POJO_ANNOTATION_ENABLED, "IgnoreStuckThreadsJ2eeId2", value);
	}

	//
	public java.lang.String getPojoAnnotationEnabledIgnoreStuckThreadsJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			return null;
		} else {
			return getAttributeValue(POJO_ANNOTATION_ENABLED, "IgnoreStuckThreadsJ2eeId2");
		}
	}

	// This attribute is optional
	public void setImplicitBeanDiscoveryEnabled(boolean value) {
		this.setValue(IMPLICIT_BEAN_DISCOVERY_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isImplicitBeanDiscoveryEnabled() {
		Boolean ret = (Boolean)this.getValue(IMPLICIT_BEAN_DISCOVERY_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setImplicitBeanDiscoveryEnabledJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			setValue(POJO_ANNOTATION_ENABLED, "");
		}
		setAttributeValue(POJO_ANNOTATION_ENABLED, "J2eeId", value);
	}

	//
	public java.lang.String getImplicitBeanDiscoveryEnabledJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			return null;
		} else {
			return getAttributeValue(POJO_ANNOTATION_ENABLED, "J2eeId");
		}
	}

	// This attribute is optional
	public void setImplicitBeanDiscoveryEnabledIgnoreStuckThreadsJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			setValue(POJO_ANNOTATION_ENABLED, "");
		}
		setAttributeValue(POJO_ANNOTATION_ENABLED, "IgnoreStuckThreadsJ2eeId2", value);
	}

	//
	public java.lang.String getImplicitBeanDiscoveryEnabledIgnoreStuckThreadsJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(POJO_ANNOTATION_ENABLED) == 0) {
			return null;
		} else {
			return getAttributeValue(POJO_ANNOTATION_ENABLED, "IgnoreStuckThreadsJ2eeId2");
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
		// Validating property pojoAnnotationEnabled
		// Validating property pojoAnnotationEnabledJ2eeId
		if (getPojoAnnotationEnabledJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPojoAnnotationEnabledJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "pojoAnnotationEnabledJ2eeId", this);	// NOI18N
			}
		}
		// Validating property pojoAnnotationEnabledIgnoreStuckThreadsJ2eeId2
		if (getPojoAnnotationEnabledIgnoreStuckThreadsJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPojoAnnotationEnabledIgnoreStuckThreadsJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "pojoAnnotationEnabledIgnoreStuckThreadsJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property implicitBeanDiscoveryEnabled
		// Validating property implicitBeanDiscoveryEnabledJ2eeId
		if (getImplicitBeanDiscoveryEnabledJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getImplicitBeanDiscoveryEnabledJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "implicitBeanDiscoveryEnabledJ2eeId", this);	// NOI18N
			}
		}
		// Validating property implicitBeanDiscoveryEnabledIgnoreStuckThreadsJ2eeId2
		if (getImplicitBeanDiscoveryEnabledIgnoreStuckThreadsJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getImplicitBeanDiscoveryEnabledIgnoreStuckThreadsJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "implicitBeanDiscoveryEnabledIgnoreStuckThreadsJ2eeId2", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("PojoAnnotationEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPojoAnnotationEnabled()?"true":"false"));
		this.dumpAttributes(POJO_ANNOTATION_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("ImplicitBeanDiscoveryEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isImplicitBeanDiscoveryEnabled()?"true":"false"));
		this.dumpAttributes(IMPLICIT_BEAN_DISCOVERY_ENABLED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("CdiDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

