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

package org.netbeans.modules.gradle.java.output;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.output.OutputDisplayer;
import org.netbeans.modules.gradle.api.output.OutputProcessor;
import org.netbeans.modules.gradle.api.output.OutputProcessorFactory;
import org.netbeans.modules.gradle.java.api.output.LocationOpener;
import org.netbeans.modules.gradle.spi.GradleSettings;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.windows.IOColors;
import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = OutputProcessorFactory.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public final class JavaCompilerProcessorFactory implements OutputProcessorFactory {

    @Override
    public Set<? extends OutputProcessor> createOutputProcessors(RunConfig cfg) {
        return new HashSet<>(Arrays.asList(new StackTraceProcessor(cfg)));
    }

    static class StackTraceProcessor implements OutputProcessor {

        private static final Pattern STACKTRACE_PATTERN = Pattern.compile("(.*)at ((\\w[\\w\\.]*)/)?(\\w[\\w\\.\\$<>]*)\\((\\w+)\\.java\\:([0-9]+)\\)(.*)");
        private static final IOColors.OutputType OUT_TYPE = IOColors.OutputType.ERROR;
        private final Project project;
        private final ClassPath classPath;

        // Used in unittest only
        StackTraceProcessor() {
            project = null;
            classPath = null;
        }

        private StackTraceProcessor(RunConfig cfg) {
            project = cfg.getProject();
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            ClassPath[] projectClassPath = cpProvider.getProjectClassPath(ClassPath.EXECUTE);
            ClassPath[] bootClassPath = cpProvider.getProjectClassPath(ClassPath.BOOT);
            classPath = ClassPathSupport.createProxyClassPath(
                    ClassPathSupport.createProxyClassPath(projectClassPath),
                    ClassPathSupport.createProxyClassPath(bootClassPath)
            );
        }

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = STACKTRACE_PATTERN.matcher(line);
            if (m.matches()) {
                String prefix = m.group(1);
                String modulePrefix = m.group(2) != null ? m.group(2) : ""; //NOI18N
                //String module = m.group(3);
                String method = m.group(4);
                String fileName = m.group(5);
                String lineNum = m.group(6);
                String postfix = m.group(7);

                int ppos = method.indexOf(fileName);
                if (ppos >= 0) {
                    String pack = method.substring(0, ppos).replace('.', '/');
                    String javaName = pack + fileName + ".java"; //NOI18N

                    int lineInt = Integer.parseInt(lineNum);

                    Runnable action = openFileAt(classPath, javaName, lineInt);

                    out.print(prefix).print("at ", null, OUT_TYPE)
                            .print(modulePrefix, null, OUT_TYPE)
                            .print(method, null, OUT_TYPE)
                            .print("(", null, OUT_TYPE);
                    out.print(fileName + ".java:" + lineNum, action);
                    out.print(")" + postfix, null, OUT_TYPE);
                    return true;
                }
            }
            return false;
        }
    }

    private static Runnable openFileAt(final ClassPath classPath, final String fileName, final int line) {
        return new Runnable() {
            @Override
            public void run() {
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                String resourceName = baseName + ".class"; //NOI18N

                FileObject resource = classPath.findResource(resourceName);
                FileObject javaFo = null;
                if (resource != null) {
                    FileObject cpRoot = classPath.findOwnerRoot(resource);
                    if (cpRoot != null) {
                        URL url = URLMapper.findURL(cpRoot, URLMapper.INTERNAL);
                        SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(url);
                        FileObject[] rootz = res.getRoots();
                        for (FileObject root : rootz) {
                            javaFo = root.getFileObject(fileName);
                            if (javaFo != null) {
                                LocationOpener.openAtLine(javaFo, line, GradleSettings.getDefault().isReuseEditorOnStackTace());
                                break;
                            }
                        }
                    }
                }
                if (javaFo == null) {
                    StatusDisplayer.getDefault().setStatusText("Not found: " + fileName);
                }
            }

        };
    }

}
