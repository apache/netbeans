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
/*
 * SunWebApp.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;


import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;

public interface SunWebApp extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {
    
    public static final String VERSION_3_0_1 = "3.01"; //NOI18N
    public static final String VERSION_3_0_0 = "3.00"; //NOI18N
    public static final String VERSION_2_5_0 = "2.50"; //NOI18N
    public static final String VERSION_2_4_1 = "2.41"; //NOI18N
    public static final String VERSION_2_4_0 = "2.40"; //NOI18N
    public static final String VERSION_2_3_0 = "2.30"; //NOI18N

    public static final String ERRORURL = "ErrorUrl";	// NOI18N
    public static final String HTTPSERVLETSECURITYPROVIDER = "HttpservletSecurityProvider";	// NOI18N
    public static final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
    public static final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
    public static final String SERVLET = "Servlet";	// NOI18N
    public static final String IDEMPOTENT_URL_PATTERN = "IdempotentUrlPattern";	// NOI18N
    public static final String IDEMPOTENTURLPATTERNURLPATTERN = "IdempotentUrlPatternUrlPattern";	// NOI18N
    public static final String IDEMPOTENTURLPATTERNNUMOFRETRIES = "IdempotentUrlPatternNumOfRetries";	// NOI18N
    public static final String SESSION_CONFIG = "SessionConfig";	// NOI18N
    public static final String EJB_REF = "EjbRef";	// NOI18N
    public static final String RESOURCE_REF = "ResourceRef";	// NOI18N
    public static final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
    public static final String SERVICE_REF = "ServiceRef";	// NOI18N
    public static final String CACHE = "Cache";	// NOI18N
    public static final String CLASS_LOADER = "MyClassLoader";	// NOI18N
    public static final String JSP_CONFIG = "JspConfig";	// NOI18N
    public static final String LOCALE_CHARSET_INFO = "LocaleCharsetInfo";	// NOI18N
    public static final String PARAMETER_ENCODING = "ParameterEncoding";	// NOI18N
    public static final String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField";	// NOI18N
    public static final String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset";	// NOI18N
    public static final String PROPERTY = "WebProperty";	// NOI18N
    public static final String VALVE = "Valve";	// NOI18N
    public static final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
    public static final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N
    public static final String MESSAGE_DESTINATION_REF = "MessageDestinationRef";	// NOI18N

        
    /** Setter for error-url property
     * @param value property value
     */
    public void setErrorUrl(java.lang.String value) throws VersionNotSupportedException;
    /** Getter for error-url property.
     * @return property value
     */
    public java.lang.String getErrorUrl() throws VersionNotSupportedException;
    /** Setter for context-root property
     * @param value property value
     */
    public void setContextRoot(String value);
    /** Getter for context-root property.
     * @return property value
     */
    public String getContextRoot();

    public void setHttpservletSecurityProvider(java.lang.String value) throws VersionNotSupportedException;
    public java.lang.String getHttpservletSecurityProvider() throws VersionNotSupportedException;

    public void setSecurityRoleMapping(int index, SecurityRoleMapping value);
    public SecurityRoleMapping getSecurityRoleMapping(int index);
    public int sizeSecurityRoleMapping();
    public void setSecurityRoleMapping(SecurityRoleMapping[] value);
    public SecurityRoleMapping[] getSecurityRoleMapping();
    public int addSecurityRoleMapping(SecurityRoleMapping value);
    public int removeSecurityRoleMapping(SecurityRoleMapping value);
    public SecurityRoleMapping newSecurityRoleMapping();

    public void setServlet(int index, Servlet value); 
    public Servlet getServlet(int index);
    public int sizeServlet();
    public void setServlet(Servlet[] value);
    public Servlet[] getServlet();
    public int addServlet(Servlet value);
    public int removeServlet(Servlet value);
    public Servlet newServlet();

    public void setIdempotentUrlPattern(int index, boolean value) throws VersionNotSupportedException;
    public boolean isIdempotentUrlPattern(int index) throws VersionNotSupportedException;
    public int sizeIdempotentUrlPattern() throws VersionNotSupportedException;
    public void setIdempotentUrlPattern(boolean[] value) throws VersionNotSupportedException;
    public boolean[] getIdempotentUrlPattern() throws VersionNotSupportedException;
    public int addIdempotentUrlPattern(boolean value) throws VersionNotSupportedException;
    public int removeIdempotentUrlPattern(boolean value) throws VersionNotSupportedException;
    public void removeIdempotentUrlPattern(int index) throws VersionNotSupportedException;

    /** Setter for url-pattern attribute of idempotent-url-pattern
     * @param value attribute value
     */
    public void setIdempotentUrlPatternUrlPattern(int index, java.lang.String value) throws VersionNotSupportedException;
    /** Getter for url-pattern attribute of idempotent-url-pattern
     * @return attribute value
     */
    public java.lang.String getIdempotentUrlPatternUrlPattern(int index) throws VersionNotSupportedException;
    public int sizeIdempotentUrlPatternUrlPattern() throws VersionNotSupportedException;

    /** Setter for num-of-retries attribute of idempotent-url-pattern
     * @param value attribute value
     */
    public void setIdempotentUrlPatternNumOfRetries(int index, java.lang.String value) throws VersionNotSupportedException;
    /** Getter for num-of-retries attribute of idempotent-url-pattern
     * @return attribute value
     */
    public java.lang.String getIdempotentUrlPatternNumOfRetries(int index) throws VersionNotSupportedException;
    public int sizeIdempotentUrlPatternNumOfRetries() throws VersionNotSupportedException;

    /** Setter for session-config property
     * @param value property value
     */
    public void setSessionConfig(SessionConfig value); 
    /** Getter for session-config property.
     * @return property value
     */
    public SessionConfig getSessionConfig();

    public SessionConfig newSessionConfig(); 

    public void setEjbRef(int index, EjbRef value);
    public EjbRef getEjbRef(int index);
    public int sizeEjbRef();
    public void setEjbRef(EjbRef[] value);
    public EjbRef[] getEjbRef();
    public int addEjbRef(EjbRef value);
    public int removeEjbRef(EjbRef value);
    public EjbRef newEjbRef();

    public void setResourceRef(int index, ResourceRef value);
    public ResourceRef getResourceRef(int index);
    public int sizeResourceRef();
    public void setResourceRef(ResourceRef[] value);
    public ResourceRef[] getResourceRef();
    public int addResourceRef(ResourceRef value);
    public int removeResourceRef(ResourceRef value);
    public ResourceRef newResourceRef();

    public void setResourceEnvRef(int index, ResourceEnvRef value);
    public ResourceEnvRef getResourceEnvRef(int index);
    public int sizeResourceEnvRef();
    public void setResourceEnvRef(ResourceEnvRef[] value);
    public ResourceEnvRef[] getResourceEnvRef();
    public int addResourceEnvRef(ResourceEnvRef value);
    public int removeResourceEnvRef(ResourceEnvRef value);
    public ResourceEnvRef newResourceEnvRef();

    public void setServiceRef(int index, ServiceRef value);
    public ServiceRef getServiceRef(int index);
    public int sizeServiceRef();
    public void setServiceRef(ServiceRef[] value);
    public ServiceRef[] getServiceRef();
    public int addServiceRef(ServiceRef value);
    public int removeServiceRef(ServiceRef value);
    public ServiceRef newServiceRef();

    /** Setter for cache property
     * @param value property value
     */
    public void setCache(Cache value);
    /** Getter for cache property.
     * @return property value
     */
    public Cache getCache();
    public Cache newCache();

    //-------------------------------------------------------------------------
    // ClassLoader property support in AS 8.1 and AS 9.x
    //-------------------------------------------------------------------------
    /** Setter for class-loader property in sun-web_2_4-1 and later
     * @param value property value
     */
    public void setMyClassLoader(MyClassLoader value) throws VersionNotSupportedException;
    /** Getter for class-loader property in sun-web_2_4-1 and later
     * @return property value
     */
    public MyClassLoader getMyClassLoader() throws VersionNotSupportedException;
    /** Creator for class-loader property in sun-web_2_4-1 and later
     * @return property value
     */
    public MyClassLoader newMyClassLoader() throws VersionNotSupportedException;

    //-------------------------------------------------------------------------
    // ClassLoader property support in AS 7.0 and AS 8.0
    //-------------------------------------------------------------------------
    /** Setter for class-loader property in sun-web_2_3-0 & sun-web_2_4-0
     * @param value property value
     */
    public void setMyClassLoader(boolean value) throws VersionNotSupportedException;
    /** Getter for class-loader property in sun-web_2_3-0 & sun-web_2_4-0
     * @return property value
     */
    public boolean isMyClassLoader() throws VersionNotSupportedException;
    /** Setter for class-loader extra-classpath property in sun-web_2_3-0 & in sun-web_2_4-0
     * @return property value
     */
	public void setMyClassLoaderExtraClassPath(java.lang.String value) throws VersionNotSupportedException;
    /** Getter for class-loader extra-classpath property in sun-web_2_3-0 & in sun-web_2_4-0
     * @return property value
     */
	public java.lang.String getMyClassLoaderExtraClassPath() throws VersionNotSupportedException;
    /** Setter for class-loader delegate property in sun-web_2_3-0 & in sun-web_2_4-0
     * @return property value
     */
	public void setMyClassLoaderDelegate(java.lang.String value) throws VersionNotSupportedException;
    /** Getter for class-loader delegate property in sun-web_2_3-0 & in sun-web_2_4-0
     * @return property value
     */
	public java.lang.String getMyClassLoaderDelegate() throws VersionNotSupportedException;
    
    /** Setter for jsp-config property
     * @param value property value
     */
    public void setJspConfig(JspConfig value);
    /** Getter for jsp-config property.
     * @return property value
     */
    public JspConfig getJspConfig();

    public JspConfig newJspConfig();

    /** Setter for locale-charset-info property
     * @param value property value
     */
    public void setLocaleCharsetInfo(LocaleCharsetInfo value);
    /** Getter for locale-charset-info property.
     * @return property value
     */
    public LocaleCharsetInfo getLocaleCharsetInfo();

    public LocaleCharsetInfo newLocaleCharsetInfo();

    /** Setter for parameter-encoding property.
     * @param value property value
     */
    public void setParameterEncoding(boolean value) throws VersionNotSupportedException;
    /** Check for parameter-encoding property.
     * @return property value
     */
    public boolean isParameterEncoding() throws VersionNotSupportedException;
    /** Setter for form-hint-field attribute.
     * @param value attribute value
     */
    public void setParameterEncodingFormHintField(java.lang.String value) throws VersionNotSupportedException;
    /** Getter for form-hint-field attribute.
     * @return attribute value
     */
    public java.lang.String getParameterEncodingFormHintField() throws VersionNotSupportedException;
    /** Setter for default-charset attribute.
     * @param value attribute value
     */
    public void setParameterEncodingDefaultCharset(java.lang.String value) throws VersionNotSupportedException;
    /** Getter for default-charset attribute.
     * @return attribute value
     */
    public java.lang.String getParameterEncodingDefaultCharset() throws VersionNotSupportedException;

    public void setWebProperty(int index, WebProperty value);
    public WebProperty getWebProperty(int index);
    public int sizeWebProperty();
    public void setWebProperty(WebProperty[] value);
    public WebProperty[] getWebProperty();
    public int addWebProperty(WebProperty value);
    public int removeWebProperty(WebProperty value);
    public WebProperty newWebProperty();

    public void setValve(int index, Valve value) throws VersionNotSupportedException;
    public Valve getValve(int index) throws VersionNotSupportedException;
    public int sizeValve() throws VersionNotSupportedException;
    public void setValve(Valve[] value) throws VersionNotSupportedException;
    public Valve[] getValve() throws VersionNotSupportedException;
    public int addValve(Valve value) throws VersionNotSupportedException;
    public int removeValve(Valve value) throws VersionNotSupportedException;
    public Valve newValve() throws VersionNotSupportedException;

    public void setMessageDestination(int index, MessageDestination value);
    public MessageDestination getMessageDestination(int index);
    public int sizeMessageDestination();
    public void setMessageDestination(MessageDestination[] value);
    public MessageDestination[] getMessageDestination();
    public int addMessageDestination(MessageDestination value);
    public int removeMessageDestination(MessageDestination value);
    public MessageDestination newMessageDestination();

    public void setWebserviceDescription(int index, WebserviceDescription value);
    public WebserviceDescription getWebserviceDescription(int index);
    public int sizeWebserviceDescription();
    public void setWebserviceDescription(WebserviceDescription[] value);
    public WebserviceDescription[] getWebserviceDescription();
    public int addWebserviceDescription(WebserviceDescription value);
    public int removeWebserviceDescription(WebserviceDescription value);
    public WebserviceDescription newWebserviceDescription();

    //Required for web 2.5.0
    public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException;
    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;
    public int sizeMessageDestinationRef() throws VersionNotSupportedException;
    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException;
    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
    public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;
    public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;
    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;
}
