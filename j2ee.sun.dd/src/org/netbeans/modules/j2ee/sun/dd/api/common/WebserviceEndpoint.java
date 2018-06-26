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
 * WebserviceEndpoint.java
 *
 * Created on November 17, 2004, 5:21 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface WebserviceEndpoint extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String VERSION_SERVER_8_0 = "Server 8.0";
    public static final String VERSION_SERVER_8_1 = "Server 8.1";
    public static final String VERSION_SERVER_9_0 = "Server 9.0";
    
    public static final String PORT_COMPONENT_NAME = "PortComponentName";	// NOI18N
    public static final String ENDPOINT_ADDRESS_URI = "EndpointAddressUri";	// NOI18N
    public static final String LOGIN_CONFIG = "LoginConfig";	// NOI18N
    public static final String TRANSPORT_GUARANTEE = "TransportGuarantee";	// NOI18N
    public static final String SERVICE_QNAME = "ServiceQname";	// NOI18N
    public static final String TIE_CLASS = "TieClass";	// NOI18N
    public static final String SERVLET_IMPL_CLASS = "ServletImplClass";	// NOI18N
    public static final String MESSAGE_SECURITY_BINDING = "MessageSecurityBinding";	// NOI18N
    public static final String DEBUGGING_ENABLED = "DebuggingEnabled";	// NOI18N

    /** Setter for port-component-name property
     * @param value property value
     */
    public void setPortComponentName(java.lang.String value);
    /** Getter for port-component-name property.
     * @return property value
     */
    public java.lang.String getPortComponentName();
    
    /** Setter for endpoint-address-uri property
     * @param value property value
     */
    public void setEndpointAddressUri(java.lang.String value);
    /** Getter for endpoint-address-uri property.
     * @return property value
     */
    public java.lang.String getEndpointAddressUri();
    
    /** Setter for login-config property
     * @param value property value
     */
    public void setLoginConfig(LoginConfig value);
    /** Getter for login-config property.
     * @return property value
     */
    public LoginConfig getLoginConfig();
    
    public LoginConfig newLoginConfig();
    
    /** Setter for transport-guarantee property
     * @param value property value
     */
    public void setTransportGuarantee(java.lang.String value);
    /** Getter for transport-guarantee property.
     * @return property value
     */
    public java.lang.String getTransportGuarantee();
    
    
    /** Setter for service-qname property
     * @param value property value
     */
    public void setServiceQname(ServiceQname value);
    /** Getter for service-qname property.
     * @return property value
     */
    public ServiceQname getServiceQname(); 
    
    public ServiceQname newServiceQname();

    /** Setter for tie-class property
     * @param value property value
     */
    public void setTieClass(java.lang.String value);
    /** Getter for tie-class property.
     * @return property value
     */
    public java.lang.String getTieClass();
    
    /** Setter for servlet-impl-class property
     * @param value property value
     */
    public void setServletImplClass(java.lang.String value);
    /** Getter for servlet-impl-class property.
     * @return property value
     */
    public java.lang.String getServletImplClass();
    
    //For AppServer 8.1 & 9.0
    /** Setter for message-security-binding property
     * @param value property value
     */
    public void setMessageSecurityBinding(MessageSecurityBinding value) throws VersionNotSupportedException; 
    /** Getter for message-security-binding property.
     * @return property value
     */
    public MessageSecurityBinding getMessageSecurityBinding() throws VersionNotSupportedException; 
    
    public MessageSecurityBinding newMessageSecurityBinding() throws VersionNotSupportedException; 
    
    //For Appserver 9.0
    public void setDebuggingEnabled(String value) throws VersionNotSupportedException; 
   
    public String getDebuggingEnabled() throws VersionNotSupportedException; 
     
}
