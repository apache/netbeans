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
 * ResourceAdapterConfig.java
 *
 * Created on November 21, 2004, 4:47 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface ResourceAdapterConfig {

        public static final String NAME = "Name";	// NOI18N
	public static final String THREADPOOLIDS = "ThreadPoolIds";	// NOI18N
	public static final String RESOURCEADAPTERNAME = "ResourceAdapterName";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        public static final String OBJECTTYPE = "ObjectType";	// NOI18N
        
        /** Setter for name property
        * @param value property value
        */
	public void setName(java.lang.String value);
        /** Getter for name property
        * @return property value
        */
	public java.lang.String getName();
        /** Setter for thread-pool-ids property
        * @param value property value
        */
	public void setThreadPoolIds(java.lang.String value);
        /** Getter for thread-pool-ids property
        * @return property value
        */
	public java.lang.String getThreadPoolIds();
        /** Setter for resource-adapter-name property
        * @param value property value
        */
	public void setResourceAdapterName(java.lang.String value);
        /** Getter for resource-adapter-name property
        * @return property value
        */
	public java.lang.String getResourceAdapterName();

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
