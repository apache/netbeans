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
 *	This generated bean class WorkManagerType matches the schema element 'work-managerType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:44 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class WorkManagerType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String NAME = "Name";	// NOI18N
	static public final String NAMEID = "NameId";	// NOI18N
	static public final String RESPONSE_TIME_REQUEST_CLASS = "ResponseTimeRequestClass";	// NOI18N
	static public final String FAIR_SHARE_REQUEST_CLASS = "FairShareRequestClass";	// NOI18N
	static public final String CONTEXT_REQUEST_CLASS = "ContextRequestClass";	// NOI18N
	static public final String REQUEST_CLASS_NAME = "RequestClassName";	// NOI18N
	static public final String REQUESTCLASSNAMEJ2EEID = "RequestClassNameJ2eeId";	// NOI18N
	static public final String REQUESTCLASSNAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "RequestClassNameComponentFactoryClassNameJ2eeId2";	// NOI18N
	static public final String MIN_THREADS_CONSTRAINT = "MinThreadsConstraint";	// NOI18N
	static public final String MIN_THREADS_CONSTRAINT_NAME = "MinThreadsConstraintName";	// NOI18N
	static public final String MINTHREADSCONSTRAINTNAMEJ2EEID = "MinThreadsConstraintNameJ2eeId";	// NOI18N
	static public final String MINTHREADSCONSTRAINTNAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "MinThreadsConstraintNameComponentFactoryClassNameJ2eeId2";	// NOI18N
	static public final String MAX_THREADS_CONSTRAINT = "MaxThreadsConstraint";	// NOI18N
	static public final String MAX_THREADS_CONSTRAINT_NAME = "MaxThreadsConstraintName";	// NOI18N
	static public final String MAXTHREADSCONSTRAINTNAMEJ2EEID = "MaxThreadsConstraintNameJ2eeId";	// NOI18N
	static public final String MAXTHREADSCONSTRAINTNAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "MaxThreadsConstraintNameComponentFactoryClassNameJ2eeId2";	// NOI18N
	static public final String CAPACITY = "Capacity";	// NOI18N
	static public final String CAPACITY_NAME = "CapacityName";	// NOI18N
	static public final String CAPACITYNAMEJ2EEID = "CapacityNameJ2eeId";	// NOI18N
	static public final String CAPACITYNAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "CapacityNameComponentFactoryClassNameJ2eeId2";	// NOI18N
	static public final String WORK_MANAGER_SHUTDOWN_TRIGGER = "WorkManagerShutdownTrigger";	// NOI18N
	static public final String IGNORE_STUCK_THREADS = "IgnoreStuckThreads";	// NOI18N
	static public final String IGNORESTUCKTHREADSJ2EEID = "IgnoreStuckThreadsJ2eeId";	// NOI18N
	static public final String IGNORESTUCKTHREADSJ2EEID2 = "IgnoreStuckThreadsJ2eeId2";	// NOI18N

	public WorkManagerType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public WorkManagerType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(13);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("response-time-request-class", 	// NOI18N
			RESPONSE_TIME_REQUEST_CLASS, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResponseTimeRequestClassType.class);
		this.createAttribute(RESPONSE_TIME_REQUEST_CLASS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("fair-share-request-class", 	// NOI18N
			FAIR_SHARE_REQUEST_CLASS, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FairShareRequestClassType.class);
		this.createAttribute(FAIR_SHARE_REQUEST_CLASS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("context-request-class", 	// NOI18N
			CONTEXT_REQUEST_CLASS, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ContextRequestClassType.class);
		this.createAttribute(CONTEXT_REQUEST_CLASS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("request-class-name", 	// NOI18N
			REQUEST_CLASS_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(REQUEST_CLASS_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(REQUEST_CLASS_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("min-threads-constraint", 	// NOI18N
			MIN_THREADS_CONSTRAINT, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MinThreadsConstraintType.class);
		this.createAttribute(MIN_THREADS_CONSTRAINT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("min-threads-constraint-name", 	// NOI18N
			MIN_THREADS_CONSTRAINT_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(MIN_THREADS_CONSTRAINT_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MIN_THREADS_CONSTRAINT_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("max-threads-constraint", 	// NOI18N
			MAX_THREADS_CONSTRAINT, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MaxThreadsConstraintType.class);
		this.createAttribute(MAX_THREADS_CONSTRAINT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("max-threads-constraint-name", 	// NOI18N
			MAX_THREADS_CONSTRAINT_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(MAX_THREADS_CONSTRAINT_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MAX_THREADS_CONSTRAINT_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("capacity", 	// NOI18N
			CAPACITY, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CapacityType.class);
		this.createAttribute(CAPACITY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("capacity-name", 	// NOI18N
			CAPACITY_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CAPACITY_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(CAPACITY_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("work-manager-shutdown-trigger", 	// NOI18N
			WORK_MANAGER_SHUTDOWN_TRIGGER, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WorkManagerShutdownTriggerType.class);
		this.createAttribute(WORK_MANAGER_SHUTDOWN_TRIGGER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("ignore-stuck-threads", 	// NOI18N
			IGNORE_STUCK_THREADS, Common.SEQUENCE_OR | 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(IGNORE_STUCK_THREADS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(IGNORE_STUCK_THREADS, "j2ee:id", "IgnoreStuckThreadsJ2eeId2", 
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

	// This attribute is mandatory
	public void setResponseTimeRequestClass(ResponseTimeRequestClassType value) {
		this.setValue(RESPONSE_TIME_REQUEST_CLASS, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setFairShareRequestClass(null);
			setContextRequestClass(null);
			setRequestClassName(null);
		}
	}

	//
	public ResponseTimeRequestClassType getResponseTimeRequestClass() {
		return (ResponseTimeRequestClassType)this.getValue(RESPONSE_TIME_REQUEST_CLASS);
	}

	// This attribute is mandatory
	public void setFairShareRequestClass(FairShareRequestClassType value) {
		this.setValue(FAIR_SHARE_REQUEST_CLASS, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setResponseTimeRequestClass(null);
			setContextRequestClass(null);
			setRequestClassName(null);
		}
	}

	//
	public FairShareRequestClassType getFairShareRequestClass() {
		return (FairShareRequestClassType)this.getValue(FAIR_SHARE_REQUEST_CLASS);
	}

	// This attribute is mandatory
	public void setContextRequestClass(ContextRequestClassType value) {
		this.setValue(CONTEXT_REQUEST_CLASS, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setResponseTimeRequestClass(null);
			setFairShareRequestClass(null);
			setRequestClassName(null);
		}
	}

	//
	public ContextRequestClassType getContextRequestClass() {
		return (ContextRequestClassType)this.getValue(CONTEXT_REQUEST_CLASS);
	}

	// This attribute is mandatory
	public void setRequestClassName(java.lang.String value) {
		this.setValue(REQUEST_CLASS_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setResponseTimeRequestClass(null);
			setFairShareRequestClass(null);
			setContextRequestClass(null);
		}
	}

	//
	public java.lang.String getRequestClassName() {
		return (java.lang.String)this.getValue(REQUEST_CLASS_NAME);
	}

	// This attribute is optional
	public void setRequestClassNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getRequestClassNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setRequestClassNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getRequestClassNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2");
		}
	}

	// This attribute is mandatory
	public void setMinThreadsConstraint(MinThreadsConstraintType value) {
		this.setValue(MIN_THREADS_CONSTRAINT, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setMinThreadsConstraintName(null);
		}
	}

	//
	public MinThreadsConstraintType getMinThreadsConstraint() {
		return (MinThreadsConstraintType)this.getValue(MIN_THREADS_CONSTRAINT);
	}

	// This attribute is mandatory
	public void setMinThreadsConstraintName(java.lang.String value) {
		this.setValue(MIN_THREADS_CONSTRAINT_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setMinThreadsConstraint(null);
		}
	}

	//
	public java.lang.String getMinThreadsConstraintName() {
		return (java.lang.String)this.getValue(MIN_THREADS_CONSTRAINT_NAME);
	}

	// This attribute is optional
	public void setMinThreadsConstraintNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getMinThreadsConstraintNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setMinThreadsConstraintNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getMinThreadsConstraintNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2");
		}
	}

	// This attribute is mandatory
	public void setMaxThreadsConstraint(MaxThreadsConstraintType value) {
		this.setValue(MAX_THREADS_CONSTRAINT, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setMaxThreadsConstraintName(null);
		}
	}

	//
	public MaxThreadsConstraintType getMaxThreadsConstraint() {
		return (MaxThreadsConstraintType)this.getValue(MAX_THREADS_CONSTRAINT);
	}

	// This attribute is mandatory
	public void setMaxThreadsConstraintName(java.lang.String value) {
		this.setValue(MAX_THREADS_CONSTRAINT_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setMaxThreadsConstraint(null);
		}
	}

	//
	public java.lang.String getMaxThreadsConstraintName() {
		return (java.lang.String)this.getValue(MAX_THREADS_CONSTRAINT_NAME);
	}

	// This attribute is optional
	public void setMaxThreadsConstraintNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getMaxThreadsConstraintNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setMaxThreadsConstraintNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getMaxThreadsConstraintNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2");
		}
	}

	// This attribute is mandatory
	public void setCapacity(CapacityType value) {
		this.setValue(CAPACITY, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setCapacityName(null);
		}
	}

	//
	public CapacityType getCapacity() {
		return (CapacityType)this.getValue(CAPACITY);
	}

	// This attribute is mandatory
	public void setCapacityName(java.lang.String value) {
		this.setValue(CAPACITY_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setCapacity(null);
		}
	}

	//
	public java.lang.String getCapacityName() {
		return (java.lang.String)this.getValue(CAPACITY_NAME);
	}

	// This attribute is optional
	public void setCapacityNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getCapacityNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setCapacityNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_CLASS_NAME) == 0) {
			setValue(REQUEST_CLASS_NAME, "");
		}
		setAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getCapacityNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2");
		}
	}

	// This attribute is mandatory
	public void setWorkManagerShutdownTrigger(WorkManagerShutdownTriggerType value) {
		this.setValue(WORK_MANAGER_SHUTDOWN_TRIGGER, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setIgnoreStuckThreads(false);
		}
	}

	//
	public WorkManagerShutdownTriggerType getWorkManagerShutdownTrigger() {
		return (WorkManagerShutdownTriggerType)this.getValue(WORK_MANAGER_SHUTDOWN_TRIGGER);
	}

	// This attribute is mandatory
	public void setIgnoreStuckThreads(boolean value) {
		this.setValue(IGNORE_STUCK_THREADS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
		if (value != false) {
			// It's a mutually exclusive property.
			setWorkManagerShutdownTrigger(null);
		}
	}

	//
	public boolean isIgnoreStuckThreads() {
		Boolean ret = (Boolean)this.getValue(IGNORE_STUCK_THREADS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setIgnoreStuckThreadsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IGNORE_STUCK_THREADS) == 0) {
			setValue(IGNORE_STUCK_THREADS, "");
		}
		setAttributeValue(IGNORE_STUCK_THREADS, "J2eeId", value);
	}

	//
	public java.lang.String getIgnoreStuckThreadsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IGNORE_STUCK_THREADS) == 0) {
			return null;
		} else {
			return getAttributeValue(IGNORE_STUCK_THREADS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setIgnoreStuckThreadsJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IGNORE_STUCK_THREADS) == 0) {
			setValue(IGNORE_STUCK_THREADS, "");
		}
		setAttributeValue(IGNORE_STUCK_THREADS, "IgnoreStuckThreadsJ2eeId2", value);
	}

	//
	public java.lang.String getIgnoreStuckThreadsJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IGNORE_STUCK_THREADS) == 0) {
			return null;
		} else {
			return getAttributeValue(IGNORE_STUCK_THREADS, "IgnoreStuckThreadsJ2eeId2");
		}
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResponseTimeRequestClassType newResponseTimeRequestClassType() {
		return new ResponseTimeRequestClassType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public FairShareRequestClassType newFairShareRequestClassType() {
		return new FairShareRequestClassType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ContextRequestClassType newContextRequestClassType() {
		return new ContextRequestClassType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MinThreadsConstraintType newMinThreadsConstraintType() {
		return new MinThreadsConstraintType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MaxThreadsConstraintType newMaxThreadsConstraintType() {
		return new MaxThreadsConstraintType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public CapacityType newCapacityType() {
		return new CapacityType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WorkManagerShutdownTriggerType newWorkManagerShutdownTriggerType() {
		return new WorkManagerShutdownTriggerType();
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
		// Validating property responseTimeRequestClass
		if (getResponseTimeRequestClass() != null) {
			getResponseTimeRequestClass().validate();
		}
		// Validating property fairShareRequestClass
		if (getFairShareRequestClass() != null) {
			getFairShareRequestClass().validate();
		}
		// Validating property contextRequestClass
		if (getContextRequestClass() != null) {
			getContextRequestClass().validate();
		}
		// Validating property requestClassName
		// Validating property requestClassNameJ2eeId
		if (getRequestClassNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRequestClassNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "requestClassNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property requestClassNameComponentFactoryClassNameJ2eeId2
		if (getRequestClassNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRequestClassNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "requestClassNameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property minThreadsConstraint
		if (getMinThreadsConstraint() != null) {
			getMinThreadsConstraint().validate();
		}
		// Validating property minThreadsConstraintName
		// Validating property minThreadsConstraintNameJ2eeId
		if (getMinThreadsConstraintNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMinThreadsConstraintNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "minThreadsConstraintNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property minThreadsConstraintNameComponentFactoryClassNameJ2eeId2
		if (getMinThreadsConstraintNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMinThreadsConstraintNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "minThreadsConstraintNameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property maxThreadsConstraint
		if (getMaxThreadsConstraint() != null) {
			getMaxThreadsConstraint().validate();
		}
		// Validating property maxThreadsConstraintName
		// Validating property maxThreadsConstraintNameJ2eeId
		if (getMaxThreadsConstraintNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxThreadsConstraintNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxThreadsConstraintNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property maxThreadsConstraintNameComponentFactoryClassNameJ2eeId2
		if (getMaxThreadsConstraintNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxThreadsConstraintNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxThreadsConstraintNameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property capacity
		if (getCapacity() != null) {
			getCapacity().validate();
		}
		// Validating property capacityName
		// Validating property capacityNameJ2eeId
		if (getCapacityNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCapacityNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "capacityNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property capacityNameComponentFactoryClassNameJ2eeId2
		if (getCapacityNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCapacityNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "capacityNameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property workManagerShutdownTrigger
		if (getWorkManagerShutdownTrigger() != null) {
			getWorkManagerShutdownTrigger().validate();
		}
		// Validating property ignoreStuckThreads
		// Validating property ignoreStuckThreadsJ2eeId
		if (getIgnoreStuckThreadsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIgnoreStuckThreadsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "ignoreStuckThreadsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property ignoreStuckThreadsJ2eeId2
		if (getIgnoreStuckThreadsJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIgnoreStuckThreadsJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "ignoreStuckThreadsJ2eeId2", this);	// NOI18N
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

		str.append(indent);
		str.append("ResponseTimeRequestClass");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getResponseTimeRequestClass();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(RESPONSE_TIME_REQUEST_CLASS, 0, str, indent);

		str.append(indent);
		str.append("FairShareRequestClass");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getFairShareRequestClass();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(FAIR_SHARE_REQUEST_CLASS, 0, str, indent);

		str.append(indent);
		str.append("ContextRequestClass");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getContextRequestClass();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CONTEXT_REQUEST_CLASS, 0, str, indent);

		str.append(indent);
		str.append("RequestClassName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRequestClassName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REQUEST_CLASS_NAME, 0, str, indent);

		str.append(indent);
		str.append("MinThreadsConstraint");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMinThreadsConstraint();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MIN_THREADS_CONSTRAINT, 0, str, indent);

		str.append(indent);
		str.append("MinThreadsConstraintName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMinThreadsConstraintName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MIN_THREADS_CONSTRAINT_NAME, 0, str, indent);

		str.append(indent);
		str.append("MaxThreadsConstraint");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMaxThreadsConstraint();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MAX_THREADS_CONSTRAINT, 0, str, indent);

		str.append(indent);
		str.append("MaxThreadsConstraintName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMaxThreadsConstraintName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MAX_THREADS_CONSTRAINT_NAME, 0, str, indent);

		str.append(indent);
		str.append("Capacity");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCapacity();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CAPACITY, 0, str, indent);

		str.append(indent);
		str.append("CapacityName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCapacityName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CAPACITY_NAME, 0, str, indent);

		str.append(indent);
		str.append("WorkManagerShutdownTrigger");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getWorkManagerShutdownTrigger();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(WORK_MANAGER_SHUTDOWN_TRIGGER, 0, str, indent);

		str.append(indent);
		str.append("IgnoreStuckThreads");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isIgnoreStuckThreads()?"true":"false"));
		this.dumpAttributes(IGNORE_STUCK_THREADS, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WorkManagerType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

