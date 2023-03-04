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

package org.netbeans.modules.project.ui;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.problems.BrokenProjectNotifier;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

/**
 * Startup and shutdown hooks for projectui module.
 * @author Jesse Glick
 */
public class ProjectUiModule extends ModuleInstall {

    @Override
    public void restored() {
        if (!GraphicsEnvironment.isHeadless()) {
            Hacks.keepCurrentProjectNameUpdated();
        }
        BrokenProjectNotifier.getInstnace().start();
    }

    @Override
    public void close() {
        OpenProjectList.shutdown();
        // Just in case something was modified outside the usual customizer dialog:
        try {
            ProjectManager.getDefault().saveAllProjects();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        BrokenProjectNotifier.getInstnace().stop();
    }
    
}
