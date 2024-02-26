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
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */
package org.netbeans.modules.j2ee.dd.api.client;

import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 *
 * @author jungi
 */
public interface AppClient extends RootInterface {
    
    public static final String PROPERTY_VERSION="dd_version"; //NOI18N
    public static final String VERSION_1_4="1.4"; //NOI18N
    public static final String VERSION_5_0="5"; //NOI18N
    public static final String VERSION_6_0="6"; //NOI18N

    /**
     * application-client.xml DD version for JavaEE7
     * @since 1.29
     */
    public static final String VERSION_7_0 = "7"; //NOI18N
    public static final String VERSION_8_0 = "8"; //NOI18N
    public static final String VERSION_9_0 = "9"; //NOI18N
    public static final String VERSION_10_0 = "10"; //NOI18N
    public static final String VERSION_11_0 = "11"; //NOI18N
    public static final int STATE_VALID=0;
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;
    public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
    int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);
    
    int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry valueInterface);
    
    int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon valueInterface) throws VersionNotSupportedException;
    
    int addMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination valueInterface) throws VersionNotSupportedException;
    
    int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    
    int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef valueInterface);
    
    int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);
    
    int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef valueInterface) throws VersionNotSupportedException;
    
    java.lang.String getCallbackHandler();
    
    org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef();
    
    org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int index);
    
    org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry();
    
    org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int index);
    
    org.xml.sax.SAXParseException getError();
    
    org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int index) throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] getMessageDestination() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestination getMessageDestination(int index) throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef();
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int index);
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef();
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int index);
    
    org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int index) throws VersionNotSupportedException;
    
    int getStatus();
    
    int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);
    
    int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry valueInterface);
    
    int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon valueInterface) throws VersionNotSupportedException;
    
    int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination valueInterface) throws VersionNotSupportedException;
    
    int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    
    int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef valueInterface);
    
    int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);
    
    int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef valueInterface) throws VersionNotSupportedException;
    
    void setCallbackHandler(java.lang.String value);
    
    void setEjbRef(int index, org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);
    
    void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[] value);
    
    void setEnvEntry(int index, org.netbeans.modules.j2ee.dd.api.common.EnvEntry valueInterface);
    
    void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] value);
    
    void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon valueInterface) throws VersionNotSupportedException;
    
    void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] value) throws VersionNotSupportedException;
    
    void setMessageDestination(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestination valueInterface) throws VersionNotSupportedException;
    
    void setMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] value) throws VersionNotSupportedException;
    
    void setMessageDestinationRef(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    
    void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] value) throws VersionNotSupportedException;
    
    void setResourceEnvRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef valueInterface);
    
    void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] value);
    
    void setResourceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);
    
    void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] value);
    
    void setServiceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ServiceRef valueInterface) throws VersionNotSupportedException;
    
    void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] value) throws VersionNotSupportedException;
    
    int sizeEjbRef();
    
    int sizeEnvEntry();
    
    int sizeIcon() throws VersionNotSupportedException;
    
    int sizeMessageDestination() throws VersionNotSupportedException;
    
    int sizeMessageDestinationRef() throws VersionNotSupportedException;
    
    int sizeResourceEnvRef();
    
    int sizeResourceRef();
    
    int sizeServiceRef() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.EjbRef newEjbRef();

    org.netbeans.modules.j2ee.dd.api.common.EnvEntry newEnvEntry();

    org.netbeans.modules.j2ee.dd.api.common.Icon newIcon() throws VersionNotSupportedException;

    org.netbeans.modules.j2ee.dd.api.common.MessageDestination newMessageDestination() throws VersionNotSupportedException;

    org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;

    org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef newResourceEnvRef();

    org.netbeans.modules.j2ee.dd.api.common.ResourceRef newResourceRef();

    org.netbeans.modules.j2ee.dd.api.common.ServiceRef newServiceRef() throws VersionNotSupportedException;
    
    java.math.BigDecimal getVersion();
    void setVersion(java.math.BigDecimal version);
}
