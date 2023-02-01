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

package org.netbeans.modules.gradle.tooling;

import static java.util.Arrays.asList;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.GradleVersion;
/**
 *
 * @author Laszlo Kishalmi
 */
class NetBeansRunSinglePlugin implements Plugin<Project> {
    private static final GradleVersion GRADLE_VERSION = GradleVersion.current().getBaseVersion();
    private static final Logger LOG = Logging.getLogger(NetBeansRunSinglePlugin.class);

    private static final String RUN_SINGLE_TASK = "runSingle";
    private static final String RUN_SINGLE_MAIN = "runClassName";
    private static final String RUN_SINGLE_ARGS = "runArgs";
    private static final String RUN_SINGLE_JVM_ARGS = "runJvmArgs";
    private static final String RUN_SINGLE_CWD = "runWorkingDir";

    @Override
    public void apply(Project project) {
        project.afterEvaluate(p -> {
            if (p.getPlugins().hasPlugin("java") 
                    && (project.getTasks().findByPath(RUN_SINGLE_TASK) == null)
                    && project.hasProperty(RUN_SINGLE_MAIN)){
                addTask(p);
            }
            p.getTasks().withType(JavaExec.class).configureEach(je -> {
                if (p.hasProperty(RUN_SINGLE_JVM_ARGS)) {
                    // Property jvmArgumentProviders should not be implemented as a lambda to allow execution optimizations.
                    // See https://docs.gradle.org/current/userguide/validation_problems.html#implementation_unknown
                    je.getJvmArgumentProviders().add(new CommandLineArgumentProvider() {
                        // Do not convert to lambda.
                        @Override
                        public Iterable<String> asArguments() {
                            return asList(p.property(RUN_SINGLE_JVM_ARGS).toString().split(" "));
                        }
                    });
                }
                try {
                    je.setStandardInput(System.in);
                } catch (RuntimeException ex) {
                    if(LOG.isEnabled(LogLevel.DEBUG)) {
                        LOG.debug("Failed to set STDIN for Plugin: " + je.toString(), ex);
                    } else {
                        LOG.info("Failed to set STDIN for Plugin: " + je.toString());
                    }
                }
                if (project.hasProperty(RUN_SINGLE_CWD)) {
                    je.setWorkingDir(project.property(RUN_SINGLE_CWD).toString());
                }
            });
        });
    }

    private void addTask(Project project) {
        SourceSetContainer sourceSets = project.getExtensions().findByType(SourceSetContainer.class);

        project.getTasks().register(RUN_SINGLE_TASK, JavaExec.class, (je) -> {
            String mainClass = project.property(RUN_SINGLE_MAIN).toString();
            if (GRADLE_VERSION.compareTo(GradleVersion.version("6.4")) < 0) {
                // Using setMain to keep the backward compatibility before Gradle 6.4
                je.setMain(mainClass);
            } else {
                je.getMainClass().set(mainClass);
            }
            je.setClasspath(sourceSets.findByName("main").getRuntimeClasspath());
            if (project.hasProperty(RUN_SINGLE_ARGS)) {
                je.setArgs(asList(project.property(RUN_SINGLE_ARGS).toString().split(" ")));
            }
        });
    }

}
