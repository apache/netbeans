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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

/**
 *
 * @author Laszlo Kishalmi
 */
class NetBeansRunSinglePlugin implements Plugin<Project> {
    
    static String RUN_SINGLE_TASK = "runSingle"
    static String RUN_SINGLE_MAIN = "runClassName"
    static String RUN_SINGLE_ARGS = "runArgs"
    
    void apply(Project project) {
        project.afterEvaluate {
            if (project.plugins.hasPlugin('java') 
                && (project.tasks.findByPath(RUN_SINGLE_TASK) == null)
                && project.hasProperty(RUN_SINGLE_MAIN)) {
                
                addTask(project)
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
        }
    }
    
}

