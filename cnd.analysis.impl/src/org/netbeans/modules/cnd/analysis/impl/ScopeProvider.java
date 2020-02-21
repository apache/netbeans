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
package org.netbeans.modules.cnd.analysis.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.analysis.spi.AnalysisScopeProvider;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 */
@ProjectServiceProvider(projectType="org-netbeans-modules-cnd-makeproject", service=AnalysisScopeProvider.class)
public class ScopeProvider implements AnalysisScopeProvider {

    private final Project project;

    public ScopeProvider(Project project) {
        this.project = project;
    }

    @Override
    public Scope getScope() {
        Collection<FileObject> sourceRoots = null;
        Collection<NonRecursiveFolder> folders = null;
        Collection<FileObject> files = null;
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp.gotDescriptor()) {
            MakeConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
            files = new ArrayList<FileObject>();
            for (Item item : configurationDescriptor.getProjectItems()) {
                PredefinedToolKind defaultTool = item.getDefaultTool();
                if (defaultTool == PredefinedToolKind.CCompiler || defaultTool == PredefinedToolKind.CCCompiler) {
                    files.add(item.getFileObject());
                }
            }
        }
        return Scope.create(sourceRoots, folders, files);
    }
}
