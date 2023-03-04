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
package org.netbeans.modules.java.mx.project;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;

final class SuiteClassPathProvider extends ProjectOpenedHook implements ClassPathProvider, AnnotationProcessingQueryImplementation {
    private final SuiteProject project;
    private final Jdks jdks;

    public SuiteClassPathProvider(SuiteProject project, Jdks jdks) {
        this.project = project;
        this.jdks = jdks;
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        SuiteSources.Group g = project.getSources().findGroup(file);
        if (g == null) {
            return null;
        }
        if (ClassPath.BOOT.equals(type)) {
            return g.getBootCP();
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
        for ( SuiteSources.Group s : project.getSources().groups()) {
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] { s.getBootCP() });
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { s.getSourceCP() });
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] { s.getCP() });
        }
    }

    @Override
    public void projectClosed() {
        for ( SuiteSources.Group s : project.getSources().groups()) {
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, new ClassPath[] { s.getBootCP() });
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
