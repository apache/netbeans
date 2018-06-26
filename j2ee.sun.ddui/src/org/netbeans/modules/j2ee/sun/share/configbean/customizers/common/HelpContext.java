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
 * HelpContext.java
 *
 * Created on March 25, 2004, 9:39 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

/**
 *
 * @author Peter Williams
 */
public class HelpContext {

    public static final String HELP_WEBAPP_IDEMPOTENTURLPATTERN_POPUP="AS_CFG_WebAppIdempotentUrlPatternProperty";
    public static final String HELP_WEBAPP_JSPCONFIG_POPUP = "AS_CFG_WebAppJspConfigProperty";
    public static final String HELP_WEBAPP_PROPERTY_POPUP = "AS_CFG_WebAppProperty";
    public static final String HELP_WEBAPP_CLASSLOADER_PROPERTY_POPUP = "AS_CFG_WebAppClassloaderProperty";
    public static final String HELP_SESSIONCONFIG_MANAGER_POPUP = "AS_CFG_SessionConfigurationManagerProperty";
    public static final String HELP_SESSIONCONFIG_STORE_POPUP = "AS_CFG_SessionConfigurationStoreProperty";
    public static final String HELP_SESSIONCONFIG_SESSION_POPUP = "AS_CFG_SessionConfigurationSessionProperty";
    public static final String HELP_SESSIONCONFIG_COOKIE_POPUP = "AS_CFG_SessionConfigurationCookieProperty";
    public static final String HELP_WEBAPP_SERVICE_DESCRIPTION_POPUP = "AS_CFG_WebAppWebserviceDescription";
    public static final String HELP_WEBAPP_MESSAGE_DESTINATION_POPUP = "AS_CFG_WebAppMessageDestination";
    public static final String HELP_WEBAPP_MESSAGE_DESTINATION_REF_POPUP = "AS_CFG_WebAppMessageDestinationRef";
    public static final String HELP_WEBAPP_LOCALE_MAPPING_POPUP = "AS_CFG_WebAppLocaleCharsetMapping";
    public static final String HELP_CACHE_PROPERTY_POPUP = "AS_CFG_WebAppCacheProperty";
    public static final String HELP_CACHE_DEFAULT_HELPER_POPUP = "AS_CFG_WebAppCacheDefaultHelperProperty";
    public static final String HELP_CACHE_HELPER_DEFINITION_POPUP = "AS_CFG_WebAppCacheHelperDefinition";
    public static final String HELP_CACHE_HELPER_PROPERTY_POPUP = "AS_CFG_WebAppCacheHelperProperty";
    public static final String HELP_CACHE_MAPPING_POLICY_POPUP = "AS_CFG_WebAppCacheMappingPolicy";
    public static final String HELP_CACHE_MAPPING_DISPATCHER_POPUP = "AS_CFG_WebAppCacheMappingDispatcherField";
    public static final String HELP_CACHE_MAPPING_KEYFIELD_POPUP = "AS_CFG_WebAppCacheMappingKeyField";
    public static final String HELP_CACHE_MAPPING_CONSTRAINT_POPUP = "AS_CFG_WebAppCacheMappingConstraintField";
    public static final String HELP_CACHE_MAPPING_CONSTRAINT_VALUE_POPUP = "AS_CFG_WebAppCacheMappingConstraintFieldValue";
    public static final String HELP_SERVLET_SERVICE_ENDPOINT_POPUP = "AS_CFG_ServletWebServiceEndpoint";
    public static final String HELP_SERVICE_ENDPOINT_POPUP = "AS_CFG_WebServiceEndpoint";
    public static final String HELP_SERVICE_ENDPOINT_SECURITY = "AS_CFG_WebServiceEndpointSecurity";
    public static final String HELP_SERVICE_ENDPOINT_SECURITY_POPUP = "AS_CFG_WebServiceEndpointMessageSecurity";
    public static final String HELP_SERVICE_CALL_PROPERTY_POPUP = "AS_CFG_ServiceRefCallProperty";
    public static final String HELP_SERVICE_PORT_STUB_PROPERTY_POPUP = "AS_CFG_ServiceRefPortInfoStubProperty";
    public static final String HELP_SERVICE_PORT_CALL_PROPERTY_POPUP = "AS_CFG_ServiceRefPortInfoCallProperty";
    public static final String HELP_SECURITY_NEW_PRINCIPAL = "AS_CFG_SecurityRoleNewPrincipal";
    public static final String HELP_SECURITY_EDIT_PRINCIPAL = "AS_CFG_SecurityRoleEditPrincipal";
    public static final String HELP_SECURITY_NEW_GROUP = "AS_CFG_SecurityRoleNewGroup";
    public static final String HELP_SECURITY_EDIT_GROUP = "AS_CFG_SecurityRoleEditGroup";

    // FIXME help id's added ad-hoc to make code compile - need to reevaluate 
    // when ui spec is done for application, app-client, and connector.
    public static final String HELP_APPCLIENT_MESSAGE_DESTINATION_POPUP = "AS_CFG_AppClientMessageDestination";
    public static final String HELP_CONNECTOR_PROPERTY_POPUP = "AS_CFG_ConnectorProperty";
    public static final String HELP_CONNECTOR_MAPELEMENT_POPUP = "AS_CFG_ConnectorMapElement";
    public static final String HELP_EJBJAR_PM_DESCRIPTOR_POPUP = "AS_CFG_EjbJarPmDescriptor";

    public static final String HELP_EJBJAR_CMP_PROPERTY_POPUP = "AS_CFG_EjbJarCmpProperty";
    public static final String HELP_EJBJAR_SCHEMA_PROPERTY_POPUP = "AS_CFG_EjbJarCmpSchemaProperty";

    /** Creates a new instance of HelpContext */
    private HelpContext() {
    }	
}
