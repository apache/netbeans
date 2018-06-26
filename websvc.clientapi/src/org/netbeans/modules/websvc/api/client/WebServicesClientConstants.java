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

package org.netbeans.modules.websvc.api.client;

/**
 * @author  rico
 */
public class WebServicesClientConstants {
    public static final String WEBSERVICES_DD = "webservices";//NOI18N
    public static final String WEB_SERVICES =     "web-services";//NOI18N
    public static final String WEB_SERVICE  =     "web-service";//NOI18N
    public static final String WEB_SERVICE_NAME = "web-service-name";//NOI18N
    public static final String WEB_SERVICE_FROM_WSDL = "from-wsdl"; // NOI18N
    public static final String CONFIG_PROP_SUFFIX = ".config.name";//NOI18N
    public static final String MAPPING_PROP_SUFFIX = ".mapping";//NOI18N
    public static final String MAPPING_FILE_SUFFIX = "-mapping.xml";//NOI18N
    public static final String WebServiceServlet_PREFIX = "WSServlet_";//NOI18N

    public static final String WSDL_FOLDER = "wsdl"; // NOI18N
    public static final String WEB_SERVICE_CLIENTS = "web-service-clients"; //NOI18N
    public static final String WEB_SERVICE_CLIENT = "web-service-client"; //NOI18N
    public static final String WEB_SERVICE_CLIENT_NAME = "web-service-client-name"; //NOI18N
    public static final String WEB_SERVICE_STUB_TYPE = "web-service-stub-type"; //NOI18N
    public static final String CLIENT_SOURCE_URL = "client-source-url"; //NOI18N
    public static final String WSCOMPILE="wscompile"; //NOI18N
    public static final String WSCOMPILE_CLASSPATH = "wscompile.classpath"; //NOI18N
    public static final String WSCOMPILE_TOOLS_CLASSPATH = "wscompile.tools.classpath"; //NOI18N
    public static final String WEBSVC_GENERATED_DIR = "websvc.generated.dir"; // NOI18N
    public static final String J2EE_PLATFORM_WSCOMPILE_CLASSPATH="j2ee.platform.wscompile.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_WSIMPORT_CLASSPATH="j2ee.platform.wsimport.classpath"; //NOI18N
    public static final String [] WSCOMPILE_JARS = {
        "${" + J2EE_PLATFORM_WSCOMPILE_CLASSPATH + "}", //NOI18N
        "${javac.classpath}",
        "${wscompile.tools.classpath}" //NOI18N
    };
}
