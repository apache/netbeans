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
        "ProblemNotification.ignore.question=Do you wish to cease to receive notifications of failures in {0}? If you change your mind, use Services > Hudson Builders > {1} > {0} > Properties > Watched.",
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
