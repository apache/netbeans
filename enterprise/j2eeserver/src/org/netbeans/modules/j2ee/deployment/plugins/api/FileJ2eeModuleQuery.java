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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;

/**
 * Finds the J2EE module for a file.
 * 
 * @author sherold
 */
public class FileJ2eeModuleQuery {
    
    /** Creates a new instance of FileJ2eeModuleQuery */
    private FileJ2eeModuleQuery() {
    }
    
    /**
     * Finds a J2EE module which owns the specified file.
     * 
     * @param fileObject the file
     * 
     * @return J2EE module which owns the specified file, or null if there is no
     *         J2EE module containing it. 
     */
    public static J2eeModule getJ2eeModule(FileObject fileObject) {
        if (fileObject == null) {
            throw new NullPointerException("FileObject parameter cannot be null."); // NOI18N
        }
        
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return null;
        }
        
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return null;
        }
        
        return j2eeModuleProvider.getJ2eeModule();
    }
}
