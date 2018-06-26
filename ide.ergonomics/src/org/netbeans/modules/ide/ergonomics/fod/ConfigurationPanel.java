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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 * Provider for fake web module extenders. Able to download and enable the proper module
 * as well as delegate to the proper configuration panel.
 *
 * @author Tomas Mysik
 * @author Pavel Flaska
 */
public class ConfigurationPanel extends JPanel implements Runnable {
    private static final long serialVersionUID = 27938464212508L;
    
    final DownloadProgressMonitor progressMonitor = new DownloadProgressMonitor();
    private FeatureInfo featureInfo;
    private Callable<JComponent> callable;
    private final Boolean autoActivate;

    public ConfigurationPanel(String displayName, final Callable<JComponent> callable, FeatureInfo info, Boolean auto) {
        this(callable, auto);
        setInfo(info);
        setPanelName(displayName);
    }
    
    public ConfigurationPanel(final Callable<JComponent> callable, Boolean auto) {
        assert EventQueue.isDispatchThread();
        initComponents();
        this.featureInfo = null;
        this.callable = callable;
        autoActivate = auto;

        setError(" "); // NOI18N
    }

    public void setInfo(FeatureInfo info) {
        this.featureInfo = info;
        boolean activateNow;
        if (autoActivate != null) {
            activateNow = Boolean.TRUE.equals(autoActivate);
        } else {
            activateNow = Boolean.getBoolean("noActivateButton"); // NOI18N
        }
        if (activateNow) {
            infoLabel.setVisible(false);
            downloadButton.setVisible(false);
            downloadButtonActionPerformed(null);
        }
    }
    
    public void setPanelName(String displayName) {
        FeatureManager.logUI("ERGO_QUESTION", featureInfo.clusterName, displayName);
        String lblMsg = null;
        String btnMsg = null;
        if (featureInfo != null && featureInfo.isPresent()) {
            lblMsg = NbBundle.getMessage(ConfigurationPanel.class, "LBL_EnableInfo", displayName);
            btnMsg = NbBundle.getMessage(ConfigurationPanel.class, "LBL_Enable");
        } else {
            lblMsg = NbBundle.getMessage(ConfigurationPanel.class, "LBL_DownloadInfo", displayName);
            btnMsg = NbBundle.getMessage(ConfigurationPanel.class, "LBL_Download");
        }
        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, lblMsg);
        org.openide.awt.Mnemonics.setLocalizedText(downloadButton, btnMsg);
        
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        FeatureManager.logUI("ERGO_CLOSE");
    }

    void setError(String msg) {
        assert SwingUtilities.isEventDispatchThread ();
        errorLabel.setText(msg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
-     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        errorLabel = BrokenProjectInfo.getErrorPane("dummy");
        infoLabel = new JLabel();
        downloadButton = new JButton();
        progressPanel = new JPanel();
        Mnemonics.setLocalizedText(infoLabel, "dummy");
        Mnemonics.setLocalizedText(downloadButton, "dummy");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.PAGE_AXIS));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(progressPanel, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addComponent(infoLabel)
                    .addComponent(downloadButton)
                    .addComponent(errorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(errorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(infoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(downloadButton)
                .addGap(18, 18, 18)
                .addComponent(progressPanel, GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public void run() {
        ModulesInstaller.installModules(progressMonitor, featureInfo);
    }

    private void downloadButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        FeatureManager.logUI("ERGO_DOWNLOAD");
        downloadButton.setEnabled(false);
        Task task = FeatureManager.getInstance().create(this);
        task.addTaskListener(new TaskListener() {

            public void taskFinished(org.openide.util.Task task) {
                if (!progressMonitor.error) {
                    SwingUtilities.invokeLater(new Runnable() {
                        private String msg;

                        public void run() {
                            ConfigurationPanel.this.removeAll();
                            ConfigurationPanel.this.setLayout(new BorderLayout());
                            try {
                                ConfigurationPanel.this.add(callable.call(), BorderLayout.CENTER);
                            } catch (Exception ex) {
                                // TODO: add warning panel
                                Exceptions.printStackTrace(ex);
                            }
                            ConfigurationPanel.this.invalidate();
                            ConfigurationPanel.this.revalidate();
                            ConfigurationPanel.this.repaint();
                            if (featureInfo != null && featureInfo.isPresent()) {
                                msg = NbBundle.getMessage(ConfigurationPanel.class, "MSG_EnableFailed");
                            } else {
                                msg = NbBundle.getMessage(ConfigurationPanel.class, "MSG_DownloadFailed");
                            }
                            setError(msg);
                            downloadButton.setEnabled(true);
                            progressPanel.removeAll();
                            progressPanel.revalidate();
                            progressPanel.repaint();
                        }
                    });
                }
            }
        });
        task.schedule(0);
    }//GEN-LAST:event_downloadButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton downloadButton;
    private JEditorPane errorLabel;
    private JLabel infoLabel;
    private JPanel progressPanel;
    // End of variables declaration//GEN-END:variables

    private final class DownloadProgressMonitor implements ProgressMonitor {
        private boolean error = false;

        public void onDownload(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onValidate(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onInstall(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onEnable(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        private void updateProgress(final ProgressHandle progressHandle) {
            final JLabel tmpMainLabel = ProgressHandleFactory.createMainLabelComponent(progressHandle);
            final JComponent tmpProgressPanel = ProgressHandleFactory.createProgressComponent(progressHandle);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.removeAll();
                    progressPanel.add(tmpMainLabel);
                    progressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    progressPanel.add(tmpProgressPanel);
                    progressPanel.revalidate();
                    progressPanel.repaint();
                }
            });
        }

        public void onError(final String message) {
            error = true;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    // TODO: mark as html
                    setError("<html>" + message + "</html>"); // NOI18N
                    progressPanel.removeAll();
                    progressPanel.add(errorLabel);
                }
            });
        }
    }
}
