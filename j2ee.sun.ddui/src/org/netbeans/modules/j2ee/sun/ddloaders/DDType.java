/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ddloaders;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/**
 *
 * @author Peter Williams
 */
public final class DDType {

    private static final String NAME_SUNAPPCLIENT = "sun-application-client.xml"; // NOI18N
    private static final String NAME_SUNAPPLICATION = "sun-application.xml"; // NOI18N
    private static final String NAME_SUNCMPMAPPING = "sun-cmp-mappings.xml"; // NOI18N
    private static final String NAME_SUNEJBJAR = "sun-ejb-jar.xml"; // NOI18N
    private static final String NAME_SUNWEBAPP = "sun-web.xml"; // NOI18N
    private static final String NAME_SUNRESOURCE = "sun-resources.xml"; // NOI18N
    
    private static final String NAME_GFAPPCLIENT = "glassfish-application-client.xml"; // NOI18N
    private static final String NAME_GFAPPLICATION = "glassfish-application.xml"; // NOI18N
    private static final String NAME_GFEJBJAR = "glassfish-ejb-jar.xml"; // NOI18N
    private static final String NAME_GFWEBAPP = "glassfish-web.xml"; // NOI18N
    private static final String NAME_GFRESOURCE = "glassfish-resources.xml"; // NOI18N

    // Type declarations for the different descriptor types.
    public static final DDType DD_SUN_WEB_APP = new DDType(NAME_SUNWEBAPP, J2eeModule.Type.WAR);
    public static final DDType DD_SUN_EJB_JAR = new DDType(NAME_SUNEJBJAR, J2eeModule.Type.EJB);
    public static final DDType DD_SUN_APP_CLIENT = new DDType(NAME_SUNAPPCLIENT, J2eeModule.Type.CAR);
    public static final DDType DD_SUN_APPLICATION = new DDType(NAME_SUNAPPLICATION, J2eeModule.Type.EAR);
    public static final DDType DD_SUN_CMP_MAPPINGS = new DDType(NAME_SUNCMPMAPPING, J2eeModule.Type.EJB);
    public static final DDType DD_SUN_RESOURCE = new DDType(NAME_SUNRESOURCE, null);

    public static final DDType DD_GF_WEB_APP = new DDType(NAME_GFWEBAPP, J2eeModule.Type.WAR);
    public static final DDType DD_GF_EJB_JAR = new DDType(NAME_GFEJBJAR, J2eeModule.Type.EJB);
    public static final DDType DD_GF_APP_CLIENT = new DDType(NAME_GFAPPCLIENT, J2eeModule.Type.CAR);
    public static final DDType DD_GF_APPLICATION = new DDType(NAME_GFAPPLICATION, J2eeModule.Type.EAR);
    public static final DDType DD_GF_RESOURCE = new DDType(NAME_GFRESOURCE, null);

    // Various indexes for finding a DDType object
    private static final Map<String, DDType> fileToTypeMap = new HashMap<String, DDType>(11);

    static {
        fileToTypeMap.put(NAME_SUNWEBAPP, DD_SUN_WEB_APP);
        fileToTypeMap.put(NAME_SUNEJBJAR, DD_SUN_EJB_JAR);
        fileToTypeMap.put(NAME_SUNAPPLICATION, DD_SUN_APPLICATION);
        fileToTypeMap.put(NAME_SUNAPPCLIENT, DD_SUN_APP_CLIENT);
        fileToTypeMap.put(NAME_SUNCMPMAPPING, DD_SUN_CMP_MAPPINGS);
        fileToTypeMap.put(NAME_SUNRESOURCE, DD_SUN_RESOURCE);
        fileToTypeMap.put(NAME_GFWEBAPP, DD_GF_WEB_APP);
        fileToTypeMap.put(NAME_GFEJBJAR, DD_GF_EJB_JAR);
        fileToTypeMap.put(NAME_GFAPPLICATION, DD_GF_APPLICATION);
        fileToTypeMap.put(NAME_GFAPPCLIENT, DD_GF_APP_CLIENT);
        fileToTypeMap.put(NAME_GFRESOURCE, DD_GF_RESOURCE);
    }
    
    public static final String WEB_MIME_TYPE = "text/x-dd-sun-web+xml"; // NOI18N
    public static final String EJB_MIME_TYPE = "text/x-dd-sun-ejb-jar+xml"; // NOI18N
    public static final String APP_MIME_TYPE = "text/x-dd-sun-application+xml"; // NOI18N
    public static final String APP_CLI_MIME_TYPE = "text/x-dd-sun-app-client+xml"; // NOI18N
    public static final String RSRC_MIME_TYPE = "text/x-sun-resource+xml"; // NOI18N
    public static final String CMP_MIME_TYPE = "text/x-sun-cmp-mapping+xml"; // NOI18N
    
    static final String WEB_MIME_TYPE_SUFFIX = "-web+xml"; // NOI18N
    static final String EJB_MIME_TYPE_SUFFIX = "-ejb-jar+xml"; // NOI18N
    static final String APP_MIME_TYPE_SUFFIX = "-application+xml"; // noi18n
    static final String APP_CLI_MIME_TYPE_SUFFIX = "-app-client+xml"; // noi18n
    static final String RSRC_MIME_TYPE_SUFFIX = "-resource+xml"; // noi18n
    static final String CMP_MIME_TYPE_SUFFIX = "-cmp-mapping+xml"; // noi18n
    
    
    private static final Map<String,String> fileToMimeSuffixMap = new HashMap<String,String>(8);
    static {
        fileToMimeSuffixMap.put(NAME_SUNWEBAPP, WEB_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_SUNEJBJAR, EJB_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_SUNAPPLICATION, APP_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_SUNAPPCLIENT, APP_CLI_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_SUNCMPMAPPING, CMP_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_SUNRESOURCE, RSRC_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_GFWEBAPP, WEB_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_GFEJBJAR, EJB_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_GFAPPLICATION, APP_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_GFAPPCLIENT, APP_CLI_MIME_TYPE_SUFFIX);
        fileToMimeSuffixMap.put(NAME_GFRESOURCE, RSRC_MIME_TYPE_SUFFIX);
    }
    static final String IPLANET_MIME_TYPE_PREFIX = "text/x-dd-iplanet"; // noi18n
    static final String SUN_MIME_TYPE_PREFIX = "text/x-dd-sun"; // noi18n
    static final String GLASSFISH_MIME_TYPE_PREFIX = "text/x-dd-glassfish"; // noi18n
    
    public static DDType getDDType(String fileName) {
        return fileToTypeMap.get(fileName);
    }
    
    // Internal data
    private final String descriptorName;
    private final J2eeModule.Type moduleType;
    
    private DDType(final String ddName, final J2eeModule.Type type) {
        descriptorName = ddName;
        moduleType = type;
    }
    
    public String getDescriptorFileName() {
        return this.descriptorName;
    }
    
    public J2eeModule.Type getEditorModuleType() {
        return moduleType;
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

        final DDType other = (DDType) obj;
        if(!moduleType.equals(other.moduleType)) {
            return false;
        }
        if(!descriptorName.equals(other.descriptorName)) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (descriptorName != null ? descriptorName.hashCode() : 0);
        return hash;
    }

    String getDescriptorMimeTypeSuffix() {
        return fileToMimeSuffixMap.get(descriptorName);
    }
}
