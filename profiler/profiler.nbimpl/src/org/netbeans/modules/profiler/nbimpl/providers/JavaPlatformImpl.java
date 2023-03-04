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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.profiler.spi.JavaPlatformProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Hurka
 */

class JavaPlatformImpl extends JavaPlatformProvider {

    private final JavaPlatform platform;
    
    JavaPlatformImpl(JavaPlatform p) {
        platform = p;
    }
    
    JavaPlatform getDelegate() {
        return platform;
    }
    
    @Override
    public String getDisplayName() {
        return platform.getDisplayName();
    }

    @Override
    public Map<String, String> getSystemProperties() {
        return platform.getSystemProperties();
    }
    
    @Override
    public Map<String, String> getProperties() {
        return platform.getProperties();
    }

    @Override
    public String getPlatformJavaFile() {
        if (JavaPlatformManagerImpl.REMOTE_J2SE.getName().equals(platform.getSpecification().getName())) {
            //Todo: create API in JavaPlatform to return install folder as an URI
            final String installFolder = platform.getProperties().get("platform.install.folder"); //NOI18N
            if (installFolder != null) {
                return String.format("%s/bin/java", installFolder); //NOI18N
            }
        } else {
            FileObject javaBinary = platform.findTool("java"); // NOI18N
            if (javaBinary != null) {
                return FileUtil.toFile(javaBinary).getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public String getPlatformId() {
        return platform.getProperties().get("platform.ant.name");
    }
    
}
