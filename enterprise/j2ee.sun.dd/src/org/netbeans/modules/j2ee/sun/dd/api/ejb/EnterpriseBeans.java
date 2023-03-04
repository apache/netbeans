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
 * EnterpriseBeans.java
 *
 * Created on November 17, 2004, 4:38 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface EnterpriseBeans extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String NAME = "Name";	// NOI18N
    public static final String UNIQUE_ID = "UniqueId";	// NOI18N
    public static final String EJB = "Ejb";	// NOI18N
    public static final String PM_DESCRIPTORS = "PmDescriptors";	// NOI18N
    public static final String CMP_RESOURCE = "CmpResource";	// NOI18N
    public static final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
    public static final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N
        
    public String getName();
    public void setName(String value);
    
    public String getUniqueId();
    public void setUniqueId(String value);
    
    public Ejb[] getEjb(); 
    public Ejb getEjb(int index);
    public void setEjb(Ejb[] value);
    public void setEjb(int index, Ejb value);
    public int addEjb(Ejb value);
    public int removeEjb(Ejb value);
    public int sizeEjb();
    public Ejb newEjb();
    
    public PmDescriptors getPmDescriptors();
    public void setPmDescriptors(PmDescriptors value);
    public PmDescriptors newPmDescriptors(); 
    
    public CmpResource getCmpResource();
    public void setCmpResource(CmpResource value); 
    public CmpResource newCmpResource();
    
    public MessageDestination[] getMessageDestination(); 
    public MessageDestination getMessageDestination(int index);
    public void setMessageDestination(MessageDestination[] value);
    public void setMessageDestination(int index, MessageDestination value);
    public int addMessageDestination(MessageDestination value);
    public int removeMessageDestination(MessageDestination value);
    public int sizeMessageDestination(); 
    public MessageDestination newMessageDestination();
    
    public WebserviceDescription[] getWebserviceDescription(); 
    public WebserviceDescription getWebserviceDescription(int index);
    public void setWebserviceDescription(WebserviceDescription[] value);
    public void setWebserviceDescription(int index, WebserviceDescription value);
    public int addWebserviceDescription(WebserviceDescription value);
    public int removeWebserviceDescription(WebserviceDescription value);
    public int sizeWebserviceDescription(); 
    public WebserviceDescription newWebserviceDescription();
    
    public void setPropertyElement(int index, PropertyElement value) throws VersionNotSupportedException;
    public PropertyElement getPropertyElement(int index) throws VersionNotSupportedException;
    public int sizePropertyElement() throws VersionNotSupportedException;
    public void setPropertyElement(PropertyElement[] value) throws VersionNotSupportedException;
    public PropertyElement[] getPropertyElement() throws VersionNotSupportedException;
    public int addPropertyElement(PropertyElement value) throws VersionNotSupportedException;
    public int removePropertyElement(PropertyElement value) throws VersionNotSupportedException;
    public PropertyElement newPropertyElement() throws VersionNotSupportedException;
    
}
