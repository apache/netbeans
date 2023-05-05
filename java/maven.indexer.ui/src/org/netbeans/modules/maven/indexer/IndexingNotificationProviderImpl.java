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
package org.netbeans.modules.maven.indexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.spi.impl.IndexingNotificationProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

import static org.netbeans.modules.maven.indexer.Bundle.NOTIF_RequestPermissionBody;
import static org.netbeans.modules.maven.indexer.Bundle.NOTIF_RequestPermissionTitle;

/**
 *
 * @author Tomas Stupka
 */
@ServiceProvider(service=IndexingNotificationProvider.class, position=100)
public class IndexingNotificationProviderImpl implements IndexingNotificationProvider {

    private static final Map<String, Notification> indexDownloadNotifications = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void notifyError(String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    private static JTextPane createIndexDownloadPermissionPane(RepositoryInfo repo) {
        JTextPane pane = new JTextPane();
        pane.setContentType("text/html"); // NOI18N
        pane.setText(NOTIF_RequestPermissionBody(repo.getRepositoryUrl()));
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                if ("allow".equals(e.getDescription())) { // NOI18N
                    RepositoryPreferences.allowIndexDownloadFor(repo);
                    Notification notification = indexDownloadNotifications.remove(repo.getRepositoryUrl());
                    if (notification != null) {
                        notification.clear();
                    }
                    // run a query to trigger download; non blocking call
                    RepositoryQueries.getVersionsResult("group", "artifact", List.of(repo)); // NOI18N
                } else if ("deny".equals(e.getDescription())) { // NOI18N
                    RepositoryPreferences.denyIndexDownloadFor(repo);
                    Notification notification = indexDownloadNotifications.remove(repo.getRepositoryUrl());
                    if (notification != null) {
                        notification.clear();
                    }
                } else if ("disable".equals(e.getDescription())) { // NOI18N
                    RepositoryPreferences.setIndexDownloadEnabled(false);
                    synchronized (indexDownloadNotifications) {
                        for (Notification not : indexDownloadNotifications.values()) {
                            not.clear();
                        }
                        indexDownloadNotifications.clear();
                    }
                }
            }
        });
        return pane;
    }

    @Messages({
        "# {0} - repository name",
        "NOTIF_RequestPermissionTitle=Index Download Permission Request for ''{0}''",
        "# {0} - repository URL",
        "NOTIF_RequestPermissionBody="
            + "Grant permission for maven index downloads from ''{0}''.<br/><br/>"
            + "<a href=\"allow\">Allow</a> or <a href=\"deny\">deny</a> downloads for this repository. Or <a href=\"disable\">disable</a> all index downloads globally.<br/><br/"
            + "A repository index contains artifact metadata which is useful for some NetBeans features.",
    })
    @Override
    public void requestPermissionsFor(RepositoryInfo repo) {
        if (!indexDownloadNotifications.containsKey(repo.getRepositoryUrl())) {
            Notification notification = NotificationDisplayer.getDefault().notify(
                    NOTIF_RequestPermissionTitle(repo.getName()),
                    ImageUtilities.loadImageIcon("org/netbeans/modules/maven/indexer/fetch.png", false), // NOI18N
                    createIndexDownloadPermissionPane(repo),
                    createIndexDownloadPermissionPane(repo),
                    NotificationDisplayer.Priority.NORMAL,
                    NotificationDisplayer.Category.INFO
            );
            indexDownloadNotifications.put(repo.getRepositoryUrl(), notification);
        }
    }
    
}
