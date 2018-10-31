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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColors;
import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = OutputProcessorFactory.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public class JDPAProcessorFactory implements OutputProcessorFactory {

    private static final RequestProcessor RP = new RequestProcessor("GradleDebug", 1);

    @Override
    public Set<? extends OutputProcessor> createOutputProcessors(RunConfig cfg) {
        return Collections.singleton(new JDPAOutputProcessor(cfg));
    }

    private static class JDPAOutputProcessor implements OutputProcessor {

        private final static Pattern JDPA_LISTEN = Pattern.compile("Listening for transport dt_socket at address: (\\d+)");
        final RunConfig cfg;
        boolean activated;

        public JDPAOutputProcessor(RunConfig cfg) {
            this.cfg = cfg;
        }

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = JDPA_LISTEN.matcher(line);
            if (m.matches()) {
                String portStr = m.group(1);
                int port = 5005;
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException ex) {
                }
                final int finalPort = port;

                if (!activated) {
                    activated = true;
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            Map<String, Object> services = new HashMap<>();
                            services.put("name", cfg.getTaskDisplayName());
                            services.put("baseDir", FileUtil.toFile(cfg.getProject().getProjectDirectory()));
                            services.put("jdksources", getJdkSources());
                            services.put("sourcepath", getSources());
                            try {
                                JPDADebugger.attach("localhost", finalPort, new Object[]{services});
                            } catch (DebuggerStartException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
                out.print(line, null, IOColors.OutputType.LOG_DEBUG);
                return true;
            }
            return false;
        }

        private ClassPath getJdkSources() {
            JavaPlatform jdk = JavaPlatformManager.getDefault().getDefaultPlatform();
            if (jdk != null) {
                return jdk.getSourceFolders();
            }
            return null;
        }

        private ClassPath getSources() {
            ProjectSourcesClassPathProvider pgcpp = cfg.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class);
            List<SourceForBinaryQueryImplementation2> sourceQueryImpls = new ArrayList<>(2);
            sourceQueryImpls.addAll(cfg.getProject().getLookup().lookupAll(SourceForBinaryQueryImplementation2.class));
            sourceQueryImpls.addAll(Lookup.getDefault().lookupAll(SourceForBinaryQueryImplementation2.class));

            Set<FileObject> srcs = new LinkedHashSet<>();
            for (ClassPath projectSourcePath : pgcpp.getProjectClassPath(ClassPath.SOURCE)) {
                srcs.addAll(Arrays.asList(projectSourcePath.getRoots()));
            }

            for (ClassPath cp : pgcpp.getProjectClassPath(ClassPath.EXECUTE)) {
                for (ClassPath.Entry entry : cp.entries()) {
                    URL url = entry.getURL();
                    SourceForBinaryQueryImplementation2.Result ret;
                    for (SourceForBinaryQueryImplementation2 sourceQuery : sourceQueryImpls) {
                        ret = sourceQuery.findSourceRoots2(url);
                        if (ret != null) {
                            List<FileObject> roots = Arrays.asList(ret.getRoots());
                            if (!roots.isEmpty()) {
                                srcs.addAll(roots);
                                break;
                            }
                        }
                    }
                }
            }
            FileObject[] roots = srcs.toArray(new FileObject[srcs.size()]);
            return ClassPathSupport.createClassPath(roots);
        }

    }
}
