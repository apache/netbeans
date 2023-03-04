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

package org.netbeans.modules.j2ee.earproject;

import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public enum ModuleType {
    
    WEB(NbBundle.getMessage(ModuleType.class, "CTL_WebModule")),
    EJB(NbBundle.getMessage(ModuleType.class, "CTL_EjbModule")),
    CLIENT(NbBundle.getMessage(ModuleType.class, "CTL_ClientModule"));
    
    private final String description;
    
    ModuleType(final String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /** Maps relative deployment descriptor's path to {@link ModuleType}. */
    private static final Map<String, ModuleType> DEFAULT_DD = new HashMap<String, ModuleType>();
    
    static {
        DEFAULT_DD.put("web/WEB-INF/web.xml", ModuleType.WEB); // NOI18N
        DEFAULT_DD.put("src/conf/ejb-jar.xml", ModuleType.EJB); // NOI18N
        DEFAULT_DD.put("src/conf/application-client.xml", ModuleType.CLIENT); // NOI18N
    }
    
    /**
     * Detects Enterprise Application modules in the <code>appRoot</code>'s
     * subfolders recursively.
     *
     * @param folder root folder - typically enterprise application folder
     * @return map of FileObject to ModuleType entries
     */
    public static Map<FileObject, ModuleType> detectModules(final FileObject appRoot) {
        Map<FileObject, ModuleType> descriptors =
                new HashMap<FileObject, ModuleType>();
        // do detection for each subdirectory
        for (FileObject subprojectRoot : appRoot.getChildren()) {
            if (subprojectRoot.isFolder()) {
                ModuleType type = ModuleType.detectModuleType(subprojectRoot);
                if (type != null) {
                    descriptors.put(subprojectRoot, type);
                }
            }
        }
        return descriptors;
    }
    
    /**
     * Tries to detect Enterprise Application module's type in the given folder.
     *
     * @param folder folder which possibly containing module
     * @return <code>null</code> if no module were detected; instance otherwise
     */
    public static ModuleType detectModuleType(final FileObject moduleRoot) {
        ModuleType result = null;
        for (Map.Entry<String, ModuleType> entry : DEFAULT_DD.entrySet()) {
            FileObject ddFO = moduleRoot.getFileObject(entry.getKey());
            if (ddFO != null && ddFO.isData()) { // deployment descriptor detected
                result = entry.getValue();
            }
        }
        return result;
    }
    
}
