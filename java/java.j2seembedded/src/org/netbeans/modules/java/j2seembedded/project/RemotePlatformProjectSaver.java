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
package org.netbeans.modules.java.j2seembedded.project;

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.api.J2SECustomPropertySaver;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service=J2SECustomPropertySaver.class, projectType="org-netbeans-modules-java-j2seproject")
public class RemotePlatformProjectSaver implements J2SECustomPropertySaver {

    

    @Override
    public void save(@NonNull final Project project) {
        Parameters.notNull("project", project);         //NOI18N
        final Runnable action = new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean hasExtension = Utilities.hasRemoteExtension(project);
                    final Utilities.UpdateConfigResult res = Utilities.updateRemotePlatformConfigurations(project);
                    if (!hasExtension && res.hasRemotePlatform()) {
                        try {
                            Utilities.addRemoteExtension(project);
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        };
        runDeferred(action);
    }


    private static void runDeferred(@NonNull final Runnable r) {
        ProjectManager.mutex().postReadRequest(new Runnable() {
            @Override
            public void run() {
                ProjectManager.mutex().postWriteRequest(r);
            }
        });
    }

}
