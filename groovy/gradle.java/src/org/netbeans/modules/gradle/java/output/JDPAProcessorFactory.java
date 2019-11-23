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
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.windows.IOColors;
import org.netbeans.modules.gradle.java.spi.debug.GradleJavaDebugger;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = OutputProcessorFactory.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public class JDPAProcessorFactory implements OutputProcessorFactory {

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
                if (!activated) {
                    String portStr = m.group(1);
                    GradleJavaDebugger dbg = cfg.getProject().getLookup().lookup(GradleJavaDebugger.class);
                    if (dbg != null) {
                        try {
                            dbg.attachDebugger(cfg.getTaskDisplayName() , "dt_socket", "localhost", portStr);
                            activated = true;
                        } catch (Exception ex) {
                            out.print(ex.getCause().getMessage(), null, IOColors.OutputType.ERROR);
                        }
                    }
                    out.print(line, null, IOColors.OutputType.LOG_DEBUG);
                } else {
                    RunUtils.cancelGradle(cfg);
                }
                return true;
            }
            return false;
        }

    }
}
