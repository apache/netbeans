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
package org.netbeans.modules.java.mx.project;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;

final class SuiteClassPathProvider extends ProjectOpenedHook implements ClassPathProvider, AnnotationProcessingQueryImplementation {
    private final SuiteProject project;
    private final ClassPath bootCP;

    public SuiteClassPathProvider(SuiteProject project) {
        this.project = project;
        List<ClassPath.Entry> entries = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries().entries();
        List<URL> roots = new ArrayList<>();
        for (ClassPath.Entry entry : entries) {
            URL root = entry.getURL();
            if (root.getPath().contains("/graal-sdk.jar")) {
                continue;
            }
            if (root.getPath().contains("/graaljs-scriptengine.jar")) {
                continue;
            }
            if (root.getPath().contains("/graal-sdk.src.zip")) {
                continue;
            }
            roots.add(entry.getURL());
        }
        this.bootCP = ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        SuiteSources.Group g = project.getSources().findGroup(file);
        if (g == null) {
            return null;
        }
        if (ClassPath.BOOT.equals(type)) {
            return bootCP;
        } else if (ClassPath.COMPILE.equals(type)) {
            return g.getCP();
        } else if (ClassPath.SOURCE.equals(type)) {
            return g.getSourceCP();
        } else if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
            return g.getProcessorCP();
        }
        return null;
    }

    @Override
    public void projectOpened() {
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {bootCP});
        for ( SuiteSources.Group s : project.getSources().groups()) {
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { s.getSourceCP() });
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] { s.getCP() });
        }
    }

    @Override
    public void projectClosed() {
        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, new ClassPath[] {bootCP});
        for ( SuiteSources.Group s : project.getSources().groups()) {
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] { s.getSourceCP() });
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, new ClassPath[] { s.getCP() });
        }
    }

    @Override
    public Result getAnnotationProcessingOptions(FileObject file) {
        SuiteSources.Group g = project.getSources().findGroup(file);
        return g;
    }
}
