/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
