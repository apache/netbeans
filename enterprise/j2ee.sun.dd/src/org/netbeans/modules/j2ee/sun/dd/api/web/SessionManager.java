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
 * SessionManager.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface SessionManager extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String PERSISTENCETYPE = "PersistenceType";	// NOI18N
	public static final String MANAGER_PROPERTIES = "ManagerProperties";	// NOI18N
	public static final String STORE_PROPERTIES = "StoreProperties";	// NOI18N


        /** Setter for persistence-type attribute
         * @param value attribute value
         */
	public void setPersistenceType(java.lang.String value);
        /** Getter for persistence-type attribute.
         * @return attribute value
         */
	public java.lang.String getPersistenceType();
        /** Setter for manager-properties property
         * @param value property value
         */
	public void setManagerProperties(ManagerProperties value);
        /** Getter for manager-properties property.
         * @return property value
         */
	public ManagerProperties getManagerProperties();

	public ManagerProperties newManagerProperties();
        /** Setter for store-properties property
         * @param value property value
         */
	public void setStoreProperties(StoreProperties value);
        /** Getter for store-properties property.
         * @return property value
         */
	public StoreProperties getStoreProperties();

	public StoreProperties newStoreProperties(); 

}
