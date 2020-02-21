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

package org.netbeans.modules.subversion.remote.util;

import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Displays VCS notifications
 * 
 */
public abstract class VCSNotificationDisplayer {

    private static final String NOTIFICATION_ICON_PATH = "org/netbeans/modules/versioning/util/resources/info.png"; //NOI18N

    /**
     * Called to setup the textpane used for the notification buble
     *
     * @param files
     * @param url
     * @param revision
     * @return
     */
    protected abstract void setupPane(JTextPane pane, VCSFileProxy[] file, VCSFileProxy projectDir, String url, String revision);

    /**
     * Removes a leading and trailing slash from the given string
     * @param str
     * @return
     */
    protected String trim(String str) {
        if (str.startsWith("/")) { //NOI18N
            str = str.substring(1, str.length());
        }
        if (str.endsWith("/")) { //NOI18N
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     *
     * @param files
     * @return
     */
    protected String getFileNames(final VCSFileProxy[] files) {
        StringBuilder filesSb = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            if (i == 5) {
                filesSb.append("...");                                      // NOI18N
                break;
            }
            VCSFileProxy file = files[i];
            filesSb.append("&nbsp;&nbsp;&nbsp;&nbsp;");                     // NOI18N
            filesSb.append(file.getName());
            filesSb.append("<br>");                                         // NOI18N
            }
        filesSb.append("<br>");                                             // NOI18N
        return filesSb.toString();
    }

    /**
     *
     * Opens a notification buble in the IDEs status bar containing the text returned in
     * {@link #getPaneText(java.io.File, java.lang.String, java.lang.String)}
     *
     * @param files
     * @param url
     * @param revision
     */
    protected void notifyFileChange(VCSFileProxy[] files, VCSFileProxy projectDir, String url, String revision) {
        JTextPane ballonDetails = getPane(files, projectDir, url, revision); // using the same pane causes the balloon popup
        JTextPane popupDetails = getPane(files, projectDir, url, revision);  // to trim the text to the first line
        NotificationDisplayer.getDefault().notify(
                NbBundle.getMessage(VCSNotificationDisplayer.class, "MSG_NotificationBubble_Title"), //NOI18N
                ImageUtilities.loadImageIcon(NOTIFICATION_ICON_PATH, false),
                ballonDetails, popupDetails, NotificationDisplayer.Priority.NORMAL);
    }

    private JTextPane getPane(VCSFileProxy[] files, VCSFileProxy projectDir, String url, String revision) {
        JTextPane bubble = new JTextPane();
        bubble.setOpaque(false);
        bubble.setEditable(false);

        if (UIManager.getLookAndFeel().getID().equals("Nimbus")) {                   //NOI18N
            //#134837
            //http://forums.java.net/jive/thread.jspa?messageID=283882
            bubble.setBackground(new Color(0, 0, 0, 0));
        }

        bubble.setContentType("text/html");                                          //NOI18N
        setupPane(bubble, files, projectDir, url, revision);
        return bubble;
    }
}
