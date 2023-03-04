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

    public static synchronized JiraUpdater getInstance() {
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
