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

package org.netbeans.modules.cnd.simpleunit.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.builds.MakefileTargetProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 */
public class MakefileUtils {

    private MakefileUtils() {
    }

    public static FileObject getMakefile(Project project) {
        ConfigurationDescriptorProvider confDescriptorProvider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (confDescriptorProvider != null) {
            MakeConfigurationDescriptor makeConfDescriptor = confDescriptorProvider.getConfigurationDescriptor();
            if (makeConfDescriptor != null && !makeConfDescriptor.getProjectMakefileName().isEmpty()) {
                return project.getProjectDirectory().getFileObject(makeConfDescriptor.getProjectMakefileName());
            }
        }
        return null;
    }

    public static boolean hasTestTargets(Project project) {
        FileObject makefile = getMakefile(project);
        if(makefile != null && makefile.isValid()) {
            try {
                DataObject dataObject = DataObject.find(makefile);
                MakefileTargetProvider targetProvider = dataObject.getLookup().lookup(MakefileTargetProvider.class);
                if (targetProvider != null) {
                    Set<String> targets = targetProvider.getRunnableTargets();
                    return targets.contains("test") || targets.contains("build-tests"); // NOI18N
                }
            } catch (DataObjectNotFoundException ex) {
            } catch (IOException ex) {}        
        }
        return false;
    }

    public static void createTestTargets(Project project) {
        if (hasTestTargets(project)) {
            return;
        }
        FileObject makefile = getMakefile(project);
        StringBuilder makefiledata;
        try {
            makefiledata = new StringBuilder(makefile.asText());
            makefiledata.append("\n\n") // NOI18N
                .append("# build tests\n") // NOI18N
                .append("build-tests: .build-tests-post\n") // NOI18N
                .append("\n") // NOI18N
                .append(".build-tests-pre:\n") // NOI18N
                .append("# Add your pre 'build-tests' code here...\n") // NOI18N
                .append("\n") // NOI18N
                .append(".build-tests-post: .build-tests-impl\n") // NOI18N
                .append("# Add your post 'build-tests' code here...\n") // NOI18N
                .append("\n") // NOI18N
                .append("\n") // NOI18N
                .append("# run tests\n") // NOI18N
                .append("test: .test-post\n") // NOI18N
                .append("\n") // NOI18N
                .append(".test-pre:\n") // NOI18N
                .append("# Add your pre 'test' code here...\n") // NOI18N
                .append("\n") // NOI18N
                .append(".test-post: .test-impl\n") // NOI18N
                .append("# Add your post 'test' code here...\n"); // NOI18N
            OutputStream outputStream = makefile.getOutputStream();
            try {
                outputStream.write(makefiledata.toString().getBytes());
            } finally {
                outputStream.close();
            }
        } catch (IOException ex) {
        }
    }
}
