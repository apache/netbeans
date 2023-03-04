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
package org.netbeans.modules.jshell.env;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ClassPathProvider.class, position=1200)
public class ShellClasspathProvider implements ClassPathProvider {

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        JShellEnvironment jshe = ShellRegistry.get().getOwnerEnvironment(file);
        if (jshe == null) {
            return null;
        }
        ShellSession ss = jshe.getSession();
        if (ss == null) {
            return null;
        }
        ClasspathInfo cpi = ss.getClasspathInfo();
        if (cpi == null) {
            return null;
        }
        switch (type) {
            case JavaClassPathConstants.MODULE_BOOT_PATH:
                return cpi.getClassPath(PathKind.MODULE_BOOT);
                
            case JavaClassPathConstants.MODULE_COMPILE_PATH:
                return cpi.getClassPath(PathKind.MODULE_COMPILE);
                
            case JavaClassPathConstants.MODULE_CLASS_PATH:
                return cpi.getClassPath(PathKind.MODULE_CLASS);
                
            case JavaClassPathConstants.MODULE_SOURCE_PATH:
                return cpi.getClassPath(PathKind.MODULE_SOURCE);
                
            case ClassPath.COMPILE:
                return cpi.getClassPath(PathKind.COMPILE);
            case ClassPath.SOURCE:
                return cpi.getClassPath(PathKind.SOURCE);
            case ClassPath.BOOT:
                return cpi.getClassPath(PathKind.BOOT);
        }
        return null;
    }
}
