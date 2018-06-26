/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
