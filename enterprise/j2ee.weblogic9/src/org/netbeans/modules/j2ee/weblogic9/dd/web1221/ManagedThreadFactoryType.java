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
 *	This generated bean class ManagedThreadFactoryType matches the schema element 'managed-thread-factoryType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:05 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ManagedThreadFactoryType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String NAME = "Name";	// NOI18N
	static public final String NAMEID = "NameId";	// NOI18N
	static public final String MAX_CONCURRENT_NEW_THREADS = "MaxConcurrentNewThreads";	// NOI18N
	static public final String MAXCONCURRENTNEWTHREADSJ2EEID = "MaxConcurrentNewThreadsJ2eeId";	// NOI18N
	static public final String PRIORITY = "Priority";	// NOI18N
	static public final String PRIORITYJ2EEID = "PriorityJ2eeId";	// NOI18N

	public ManagedThreadFactoryType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ManagedThreadFactoryType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("max-concurrent-new-threads", 	// NOI18N
			MAX_CONCURRENT_NEW_THREADS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(MAX_CONCURRENT_NEW_THREADS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("priority", 	// NOI18N
			PRIORITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(PRIORITY, "j2ee:id", "J2eeId", 
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
	public void setName(java.lang.String value) {
		this.setValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return (java.lang.String)this.getValue(NAME);
	}

	// This attribute is optional
	public void setNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NAME) == 0) {
			setValue(NAME, "");
		}
		setAttributeValue(NAME, "Id", value);
	}

	//
	public java.lang.String getNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(NAME, "Id");
		}
	}

	// This attribute is optional
	public void setMaxConcurrentNewThreads(long value) {
		this.setValue(MAX_CONCURRENT_NEW_THREADS, java.lang.Long.valueOf(value));
	}

	//
	public long getMaxConcurrentNewThreads() {
		Long ret = (Long)this.getValue(MAX_CONCURRENT_NEW_THREADS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_CONCURRENT_NEW_THREADS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMaxConcurrentNewThreadsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_CONCURRENT_NEW_THREADS) == 0) {
			setValue(MAX_CONCURRENT_NEW_THREADS, "");
		}
		setAttributeValue(MAX_CONCURRENT_NEW_THREADS, "J2eeId", value);
	}

	//
	public java.lang.String getMaxConcurrentNewThreadsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_CONCURRENT_NEW_THREADS) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_CONCURRENT_NEW_THREADS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPriority(long value) {
		this.setValue(PRIORITY, java.lang.Long.valueOf(value));
	}

	//
	public long getPriority() {
		Long ret = (Long)this.getValue(PRIORITY);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"PRIORITY", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setPriorityJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MAX_CONCURRENT_NEW_THREADS) == 0) {
			setValue(MAX_CONCURRENT_NEW_THREADS, "");
		}
		setAttributeValue(MAX_CONCURRENT_NEW_THREADS, "J2eeId", value);
	}

	//
	public java.lang.String getPriorityJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MAX_CONCURRENT_NEW_THREADS) == 0) {
			return null;
		} else {
			return getAttributeValue(MAX_CONCURRENT_NEW_THREADS, "J2eeId");
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
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property nameId
		if (getNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "nameId", this);	// NOI18N
			}
		}
		// Validating property maxConcurrentNewThreads
		if (getMaxConcurrentNewThreads() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMaxConcurrentNewThreads() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxConcurrentNewThreads", this);	// NOI18N
		}
		// Validating property maxConcurrentNewThreadsJ2eeId
		if (getMaxConcurrentNewThreadsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxConcurrentNewThreadsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxConcurrentNewThreadsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property priority
		if (getPriority() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPriority() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "priority", this);	// NOI18N
		}
		// Validating property priorityJ2eeId
		if (getPriorityJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPriorityJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "priorityJ2eeId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		if (this.getValue(MAX_CONCURRENT_NEW_THREADS) != null) {
			str.append(indent);
			str.append("MaxConcurrentNewThreads");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxConcurrentNewThreads());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_CONCURRENT_NEW_THREADS, 0, str, indent);
		}

		if (this.getValue(PRIORITY) != null) {
			str.append(indent);
			str.append("Priority");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getPriority());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PRIORITY, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ManagedThreadFactoryType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

