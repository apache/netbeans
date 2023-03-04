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
