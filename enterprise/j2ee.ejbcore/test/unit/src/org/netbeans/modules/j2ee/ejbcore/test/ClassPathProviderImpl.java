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

package org.netbeans.modules.j2ee.ejbcore.test;

import java.net.URL;
import javax.ejb.Stateless;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class ClassPathProviderImpl implements ClassPathProvider {
    
    private ClassPath sourcePath;
    private final ClassPath compilePath;
    private final ClassPath bootPath;
    
    public ClassPathProviderImpl() {
        URL statelessAnnotationURL = Stateless.class.getProtectionDomain().getCodeSource().getLocation();
        this.compilePath = ClassPathSupport.createClassPath(new URL[] { FileUtil.getArchiveRoot(statelessAnnotationURL) });
        this.bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (ClassPath.SOURCE.equals(type)) {
            return sourcePath;
        } else if (ClassPath.COMPILE.equals(type)) {
            return compilePath;
        } else if (ClassPath.BOOT.equals(type)) {
            return bootPath;
        }
        return null;
    }
    
    public void setClassPath(FileObject[] sources) {
        sourcePath = ClassPathSupport.createClassPath(sources);
    }
    
}
