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
 *	This generated bean class ResourceEnvDescriptionType matches the schema element 'resource-env-descriptionType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:54 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ResourceEnvDescriptionType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String RESOURCE_ENV_REF_NAME = "ResourceEnvRefName";	// NOI18N
	static public final String JNDI_NAME = "JndiName";	// NOI18N
	static public final String JNDINAMEJ2EEID = "JndiNameJ2eeId";	// NOI18N
	static public final String JNDINAMEPRINCIPALNAMEJ2EEID2 = "JndiNamePrincipalNameJ2eeId2";	// NOI18N
	static public final String RESOURCE_LINK = "ResourceLink";	// NOI18N
	static public final String RESOURCELINKJ2EEID = "ResourceLinkJ2eeId";	// NOI18N
	static public final String RESOURCELINKPRINCIPALNAMEJ2EEID2 = "ResourceLinkPrincipalNameJ2eeId2";	// NOI18N

	public ResourceEnvDescriptionType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ResourceEnvDescriptionType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("resource-env-ref-name", 	// NOI18N
			RESOURCE_ENV_REF_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jndi-name", 	// NOI18N
			JNDI_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(JNDI_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JNDI_NAME, "j2ee:id", "PrincipalNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("resource-link", 	// NOI18N
			RESOURCE_LINK, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(RESOURCE_LINK, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(RESOURCE_LINK, "j2ee:id", "PrincipalNameJ2eeId2", 
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
	public void setResourceEnvRefName(java.lang.String value) {
		this.setValue(RESOURCE_ENV_REF_NAME, value);
	}

	//
	public java.lang.String getResourceEnvRefName() {
		return (java.lang.String)this.getValue(RESOURCE_ENV_REF_NAME);
	}

	// This attribute is mandatory
	public void setJndiName(java.lang.String value) {
		this.setValue(JNDI_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setResourceLink(null);
		}
	}

	//
	public java.lang.String getJndiName() {
		return (java.lang.String)this.getValue(JNDI_NAME);
	}

	// This attribute is optional
	public void setJndiNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JNDI_NAME) == 0) {
			setValue(JNDI_NAME, "");
		}
		setAttributeValue(JNDI_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getJndiNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(JNDI_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setJndiNamePrincipalNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JNDI_NAME) == 0) {
			setValue(JNDI_NAME, "");
		}
		setAttributeValue(JNDI_NAME, "PrincipalNameJ2eeId2", value);
	}

	//
	public java.lang.String getJndiNamePrincipalNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(JNDI_NAME, "PrincipalNameJ2eeId2");
		}
	}

	// This attribute is mandatory
	public void setResourceLink(java.lang.String value) {
		this.setValue(RESOURCE_LINK, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setJndiName(null);
		}
	}

	//
	public java.lang.String getResourceLink() {
		return (java.lang.String)this.getValue(RESOURCE_LINK);
	}

	// This attribute is optional
	public void setResourceLinkJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JNDI_NAME) == 0) {
			setValue(JNDI_NAME, "");
		}
		setAttributeValue(JNDI_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getResourceLinkJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(JNDI_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setResourceLinkPrincipalNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JNDI_NAME) == 0) {
			setValue(JNDI_NAME, "");
		}
		setAttributeValue(JNDI_NAME, "PrincipalNameJ2eeId2", value);
	}

	//
	public java.lang.String getResourceLinkPrincipalNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(JNDI_NAME, "PrincipalNameJ2eeId2");
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
		// Validating property resourceEnvRefName
		if (getResourceEnvRefName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getResourceEnvRefName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "resourceEnvRefName", this);	// NOI18N
		}
		// Validating property jndiName
		if (getJndiName() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJndiName() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jndiName", this);	// NOI18N
			}
		}
		if (getJndiName() != null) {
			if (getResourceLink() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: JndiName and ResourceLink", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "ResourceLink", this);	// NOI18N
			}
		}
		// Validating property jndiNameJ2eeId
		if (getJndiNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJndiNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jndiNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property jndiNamePrincipalNameJ2eeId2
		if (getJndiNamePrincipalNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJndiNamePrincipalNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jndiNamePrincipalNameJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property resourceLink
		if (getResourceLink() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getResourceLink() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "resourceLink", this);	// NOI18N
			}
		}
		if (getResourceLink() != null) {
			if (getJndiName() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: ResourceLink and JndiName", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "JndiName", this);	// NOI18N
			}
		}
		// Validating property resourceLinkJ2eeId
		if (getResourceLinkJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getResourceLinkJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "resourceLinkJ2eeId", this);	// NOI18N
			}
		}
		// Validating property resourceLinkPrincipalNameJ2eeId2
		if (getResourceLinkPrincipalNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getResourceLinkPrincipalNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "resourceLinkPrincipalNameJ2eeId2", this);	// NOI18N
			}
		}
		if (getJndiName() == null && getResourceLink() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getJndiName() == null && getResourceLink() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "ResourceLink", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ResourceEnvRefName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getResourceEnvRefName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RESOURCE_ENV_REF_NAME, 0, str, indent);

		str.append(indent);
		str.append("JndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("ResourceLink");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getResourceLink();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RESOURCE_LINK, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ResourceEnvDescriptionType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

