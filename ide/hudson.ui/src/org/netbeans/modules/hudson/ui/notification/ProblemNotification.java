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

package org.netbeans.modules.hudson.ui.notification;

import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.ui.api.UI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.util.lookup.Lookups;

/**
 * Build failed or was unstable.
 */
class ProblemNotification {

    private static final Logger LOG = Logger.getLogger(ProblemNotification.class.getName());

    final HudsonJob job;
    private final int build;
    private final boolean failed;
    private Notification notification;

    ProblemNotification(HudsonJob job, int build, boolean failed) {
        this.job = job;
        this.build = build;
        this.failed = failed;
    }

    @Messages({
        "# {0} - job name",
        "# {1} - build number",
        "# Please translate only \"failed\"",
        "ProblemNotification.title.failed={0} #{1,number,#} failed",
        "# {0} - job name",
        "# {1} - build number",
        "# Please translate only \"is unstable\"",
        "ProblemNotification.title.unstable={0} #{1,number,#} is unstable"
    })
    private String getTitle() {
        // XXX use HudsonJobBuild.getDisplayName
        return failed ? Bundle.ProblemNotification_title_failed(job.getDisplayName(), build) : Bundle.ProblemNotification_title_unstable(job.getDisplayName(), build);
    }

    @Messages({
        "ProblemNotification.description.failed=The build failed.",
        "ProblemNotification.description.unstable=Some tests failed."
    })
    String showFailureText() {
        return failed ? Bundle.ProblemNotification_description_failed() : Bundle.ProblemNotification_description_unstable();
    }

    void showFailure() {
        // BuildHandleImpl.getDefaultAction similar but not identical.
        UI.selectNode(job.getInstance().getUrl(), job.getName(), Integer.toString(build));
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                for (HudsonJobBuild b : job.getBuilds()) {
                    if (b.getNumber() == build) {
                        if (failed) {
                            UI.showConsoleAction(b).actionPerformed(null);
                        } else if (b.getMavenModules().isEmpty()) {
                            UI.showFailuresAction().createContextAwareInstance(
                                    Lookups.singleton(b)).actionPerformed(null);
                        } else {
                            for (HudsonMavenModuleBuild module : b.getMavenModules()) {
                                switch (module.getColor()) {
                                case yellow:
                                case yellow_anime:
                                    UI.showFailuresAction()
                                            .createContextAwareInstance(Lookups.singleton(module))
                                            .actionPerformed(null);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    @Messages({
        "# {0} - job name",
        "# {1} - server name",
        "ProblemNotification.ignore.question=Do you wish to cease to receive notifications of failures in {0}? If you change your mind, use Services > Jenkins Builders > {1} > {0} > Properties > Watched.",
        "# {0} - job name",
        "ProblemNotification.ignore.title=Ignore Failures in {0}"
    })
    void ignore() { // #161601
        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                Bundle.ProblemNotification_ignore_question(job.getDisplayName(), job.getInstance().getName()),
                Bundle.ProblemNotification_ignore_title(job.getDisplayName()),
                NotifyDescriptor.OK_CANCEL_OPTION)) == NotifyDescriptor.OK_OPTION) {
            job.setSalient(false);
        }
    }

    private Priority getPriority() {
        return failed ? Priority.HIGH : Priority.NORMAL;
    }

    private Category getCategory() {
        return failed ? Category.ERROR : Category.WARNING;
    }

    private Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/hudson/ui/resources/notification.png", true); // NOI18N
    }

    void add() {
        LOG.log(Level.FINE, "Adding {0}", this);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                notification = NotificationDisplayer.getDefault().notify(
                        getTitle(), getIcon(),
                        new ProblemPanel(ProblemNotification.this),
                        new ProblemPanel(ProblemNotification.this),
                        getPriority(),
                        getCategory());
            }
        });
    }

    void remove() {
        if (notification != null) {
            LOG.log(Level.FINE, "Removing {0}", this);
            notification.clear();
            notification = null;
        }
    }

    public @Override boolean equals(Object obj) {
        if (!(obj instanceof ProblemNotification)) {
            return false;
        }
        ProblemNotification other = (ProblemNotification) obj;
        return job.getName().equals(other.job.getName()) && build == other.build;
    }

    public @Override int hashCode() {
        return job.getName().hashCode() ^ build;
    }

    public @Override String toString() {
        return "ProblemNotification[" + job.getName() + "#" + build + "]"; // NOI18N
    }

}
