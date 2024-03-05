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

package org.netbeans.modules.subversion.notifications;

import java.io.File;
import java.io.IOException;
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
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.diff.DiffAction;
import org.netbeans.modules.subversion.ui.diff.Setup;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.VCSNotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Notifies about external changes on the given files.
 * @author Ondra Vrabec
 */
public class NotificationsManager {

    private static NotificationsManager instance;
    private static final Logger LOG = Logger.getLogger(NotificationsManager.class.getName());
    private static final Set<File> alreadySeen = Collections.synchronizedSet(new WeakSet<File>());
    private static final String CMD_DIFF = "cmd.diff";                  //NOI18N

    private final HashSet<File> files;
    private final RequestProcessor rp;
    private final RequestProcessor.Task notificationTask;
    private final FileStatusCache cache;
    private Boolean enabled;

    private Map<File, Long> notifiedFiles = Collections.synchronizedMap(new HashMap<File, Long>());

    private NotificationsManager () {
        files = new HashSet<File>();
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
    public void scheduleFor(File file) {
        if (isSeen(file) || !isUpToDate(file) || !isEnabled(file)) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "File {0} is {1} up to date, notifications enabled: {2}", new Object[]{file.getAbsolutePath(), isUpToDate(file) ? "" : "not ", isEnabled(file)}); //NOI18N
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

    public void notfied(File[] files, Long revision) {
        for (File file : files) {
            notifiedFiles.put(file, revision);
        }
    }

    public void setupPane(JTextPane pane, final File[] files, String fileNames, final File projectDir, final String url, final String revision) {
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

    private boolean isEnabled (File file) {
        if (enabled == null) {
            // let's leave a possibility to disable the notifications
            enabled = !"false".equals(System.getProperty("subversion.notificationsEnabled", "true")); //NOI18N
        }
        return enabled;
    }

    private boolean isUpToDate(File file) {
        boolean upToDate = false;
        FileInformation info = cache.getCachedStatus(file);
        if (info == null || (info.getStatus() & FileInformation.STATUS_VERSIONED_UPTODATE) != 0 && !info.isDirectory()) {
            upToDate = true;
        }
        return upToDate;
    }

    private boolean isSeen(File file) {
        return alreadySeen.contains(file) || notifiedFiles.containsKey(file);
    }

    private class NotificationTask extends VCSNotificationDisplayer implements Runnable {

        @Override
        public void run() {
            HashSet<File> filesToScan;
            synchronized (files) {
                filesToScan = new HashSet<File>(files);
                files.clear();
            }
            removeDirectories(filesToScan);
            removeSeenFiles(filesToScan);
            removeNotEnabled(filesToScan);
            if (!filesToScan.isEmpty()) {
                scanFiles(filesToScan);
            }
        }

        @Override
        protected void setupPane(JTextPane pane, final File[] files, final File projectDir, final String url, final String revision) {
            NotificationsManager.this.setupPane(pane, files, getFileNames(files), projectDir, url, revision);
        }

        private void removeDirectories (Collection<File> filesToScan) {
            for (Iterator<File> it = filesToScan.iterator(); it.hasNext();) {
                File file = it.next();
                if (!file.isFile()) {
                    it.remove();
                }
            }
        }

        private void removeNotEnabled (Collection<File> filesToScan) {
            for (Iterator<File> it = filesToScan.iterator(); it.hasNext();) {
                File file = it.next();
                if (!isEnabled(file)) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "File {0} is probably not from kenai, notifications disabled", new Object[] { file.getAbsolutePath() } ); //NOI18N
                    }
                    it.remove();
                }
            }
        }

        private void removeSeenFiles (Collection<File> filesToScan) {
            for (Iterator<File> it = filesToScan.iterator(); it.hasNext();) {
                File file = it.next();
                if (isSeen(file)) {
                    it.remove();
                }
            }
        }

        private void scanFiles (Collection<File> filesToScan) {
            HashMap<SVNUrl, HashSet<File>> filesPerRepository = sortByRepository(filesToScan);
            for (Map.Entry<SVNUrl, HashSet<File>> entry : filesPerRepository.entrySet()) {
                SVNUrl repositoryUrl = entry.getKey();
                HashMap<Long, Notification> notifications = new HashMap<Long, Notification>();
                try {
                    SvnClient client = Subversion.getInstance().getClient(repositoryUrl);
                    if (client != null) {
                        HashSet<File> files = entry.getValue();
                        ISVNStatus[] statuses = client.getStatus(files.toArray(new File[0]));
                        for (ISVNStatus status : statuses) {
                            if ((SVNStatusKind.UNVERSIONED.equals(status.getTextStatus())
                                    || SVNStatusKind.IGNORED.equals(status.getTextStatus()))) {
                                continue;
                            }
                            File file = status.getFile();
                            SVNRevision.Number rev = status.getRevision();
                            // get repository info - last revision if possible
                            ISVNInfo info = null;
                            boolean removed = false;
                            try {
                                SVNUrl url = status.getUrl();
                                if (url == null) {
                                    String canonicalPath;
                                    try {
                                        // i suspect the file to be under a symlink folder
                                        canonicalPath = status.getFile().getCanonicalPath();
                                    } catch (IOException ex) {
                                        canonicalPath = null;
                                    }
                                    LOG.log(Level.WARNING, "scanFiles: though versioned it has no svn url: {0}, {1}, {2}, {3}, {4}", //NOI18N
                                            new Object[] { file, status.getFile(), status.getTextStatus(), status.getUrlString(), canonicalPath });
                                } else {
                                    info = client.getInfo(url, SVNRevision.HEAD, rev);
                                }
                            } catch (SVNClientException ex) {
                                LOG.log(Level.FINE, null, ex);
                                // XXX or should we run remote status to determine if the file was deleted?
                                removed = SvnClientExceptionHandler.isFileNotFoundInRevision(ex.getMessage());
                            }
                            if (info != null || removed) {
                                Long repositoryRev = removed ? null : info.getLastChangedRevision().getNumber();
                                // XXX some hack to look up deleted file's last revision
                                if (isModifiedInRepository(rev.getNumber(), repositoryRev)) {
                                    addToMap(notifications, file, repositoryRev);
                                    // this will refresh versioning view as well
                                    cache.refresh(file, new FileStatusCache.RepositoryStatus(status, null));
                                }
                            }
                        }
                        alreadySeen.addAll(files);
                    }
                } catch (SVNClientException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
                notifyChanges(notifications, repositoryUrl);
            }
        }

        private boolean isModifiedInRepository (long revision, Long repositoryRevision) {
            return repositoryRevision == null // file is probably remotely deleted
                    || revision < repositoryRevision; // base revision is lower than the repository revision
        }

        /**
         * Adds new notification or adds file to already existing notification
         * @param notifications sorted by revision number
         * @param file file to add to a notification
         * @param revision repository revision
         */
        private void addToMap(HashMap<Long, Notification> notifications, File file, Long revision) {
            Notification revisionNotification = notifications.get(revision);
            if (revisionNotification == null) {
                revisionNotification = new Notification(revision);
                notifications.put(revision, revisionNotification);
            }
            revisionNotification.addFile(file);
        }

        private class Notification {
            private final Set<File> files;
            private final Long revision;

            Notification(Long revision) {
                files = new HashSet<File>();
                this.revision = revision;
            }

            void addFile(File file) {
                files.add(file);
            }

            File[] getFiles () {
                return files.toArray(new File[0]);
            }

            Long getRevision() {
                return revision;
            }
        }

        private HashMap<SVNUrl, HashSet<File>> sortByRepository (Collection<File> files) {
            HashMap<SVNUrl, HashSet<File>> filesByRepository = new HashMap<SVNUrl, HashSet<File>>();
            for (File file : files) {
                SVNUrl repositoryUrl = getRepositoryRoot(file);
                if (repositoryUrl != null) {
                    HashSet<File> filesPerRepository = filesByRepository.get(repositoryUrl);
                    if (filesPerRepository == null) {
                        filesPerRepository = new HashSet<File>();
                        filesByRepository.put(repositoryUrl, filesPerRepository);
                    }
                    filesPerRepository.add(file);
                }
            }
            return filesByRepository;
        }

        private void notifyChanges (HashMap<Long, Notification> notifications, SVNUrl repositoryUrl) {
            for (Map.Entry<Long, Notification> e : notifications.entrySet()) {
                Notification notification = e.getValue();
                File[] files = notification.getFiles();
                Long revision = notification.getRevision();
                notifyFileChange(files, files[0].getParentFile(), repositoryUrl.toString(), revision == null ? null : revision.toString());
            }
        }

        /**
         * Returns repository root url for the given file or null if the repository is unknown or does not belong to allowed urls.
         * Currently allowed repositories are kenai repositories.
         * @param file
         * @return
         */
        private SVNUrl getRepositoryRoot (File file) {
            SVNUrl repositoryUrl = null;
            SVNUrl url = getRepositoryUrl(file);
            return repositoryUrl;
        }

        private SVNUrl getRepositoryUrl (File file) {
            SVNUrl url = null;
            try {
                url = SvnUtils.getRepositoryRootUrl(file);
            } catch (SVNClientException ex) {
                LOG.log(Level.FINE, "getRepositoryUrl: No repository root url found for managed file : [" + file + "]", ex); //NOI18N
                try {
                    url = SvnUtils.getRepositoryUrl(file); // try to falback
                } catch (SVNClientException ex1) {
                    LOG.log(Level.FINE, "getRepositoryUrl: No repository url found for managed file : [" + file + "]", ex1); //NOI18N
                }
            }
            return url;
        }
    }
}
