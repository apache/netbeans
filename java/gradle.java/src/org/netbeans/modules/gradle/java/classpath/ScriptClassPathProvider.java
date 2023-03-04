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
package org.netbeans.modules.gradle.java.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.gradle.api.GradleProjects;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ServiceProvider(service = ClassPathProvider.class)
public class ScriptClassPathProvider implements ClassPathProvider {

    final ClassPath BOOT_CP;
    final ClassPath GRADLE_CP;
    final ClassPath SOURCE_CP;

    public ScriptClassPathProvider() {
        BOOT_CP = ClassPathSupport.createProxyClassPath(
                JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries(),
                ClassPathFactory.createClassPath(new GradleScriptClassPath()));
        GRADLE_CP = BOOT_CP;
        SOURCE_CP = ClassPathFactory.createClassPath(new GradleScriptSourcePath());
    }

    HashMap<FileObject, ClassPath> cache = new HashMap<>();

    @Override
    public ClassPath findClassPath(FileObject fo, String type) {
        if ("classpath/html5".equals(type)) {
            return null;
        }
        boolean isGradle = "gradle".equals(fo.getExt());
        if (fo.isFolder() && (FileUtil.toFile(fo) != null)) {
            isGradle = GradleProjects.testForProject(FileUtil.toFile(fo));
        }

        if (isGradle) {
            switch (type) {
                case ClassPath.BOOT:
                    return BOOT_CP;
                case ClassPath.SOURCE:
                    return SOURCE_CP;
                case ClassPath.COMPILE:
                    return GRADLE_CP;
                case ClassPath.EXECUTE:
                    return GRADLE_CP;
            }
        }
        return null;
    }

    public static class GradleScriptSourcePath extends AbstractGradleScriptClassPath {

        @Override
        protected List<FileObject> createPath() {
            List<FileObject> ret = new ArrayList<>();
            if (distDir != null) {
                FileObject srcDir = FileUtil.toFileObject(new File(distDir, "src"));
                if ((srcDir != null) && srcDir.isFolder()) {
                    Enumeration<? extends FileObject> folders = srcDir.getFolders(false);
                    while (folders.hasMoreElements()) {
                        ret.add(folders.nextElement());
                    }
                }
            }
            return ret;
        }
    }

    public static final class GradleScriptClassPath extends AbstractGradleScriptClassPath {

        @Override
        protected List<FileObject> createPath() {
            List<FileObject> ret = new ArrayList<>();
            if (distDir != null) {
                addJars(ret, FileUtil.toFileObject(new File(distDir, "lib")));
                addJars(ret, FileUtil.toFileObject(new File(distDir, "lib/plugins")));
            }
            return ret;
        }

        private void addJars(List<FileObject> ret, FileObject dir) {
            if ((dir != null) && dir.isFolder()) {
                for (FileObject child : dir.getChildren()) {
                    if (FileUtil.isArchiveFile(child)) {
                        ret.add(FileUtil.getArchiveRoot(child));
                    }
                }
            }
        }
    }
}
