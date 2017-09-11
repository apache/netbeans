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

package org.netbeans.modules.bugtracking.commons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Notifies and eventually downloads a missing JIRA plugin from the Update Center
 * @author Tomas Stupka
 */
public class JiraUpdater {

    private static final String JIRA_CNB = "org.netbeans.modules.jira";         // NOI18N
    
    private static JiraUpdater instance;

    private JiraUpdater() {
    }

    public synchronized static JiraUpdater getInstance() {
        if(instance == null) {
            instance = new JiraUpdater();
        }
        return instance;
    }

    /**
     * Download and install the JIRA plugin from the Update Center
     * @param projectUrl
     */
    @NbBundle.Messages({"MSG_JiraPluginName=JIRA"})
    public void downloadAndInstall(String projectUrl) {
        if(projectUrl != null && !JiraUpdater.notifyJiraDownload(projectUrl)) {
            return;
        }
        IDEServices ideServices = Support.getInstance().getIDEServices();
        if(ideServices != null) {
            IDEServices.Plugin plugin = ideServices.getPluginUpdates(JIRA_CNB, Bundle.MSG_JiraPluginName());
            if(plugin != null) {
                plugin.installOrUpdate();
            }
        }
    }

    /**
     * Determines if the jira plugin is instaled or not
     *
     * @return true if jira plugin is installed, otherwise false
     */
    public static boolean isJiraInstalled() {
        IDEServices ideServices = Support.getInstance().getIDEServices();
        return ideServices != null && ideServices.isPluginInstalled(JIRA_CNB);
    }
    
    /**
     * Notifies about the missing jira plugin and provides an option to choose
     * if it should be downloaded
     *
     * @param url if not null a hyperlink is shown in the dialog
     * 
     * @return true if the user pushes the Download button, otherwise false
     */
    public static boolean notifyJiraDownload(String url) {
        if(isJiraInstalled()) {
           return false; 
        }
        final JButton download = new JButton(NbBundle.getMessage(JiraUpdater.class, "CTL_Action_Download"));     // NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(JiraUpdater.class, "CTL_Action_Cancel"));   // NOI18N

        URL openURL = null;
        if (url != null) {
            try {
                openURL = new URL(url);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        JPanel panel = createNotificationPanel(openURL);

        DialogDescriptor dd =
            new DialogDescriptor(
                panel,
                NbBundle.getMessage(JiraUpdater.class, "CTL_MissingJiraPlugin"),                    // NOI18N
                true,
                new Object[] {download, cancel},
                download,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(JiraUpdater.class),
                null);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                download.requestFocusInWindow();
            }
        });
        return DialogDisplayer.getDefault().notify(dd) == download;
    }

    private static JPanel createNotificationPanel(final URL url) {
        JPanel panel = new JPanel();

        JLabel msgLabel = new JLabel("<html>" + NbBundle.getMessage(JiraUpdater.class, "MSG_PROJECT_NEEDS_JIRA")); // NOI18N
        JButton linkButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        org.openide.awt.Mnemonics.setLocalizedText(linkButton, NbBundle.getMessage(JiraUpdater.class, "MSG_PROJECT_NEEDS_JIRA_LINK")); // NOI18N
        if (url != null) {
            linkButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                    if (displayer != null) {
                        displayer.showURL(url);
                    } else {
                        // XXX nice error message?
                        Support.LOG.warning("No URLDisplayer found.");             // NOI18N
                    }
                }
            });
        } else {
            linkButton.setVisible(false);
        }

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(msgLabel, GroupLayout.PREFERRED_SIZE, 470, Short.MAX_VALUE)
                    .addComponent(linkButton))
                .addContainerGap()
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(msgLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(linkButton)
                .addContainerGap(25, Short.MAX_VALUE)
        );

        return panel;
    }
    
    
}
