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

package org.netbeans.modules.subversion.remote.notifications;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.FileStatusCache;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.ui.diff.DiffAction;
import org.netbeans.modules.subversion.remote.ui.diff.Setup;
import org.netbeans.modules.subversion.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.versioning.util.VCSNotificationDisplayer;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 * Notifies about external changes on the given files.
 * 
 */
public class NotificationsManager {

    private static NotificationsManager instance;
    private static final Logger LOG = Logger.getLogger(NotificationsManager.class.getName());
    private static final Set<VCSFileProxy> alreadySeen = Collections.synchronizedSet(new WeakSet<VCSFileProxy>());
    private static final String CMD_DIFF = "cmd.diff";                  //NOI18N

    private final HashSet<VCSFileProxy> files;
    private final RequestProcessor rp;
    private final RequestProcessor.Task notificationTask;
    private final FileStatusCache cache;
    private Boolean enabled;

    private final Map<VCSFileProxy, Long> notifiedFiles = Collections.synchronizedMap(new HashMap<VCSFileProxy, Long>());

    private NotificationsManager () {
        files = new HashSet<>();
        rp = new RequestProcessor("SubversionNotifications", 1, true);  //NOI18N
        notificationTask = rp.create(new NotificationTask());
        cache = Subversion.getInstance().getStatusCache();
    }

    /**
     * Returns an instance of the class
     * @return
     */
    public static synchronized NotificationsManager getInstance() {
        if (instance == null) {
            instance = new NotificationsManager();
        }
        return instance;
    }

    /**
     * Plans a task detecting if a notification for the file is needed and in that case displaying the notification.
     * The notification is displayed if the file is still up-to-date (during the time of the call) and there's an external change
     * in the repository.
     * @param file file to scan
     */
    public void scheduleFor(VCSFileProxy file) {
        if (isSeen(file) || !isUpToDate(file) || !isEnabled(file, false)) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "File {0} is {1} up to date, notifications enabled: {2}", new Object[]{file.getPath(), isUpToDate(file) ? "" : "not ", isEnabled(file, false)}); //NOI18N
            }
            return;
        }
        boolean refresh;
        // register the file for the scan
        synchronized (files) {
            int size = files.size();
            files.add(file);
            refresh = files.size() != size;
        }
        if (refresh) {
            notificationTask.schedule(1000);
        }
    }

    public void notfied(VCSFileProxy[] files, Long revision) {
        for (VCSFileProxy file : files) {
            notifiedFiles.put(file, revision);
        }
    }

    public void setupPane(JTextPane pane, final VCSFileProxy[] files, String fileNames, final VCSFileProxy projectDir, final String url, final String revision) {
         String msg = revision == null
                 ? NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_DeleteDescription", fileNames, CMD_DIFF) //NOI18N
                 : NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_Description", fileNames, url, CMD_DIFF); //NOI18N
        pane.setText(msg);

        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    if(CMD_DIFF.equals(e.getDescription())) {
                        Context ctx = new Context(files);
                        DiffAction.diff(ctx, Setup.DIFFTYPE_REMOTE, NbBundle.getMessage(NotificationsManager.class, "LBL_Remote_Changes", projectDir.getName()), false); //NOI18N
                    } else if (revision != null) {
                        try {
                            SearchHistoryAction.openSearch(new SVNUrl(url), projectDir, Long.parseLong(revision));
                        } catch (MalformedURLException ex) {
                            LOG.log(Level.WARNING, null, ex);
                        }
                    }
                }
            }
        });
    }

    /**
     * Notifications are enabled only for logged kenai users and unless disabled by a switch
     * @return
     */
    private boolean isEnabled (VCSFileProxy file, boolean checkUrl) {
        if (enabled == null) {
            // let's leave a possibility to disable the notifications
            enabled = !"false".equals(System.getProperty("subversion.notificationsEnabled", "true")); //NOI18N
        }
        boolean retval = false;
        if (enabled.booleanValue()) {
            if (!checkUrl) {
                retval = true;
            }
        }
        return retval;
    }

    private boolean isUpToDate(VCSFileProxy file) {
        boolean upToDate = false;
        FileInformation info = cache.getCachedStatus(file);
        if (info == null || (info.getStatus() & FileInformation.STATUS_VERSIONED_UPTODATE) != 0 && !info.isDirectory()) {
            upToDate = true;
        }
        return upToDate;
    }

    private boolean isSeen(VCSFileProxy file) {
        return alreadySeen.contains(file) || notifiedFiles.containsKey(file);
    }

    private class NotificationTask extends VCSNotificationDisplayer implements Runnable {

        @Override
        public void run() {
            HashSet<VCSFileProxy> filesToScan;
            synchronized (files) {
                filesToScan = new HashSet<>(files);
                files.clear();
            }
            removeDirectories(filesToScan);
            removeSeenFiles(filesToScan);
            removeNotEnabled(filesToScan);
        }

        @Override
        protected void setupPane(JTextPane pane, final VCSFileProxy[] files, final VCSFileProxy projectDir, final String url, final String revision) {
            NotificationsManager.this.setupPane(pane, files, getFileNames(files), projectDir, url, revision);
        }

        private void removeDirectories (Collection<VCSFileProxy> filesToScan) {
            for (Iterator<VCSFileProxy> it = filesToScan.iterator(); it.hasNext();) {
                VCSFileProxy file = it.next();
                if (!file.isFile()) {
                    it.remove();
                }
            }
        }

        private void removeNotEnabled (Collection<VCSFileProxy> filesToScan) {
            for (Iterator<VCSFileProxy> it = filesToScan.iterator(); it.hasNext();) {
                VCSFileProxy file = it.next();
                if (!isEnabled(file, true)) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "File {0} is probably not from kenai, notifications disabled", new Object[] { file.getPath() } ); //NOI18N
                    }
                    it.remove();
                }
            }
        }

        private void removeSeenFiles (Collection<VCSFileProxy> filesToScan) {
            for (Iterator<VCSFileProxy> it = filesToScan.iterator(); it.hasNext();) {
                VCSFileProxy file = it.next();
                if (isSeen(file)) {
                    it.remove();
                }
            }
        }

    }
}
