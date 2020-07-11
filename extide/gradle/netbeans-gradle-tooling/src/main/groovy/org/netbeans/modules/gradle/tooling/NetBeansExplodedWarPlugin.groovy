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
import org.gradle.api.tasks.Sync

/**
 *
 * @author Laszlo Kishalmi
 */
class NetBeansExplodedWarPlugin implements Plugin<Project> {
    static String EXPLODED_WAR_TASK = "explodedWar"
    
    void apply(Project project) {
        project.afterEvaluate {
            if (project.plugins.hasPlugin('war') && (project.tasks.findByPath(EXPLODED_WAR_TASK) == null)){
                addTask(project)
            }
        }
    }
    
    void addTask(Project project) {
        def explodedWar = project.tasks.create(EXPLODED_WAR_TASK, Sync) {
            group = "build"
            into "${project.buildDir}/exploded/${project.war.archiveName}"
            with project.war
        }
        project.war.dependsOn explodedWar
    }
}

