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
 *	This generated bean class StatefulSessionCacheType matches the schema element 'stateful-session-cacheType'.
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

public class StatefulSessionCacheType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String MAX_BEANS_IN_CACHE = "MaxBeansInCache";	// NOI18N
	static public final String MAXBEANSINCACHEJ2EEID = "MaxBeansInCacheJ2eeId";	// NOI18N
	static public final String IDLE_TIMEOUT_SECONDS = "IdleTimeoutSeconds";	// NOI18N
	static public final String IDLETIMEOUTSECONDSJ2EEID = "IdleTimeoutSecondsJ2eeId";	// NOI18N
	static public final String SESSION_TIMEOUT_SECONDS = "SessionTimeoutSeconds";	// NOI18N
	static public final String SESSIONTIMEOUTSECONDSJ2EEID = "SessionTimeoutSecondsJ2eeId";	// NOI18N
	static public final String CACHE_TYPE = "CacheType";	// NOI18N
	static public final String CACHETYPEID = "CacheTypeId";	// NOI18N

	public StatefulSessionCacheType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public StatefulSessionCacheType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("max-beans-in-cache", 	// NOI18N
			MAX_BEANS_IN_CACHE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(MAX_BEANS_IN_CACHE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("idle-timeout-seconds", 	// NOI18N
			IDLE_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(IDLE_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("session-timeout-seconds", 	// NOI18N
			SESSION_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(SESSION_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("cache-type", 	// NOI18N
			CACHE_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CACHE_TYPE, "id", "Id", 
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
	public void setMaxBeansInCache(long value) {
		this.setValue(MAX_BEANS_IN_CACHE, java.lang.Long.valueOf(value));
	}

	//
	public long getMaxBeansInCache() {
		Long ret = (Long)this.getValue(MAX_BEANS_IN_CACHE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_BEANS_IN_CACHE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMaxBeansInCacheJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_BEANS_IN_CACHE) == 0) {
			setValue(MAX_BEANS_IN_CACHE, "");
		}
		setAttributeValue(MAX_BEANS_IN_CACHE, "J2eeId", value);
	}

	//
	public java.lang.String getMaxBeansInCacheJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_BEANS_IN_CACHE) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_BEANS_IN_CACHE, "J2eeId");
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
		if (size(MAX_BEANS_IN_CACHE) == 0) {
			setValue(MAX_BEANS_IN_CACHE, "");
		}
		setAttributeValue(MAX_BEANS_IN_CACHE, "J2eeId", value);
	}

	//
	public java.lang.String getIdleTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_BEANS_IN_CACHE) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_BEANS_IN_CACHE, "J2eeId");
		}
	}

	// This attribute is optional
	public void setSessionTimeoutSeconds(long value) {
		this.setValue(SESSION_TIMEOUT_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getSessionTimeoutSeconds() {
		Long ret = (Long)this.getValue(SESSION_TIMEOUT_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"SESSION_TIMEOUT_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setSessionTimeoutSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_BEANS_IN_CACHE) == 0) {
			setValue(MAX_BEANS_IN_CACHE, "");
		}
		setAttributeValue(MAX_BEANS_IN_CACHE, "J2eeId", value);
	}

	//
	public java.lang.String getSessionTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_BEANS_IN_CACHE) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_BEANS_IN_CACHE, "J2eeId");
		}
	}

	// This attribute is optional
	public void setCacheType(java.lang.String value) {
		this.setValue(CACHE_TYPE, value);
	}

	//
	public java.lang.String getCacheType() {
		return (java.lang.String)this.getValue(CACHE_TYPE);
	}

	// This attribute is optional
	public void setCacheTypeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CACHE_TYPE) == 0) {
			setValue(CACHE_TYPE, "");
		}
		setAttributeValue(CACHE_TYPE, "Id", value);
	}

	//
	public java.lang.String getCacheTypeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CACHE_TYPE) == 0) {
			return null;
		} else {
			return getAttributeValue(CACHE_TYPE, "Id");
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
		// Validating property maxBeansInCache
		if (getMaxBeansInCache() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMaxBeansInCache() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxBeansInCache", this);	// NOI18N
		}
		// Validating property maxBeansInCacheJ2eeId
		if (getMaxBeansInCacheJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxBeansInCacheJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxBeansInCacheJ2eeId", this);	// NOI18N
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
		// Validating property sessionTimeoutSeconds
		if (getSessionTimeoutSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getSessionTimeoutSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "sessionTimeoutSeconds", this);	// NOI18N
		}
		// Validating property sessionTimeoutSecondsJ2eeId
		if (getSessionTimeoutSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSessionTimeoutSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "sessionTimeoutSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property cacheType
		// Validating property cacheTypeId
		if (getCacheTypeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCacheTypeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "cacheTypeId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(MAX_BEANS_IN_CACHE) != null) {
			str.append(indent);
			str.append("MaxBeansInCache");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxBeansInCache());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_BEANS_IN_CACHE, 0, str, indent);
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

		if (this.getValue(SESSION_TIMEOUT_SECONDS) != null) {
			str.append(indent);
			str.append("SessionTimeoutSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getSessionTimeoutSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SESSION_TIMEOUT_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("CacheType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCacheType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CACHE_TYPE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("StatefulSessionCacheType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

