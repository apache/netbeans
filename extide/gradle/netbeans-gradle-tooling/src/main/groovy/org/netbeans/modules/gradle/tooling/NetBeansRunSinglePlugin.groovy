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

package org.netbeans.modules.gradle.tooling

import java.lang.Iterable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.process.CommandLineArgumentProvider

/**
 *
 * @author Laszlo Kishalmi
 */
class NetBeansRunSinglePlugin implements Plugin<Project> {
    
    static String RUN_SINGLE_TASK = "runSingle"
    static String RUN_SINGLE_MAIN = "runClassName"
    static String RUN_SINGLE_ARGS = "runArgs"
    static String RUN_SINGLE_JVM_ARGS = "runJvmArgs"
    static String RUN_SINGLE_CWD = "runWorkingDir"
    static String RUN_SINGLE_ENV = "runEnvironment"
    
    void apply(Project project) {
        project.afterEvaluate {
            if (project.plugins.hasPlugin('java') 
                && (project.tasks.findByPath(RUN_SINGLE_TASK) == null)
                && project.hasProperty(RUN_SINGLE_MAIN)) {
                
                addTask(project)
            }
            if (project.hasProperty(RUN_SINGLE_JVM_ARGS)) {
                project.tasks.withType(JavaExec).configureEach { je ->
                    je.jvmArgumentProviders.add(new CommandLineArgumentProvider() {
                        public Iterable<String> asArguments() {
                            return project.getProperty(RUN_SINGLE_JVM_ARGS).tokenize(' ')
                        }
                    })
                }
            }
        }
    }

    void addTask(Project project) {
        def runSingle = project.tasks.create(RUN_SINGLE_TASK, JavaExec) {
            main = project.getProperty(RUN_SINGLE_MAIN)
            classpath = project.sourceSets.main.runtimeClasspath
            standardInput = System.in
                    
            if (project.hasProperty(RUN_SINGLE_ARGS)) {
                args = project.getProperty(RUN_SINGLE_ARGS).tokenize(' ')
            }
            if (project.hasProperty(RUN_SINGLE_CWD)) {
                workingDir = project.getProperty(RUN_SINGLE_CWD)
            }
            if (project.hasProperty(RUN_SINGLE_ENV)) {
                // Quoted space-separated expressions of <ENV_VAR>=<ENV_VALUE>
                // to set environment variables, or !<ENV_VAR>
                // to remove environment variables
                unescapeParameters(project.getProperty(RUN_SINGLE_ENV)).each {
                    env ->
                        if (env.startsWith("!")) {
                            environment.remove(env.substring(1))
                        } else {
                            def i = env.indexOf("=")
                            if (i > 0) {
                                environment[env.substring(0, i)] = env.substring(i + 1)
                            }
                        }
                }
            }
        }
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

        return params.toArray(new String[params.size()])
    }
}
