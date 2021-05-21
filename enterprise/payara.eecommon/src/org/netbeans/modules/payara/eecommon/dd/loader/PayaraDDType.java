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

package org.netbeans.modules.payara.eecommon.dd.loader;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/**
 *
 * @author Peter Williams
 * @author Gaurav Gupta
 */
public final class PayaraDDType {

    private static final String NAME_PAYARA_WEB_APP = "payara-web.xml"; // NOI18N
    private static final String NAME_PAYARA_EJB_JAR = "payara-ejb-jar.xml"; // NOI18N
    private static final String NAME_PAYARA_APPLICATION = "payara-application.xml"; // NOI18N
    private static final String NAME_PAYARA_APP_CLIENT = "payara-application-client.xml"; // NOI18N
    private static final String NAME_PAYARA_RESOURCE = "payara-resources.xml"; // NOI18N
    
    public static final String NAME_WEBXML = "web.xml"; // NOI18N
    
    // Type declarations for the different descriptor types.
    public static final PayaraDDType DD_PAYARA_WEB_APP = new PayaraDDType(NAME_PAYARA_WEB_APP, J2eeModule.Type.WAR);
    public static final PayaraDDType DD_PAYARA_EJB_JAR = new PayaraDDType(NAME_PAYARA_EJB_JAR, J2eeModule.Type.EJB);
    public static final PayaraDDType DD_PAYARA_APP_CLIENT = new PayaraDDType(NAME_PAYARA_APP_CLIENT, J2eeModule.Type.CAR);
    public static final PayaraDDType DD_PAYARA_APPLICATION = new PayaraDDType(NAME_PAYARA_APPLICATION, J2eeModule.Type.EAR);
    public static final PayaraDDType DD_PAYARA_RESOURCE = new PayaraDDType(NAME_PAYARA_RESOURCE, null);


    // Various indexes for finding a DDType object
    private static final Map<String, PayaraDDType> fileToTypeMap = new HashMap<String, PayaraDDType>(11);

    static {
        fileToTypeMap.put(NAME_PAYARA_WEB_APP, DD_PAYARA_WEB_APP);
//        fileToTypeMap.put(NAME_PAYARA_EJB_JAR, DD_PAYARA_EJB_JAR);
//        fileToTypeMap.put(NAME_PAYARA_APP_CLIENT, DD_PAYARA_APP_CLIENT);
//        fileToTypeMap.put(NAME_PAYARA_APPLICATION, DD_PAYARA_APPLICATION);
//        fileToTypeMap.put(NAME_PAYARA_RESOURCE, DD_PAYARA_RESOURCE);
    }
    
    static final String PAYARA_MIME_TYPE_PREFIX = "text/x-dd-payara"; // noi18n

    static final String WEB_MIME_TYPE_SUFFIX = "-web+xml"; // NOI18N
    static final String EJB_MIME_TYPE_SUFFIX = "-ejb-jar+xml"; // NOI18N
    static final String APP_MIME_TYPE_SUFFIX = "-application+xml"; // noi18n
    static final String APP_CLI_MIME_TYPE_SUFFIX = "-app-client+xml"; // noi18n
    static final String RSRC_MIME_TYPE_SUFFIX = "-resource+xml"; // noi18n
    
    public static final String PAYARA_WEB_MIME_TYPE = PAYARA_MIME_TYPE_PREFIX + WEB_MIME_TYPE_SUFFIX;
    public static final String PAYARA_EJB_MIME_TYPE = PAYARA_MIME_TYPE_PREFIX + EJB_MIME_TYPE_SUFFIX;
    public static final String PAYARA_APP_MIME_TYPE = PAYARA_MIME_TYPE_PREFIX + APP_MIME_TYPE_SUFFIX;
    public static final String PAYARA_APP_CLI_MIME_TYPE = PAYARA_MIME_TYPE_PREFIX + APP_CLI_MIME_TYPE_SUFFIX;
    public static final String PAYARA_RSRC_MIME_TYPE = "text/x-payara" + RSRC_MIME_TYPE_SUFFIX;
    
    
    private static final Map<String,String> fileToMimeSuffixMap = new HashMap<String,String>(8);
    
    static {
        fileToMimeSuffixMap.put(NAME_PAYARA_WEB_APP, WEB_MIME_TYPE_SUFFIX);
//        fileToMimeSuffixMap.put(NAME_PAYARA_EJB_JAR, EJB_MIME_TYPE_SUFFIX);
//        fileToMimeSuffixMap.put(NAME_PAYARA_APPLICATION, APP_MIME_TYPE_SUFFIX);
//        fileToMimeSuffixMap.put(NAME_PAYARA_APP_CLIENT, APP_CLI_MIME_TYPE_SUFFIX);
//        fileToMimeSuffixMap.put(NAME_PAYARA_RESOURCE, RSRC_MIME_TYPE_SUFFIX);
    }
    
    public static PayaraDDType getDDType(String fileName) {
        return fileToTypeMap.get(fileName);
    }


    private final String descriptorName;
    private final J2eeModule.Type moduleType;
    
    private PayaraDDType(final String ddName, final J2eeModule.Type type) {
        descriptorName = ddName;
        moduleType = type;
    }
    
    public String getDescriptorFileName() {
        return this.descriptorName;
    }
    
    public J2eeModule.Type getEditorModuleType() {
        return moduleType;
    }
    
    String getDescriptorMimeTypeSuffix() {
        return fileToMimeSuffixMap.get(descriptorName);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj == this) {
            return true;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }

        final PayaraDDType other = (PayaraDDType) obj;
        if(!moduleType.equals(other.moduleType)) {
            return false;
        }
        return descriptorName.equals(other.descriptorName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (descriptorName != null ? descriptorName.hashCode() : 0);
        return hash;
    }

}
