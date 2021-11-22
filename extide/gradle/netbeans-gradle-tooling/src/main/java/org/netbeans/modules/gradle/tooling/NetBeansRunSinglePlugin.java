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

import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.Map;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.process.CommandLineArgumentProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
class NetBeansRunSinglePlugin implements Plugin<Project> {

    private static final String RUN_SINGLE_TASK = "runSingle";
    private static final String RUN_SINGLE_MAIN = "runClassName";
    private static final String RUN_SINGLE_ARGS = "runArgs";
    private static final String RUN_SINGLE_JVM_ARGS = "runJvmArgs";
    private static final String RUN_SINGLE_CWD = "runWorkingDir";
    private static final String RUN_SINGLE_ENV = "runEnvironment";

    @Override
    public void apply(Project project) {
        project.afterEvaluate(p -> {
            if (p.getPlugins().hasPlugin("java") 
                    && (project.getTasks().findByPath(RUN_SINGLE_TASK) == null)
                    && project.hasProperty(RUN_SINGLE_CWD)){
                addTask(p);
            }
            if(p.hasProperty(RUN_SINGLE_JVM_ARGS)) {
                p.getTasks().withType(JavaExec.class).configureEach(je -> {
                    je.getJvmArgumentProviders().add(() -> {
                        return asList(p.property(RUN_SINGLE_JVM_ARGS).toString().split(" "));
                    });
                });
            }
        });
    }

    private void addTask(Project project) {
        Map<String,SourceSet> sourceSets = (Map<String,SourceSet>) project.property("sourceSets");

        JavaExec je = new JavaExec();
        je.setMain(project.property(RUN_SINGLE_MAIN).toString());
        je.setClasspath(sourceSets.get("main").getRuntimeClasspath());
        je.setStandardInput(System.in);

        if (project.hasProperty(RUN_SINGLE_ARGS)) {
            je.setArgs(asList(project.property(RUN_SINGLE_ARGS).toString().split(" ")));
        }

        if(project.hasProperty(RUN_SINGLE_CWD)) {
            je.setWorkingDir(project.property(RUN_SINGLE_CWD).toString());
        }

        if (project.hasProperty(RUN_SINGLE_ENV)) {
            // Quoted space-separated expressions of <ENV_VAR>=<ENV_VALUE>
            // to set environment variables, or !<ENV_VAR>
            // to remove environment variables
            Arrays.stream(unescapeParameters(project.property(RUN_SINGLE_ENV).toString()))
                    .forEach(env -> {
                        if (env.startsWith("!")) {
                            je.getEnvironment().remove(env);
                        } else {
                            int i = env.indexOf("=");
                            if (i > 0) {
                                je.getEnvironment().put(env.substring(0, i), env.substring(i + 1));
                            }
                        }
                    });
        }

        project.getTasks().create(RUN_SINGLE_TASK, JavaExec.class, je);
    }

    private String[] unescapeParameters(String s) {
        final int NULL = 0x0;
        final int IN_PARAM = 0x1;
        final int IN_DOUBLE_QUOTE = 0x2;
        final int IN_SINGLE_QUOTE = 0x3;
        ArrayList<String> params = new ArrayList<>(5);
        char c;

        int state = NULL;
        StringBuilder buff = new StringBuilder(20);
        int slength = s.length();

        for (int i = 0; i < slength; i++) {
            c = s.charAt(i);
            switch (state) {
                case NULL:
                    switch (c) {
                        case '\'':
                            state = IN_SINGLE_QUOTE;
                            break;
                        case '"':
                            state = IN_DOUBLE_QUOTE;
                            break;
                        default:
                            if (!Character.isWhitespace(c)) {
                                buff.append(c);
                                state = IN_PARAM;
                            }
                    }
                    break;
                case IN_SINGLE_QUOTE:
                    if (c != '\'') {
                        buff.append(c);
                    } else {
                        state = IN_PARAM;
                    }
                    break;
                case IN_DOUBLE_QUOTE:
                    switch (c) {
                        case '\\':
                            char peek = (i < slength - 1) ? s.charAt(i+1) : Character.MIN_VALUE;
                            if (peek == '"' || peek =='\\') {
                                buff.append(peek);
                                i++;
                            } else {
                                buff.append(c);
                            }
                            break;
                        case '"':
                            state = IN_PARAM;
                            break;
                        default:
                            buff.append(c);
                    }
                    break;
                case IN_PARAM:
                    switch (c) {
                        case '\'':
                            state = IN_SINGLE_QUOTE;
                            break;
                        case '"':
                            state = IN_DOUBLE_QUOTE;
                            break;
                        default:
                          if (Character.isWhitespace(c)) {
                              params.add(buff.toString());
                              buff.setLength(0);
                              state = NULL;
                          } else {
                              buff.append(c);
                          }
                    }
                    break;
            }
        }
        if (buff.length() > 0) {
            params.add(buff.toString());
        }

        return params.toArray(new String[params.size()]);
    }
}
