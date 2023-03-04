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

package org.netbeans.modules.gradle.tooling;

import java.io.File;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.War;

/**
 *
 * @author Laszlo Kishalmi
 */
class NetBeansExplodedWarPlugin implements Plugin<Project> {
    private static final String EXPLODED_WAR_TASK = "explodedWar";

    @Override
    public void apply(Project project) {
        project.afterEvaluate(p -> {
            if (p.getPlugins().hasPlugin("war") && (project.getTasks().findByPath(EXPLODED_WAR_TASK) == null)){
                addTask(p);
            }
        });
    }

    private void addTask(Project p) {
        War war = (War) p.getTasks().getByName("war");
        p.getTasks().register(EXPLODED_WAR_TASK, Sync.class, (sync) -> {
            sync.setGroup("build");
            sync.into(new File(new File(p.getBuildDir(), "exploded"), war.getArchiveFileName().get()));
            sync.with(war);
            
        });
        war.dependsOn(EXPLODED_WAR_TASK);
    }

}

