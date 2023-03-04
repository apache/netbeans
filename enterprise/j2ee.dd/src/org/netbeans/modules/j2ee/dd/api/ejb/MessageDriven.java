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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface MessageDriven extends Ejb {

        public static final String MESSAGING_TYPE = "MessagingType";	// NOI18N
	public static final String TRANSACTION_TYPE = "TransactionType";	// NOI18N
	public static final String MESSAGE_DESTINATION_TYPE = "MessageDestinationType";	// NOI18N
	public static final String MESSAGE_DESTINATION_LINK = "MessageDestinationLink";	// NOI18N
	public static final String ACTIVATION_CONFIG = "ActivationConfig";	// NOI18N
        public static final String TRANSACTION_TYPE_BEAN = "Bean"; // NOI18N
        public static final String TRANSACTION_TYPE_CONTAINER = "Container"; // NOI18N
    
        public void setTransactionType(String value);

        public String getTransactionType();
        
        //2.1
        public void setMessagingType(String value) throws VersionNotSupportedException;

	public String getMessagingType() throws VersionNotSupportedException;
        
        public void setMessageDestinationType(String value) throws VersionNotSupportedException;

	public String getMessageDestinationType() throws VersionNotSupportedException;
                
        public void setMessageDestinationLink(String value) throws VersionNotSupportedException;

	public String getMessageDestinationLink() throws VersionNotSupportedException;
        
        public void setActivationConfig(ActivationConfig value) throws VersionNotSupportedException;

	public ActivationConfig getActivationConfig() throws VersionNotSupportedException;
        
        public ActivationConfig newActivationConfig() throws VersionNotSupportedException;

        // EJB 3.0
        
	void setMappedName(String value) throws VersionNotSupportedException;
	String getMappedName() throws VersionNotSupportedException;
	void setTimeoutMethod(NamedMethod valueInterface) throws VersionNotSupportedException;
	NamedMethod getTimeoutMethod() throws VersionNotSupportedException;
	void setAroundInvoke(int index, AroundInvoke valueInterface) throws VersionNotSupportedException;
	AroundInvoke getAroundInvoke(int index) throws VersionNotSupportedException;
	int sizeAroundInvoke() throws VersionNotSupportedException;
	void setAroundInvoke(AroundInvoke[] value) throws VersionNotSupportedException;
	AroundInvoke[] getAroundInvoke() throws VersionNotSupportedException;
	int addAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException;
	int removeAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException;
	void setPersistenceContextRef(int index, PersistenceContextRef valueInterface) throws VersionNotSupportedException;
	PersistenceContextRef getPersistenceContextRef(int index) throws VersionNotSupportedException;
	int sizePersistenceContextRef() throws VersionNotSupportedException;
	void setPersistenceContextRef(PersistenceContextRef[] value) throws VersionNotSupportedException;
	PersistenceContextRef[] getPersistenceContextRef() throws VersionNotSupportedException;
	int addPersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException;
	int removePersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException;
	void setPersistenceUnitRef(int index, PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
	PersistenceUnitRef getPersistenceUnitRef(int index) throws VersionNotSupportedException;
	int sizePersistenceUnitRef() throws VersionNotSupportedException;
	void setPersistenceUnitRef(PersistenceUnitRef[] value) throws VersionNotSupportedException;
	PersistenceUnitRef[] getPersistenceUnitRef() throws VersionNotSupportedException;
	int addPersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
	int removePersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
	void setPostConstruct(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
	LifecycleCallback getPostConstruct(int index) throws VersionNotSupportedException;
	int sizePostConstruct() throws VersionNotSupportedException;
	void setPostConstruct(LifecycleCallback[] value) throws VersionNotSupportedException;
	LifecycleCallback[] getPostConstruct() throws VersionNotSupportedException;
	int addPostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	int removePostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	void setPreDestroy(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
	LifecycleCallback getPreDestroy(int index) throws VersionNotSupportedException;
	int sizePreDestroy() throws VersionNotSupportedException;
	void setPreDestroy(LifecycleCallback[] value) throws VersionNotSupportedException;
	LifecycleCallback[] getPreDestroy() throws VersionNotSupportedException;
	int addPreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	int removePreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	NamedMethod newNamedMethod() throws VersionNotSupportedException;
	AroundInvoke newAroundInvoke() throws VersionNotSupportedException;
	PersistenceContextRef newPersistenceContextRef() throws VersionNotSupportedException;
	PersistenceUnitRef newPersistenceUnitRef() throws VersionNotSupportedException;
	LifecycleCallback newLifecycleCallback() throws VersionNotSupportedException;

        }
 
