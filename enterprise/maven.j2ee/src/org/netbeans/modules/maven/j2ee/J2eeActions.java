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
package org.netbeans.modules.maven.j2ee;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 */
public class J2eeActions {

    @ActionID(id = "org.netbeans.modules.maven.j2ee.verify", category = "Project")
    @ActionRegistration(displayName = "#ACT_Verify", lazy=false)
    @ActionReference(position = 651, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_Verify=Verify")
    public static ContextAwareAction verifyAction() {
        return new VerifyAction();
    }
    
    @NbBundle.Messages({"VerifyAction_Name=Verify"})
    private static class VerifyAction extends AbstractAction implements ContextAwareAction {
        
        private Project project;
        private J2eeModuleProvider jmp;
        private NbMavenProject mp;
        
        public VerifyAction() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }
        
        private VerifyAction(Project project, NbMavenProject mp, J2eeModuleProvider jmp) {
            this.project = project;
            this.jmp = jmp;
            this.mp = mp;
            putValue(Action.NAME, Bundle.VerifyAction_Name());
        }
        
        public final @Override Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
            if (projects.size() != 1) {
                return this;
            }
            Project prj = projects.iterator().next().getLookup().lookup(Project.class);
            if (prj == null) {
                return this;
            }
            J2eeModuleProvider prov = prj.getLookup().lookup(J2eeModuleProvider.class);
            if (prov == null) {
                return this;
            }
            NbMavenProject mProj = prj.getLookup().lookup(NbMavenProject.class);
            if (mProj == null) {
                return this;
            }
            if (!prov.hasVerifierSupport()) {
                return this;
            }
            return new VerifyAction(prj, mProj, prov);
        }
        
        public @Override void actionPerformed(ActionEvent e) {
            RunConfig cfg = RunUtils.createRunConfig(FileUtil.toFile(
                    project.getProjectDirectory()),
                    project,
                    "build", //NOI18N
                    Collections.singletonList("install")); //NOI18N

            RunUtils.executeMaven(cfg).addTaskListener(new TaskListener() {

                @Override
                public void taskFinished(Task task) {
                    final FileObject fo;
                    try {
                        fo = jmp.getJ2eeModule().getArchive();
                    } catch (IOException ex) {
                        return;
                    }
                    if (fo == null) {
                        return;
                    }
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                jmp.verify(fo, null);
                            } catch (ValidationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
            });
        }
    }
    
}
