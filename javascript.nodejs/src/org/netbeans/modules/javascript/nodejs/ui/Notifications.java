/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsPlatformProvider;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsRunPanel;
import org.netbeans.modules.javascript.nodejs.util.GraalVmUtils;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

public final class Notifications {

    private Notifications() {
    }

    @NbBundle.Messages({
        "Notifications.detection.title=Node.js detected",
        "# {0} - project name",
        "Notifications.detection.description=Enable Node.js support in project {0}?",
        "# {0} - project name",
        "Notifications.detection.done=Node.js support enabled in project {0}.",
        "# {0} - project name",
        "Notifications.detection.noop=Node.js support already enabled in project {0}.",
    })
    public static void notifyNodeJsDetected(final Project project) {
        final String projectName = NodeJsUtils.getProjectDisplayName(project);
        final NodeJsPreferences preferences = NodeJsSupport.forProject(project).getPreferences();
        assert !preferences.isEnabled() : "node.js support should not be enabled in project " + projectName;
        NotificationDisplayer.getDefault().notify(
                Bundle.Notifications_detection_title(),
                NotificationDisplayer.Priority.LOW.getIcon(),
                Bundle.Notifications_detection_description(projectName),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text;
                        if (preferences.isEnabled()) {
                            // already done
                            text = Bundle.Notifications_detection_noop(projectName);
                        } else {
                            preferences.setEnabled(true);
                            text = Bundle.Notifications_detection_done(projectName);
                        }
                        StatusDisplayer.getDefault().setStatusText(text);
                    }
                },
                NotificationDisplayer.Priority.LOW);
    }

    @NbBundle.Messages({
        "Notifications.graalvm.detection.title=GraalVM detected",
        "Notifications.graalvm.detection.description=Set proper node and npm paths?",
        "Notifications.graalvm.detection.done=Proper node and npm paths set.",
        "Notifications.graalvm.detection.noop=Proper node and npm paths already set.",
    })
    public static void notifyGraalVmDetected() {
        NotificationDisplayer.getDefault().notify(
                Bundle.Notifications_graalvm_detection_title(),
                NotificationDisplayer.Priority.LOW.getIcon(),
                Bundle.Notifications_graalvm_detection_description(),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text;
                        if (GraalVmUtils.properPathsSet()) {
                            // already done
                            text = Bundle.Notifications_graalvm_detection_noop();
                        } else {
                            NodeJsOptions.getInstance().setNode(GraalVmUtils.getNode());
                            NodeJsOptions.getInstance().setNpm(GraalVmUtils.getNpm(true));
                            text = Bundle.Notifications_graalvm_detection_done();
                        }
                        StatusDisplayer.getDefault().setStatusText(text);
                    }
                },
                NotificationDisplayer.Priority.LOW);
    }

    @NbBundle.Messages({
        "Notifications.enabled.title=Node.js support enabled",
        "# {0} - project name",
        "Notifications.enabled.description=Enable running project {0} as Node.js application?",
        "# {0} - project name",
        "Notifications.enabled.done=Project {0} will be run as Node.js application.",
        "# {0} - project name",
        "Notifications.enabled.noop=Project {0} already runs as Node.js application.",
        "# {0} - project name",
        "Notifications.enabled.invalid=Node.js support not enabled in project {0}.",
    })
    public static void notifyRunConfiguration(Project project) {
        final String projectName = NodeJsUtils.getProjectDisplayName(project);
        final NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        final NodeJsPreferences preferences = nodeJsSupport.getPreferences();
        assert !preferences.isRunEnabled() : "node.js run should not be enabled in " + projectName;
        NotificationDisplayer.getDefault().notify(
                Bundle.Notifications_enabled_title(),
                NotificationDisplayer.Priority.LOW.getIcon(),
                Bundle.Notifications_enabled_description(projectName),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text;
                        if (!preferences.isEnabled()) {
                            // not enabled at all (happens if one clicks in notifications window later)
                            text = Bundle.Notifications_enabled_invalid(projectName);
                        } else if (preferences.isRunEnabled()) {
                            // already done
                            text = Bundle.Notifications_enabled_noop(projectName);
                        } else {
                            nodeJsSupport.firePropertyChanged(NodeJsPlatformProvider.PROP_RUN_CONFIGURATION, null, NodeJsRunPanel.IDENTIFIER);
                            text = Bundle.Notifications_enabled_done(projectName);
                        }
                        StatusDisplayer.getDefault().setStatusText(text);
                    }
                },
                NotificationDisplayer.Priority.LOW);
    }

    public static void notifyUser(String title, String details) {
        NotificationDisplayer.getDefault().notify(
                title,
                NotificationDisplayer.Priority.LOW.getIcon(),
                details,
                null,
                NotificationDisplayer.Priority.LOW);
    }

    public static void informUser(String message) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message));
    }

    public static void ask(final String title, final String question, @NullAllowed final Runnable yesTask, @NullAllowed final Runnable noTask) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotifyDescriptor confirmation = new NotifyDescriptor.Confirmation(question, title, NotifyDescriptor.YES_NO_OPTION);
                if (DialogDisplayer.getDefault().notify(confirmation) == NotifyDescriptor.YES_OPTION) {
                    if (yesTask != null) {
                        yesTask.run();
                    }
                } else if (noTask != null) {
                    noTask.run();
                }
            }
        });
    }

    @NbBundle.Messages("Notifications.ask.sync=Sync changes between project and package.json?")
    public static void askSyncChanges(Project project, @NullAllowed Runnable yesTask, @NullAllowed Runnable noTask) {
        ask(NodeJsUtils.getProjectDisplayName(project), Bundle.Notifications_ask_sync(), yesTask, noTask);
    }

}
