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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;

/**
 *
 * @author  tom
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceLevelQueryImplementation.class, position=150)
public class J2SEPlatformSourceLevelQueryImpl implements SourceLevelQueryImplementation {

    /** Creates a new instance of J2SEPlatformSourceLevelQueryImpl */
    public J2SEPlatformSourceLevelQueryImpl() {
    }

    public String getSourceLevel(org.openide.filesystems.FileObject javaFile) {
        try {
        } catch (Exception e) {}
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms ();
        for (int i=0; i< platforms.length; i++) {
            if (J2SEPlatformImpl.PLATFORM_J2SE.equalsIgnoreCase(platforms[i].getSpecification().getName()) && platforms[i].getSourceFolders().contains(javaFile)) {   //NOI18N
                return platforms[i].getSpecification().getVersion().toString();
            }
        }        
        return null;
    }    
        
            
}
