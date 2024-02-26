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
package org.netbeans.modules.j2ee.dd.api.web;

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;

/**
 * Generated interface for WebApp element.<br>
 * The WebApp object is the root of bean graph generated<br>
 * for deployment descriptor(web.xml) file.<br>
 * For getting the root (WebApp object) use the {@link DDProvider#getDDRoot} method.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WebApp extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {

    static final String PROPERTY_VERSION = "dd_version"; //NOI18N
    static final String VERSION_2_4 = "2.4"; //NOI18N
    static final String VERSION_2_5 = "2.5"; //NOI18N
    static final String VERSION_3_0 = "3.0"; //NOI18N

    /**
     * web.xml, web-fragment.xml DD version for JavaEE7
     * @since 1.29
     */
    static final String VERSION_3_1 = "3.1"; //NOI18N
    /**
     * web.xml, web-fragment.xml DD version for JavaEE8/JakartaEE8
     * @since 1.29
     */
    static final String VERSION_4_0 = "4.0"; //NOI18N
    /**
     * web.xml, web-fragment.xml DD version for JakartaEE9/JakartaEE91
     * @since 1.29
     */
    static final String VERSION_5_0 = "5.0"; //NOI18N
    /**
     * web.xml, web-fragment.xml DD version for JakartaEE10
     */
    static final String VERSION_6_0 = "6.0"; //NOI18N
    /**
     * web.xml, web-fragment.xml DD version for Jakarta EE 11
     */
    static final String VERSION_6_1 = "6.1"; //NOI18N
    static final int STATE_VALID = 0;
    static final int STATE_INVALID_PARSABLE = 1;
    static final int STATE_INVALID_UNPARSABLE = 2;
    static final int STATE_INVALID_OLD_VERSION = 3;
    static final String PROPERTY_STATUS = "dd_status"; //NOI18N

    /** Getter for version property.
     * @return property value
     */
    String getVersion();

    /** Getter for SAX Parse Error property.
     * Used when deployment descriptor is in invalid state.
     * @return property value or null if in valid state
     */
    org.xml.sax.SAXParseException getError();

    /** Getter for status property.
     * @return property value
     */
    int getStatus();

    /** Setter for distributable property.
     * @param value property value
     */
    void setDistributable(boolean value);

    /** Getter for distributable property.
     * @return property value
     */
    boolean isDistributable();

    void setContextParam(int index, InitParam valueInterface);
    InitParam getContextParam(int index);
    void setContextParam(InitParam[] value);
    InitParam[] getContextParam();
    int sizeContextParam();
    int addContextParam(InitParam valueInterface);
    int removeContextParam(InitParam valueInterface);

    void setFilter(int index, Filter valueInterface);
    Filter getFilter(int index);
    void setFilter(Filter[] value);
    Filter[] getFilter();
    int sizeFilter();
    int addFilter(Filter valueInterface);
    int removeFilter(Filter valueInterface);

    void setFilterMapping(int index, FilterMapping valueInterface);
    FilterMapping getFilterMapping(int index);
    void setFilterMapping(FilterMapping[] value);
    FilterMapping[] getFilterMapping();
    int sizeFilterMapping();
    int addFilterMapping(FilterMapping valueInterface);
    int removeFilterMapping(FilterMapping valueInterface);

    void setListener(int index, Listener valueInterface);
    Listener getListener(int index);
    void setListener(Listener[] value);
    Listener[] getListener();
    int sizeListener();
    int addListener(Listener valueInterface);
    int removeListener(Listener valueInterface);

    void setServlet(int index, Servlet valueInterface);
    Servlet getServlet(int index);
    void setServlet(Servlet[] value);
    Servlet[] getServlet();
    int sizeServlet();
    int addServlet(Servlet valueInterface);
    int removeServlet(Servlet valueInterface);

    void setServletMapping(int index, ServletMapping valueInterface);
    ServletMapping getServletMapping(int index);
    void setServletMapping(ServletMapping[] value);
    ServletMapping[] getServletMapping();
    int sizeServletMapping();
    int addServletMapping(ServletMapping valueInterface);
    int removeServletMapping(ServletMapping valueInterface);

    void setSessionConfig(SessionConfig value);
    SessionConfig getSingleSessionConfig();

    void setMimeMapping(int index, MimeMapping valueInterface);
    MimeMapping getMimeMapping(int index);
    void setMimeMapping(MimeMapping[] value);
    MimeMapping[] getMimeMapping();
    int sizeMimeMapping();
    int addMimeMapping(MimeMapping valueInterface);
    int removeMimeMapping(MimeMapping valueInterface);

    void setWelcomeFileList(WelcomeFileList value);
    WelcomeFileList getSingleWelcomeFileList();

    void setErrorPage(int index, ErrorPage valueInterface);
    ErrorPage getErrorPage(int index);
    void setErrorPage(ErrorPage[] value);
    ErrorPage[] getErrorPage();
    int sizeErrorPage();
    int addErrorPage(ErrorPage valueInterface);
    int removeErrorPage(ErrorPage valueInterface);

    void setJspConfig(JspConfig value) throws VersionNotSupportedException;
    JspConfig getSingleJspConfig() throws VersionNotSupportedException;
    int addJspConfig(JspConfig valueInterface) throws VersionNotSupportedException;
    int removeJspConfig(JspConfig valueInterface) throws VersionNotSupportedException;

    void setSecurityConstraint(int index, SecurityConstraint valueInterface);
    SecurityConstraint getSecurityConstraint(int index);
    void setSecurityConstraint(SecurityConstraint[] value);
    SecurityConstraint[] getSecurityConstraint();
    int sizeSecurityConstraint();
    int addSecurityConstraint(SecurityConstraint valueInterface);
    int removeSecurityConstraint(SecurityConstraint valueInterface);

    void setLoginConfig(LoginConfig value);
    LoginConfig getSingleLoginConfig();

    void setSecurityRole(int index, SecurityRole valueInterface);
    SecurityRole getSecurityRole(int index);
    void setSecurityRole(SecurityRole[] value);
    SecurityRole[] getSecurityRole();
    int sizeSecurityRole();
    int addSecurityRole(SecurityRole valueInterface);
    int removeSecurityRole(SecurityRole valueInterface);

    void setEnvEntry(int index, EnvEntry valueInterface);
    EnvEntry getEnvEntry(int index);
    void setEnvEntry(EnvEntry[] value);
    EnvEntry[] getEnvEntry();
    int sizeEnvEntry();
    int addEnvEntry(EnvEntry valueInterface);
    int removeEnvEntry(EnvEntry valueInterface);

    void setEjbRef(int index, EjbRef valueInterface);
    EjbRef getEjbRef(int index);
    void setEjbRef(EjbRef[] value);
    EjbRef[] getEjbRef();
    int sizeEjbRef();
    int addEjbRef(EjbRef valueInterface);
    int removeEjbRef(EjbRef valueInterface);

    void setEjbLocalRef(int index, EjbLocalRef valueInterface);
    EjbLocalRef getEjbLocalRef(int index);
    void setEjbLocalRef(EjbLocalRef[] value);
    EjbLocalRef[] getEjbLocalRef();
    int sizeEjbLocalRef();
    int addEjbLocalRef(EjbLocalRef valueInterface);
    int removeEjbLocalRef(EjbLocalRef valueInterface);

    void setServiceRef(int index, ServiceRef valueInterface) throws VersionNotSupportedException;
    ServiceRef getServiceRef(int index) throws VersionNotSupportedException;
    void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException;
    ServiceRef[] getServiceRef() throws VersionNotSupportedException;
    int sizeServiceRef() throws VersionNotSupportedException;
    int addServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException;
    int removeServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException;

    void setResourceRef(int index, ResourceRef valueInterface);
    ResourceRef getResourceRef(int index);
    void setResourceRef(ResourceRef[] value);
    ResourceRef[] getResourceRef();
    int sizeResourceRef();
    int addResourceRef(ResourceRef valueInterface);
    int removeResourceRef(ResourceRef valueInterface);

    void setResourceEnvRef(int index, ResourceEnvRef valueInterface);
    ResourceEnvRef getResourceEnvRef(int index);
    void setResourceEnvRef(ResourceEnvRef[] value);
    ResourceEnvRef[] getResourceEnvRef();
    int sizeResourceEnvRef();
    int addResourceEnvRef(ResourceEnvRef valueInterface);
    int removeResourceEnvRef(ResourceEnvRef valueInterface);

    void setMessageDestinationRef(int index, MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;
    void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException;
    MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
    int sizeMessageDestinationRef() throws VersionNotSupportedException;
    int addMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    int removeMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException;

    void setMessageDestination(int index, MessageDestination valueInterface) throws VersionNotSupportedException;
    MessageDestination getMessageDestination(int index) throws VersionNotSupportedException;
    void setMessageDestination(MessageDestination[] value) throws VersionNotSupportedException;
    MessageDestination[] getMessageDestination() throws VersionNotSupportedException;
    int sizeMessageDestination() throws VersionNotSupportedException;
    int addMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException;
    int removeMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException;

    LocaleEncodingMappingList getSingleLocaleEncodingMappingList() throws VersionNotSupportedException;
    void setLocaleEncodingMappingList(LocaleEncodingMappingList value) throws VersionNotSupportedException;

    void setMetadataComplete(boolean value) throws VersionNotSupportedException;
    boolean isMetadataComplete() throws VersionNotSupportedException;

    void setName(String[] value) throws VersionNotSupportedException;
    String[] getName() throws VersionNotSupportedException;

    AbsoluteOrdering newAbsoluteOrdering() throws VersionNotSupportedException;
    void setAbsoluteOrdering(AbsoluteOrdering[] value) throws VersionNotSupportedException;
    AbsoluteOrdering[] getAbsoluteOrdering() throws VersionNotSupportedException;
}
