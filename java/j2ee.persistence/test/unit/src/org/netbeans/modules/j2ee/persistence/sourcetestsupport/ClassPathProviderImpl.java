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

package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * An implementation of ClassPathProvider for running tests. Includes <tt>eclipselink jars</tt> that contains
 * <code>javax.persistence.*</code> stuff. 
 *
 * @author Erno Mononen
 */
public class ClassPathProviderImpl implements ClassPathProvider {
    
    private final ClassPath classPath;
    
    public ClassPathProviderImpl(FileObject[] sources){
        this.classPath = ClassPathSupport.createClassPath(sources);
    }
    
    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if(ClassPath.SOURCE.equals(type)){
            return this.classPath;
        }
        if (ClassPath.COMPILE.equals(type)){
            try {
                URL eclipselinkJarUrl = Class.forName("javax.persistence.EntityManager").getProtectionDomain().getCodeSource().getLocation();
                URL javaEE8ApiJarUrl = Class.forName("javax.annotation.Resource").getProtectionDomain().getCodeSource().getLocation();
                return ClassPathSupport.createClassPath(new URL[]{
                    FileUtil.getArchiveRoot(eclipselinkJarUrl),
                    FileUtil.getArchiveRoot(javaEE8ApiJarUrl)
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        if (ClassPath.BOOT.equals(type)){
            return JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        return null;
    }
}
