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
 *	This generated bean class PoolType matches the schema element 'poolType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:57 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PoolType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String MAX_BEANS_IN_FREE_POOL = "MaxBeansInFreePool";	// NOI18N
	static public final String MAXBEANSINFREEPOOLJ2EEID = "MaxBeansInFreePoolJ2eeId";	// NOI18N
	static public final String INITIAL_BEANS_IN_FREE_POOL = "InitialBeansInFreePool";	// NOI18N
	static public final String INITIALBEANSINFREEPOOLJ2EEID = "InitialBeansInFreePoolJ2eeId";	// NOI18N
	static public final String IDLE_TIMEOUT_SECONDS = "IdleTimeoutSeconds";	// NOI18N
	static public final String IDLETIMEOUTSECONDSJ2EEID = "IdleTimeoutSecondsJ2eeId";	// NOI18N

	public PoolType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PoolType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("max-beans-in-free-pool", 	// NOI18N
			MAX_BEANS_IN_FREE_POOL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(MAX_BEANS_IN_FREE_POOL, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("initial-beans-in-free-pool", 	// NOI18N
			INITIAL_BEANS_IN_FREE_POOL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(INITIAL_BEANS_IN_FREE_POOL, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("idle-timeout-seconds", 	// NOI18N
			IDLE_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(IDLE_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
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
	public void setMaxBeansInFreePool(long value) {
		this.setValue(MAX_BEANS_IN_FREE_POOL, java.lang.Long.valueOf(value));
	}

	//
	public long getMaxBeansInFreePool() {
		Long ret = (Long)this.getValue(MAX_BEANS_IN_FREE_POOL);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_BEANS_IN_FREE_POOL", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMaxBeansInFreePoolJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_BEANS_IN_FREE_POOL) == 0) {
			setValue(MAX_BEANS_IN_FREE_POOL, "");
		}
		setAttributeValue(MAX_BEANS_IN_FREE_POOL, "J2eeId", value);
	}

	//
	public java.lang.String getMaxBeansInFreePoolJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_BEANS_IN_FREE_POOL) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_BEANS_IN_FREE_POOL, "J2eeId");
		}
	}

	// This attribute is optional
	public void setInitialBeansInFreePool(long value) {
		this.setValue(INITIAL_BEANS_IN_FREE_POOL, java.lang.Long.valueOf(value));
	}

	//
	public long getInitialBeansInFreePool() {
		Long ret = (Long)this.getValue(INITIAL_BEANS_IN_FREE_POOL);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"INITIAL_BEANS_IN_FREE_POOL", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setInitialBeansInFreePoolJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_BEANS_IN_FREE_POOL) == 0) {
			setValue(MAX_BEANS_IN_FREE_POOL, "");
		}
		setAttributeValue(MAX_BEANS_IN_FREE_POOL, "J2eeId", value);
	}

	//
	public java.lang.String getInitialBeansInFreePoolJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_BEANS_IN_FREE_POOL) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_BEANS_IN_FREE_POOL, "J2eeId");
		}
	}

	// This attribute is optional
	public void setIdleTimeoutSeconds(long value) {
		this.setValue(IDLE_TIMEOUT_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getIdleTimeoutSeconds() {
		Long ret = (Long)this.getValue(IDLE_TIMEOUT_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"IDLE_TIMEOUT_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setIdleTimeoutSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_BEANS_IN_FREE_POOL) == 0) {
			setValue(MAX_BEANS_IN_FREE_POOL, "");
		}
		setAttributeValue(MAX_BEANS_IN_FREE_POOL, "J2eeId", value);
	}

	//
	public java.lang.String getIdleTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_BEANS_IN_FREE_POOL) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_BEANS_IN_FREE_POOL, "J2eeId");
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
		// Validating property maxBeansInFreePool
		if (getMaxBeansInFreePool() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMaxBeansInFreePool() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxBeansInFreePool", this);	// NOI18N
		}
		// Validating property maxBeansInFreePoolJ2eeId
		if (getMaxBeansInFreePoolJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxBeansInFreePoolJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxBeansInFreePoolJ2eeId", this);	// NOI18N
			}
		}
		// Validating property initialBeansInFreePool
		if (getInitialBeansInFreePool() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getInitialBeansInFreePool() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initialBeansInFreePool", this);	// NOI18N
		}
		// Validating property initialBeansInFreePoolJ2eeId
		if (getInitialBeansInFreePoolJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInitialBeansInFreePoolJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initialBeansInFreePoolJ2eeId", this);	// NOI18N
			}
		}
		// Validating property idleTimeoutSeconds
		if (getIdleTimeoutSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getIdleTimeoutSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "idleTimeoutSeconds", this);	// NOI18N
		}
		// Validating property idleTimeoutSecondsJ2eeId
		if (getIdleTimeoutSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIdleTimeoutSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "idleTimeoutSecondsJ2eeId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(MAX_BEANS_IN_FREE_POOL) != null) {
			str.append(indent);
			str.append("MaxBeansInFreePool");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxBeansInFreePool());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_BEANS_IN_FREE_POOL, 0, str, indent);
		}

		if (this.getValue(INITIAL_BEANS_IN_FREE_POOL) != null) {
			str.append(indent);
			str.append("InitialBeansInFreePool");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getInitialBeansInFreePool());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(INITIAL_BEANS_IN_FREE_POOL, 0, str, indent);
		}

		if (this.getValue(IDLE_TIMEOUT_SECONDS) != null) {
			str.append(indent);
			str.append("IdleTimeoutSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getIdleTimeoutSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(IDLE_TIMEOUT_SECONDS, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PoolType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

