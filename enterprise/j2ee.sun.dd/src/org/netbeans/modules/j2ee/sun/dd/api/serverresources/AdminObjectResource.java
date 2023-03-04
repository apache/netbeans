/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * AdminObjectResource.java
 *
 * Created on November 21, 2004, 2:33 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface AdminObjectResource {

        public static final String JNDINAME = "JndiName";	// NOI18N
	public static final String RESTYPE = "ResType";	// NOI18N
	public static final String RESADAPTER = "ResAdapter";	// NOI18N
	public static final String ENABLED = "Enabled";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        public static final String OBJECTTYPE = "ObjectType";	// NOI18N
        
	/** Setter for jndi-name property
        * @param value property value
        */
	public void setJndiName(java.lang.String value);
        /** Getter for jndi-name property
        * @return property value
        */
        public java.lang.String getJndiName();
        /** Setter for res-type property
        * @param value property value
        */
	public void setResType(java.lang.String value);
        /** Getter for res-type property
        * @param value property value
        */
	public java.lang.String getResType();
        /** Setter for res-adapter property
        * @param value property value
        */
	public void setResAdapter(java.lang.String value);
        /** Getter for res-adapter property
        * @param value property value
        */
	public java.lang.String getResAdapter();
        /** Setter for enabled property
        * @param value property value
        */
	public void setEnabled(java.lang.String value);
        /** Getter for enabled property
        * @param value property value
        */
	public java.lang.String getEnabled();
        /** Setter for description attribute
        * @param value attribute value
        */
	public void setDescription(String value);
        /** Getter for description attribute
        * @return attribute value
        */
	public String getDescription();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();
        
        //Resource 1.2
        /** Setter for object-type property
        * @param value property value
        */
        public void setObjectType(java.lang.String value);
        /** Getter for object-type attribute.
        * @return attribute value
        */
	public java.lang.String getObjectType();

}
