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
 *	This generated bean class ApplicationEntityCacheType matches the schema element 'application-entity-cacheType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:50 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ApplicationEntityCacheType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ENTITY_CACHE_NAME = "EntityCacheName";	// NOI18N
	static public final String MAX_BEANS_IN_CACHE = "MaxBeansInCache";	// NOI18N
	static public final String MAX_CACHE_SIZE = "MaxCacheSize";	// NOI18N
	static public final String MAX_QUERIES_IN_CACHE = "MaxQueriesInCache";	// NOI18N
	static public final String CACHING_STRATEGY = "CachingStrategy";	// NOI18N

	public ApplicationEntityCacheType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ApplicationEntityCacheType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("entity-cache-name", 	// NOI18N
			ENTITY_CACHE_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("max-beans-in-cache", 	// NOI18N
			MAX_BEANS_IN_CACHE, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("max-cache-size", 	// NOI18N
			MAX_CACHE_SIZE, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MaxCacheSizeType.class);
		this.createProperty("max-queries-in-cache", 	// NOI18N
			MAX_QUERIES_IN_CACHE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("caching-strategy", 	// NOI18N
			CACHING_STRATEGY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setEntityCacheName(java.lang.String value) {
		this.setValue(ENTITY_CACHE_NAME, value);
	}

	//
	public java.lang.String getEntityCacheName() {
		return (java.lang.String)this.getValue(ENTITY_CACHE_NAME);
	}

	// This attribute is mandatory
	public void setMaxBeansInCache(int value) {
		this.setValue(MAX_BEANS_IN_CACHE, java.lang.Integer.valueOf(value));
		if (value != 0) {
			// It's a mutually exclusive property.
			setMaxCacheSize(null);
		}
	}

	//
	public int getMaxBeansInCache() {
		Integer ret = (Integer)this.getValue(MAX_BEANS_IN_CACHE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_BEANS_IN_CACHE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is mandatory
	public void setMaxCacheSize(MaxCacheSizeType value) {
		this.setValue(MAX_CACHE_SIZE, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setMaxBeansInCache(0);
		}
	}

	//
	public MaxCacheSizeType getMaxCacheSize() {
		return (MaxCacheSizeType)this.getValue(MAX_CACHE_SIZE);
	}

	// This attribute is optional
	public void setMaxQueriesInCache(int value) {
		this.setValue(MAX_QUERIES_IN_CACHE, java.lang.Integer.valueOf(value));
	}

	//
	public int getMaxQueriesInCache() {
		Integer ret = (Integer)this.getValue(MAX_QUERIES_IN_CACHE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_QUERIES_IN_CACHE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setCachingStrategy(java.lang.String value) {
		this.setValue(CACHING_STRATEGY, value);
	}

	//
	public java.lang.String getCachingStrategy() {
		return (java.lang.String)this.getValue(CACHING_STRATEGY);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MaxCacheSizeType newMaxCacheSizeType() {
		return new MaxCacheSizeType();
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
		// Validating property entityCacheName
		if (getEntityCacheName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getEntityCacheName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "entityCacheName", this);	// NOI18N
		}
		// Validating property maxBeansInCache
		// Validating property maxCacheSize
		if (getMaxCacheSize() != null) {
			getMaxCacheSize().validate();
		}
		// Validating property maxQueriesInCache
		// Validating property cachingStrategy
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

		str.append(indent);
		str.append("MaxCacheSize");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMaxCacheSize();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MAX_CACHE_SIZE, 0, str, indent);

		if (this.getValue(MAX_QUERIES_IN_CACHE) != null) {
			str.append(indent);
			str.append("MaxQueriesInCache");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxQueriesInCache());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_QUERIES_IN_CACHE, 0, str, indent);
		}

		str.append(indent);
		str.append("CachingStrategy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCachingStrategy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CACHING_STRATEGY, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ApplicationEntityCacheType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

