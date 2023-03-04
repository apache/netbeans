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

package org.netbeans.modules.java.platform.classpath;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.modules.SpecificationVersion;


@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.classpath.ClassPathProvider.class, position=150)
public class PlatformClassPathProvider implements ClassPathProvider {
    private static final SpecificationVersion JAVA_9 = new SpecificationVersion("9");   //NOI18N

    private static final Set<? extends String> SUPPORTED_CLASS_PATH_TYPES =
            new HashSet<String>(Arrays.asList(new String[]{
                ClassPath.SOURCE,
                ClassPath.BOOT,
                ClassPath.COMPILE,
                JavaClassPathConstants.MODULE_BOOT_PATH
            }));



    /** Creates a new instance of PlatformClassPathProvider */
    public PlatformClassPathProvider() {
    }
    
    
    public ClassPath findClassPath(FileObject fo, String type) {
        if (!SUPPORTED_CLASS_PATH_TYPES.contains(type)) {
            return null;
        }
        if (fo == null || type == null) {
            throw new IllegalArgumentException();
        }
        JavaPlatform lp = this.getLastUsedPlatform(fo);
        JavaPlatform[] platforms;
        if (lp != null) {
            platforms = new JavaPlatform[] {lp};
        }
        else {
            JavaPlatformManager manager = JavaPlatformManager.getDefault();
            platforms = manager.getInstalledPlatforms();
        }
        for (JavaPlatform jp : platforms) {
            ClassPath bootClassPath = jp.getBootstrapLibraries();
            ClassPath libraryPath = jp.getStandardLibraries();
            ClassPath sourcePath = jp.getSourceFolders();
            FileObject root = null;
            boolean jdk9 = JAVA_9.compareTo(jp.getSpecification().getVersion()) <= 0;
            if (ClassPath.SOURCE.equals(type) && sourcePath != null &&
                (root = sourcePath.findOwnerRoot(fo))!=null) {
                this.setLastUsedPlatform (root,jp);
                if (jdk9) {
                    return ClassPathSupport.createClassPath(root);
                }
                return sourcePath;
            } else if (ClassPath.BOOT.equals(type) &&
                    (root = getArtefactOwner(fo, bootClassPath, libraryPath, sourcePath)) != null ) {
                this.setLastUsedPlatform (root,jp);
                return bootClassPath;
            } else if (ClassPath.COMPILE.equals(type)) {
                if (libraryPath != null && (root = libraryPath.findOwnerRoot(fo))!=null) {
                    this.setLastUsedPlatform (root,jp);
                    return libraryPath;
                }
                else if ((bootClassPath != null && (root = bootClassPath.findOwnerRoot (fo))!=null) ||
                    (sourcePath != null && (root = sourcePath.findOwnerRoot(fo)) != null)) {
                    return this.getEmptyClassPath ();
                }
            } else if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type)  &&
                    jdk9 &&
                    (root = getArtefactOwner(fo, bootClassPath, libraryPath, sourcePath)) != null) {
                this.setLastUsedPlatform (root,jp);
                return bootClassPath;
            }
        }
        return null;
    }

    private synchronized ClassPath getEmptyClassPath () {
        if (this.emptyCp == null ) {
            this.emptyCp = ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        }
        return this.emptyCp;
    }

    private synchronized void setLastUsedPlatform (FileObject root, JavaPlatform platform) {
        this.lastUsedRoot = root;
        this.lastUsedPlatform = platform;
    }

    private synchronized JavaPlatform getLastUsedPlatform (FileObject file) {
        if (this.lastUsedRoot != null && FileUtil.isParentOf(this.lastUsedRoot,file)) {
            return lastUsedPlatform;
        }
        else {
            return null;
        }
    }

    @CheckForNull
    private static FileObject getArtefactOwner(
            @NonNull final FileObject file,
            @NonNull final ClassPath... cps) {
        for (ClassPath cp : cps) {
            FileObject root;
            if (cp != null && (root = cp.findOwnerRoot (file)) != null) {
                return root;
            }
        }
        return null;
    }

    private FileObject lastUsedRoot;
    private JavaPlatform lastUsedPlatform;
    private ClassPath emptyCp;
}
