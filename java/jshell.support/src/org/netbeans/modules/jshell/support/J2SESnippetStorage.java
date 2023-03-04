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
package org.netbeans.modules.jshell.support;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = SnippetStorage.class,
        projectType = {
            "org-netbeans-modules-java-j2seproject",
            "org.netbeans.modules.java.j2semodule"
        })
public class J2SESnippetStorage implements SnippetStorage {
    private final Project project;

    public J2SESnippetStorage(Project project) {
        this.project = project;
    }
    
    @Override
    public FileObject getStorageFolder(boolean createIfMissing) {
        return project.getProjectDirectory().getFileObject("nbproject/private");
    }

    @Override
    public String resourcePrefix() {
        return "jshell-snippets";
    }

    @Override
    public String startupSnippets(String action) {
        return "startup";
    }
}
