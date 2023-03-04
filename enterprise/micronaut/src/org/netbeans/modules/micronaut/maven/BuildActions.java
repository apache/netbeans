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
package org.netbeans.modules.micronaut.maven;

import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class BuildActions {
    
    @ActionID(id = "org.netbeans.modules.micronaut.maven.nativeBuild", category = "Project")
    @ActionRegistration(displayName = "#LBL_NativeBuild", lazy=false)
    @ActionReference(position = 520, path = "Projects/org-netbeans-modules-maven/Actions")
    @NbBundle.Messages({
        "LBL_NativeBuild=Build Native Executable"
    })
    public static Action    createNativeBuild() {
        return ProjectSensitiveActions.projectSensitiveAction(new NativeBuildPerformer(), Bundle.LBL_NativeBuild(), null);
    }
    
    private static class NativeBuildPerformer implements ProjectActionPerformer {
        @Override
        public boolean enable(Project project) {
            ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
            return 
                    ap != null &&
                    Arrays.asList(ap.getSupportedActions()).contains(MicronautMavenConstants.ACTION_NATIVE_COMPILE) &&
                    ap.isActionEnabled(MicronautMavenConstants.ACTION_NATIVE_COMPILE, Lookup.EMPTY);
        }

        @Override
        public void perform(Project project) {
            ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
            if (ap == null) {
                return;
            }
            if (!Arrays.asList(ap.getSupportedActions()).contains(MicronautMavenConstants.ACTION_NATIVE_COMPILE) || 
                !ap.isActionEnabled(MicronautMavenConstants.ACTION_NATIVE_COMPILE, Lookup.EMPTY)) {
                return;
            }
            ap.invokeAction(MicronautMavenConstants.ACTION_NATIVE_COMPILE, Lookup.EMPTY);
        }
    }
}
