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
 * Ejb.java
 *
 * Created on November 17, 2004, 4:45 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;


import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Ejb extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String EJB_NAME = "EjbName";	// NOI18N
    public static final String JNDI_NAME = "JndiName";	// NOI18N
    public static final String EJB_REF = "EjbRef";	// NOI18N
    public static final String RESOURCE_REF = "ResourceRef";	// NOI18N
    public static final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
    public static final String SERVICE_REF = "ServiceRef";	// NOI18N
    public static final String PASS_BY_REFERENCE = "PassByReference";	// NOI18N
    public static final String CMP = "Cmp";	// NOI18N
    public static final String PRINCIPAL = "Principal";	// NOI18N
    public static final String MDB_CONNECTION_FACTORY = "MdbConnectionFactory";	// NOI18N
    public static final String JMS_DURABLE_SUBSCRIPTION_NAME = "JmsDurableSubscriptionName";	// NOI18N
    public static final String JMS_MAX_MESSAGES_LOAD = "JmsMaxMessagesLoad";	// NOI18N
    public static final String IOR_SECURITY_CONFIG = "IorSecurityConfig";	// NOI18N
    public static final String IS_READ_ONLY_BEAN = "IsReadOnlyBean";	// NOI18N
    public static final String REFRESH_PERIOD_IN_SECONDS = "RefreshPeriodInSeconds";	// NOI18N
    public static final String COMMIT_OPTION = "CommitOption";	// NOI18N
    public static final String CMT_TIMEOUT_IN_SECONDS = "CmtTimeoutInSeconds";	// NOI18N
    public static final String GEN_CLASSES = "GenClasses";	// NOI18N
    public static final String BEAN_POOL = "BeanPool";	// NOI18N
    public static final String BEAN_CACHE = "BeanCache";	// NOI18N
    public static final String MDB_RESOURCE_ADAPTER = "MdbResourceAdapter";	// NOI18N
    public static final String WEBSERVICE_ENDPOINT = "WebserviceEndpoint";	// NOI18N
    public static final String FLUSH_AT_END_OF_METHOD = "FlushAtEndOfMethod";	// NOI18N
    public static final String CHECKPOINTED_METHODS = "CheckpointedMethods";	// NOI18N
    public static final String CHECKPOINT_AT_END_OF_METHOD = "CheckpointAtEndOfMethod";	// NOI18N
    public static final String AVAILABILITYENABLED = "AvailabilityEnabled";	// NOI18N
    public static final String MESSAGE_DESTINATION_REF = "MessageDestinationRef";	// NOI18N
    
    public void setEjbName(java.lang.String value);
    public java.lang.String getEjbName();
    
    public void setJndiName(java.lang.String value);
    public java.lang.String getJndiName();
    
    public EjbRef[] getEjbRef();
    public EjbRef getEjbRef(int index);
    public void setEjbRef(EjbRef[] value);
    public void setEjbRef(int index, EjbRef value);
    public int addEjbRef(EjbRef value);
    public int removeEjbRef(EjbRef value);
    public int sizeEjbRef();
    public EjbRef newEjbRef();
    
    public ResourceRef[] getResourceRef();
    public ResourceRef getResourceRef(int index);
    public void setResourceRef(ResourceRef[] value);
    public void setResourceRef(int index, ResourceRef value);
    public int addResourceRef(ResourceRef value);
    public int removeResourceRef(ResourceRef value);
    public int sizeResourceRef();
    public ResourceRef newResourceRef();
    
    public ResourceEnvRef[] getResourceEnvRef();
    public ResourceEnvRef getResourceEnvRef(int index);
    public void setResourceEnvRef(ResourceEnvRef[] value);
    public void setResourceEnvRef(int index, ResourceEnvRef value);
    public int addResourceEnvRef(ResourceEnvRef value);
    public int removeResourceEnvRef(ResourceEnvRef value);
    public int sizeResourceEnvRef();
    public ResourceEnvRef newResourceEnvRef();
    
    public ServiceRef[] getServiceRef();
    public ServiceRef getServiceRef(int index);
    public void setServiceRef(ServiceRef[] value);
    public void setServiceRef(int index, ServiceRef value);
    public int addServiceRef(ServiceRef value);
    public int removeServiceRef(ServiceRef value);
    public int sizeServiceRef();
    public ServiceRef newServiceRef();
    
    public void setPassByReference(java.lang.String value);
    public java.lang.String getPassByReference();
    
    public void setCmp(Cmp value);
    public Cmp getCmp(); 
    public Cmp newCmp(); 
    
    public void setPrincipal(Principal value); 
    public Principal getPrincipal(); 
    public Principal newPrincipal();
    
    public void setMdbConnectionFactory(MdbConnectionFactory value);  
    public MdbConnectionFactory getMdbConnectionFactory();
    public MdbConnectionFactory newMdbConnectionFactory();
    
    public void setJmsDurableSubscriptionName(java.lang.String value);
    public java.lang.String getJmsDurableSubscriptionName();
    
    public void setJmsMaxMessagesLoad(java.lang.String value);
    public java.lang.String getJmsMaxMessagesLoad();
    
    public void setIorSecurityConfig(IorSecurityConfig value); 
    public IorSecurityConfig getIorSecurityConfig();  
    public IorSecurityConfig newIorSecurityConfig();
    
    public void setIsReadOnlyBean(java.lang.String value);
    public java.lang.String getIsReadOnlyBean();
    
    public void setRefreshPeriodInSeconds(java.lang.String value);
    public java.lang.String getRefreshPeriodInSeconds();
    
    public void setCommitOption(java.lang.String value);
    public java.lang.String getCommitOption();
    
    public void setCmtTimeoutInSeconds(java.lang.String value);
    public java.lang.String getCmtTimeoutInSeconds();
    
    public void setUseThreadPoolId(String value);
    public String getUseThreadPoolId();
    
    public void setGenClasses(GenClasses value);
    public GenClasses getGenClasses(); 
    public GenClasses newGenClasses();
    
    public void setBeanPool(BeanPool value);
    public BeanPool getBeanPool();
    public BeanPool newBeanPool();
    
    public void setBeanCache(BeanCache value);
    public BeanCache getBeanCache(); 
    public BeanCache newBeanCache(); 
    
    public void setMdbResourceAdapter(MdbResourceAdapter value);
    public MdbResourceAdapter getMdbResourceAdapter();  
    public MdbResourceAdapter newMdbResourceAdapter();
    
    public WebserviceEndpoint[] getWebserviceEndpoint();
    public WebserviceEndpoint getWebserviceEndpoint(int index);
    public void setWebserviceEndpoint(WebserviceEndpoint[] value);
    public void setWebserviceEndpoint(int index, WebserviceEndpoint value);
    public int addWebserviceEndpoint(WebserviceEndpoint value);
    public int removeWebserviceEndpoint(WebserviceEndpoint value);
    public int sizeWebserviceEndpoint();
    public WebserviceEndpoint newWebserviceEndpoint(); 
 
    //AppServer 8.1
    /** Setter for flush-at-end-of-method property
     * @param value property value
     */
    public void setFlushAtEndOfMethod (FlushAtEndOfMethod  value) throws VersionNotSupportedException; 
    /** Getter for flush-at-end-of-method property.
     * @return property value
     */
    public FlushAtEndOfMethod getFlushAtEndOfMethod () throws VersionNotSupportedException; 
    /** Setter for checkpointed-methods property
     * @param value property value
     */
    
    public FlushAtEndOfMethod newFlushAtEndOfMethod() throws VersionNotSupportedException; 
    
    public void setCheckpointedMethods (java.lang.String  value) throws VersionNotSupportedException; 
    /** Getter for checkpointed-methods property.
     * @return property value
     */
    public java.lang.String getCheckpointedMethods () throws VersionNotSupportedException; 
    /** Setter for checkpoint-at-end-of-method property
     * @param value property value
     */
    public void setCheckpointAtEndOfMethod (CheckpointAtEndOfMethod value) throws VersionNotSupportedException; 
    /** Getter for checkpoint-at-end-of-method property.
     * @return property value
     */
    public CheckpointAtEndOfMethod getCheckpointAtEndOfMethod () throws VersionNotSupportedException;  
    
    public CheckpointAtEndOfMethod newCheckpointAtEndOfMethod() throws VersionNotSupportedException; 
    
    /** Setter for availability-enabled attribute
     * @param value attribute value
     */
    public void setAvailabilityEnabled(java.lang.String value) throws VersionNotSupportedException; 
    /** Getter for availability-enabled attribute.
     * @return attribute value
     */
    public java.lang.String getAvailabilityEnabled() throws VersionNotSupportedException; 
    
    //Required for ejb 3.0
    public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException; 
    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException; 
    public int sizeMessageDestinationRef() throws VersionNotSupportedException; 
    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException; 
    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException; 
    public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException; 
    public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException; 
    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;

}
