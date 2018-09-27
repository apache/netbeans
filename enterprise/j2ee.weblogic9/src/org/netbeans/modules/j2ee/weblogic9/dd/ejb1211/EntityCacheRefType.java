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
 *	This generated bean class EntityCacheRefType matches the schema element 'entity-cache-refType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:55 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class EntityCacheRefType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String ENTITY_CACHE_NAME = "EntityCacheName";	// NOI18N
	static public final String ENTITYCACHENAMEID = "EntityCacheNameId";	// NOI18N
	static public final String IDLE_TIMEOUT_SECONDS = "IdleTimeoutSeconds";	// NOI18N
	static public final String IDLETIMEOUTSECONDSJ2EEID = "IdleTimeoutSecondsJ2eeId";	// NOI18N
	static public final String IDLETIMEOUTSECONDSREMOTECLIENTTIMEOUTJ2EEID2 = "IdleTimeoutSecondsRemoteClientTimeoutJ2eeId2";	// NOI18N
	static public final String READ_TIMEOUT_SECONDS = "ReadTimeoutSeconds";	// NOI18N
	static public final String READTIMEOUTSECONDSJ2EEID = "ReadTimeoutSecondsJ2eeId";	// NOI18N
	static public final String READTIMEOUTSECONDSREMOTECLIENTTIMEOUTJ2EEID2 = "ReadTimeoutSecondsRemoteClientTimeoutJ2eeId2";	// NOI18N
	static public final String CONCURRENCY_STRATEGY = "ConcurrencyStrategy";	// NOI18N
	static public final String CONCURRENCYSTRATEGYID = "ConcurrencyStrategyId";	// NOI18N
	static public final String CACHE_BETWEEN_TRANSACTIONS = "CacheBetweenTransactions";	// NOI18N
	static public final String ESTIMATED_BEAN_SIZE = "EstimatedBeanSize";	// NOI18N
	static public final String ESTIMATEDBEANSIZEJ2EEID = "EstimatedBeanSizeJ2eeId";	// NOI18N
	static public final String ESTIMATEDBEANSIZEJ2EEID2 = "EstimatedBeanSizeJ2eeId2";	// NOI18N

	public EntityCacheRefType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EntityCacheRefType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("entity-cache-name", 	// NOI18N
			ENTITY_CACHE_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(ENTITY_CACHE_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("idle-timeout-seconds", 	// NOI18N
			IDLE_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(IDLE_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(IDLE_TIMEOUT_SECONDS, "j2ee:id", "RemoteClientTimeoutJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("read-timeout-seconds", 	// NOI18N
			READ_TIMEOUT_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(READ_TIMEOUT_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(READ_TIMEOUT_SECONDS, "j2ee:id", "RemoteClientTimeoutJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("concurrency-strategy", 	// NOI18N
			CONCURRENCY_STRATEGY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CONCURRENCY_STRATEGY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("cache-between-transactions", 	// NOI18N
			CACHE_BETWEEN_TRANSACTIONS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("estimated-bean-size", 	// NOI18N
			ESTIMATED_BEAN_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(ESTIMATED_BEAN_SIZE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(ESTIMATED_BEAN_SIZE, "j2ee:id", "EstimatedBeanSizeJ2eeId2", 
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

	// This attribute is mandatory
	public void setEntityCacheName(java.lang.String value) {
		this.setValue(ENTITY_CACHE_NAME, value);
	}

	//
	public java.lang.String getEntityCacheName() {
		return (java.lang.String)this.getValue(ENTITY_CACHE_NAME);
	}

	// This attribute is optional
	public void setEntityCacheNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(ENTITY_CACHE_NAME) == 0) {
			setValue(ENTITY_CACHE_NAME, "");
		}
		setAttributeValue(ENTITY_CACHE_NAME, "Id", value);
	}

	//
	public java.lang.String getEntityCacheNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(ENTITY_CACHE_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(ENTITY_CACHE_NAME, "Id");
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
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			setValue(IDLE_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(IDLE_TIMEOUT_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getIdleTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(IDLE_TIMEOUT_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setIdleTimeoutSecondsRemoteClientTimeoutJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			setValue(IDLE_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(IDLE_TIMEOUT_SECONDS, "RemoteClientTimeoutJ2eeId2", value);
	}

	//
	public java.lang.String getIdleTimeoutSecondsRemoteClientTimeoutJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(IDLE_TIMEOUT_SECONDS, "RemoteClientTimeoutJ2eeId2");
		}
	}

	// This attribute is optional
	public void setReadTimeoutSeconds(long value) {
		this.setValue(READ_TIMEOUT_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getReadTimeoutSeconds() {
		Long ret = (Long)this.getValue(READ_TIMEOUT_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"READ_TIMEOUT_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setReadTimeoutSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			setValue(IDLE_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(IDLE_TIMEOUT_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getReadTimeoutSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(IDLE_TIMEOUT_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setReadTimeoutSecondsRemoteClientTimeoutJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			setValue(IDLE_TIMEOUT_SECONDS, "");
		}
		setAttributeValue(IDLE_TIMEOUT_SECONDS, "RemoteClientTimeoutJ2eeId2", value);
	}

	//
	public java.lang.String getReadTimeoutSecondsRemoteClientTimeoutJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IDLE_TIMEOUT_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(IDLE_TIMEOUT_SECONDS, "RemoteClientTimeoutJ2eeId2");
		}
	}

	// This attribute is optional
	public void setConcurrencyStrategy(java.lang.String value) {
		this.setValue(CONCURRENCY_STRATEGY, value);
	}

	//
	public java.lang.String getConcurrencyStrategy() {
		return (java.lang.String)this.getValue(CONCURRENCY_STRATEGY);
	}

	// This attribute is optional
	public void setConcurrencyStrategyId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONCURRENCY_STRATEGY) == 0) {
			setValue(CONCURRENCY_STRATEGY, "");
		}
		setAttributeValue(CONCURRENCY_STRATEGY, "Id", value);
	}

	//
	public java.lang.String getConcurrencyStrategyId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONCURRENCY_STRATEGY) == 0) {
			return null;
		} else {
			return getAttributeValue(CONCURRENCY_STRATEGY, "Id");
		}
	}

	// This attribute is optional
	public void setCacheBetweenTransactions(boolean value) {
		this.setValue(CACHE_BETWEEN_TRANSACTIONS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCacheBetweenTransactions() {
		Boolean ret = (Boolean)this.getValue(CACHE_BETWEEN_TRANSACTIONS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setEstimatedBeanSize(long value) {
		this.setValue(ESTIMATED_BEAN_SIZE, java.lang.Long.valueOf(value));
	}

	//
	public long getEstimatedBeanSize() {
		Long ret = (Long)this.getValue(ESTIMATED_BEAN_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"ESTIMATED_BEAN_SIZE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setEstimatedBeanSizeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(ESTIMATED_BEAN_SIZE) == 0) {
			setValue(ESTIMATED_BEAN_SIZE, "");
		}
		setAttributeValue(ESTIMATED_BEAN_SIZE, "J2eeId", value);
	}

	//
	public java.lang.String getEstimatedBeanSizeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(ESTIMATED_BEAN_SIZE) == 0) {
			return null;
		} else {
			return getAttributeValue(ESTIMATED_BEAN_SIZE, "J2eeId");
		}
	}

	// This attribute is optional
	public void setEstimatedBeanSizeJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(ESTIMATED_BEAN_SIZE) == 0) {
			setValue(ESTIMATED_BEAN_SIZE, "");
		}
		setAttributeValue(ESTIMATED_BEAN_SIZE, "EstimatedBeanSizeJ2eeId2", value);
	}

	//
	public java.lang.String getEstimatedBeanSizeJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(ESTIMATED_BEAN_SIZE) == 0) {
			return null;
		} else {
			return getAttributeValue(ESTIMATED_BEAN_SIZE, "EstimatedBeanSizeJ2eeId2");
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
		// Validating property entityCacheName
		if (getEntityCacheName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getEntityCacheName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "entityCacheName", this);	// NOI18N
		}
		// Validating property entityCacheNameId
		if (getEntityCacheNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getEntityCacheNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "entityCacheNameId", this);	// NOI18N
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
		// Validating property idleTimeoutSecondsRemoteClientTimeoutJ2eeId2
		if (getIdleTimeoutSecondsRemoteClientTimeoutJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIdleTimeoutSecondsRemoteClientTimeoutJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "idleTimeoutSecondsRemoteClientTimeoutJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property readTimeoutSeconds
		if (getReadTimeoutSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getReadTimeoutSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "readTimeoutSeconds", this);	// NOI18N
		}
		// Validating property readTimeoutSecondsJ2eeId
		if (getReadTimeoutSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getReadTimeoutSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "readTimeoutSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property readTimeoutSecondsRemoteClientTimeoutJ2eeId2
		if (getReadTimeoutSecondsRemoteClientTimeoutJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getReadTimeoutSecondsRemoteClientTimeoutJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "readTimeoutSecondsRemoteClientTimeoutJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property concurrencyStrategy
		// Validating property concurrencyStrategyId
		if (getConcurrencyStrategyId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getConcurrencyStrategyId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "concurrencyStrategyId", this);	// NOI18N
			}
		}
		// Validating property cacheBetweenTransactions
		{
			boolean patternPassed = false;
			if ((isCacheBetweenTransactions() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCacheBetweenTransactions()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "cacheBetweenTransactions", this);	// NOI18N
		}
		// Validating property estimatedBeanSize
		if (getEstimatedBeanSize() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getEstimatedBeanSize() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "estimatedBeanSize", this);	// NOI18N
		}
		// Validating property estimatedBeanSizeJ2eeId
		if (getEstimatedBeanSizeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getEstimatedBeanSizeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "estimatedBeanSizeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property estimatedBeanSizeJ2eeId2
		if (getEstimatedBeanSizeJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getEstimatedBeanSizeJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "estimatedBeanSizeJ2eeId2", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("EntityCacheName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getEntityCacheName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENTITY_CACHE_NAME, 0, str, indent);

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

		if (this.getValue(READ_TIMEOUT_SECONDS) != null) {
			str.append(indent);
			str.append("ReadTimeoutSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getReadTimeoutSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(READ_TIMEOUT_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("ConcurrencyStrategy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConcurrencyStrategy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONCURRENCY_STRATEGY, 0, str, indent);

		str.append(indent);
		str.append("CacheBetweenTransactions");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCacheBetweenTransactions()?"true":"false"));
		this.dumpAttributes(CACHE_BETWEEN_TRANSACTIONS, 0, str, indent);

		if (this.getValue(ESTIMATED_BEAN_SIZE) != null) {
			str.append(indent);
			str.append("EstimatedBeanSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getEstimatedBeanSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(ESTIMATED_BEAN_SIZE, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EntityCacheRefType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

