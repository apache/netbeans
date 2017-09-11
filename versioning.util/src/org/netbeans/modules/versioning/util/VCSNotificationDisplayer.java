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

package org.netbeans.modules.versioning.util;

import java.awt.Color;
import java.io.File;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Displays VCS notifications
 * @author Tomas Stupka, Ondra Vrabec
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
    protected abstract void setupPane(JTextPane pane, File[] file, File projectDir, String url, String revision);

    /**
     * Removes a leading and trailing slash from the given string
     * @param str
     * @return
     */
    protected String trim(String str) {
        if (str.startsWith("/")) {
            str = str.substring(1, str.length());
        }
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     *
     * @param files
     * @return
     */
    protected String getFileNames(final File[] files) {
        StringBuffer filesSb = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            if (i == 5) {
                filesSb.append("...");                                      // NOI18N
                break;
            }
            File file = files[i];
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
    protected void notifyFileChange(File[] files, File projectDir, String url, String revision) {
        JTextPane ballonDetails = getPane(files, projectDir, url, revision); // using the same pane causes the balloon popup
        JTextPane popupDetails = getPane(files, projectDir, url, revision);  // to trim the text to the first line
        NotificationDisplayer.getDefault().notify(
                NbBundle.getMessage(VCSNotificationDisplayer.class, "MSG_NotificationBubble_Title"), //NOI18N
                ImageUtilities.loadImageIcon(NOTIFICATION_ICON_PATH, false),
                ballonDetails, popupDetails, NotificationDisplayer.Priority.NORMAL);
    }

    private JTextPane getPane(File[] files, File projectDir, String url, String revision) {
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
