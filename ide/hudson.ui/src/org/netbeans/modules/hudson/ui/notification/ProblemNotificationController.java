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

package org.netbeans.modules.hudson.ui.notification;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;

public class ProblemNotificationController {

    private static final Logger LOG = Logger.getLogger(ProblemNotificationController.class.getName());

    private final HudsonInstance instance;
    private final Set<ProblemNotification> notifications = new HashSet<ProblemNotification>();

    public ProblemNotificationController(HudsonInstance instance) {
        this.instance = instance;
    }

    public synchronized void updateNotifications() {
        LOG.log(Level.FINE, "Updating notifications for {0}", instance);
        Preferences prefs = instance.prefs().node("notifications"); // NOI18N
        for (HudsonJob job : instance.getJobs()) {
            if (!job.isSalient()) {
                LOG.log(Level.FINEST, "{0} is not being watched", job);
                continue;
            }
            int build = job.getLastCompletedBuild();
            if (prefs.getInt(job.getName(), 0) >= build) {
                LOG.log(Level.FINEST, "{0} was already notified", job);
                continue;
            }
            ProblemNotification n;
            Color color = job.getColor();
            LOG.log(Level.FINEST, "{0} has status {1}", new Object[] {job, color});
            switch (color) {
            case red:
            case red_anime:
                n = new ProblemNotification(job, build, true);
                break;
            case yellow:
            case yellow_anime:
                n = new ProblemNotification(job, build, false);
                break;
            case blue:
            case blue_anime:
                removeFormerNotifications(job, null);
                n = null;
                break;
            default:
                n = null;
            }
            if (n != null && notifications.add(n)) {
                prefs.putInt(job.getName(), build);
                n.add();
                removeFormerNotifications(job, n);
            }
        }
    }

    private void removeFormerNotifications(HudsonJob job, ProblemNotification except) {
        Iterator<ProblemNotification> i = notifications.iterator();
        while (i.hasNext()) {
            ProblemNotification former = i.next();
            if (former.job.getName().equals(job.getName()) && !former.equals(except)) {
                former.remove();
                i.remove();
            }
        }
    }

    public synchronized void clearNotifications() {
        for (ProblemNotification problemNotification : notifications) {
            problemNotification.remove();
        }
        notifications.clear();
    }
}
