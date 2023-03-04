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

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.ComponentInterface;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface Ejb extends CommonDDBean, ComponentInterface {
    
        //Entity & Session & Message Driven
        public static final String EJB_NAME = "EjbName";	// NOI18N
        public static final String EJB_CLASS = "EjbClass";	// NOI18N
        public static final String ENV_ENTRY = "EnvEntry";	// NOI18N
        public static final String EJB_REF = "EjbRef";	// NOI18N
	public static final String EJB_LOCAL_REF = "EjbLocalRef";	// NOI18N
        public static final String SERVICE_REF = "ServiceRef";	// NOI18N
	public static final String RESOURCE_REF = "ResourceRef";	// NOI18N
	public static final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
        public static final String MESSAGE_DESTINATION_REF = "MessageDestinationRef";	// NOI18N
        public static final String SECURITY_IDENTITY = "SecurityIdentity";	// NOI18N
        
        public EjbJar getRoot();

        public String getEjbName();
        
        public void setEjbName(String value);
        
        public String getEjbClass();
        
        public void setEjbClass(String value);
        
        public void setEnvEntry(int index, EnvEntry value);
        
        public EnvEntry getEnvEntry(int index);
        
        public void setEnvEntry(EnvEntry[] value);
        
        public EnvEntry[] getEnvEntry();
        
        public int addEnvEntry(EnvEntry value);
        
        public int removeEnvEntry(EnvEntry value);
        
        public int sizeEnvEntry();
        
        public EnvEntry newEnvEntry();
        
        public void setEjbRef(int index, EjbRef value);
        
        public EjbRef getEjbRef(int index);
        
        public void setEjbRef(EjbRef[] value);
        
        public EjbRef[] getEjbRef();
        
        public int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef value);
        
        public int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef value);
        
        public int sizeEjbRef();
        
        public EjbRef newEjbRef();
        
        public void setEjbLocalRef(int index, EjbLocalRef value);
        
        public EjbLocalRef getEjbLocalRef(int index);
        
        public void setEjbLocalRef(EjbLocalRef[] value);
        
        public EjbLocalRef[] getEjbLocalRef();
        
        public int addEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef value);
        
        public int removeEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef value);
        
        public int sizeEjbLocalRef();
                
        public EjbLocalRef newEjbLocalRef();
        
        public SecurityIdentity getSecurityIdentity ();
        
        public void setSecurityIdentity (SecurityIdentity value);
        
        public SecurityIdentity newSecurityIdentity();
        
        public void setResourceRef(int index, ResourceRef value);
        
        public ResourceRef getResourceRef(int index);
        
        public void setResourceRef(ResourceRef[] value);
        
        public ResourceRef[] getResourceRef();
        
        public int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef value);

	public int sizeResourceRef();
        
        public int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef value);
        
        public ResourceRef newResourceRef();
        
        public void setResourceEnvRef(int index, ResourceEnvRef value);
        
        public ResourceEnvRef getResourceEnvRef(int index);
        
        public void setResourceEnvRef(ResourceEnvRef[] value);
        
        public ResourceEnvRef[] getResourceEnvRef();
        
        public int sizeResourceEnvRef();
        
        public int addResourceEnvRef(ResourceEnvRef value);

	public int removeResourceEnvRef(ResourceEnvRef value);
        
        public ResourceEnvRef newResourceEnvRef();
        
        //2.1
        public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException;

        public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;

        public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException;

        public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
        
        public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;

	public int sizeMessageDestinationRef() throws VersionNotSupportedException;
        
        public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;

        public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;
        
        public void setServiceRef(int index, ServiceRef value) throws VersionNotSupportedException;

        public ServiceRef getServiceRef(int index) throws VersionNotSupportedException;

        public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException;

        public ServiceRef[] getServiceRef() throws VersionNotSupportedException;
        
        public int removeServiceRef(ServiceRef value) throws VersionNotSupportedException;

        public int sizeServiceRef() throws VersionNotSupportedException;

	public int addServiceRef(ServiceRef value) throws VersionNotSupportedException;

        public ServiceRef newServiceRef() throws VersionNotSupportedException;
         
}

