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
package org.netbeans.modules.java.openjdk.project;

import org.netbeans.modules.java.openjdk.project.ModuleDescription.ModuleRepository;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 *
 * @author lahvac
 */
public class OpenProjectHookImpl extends ProjectOpenedHook {

    private final JDKProject project;
    private final ClassPathProviderImpl cpp;
    private final ModuleRepository repository;

    public OpenProjectHookImpl(JDKProject project, ClassPathProviderImpl cpp, ModuleRepository repository) {
        this.project = project;
        this.cpp = cpp;
        this.repository = repository;
    }
    
    @Override
    protected void projectOpened() {
        if (repository != null) {
            repository.projectOpened(project);
        }
        cpp.registerClassPaths();
    }

    @Override
    protected void projectClosed() {
        cpp.unregisterClassPaths();
        if (repository != null) {
            repository.projectClosed(project);
        }
    }
    
}
