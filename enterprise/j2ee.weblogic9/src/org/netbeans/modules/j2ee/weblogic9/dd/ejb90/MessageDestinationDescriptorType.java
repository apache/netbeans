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
 *	This generated bean class MessageDestinationDescriptorType matches the schema element 'message-destination-descriptorType'.
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

public class MessageDestinationDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String MESSAGE_DESTINATION_NAME = "MessageDestinationName";	// NOI18N
	static public final String MESSAGEDESTINATIONNAMEID = "MessageDestinationNameId";	// NOI18N
	static public final String DESTINATION_JNDI_NAME = "DestinationJndiName";	// NOI18N
	static public final String DESTINATIONJNDINAMEID = "DestinationJndiNameId";	// NOI18N
	static public final String INITIAL_CONTEXT_FACTORY = "InitialContextFactory";	// NOI18N
	static public final String INITIALCONTEXTFACTORYID = "InitialContextFactoryId";	// NOI18N
	static public final String PROVIDER_URL = "ProviderUrl";	// NOI18N
	static public final String PROVIDERURLID = "ProviderUrlId";	// NOI18N
	static public final String DESTINATION_RESOURCE_LINK = "DestinationResourceLink";	// NOI18N
	static public final String DESTINATIONRESOURCELINKJ2EEID = "DestinationResourceLinkJ2eeId";	// NOI18N

	public MessageDestinationDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MessageDestinationDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("message-destination-name", 	// NOI18N
			MESSAGE_DESTINATION_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(MESSAGE_DESTINATION_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("destination-jndi-name", 	// NOI18N
			DESTINATION_JNDI_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESTINATION_JNDI_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("initial-context-factory", 	// NOI18N
			INITIAL_CONTEXT_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(INITIAL_CONTEXT_FACTORY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("provider-url", 	// NOI18N
			PROVIDER_URL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PROVIDER_URL, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("destination-resource-link", 	// NOI18N
			DESTINATION_RESOURCE_LINK, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESTINATION_RESOURCE_LINK, "j2ee:id", "J2eeId", 
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
	public void setMessageDestinationName(java.lang.String value) {
		this.setValue(MESSAGE_DESTINATION_NAME, value);
	}

	//
	public java.lang.String getMessageDestinationName() {
		return (java.lang.String)this.getValue(MESSAGE_DESTINATION_NAME);
	}

	// This attribute is optional
	public void setMessageDestinationNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MESSAGE_DESTINATION_NAME) == 0) {
			setValue(MESSAGE_DESTINATION_NAME, "");
		}
		setAttributeValue(MESSAGE_DESTINATION_NAME, "Id", value);
	}

	//
	public java.lang.String getMessageDestinationNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MESSAGE_DESTINATION_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(MESSAGE_DESTINATION_NAME, "Id");
		}
	}

	// This attribute is mandatory
	public void setDestinationJndiName(java.lang.String value) {
		this.setValue(DESTINATION_JNDI_NAME, value);
	}

	//
	public java.lang.String getDestinationJndiName() {
		return (java.lang.String)this.getValue(DESTINATION_JNDI_NAME);
	}

	// This attribute is optional
	public void setDestinationJndiNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESTINATION_JNDI_NAME) == 0) {
			setValue(DESTINATION_JNDI_NAME, "");
		}
		setAttributeValue(DESTINATION_JNDI_NAME, "Id", value);
	}

	//
	public java.lang.String getDestinationJndiNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESTINATION_JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(DESTINATION_JNDI_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setInitialContextFactory(java.lang.String value) {
		this.setValue(INITIAL_CONTEXT_FACTORY, value);
	}

	//
	public java.lang.String getInitialContextFactory() {
		return (java.lang.String)this.getValue(INITIAL_CONTEXT_FACTORY);
	}

	// This attribute is optional
	public void setInitialContextFactoryId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CONTEXT_FACTORY) == 0) {
			setValue(INITIAL_CONTEXT_FACTORY, "");
		}
		setAttributeValue(INITIAL_CONTEXT_FACTORY, "Id", value);
	}

	//
	public java.lang.String getInitialContextFactoryId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CONTEXT_FACTORY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CONTEXT_FACTORY, "Id");
		}
	}

	// This attribute is optional
	public void setProviderUrl(java.lang.String value) {
		this.setValue(PROVIDER_URL, value);
	}

	//
	public java.lang.String getProviderUrl() {
		return (java.lang.String)this.getValue(PROVIDER_URL);
	}

	// This attribute is optional
	public void setProviderUrlId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PROVIDER_URL) == 0) {
			setValue(PROVIDER_URL, "");
		}
		setAttributeValue(PROVIDER_URL, "Id", value);
	}

	//
	public java.lang.String getProviderUrlId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PROVIDER_URL) == 0) {
			return null;
		} else {
			return getAttributeValue(PROVIDER_URL, "Id");
		}
	}

	// This attribute is mandatory
	public void setDestinationResourceLink(java.lang.String value) {
		this.setValue(DESTINATION_RESOURCE_LINK, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setDestinationJndiName(null);
			setInitialContextFactory(null);
			setProviderUrl(null);
		}
	}

	//
	public java.lang.String getDestinationResourceLink() {
		return (java.lang.String)this.getValue(DESTINATION_RESOURCE_LINK);
	}

	// This attribute is optional
	public void setDestinationResourceLinkJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESTINATION_RESOURCE_LINK) == 0) {
			setValue(DESTINATION_RESOURCE_LINK, "");
		}
		setAttributeValue(DESTINATION_RESOURCE_LINK, "J2eeId", value);
	}

	//
	public java.lang.String getDestinationResourceLinkJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESTINATION_RESOURCE_LINK) == 0) {
			return null;
		} else {
			return getAttributeValue(DESTINATION_RESOURCE_LINK, "J2eeId");
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
		// Validating property messageDestinationName
		if (getMessageDestinationName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMessageDestinationName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "messageDestinationName", this);	// NOI18N
		}
		// Validating property messageDestinationNameId
		if (getMessageDestinationNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMessageDestinationNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "messageDestinationNameId", this);	// NOI18N
			}
		}
		// Validating property destinationJndiName
		if (getDestinationJndiName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getDestinationJndiName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "destinationJndiName", this);	// NOI18N
		}
		// Validating property destinationJndiNameId
		if (getDestinationJndiNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDestinationJndiNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "destinationJndiNameId", this);	// NOI18N
			}
		}
		// Validating property initialContextFactory
		// Validating property initialContextFactoryId
		if (getInitialContextFactoryId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInitialContextFactoryId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initialContextFactoryId", this);	// NOI18N
			}
		}
		// Validating property providerUrl
		// Validating property providerUrlId
		if (getProviderUrlId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getProviderUrlId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "providerUrlId", this);	// NOI18N
			}
		}
		// Validating property destinationResourceLink
		if (getDestinationResourceLink() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDestinationResourceLink() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "destinationResourceLink", this);	// NOI18N
			}
		}
		if (getDestinationResourceLink() != null) {
			if (getDestinationJndiName() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: DestinationResourceLink and DestinationJndiName", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "DestinationJndiName", this);	// NOI18N
			}
			if (getInitialContextFactory() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: DestinationResourceLink and InitialContextFactory", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "InitialContextFactory", this);	// NOI18N
			}
			if (getProviderUrl() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: DestinationResourceLink and ProviderUrl", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "ProviderUrl", this);	// NOI18N
			}
		}
		// Validating property destinationResourceLinkJ2eeId
		if (getDestinationResourceLinkJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDestinationResourceLinkJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "destinationResourceLinkJ2eeId", this);	// NOI18N
			}
		}
		if (getDestinationJndiName() == null && getDestinationResourceLink() == null && getInitialContextFactory() == null && getProviderUrl() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getDestinationJndiName() == null && getDestinationResourceLink() == null && getInitialContextFactory() == null && getProviderUrl() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "DestinationResourceLink", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("MessageDestinationName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMessageDestinationName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MESSAGE_DESTINATION_NAME, 0, str, indent);

		str.append(indent);
		str.append("DestinationJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDestinationJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESTINATION_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("InitialContextFactory");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getInitialContextFactory();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INITIAL_CONTEXT_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("ProviderUrl");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getProviderUrl();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PROVIDER_URL, 0, str, indent);

		str.append(indent);
		str.append("DestinationResourceLink");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDestinationResourceLink();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESTINATION_RESOURCE_LINK, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MessageDestinationDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

