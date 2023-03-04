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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.actions.scm;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.actions.scm.ui.CheckoutUI;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import static org.netbeans.modules.maven.actions.scm.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * @author Anuradha G
 */
public class CheckoutAction extends AbstractAction implements LookupListener {
    private static final @StaticResource String UPDATE_ICON = "org/netbeans/modules/maven/actions/scm/update.png";

    private final Lookup lookup;
    private final Lookup.Result<MavenProject> result;

    @Messages("LBL_Checkout=Check Out Sources")
    @SuppressWarnings("LeakingThisInConstructor")
    public CheckoutAction(Lookup lkp) {
        this.lookup = lkp;
        putValue(NAME, LBL_Checkout());
        //TODO proper icon
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage(UPDATE_ICON, true))); //NOI18N
        putValue("iconBase", UPDATE_ICON); //NOI18N
        result = lookup.lookupResult(MavenProject.class);
        setEnabled(getScm() != null);
        result.addLookupListener(this);
    }

    private Scm getScm() {
        Iterator<? extends MavenProject> prj = result.allInstances().iterator();
        if (!prj.hasNext()) {
            return null;
        }
        MavenProject project = prj.next();
        return project.getScm();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends MavenProject> prj = result.allInstances().iterator();
        if (!prj.hasNext()) {
            return;
        }
        MavenProject project = prj.next();

        CheckoutUI checkoutUI = new CheckoutUI(project);
        DialogDescriptor dd = new DialogDescriptor(checkoutUI,  LBL_Checkout());
        dd.setClosingOptions(new Object[]{
            checkoutUI.getCheckoutButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        dd.setOptions(new Object[]{
            checkoutUI.getCheckoutButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (checkoutUI.getCheckoutButton() == ret) {
            final RunConfig rc = checkoutUI.getRunConfig();
            if (!rc.getExecutionDirectory().exists()) {
                rc.getExecutionDirectory().mkdirs();
            }
            final File checkoutDir = checkoutUI.getCheckoutDirectory();
            ExecutorTask task = RunUtils.executeMaven(rc);
            task.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(Task task) {
                    FileObject fo = FileUtil.toFileObject(checkoutDir);
                    if (fo != null) {
                        try {
                            Project prj = ProjectManager.getDefault().findProject(fo);
                            if (prj != null) {
                                OpenProjects.getDefault().open(new Project[] {prj}, false);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setEnabled(getScm() != null);
            }
        });
    }
}
