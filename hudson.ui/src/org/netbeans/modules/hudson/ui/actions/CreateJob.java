/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.ui.api.UI;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import org.netbeans.modules.hudson.ui.util.UsageLogging;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;

/**
 * Submenu action to create a job on this server from one of the open projects.
 */
@ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.CreateJob.context")
@ActionRegistration(displayName="#CreateJob.new_build", iconInMenu=false)
@ActionReference(path=HudsonInstance.ACTION_PATH, position=300)
@Messages("CreateJob.new_build=New Build...")
public class CreateJob implements ActionListener {

    /** Global action which also allows you to pick a server. */
    @ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.CreateJob.global")
    @ActionRegistration(displayName="#CTL_CreateJob", iconInMenu=false)
    @ActionReference(path="Menu/Versioning", position=400)
    @Messages("CTL_CreateJob=Create Build &Job...")
    public static ActionListener global() {
        return new CreateJob(null);
    }

    private final HudsonInstance instance;

    public CreateJob(HudsonInstance instance) {
        this.instance = instance;
    }

    @Messages({
        "CreateJob.title=New Continuous Build",
        "CreateJob.create=Create"
    })
    @Override public void actionPerformed(ActionEvent e) {
        if (runCustomActionIfAvailable(e)) {
            return;
        }
        final CreateJobPanel panel = new CreateJobPanel();
        final DialogDescriptor dd = new DialogDescriptor(panel, Bundle.CreateJob_title());
        final AtomicReference<Dialog> dialog = new AtomicReference<Dialog>();
        final JButton createButton = new JButton(Bundle.CreateJob_create());
        createButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override public void run() {
                        finalizeJob(panel.instance, panel.creator, panel.name(), panel.selectedProject());
                    }
                });
                dialog.get().dispose();
            }
        });
        dd.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (NotifyDescriptor.PROP_VALID.equals(evt.getPropertyName())) {
                    createButton.setEnabled(dd.isValid());
                }
            }
        });
        HudsonInstance _instance;
        if (instance != null) {
            _instance = instance;
        } else {
            Collection<? extends HudsonInstance> instances = HudsonManager.getAllInstances();
            _instance = instances.isEmpty() ? null : instances.iterator().next();
        }
        panel.init(dd, _instance);
        dd.setOptions(new Object[] {createButton, NotifyDescriptor.CANCEL_OPTION});
        dd.setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
        dialog.set(DialogDisplayer.getDefault().createDialog(dd));
        dialog.get().setVisible(true);
    }

    boolean runCustomActionIfAvailable(ActionEvent e) {
        if (instance != null) {
            Action custom = instance.getPersistence().getNewJobAction();
            if (custom != null) {
                custom.actionPerformed(e);
                return true;
            }
        }
        return false;
    }

    @Messages({
        "CreateJob.failure=Could not create job. Please check your server's log for details.",
        "# UI logging of creating new build job",
        "# {0} - project type",
        "UI_HUDSON_JOB_CREATED=New Hudson build job created [project type: {0}]",
        "# Usage Logging",
        "# {0} - project type",
        "USG_HUDSON_JOB_CREATED=New Hudson build job created [project type: {0}]"
    })
    private void finalizeJob(HudsonInstance instance, ProjectHudsonJobCreator creator, String name, Project project) {
        try {
            Document doc = creator.configure();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
            String createItemURL = instance.getUrl() + "createItem?name=" + Utilities.uriEncode(name); // NOI18N
            new ConnectionBuilder().instance(instance).url(createItemURL).
                    header("Content-Type", "text/xml"). // NOI18N
                    postData(baos.toByteArray()).
                    httpConnection().disconnect();
            URLDisplayer.getDefault().showURL(new URL(instance.getUrl() + "job/" + Utilities.uriEncode(name) + "/")); // NOI18N
            instance.synchronize(false);
            ProjectHudsonProvider.getDefault().recordAssociation(project,
                    new ProjectHudsonProvider.Association(instance.getUrl(), name));
            OpenProjects.getDefault().open(new Project[] {project}, false);
            UI.selectNode(instance.getUrl(), name);
            // stats
            UsageLogging.logUI(NbBundle.getBundle(CreateJob.class), "UI_HUDSON_JOB_CREATED", project.getClass().getName()); // NOI18N
            UsageLogging.logUsage(CreateJob.class, "USG_HUDSON_JOB_CREATED", project.getClass().getName()); // NOI18N
        } catch (ProjectHudsonJobCreator.SilentIOException x) {
            Logger.getLogger(CreateJob.class.getName()).log(Level.INFO, null, x);
        } catch (IOException x) {
            Exceptions.attachLocalizedMessage(x, Bundle.CreateJob_failure());
            Logger.getLogger(CreateJob.class.getName()).log(Level.WARNING, null, x);
        }
    }

}
