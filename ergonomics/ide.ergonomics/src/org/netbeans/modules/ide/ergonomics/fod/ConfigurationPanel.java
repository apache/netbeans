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
package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.nonNull;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 * Provider for fake web module extenders. Able to download and enable the
 * proper module as well as delegate to the proper configuration panel.
 *
 * @author Tomas Mysik
 * @author Pavel Flaska
 */
public class ConfigurationPanel extends JPanel {

    private static final long serialVersionUID = 27938464212508L;
    
    private static final Logger LOG = Logger.getLogger(ConfigurationPanel.class.getName());

    private final ActivationProgressMonitor progressMonitor = new ActivationProgressMonitor();
    private FeatureInfo featureInfo;
    private Callable<JComponent> callable;
    private Collection<UpdateElement> featureInstall;
    private final SpecificationVersion jdk = new SpecificationVersion(System.getProperty("java.specification.version"));
    private HashSet<FeatureInfo.ExtraModuleInfo> extrasFilter;

    public ConfigurationPanel(String displayName, final Callable<JComponent> callable, FeatureInfo info) {
        this(callable);
        setInfo(info, displayName, Collections.<UpdateElement>emptyList(), Collections.emptyList(), Collections.emptyMap(), false);
    }

    public ConfigurationPanel(final Callable<JComponent> callable) {
        assert EventQueue.isDispatchThread();
        initComponents();
        infoLabel.setVisible(false);
        downloadLabel.setVisible(false);
        activateButton.setVisible(false);
        downloadButton.setVisible(false);
        this.featureInfo = null;
        this.callable = callable;

        setError(" "); // NOI18N
    }

    public void setInfo(FeatureInfo info, String displayName, Collection<UpdateElement> toInstall,
            Collection<FeatureInfo.ExtraModuleInfo> missingModules,
            Map<FeatureInfo.ExtraModuleInfo, FeatureInfo> extrasMap, boolean required) {
        this.extrasFilter = new HashSet<>();
        this.featureInfo = info;
        this.featureInstall = toInstall;
        boolean activateNow = toInstall.isEmpty() && missingModules.isEmpty();
        Set<FeatureInfo.ExtraModuleInfo> extraModules = featureInfo.getExtraModules();
        Collection<? extends ModuleInfo> lookupAll = Lookup.getDefault().lookupAll(ModuleInfo.class);
        FindComponentModules findModules = new FindComponentModules(info);
        Collection<UpdateElement> modulesToInstall = findModules.getModulesForInstall();
        selectionsPanel.removeAll();
        for (FeatureInfo.ExtraModuleInfo extraModule : extraModules) {
            JCheckBox jCheckBox = new JCheckBox(extraModule.displayName());
            for (ModuleInfo moduleInfo : lookupAll) {
                if (extraModule.matches(moduleInfo.getCodeName())) {
                    jCheckBox.setText(moduleInfo.getDisplayName());
                }
            }
            
            for (UpdateElement updateElement : modulesToInstall) {
                if (extraModule.matches(updateElement.getCodeName())){
                    jCheckBox.setText(updateElement.getDisplayName());
                }
            }
            
            if (extraModule.isRequiredFor(jdk)) {
                jCheckBox.setSelected(true);
//                jCheckBox.setEnabled(false);
                extrasFilter.add(extraModule);
            }
            jCheckBox.addActionListener(e -> {
                if (jCheckBox.isSelected()) {
                    extrasFilter.add(extraModule);
                } else {
                    extrasFilter.remove(extraModule);
                }
            });
            selectionsPanel.add(jCheckBox);
        }
        if (activateNow) {
            infoLabel.setVisible(false);
            downloadLabel.setVisible(false);
            activateButton.setVisible(false);
            downloadButton.setVisible(false);
            progressPanel.removeAll();
            activateButtonActionPerformed(null);
        } else {
            FeatureManager.logUI("ERGO_QUESTION", featureInfo.clusterName, displayName);
            infoLabel.setVisible(true);
            downloadLabel.setVisible(true);
            activateButton.setVisible(true);
            downloadButton.setVisible(true);
            progressPanel.removeAll();
            
            // collect descriptions from features contributing installed extras
            List<String> downloadStringList = collectExtraModulesTextsFromFeatures(extrasMap.values(), required);
            String lblDownloadMsg = generateDownloadMessageFromExtraModulesTexts(downloadStringList);
            
            if (required) {
                activateButton.setEnabled(false);
            } else {
                activateButton.setEnabled(true);
            }

            if (!missingModules.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (FeatureInfo.ExtraModuleInfo s : missingModules) {
                    if (sb.length() > 0) {
                        sb.append(", "); // NOI18N
                    }
                    sb.append(s.displayName());
                }
                String list = sb.toString();
                if (required) {
                    lblDownloadMsg = NbBundle.getMessage(ConfigurationPanel.class, "MSG_MissingRequiredModules", displayName, list);
                    activateButton.setEnabled(false);
                } else {
                    lblDownloadMsg = NbBundle.getMessage(ConfigurationPanel.class, "MSG_MissingRecommendedModules", displayName, list);
                }
                downloadButton.setEnabled(false);
            } else {
                downloadButton.setEnabled(true);
            }

            String lblActivateMsg = NbBundle.getMessage(ConfigurationPanel.class, "LBL_EnableInfo", displayName);
            String btnActivateMsg = NbBundle.getMessage(ConfigurationPanel.class, "LBL_Enable");
            String btnDownloadMsg = NbBundle.getMessage(ConfigurationPanel.class, "LBL_Download");
            org.openide.awt.Mnemonics.setLocalizedText(infoLabel, lblActivateMsg);
            org.openide.awt.Mnemonics.setLocalizedText(activateButton, btnActivateMsg);
            org.openide.awt.Mnemonics.setLocalizedText(downloadLabel, lblDownloadMsg);
            org.openide.awt.Mnemonics.setLocalizedText(downloadButton, btnDownloadMsg);
        }
    }
    
    /**
     * Collect extra modules texts
     */
    protected List<String> collectExtraModulesTextsFromFeatures(Collection<FeatureInfo> features, boolean required) {
        List<String> descriptionsList = new ArrayList<>();
        for (FeatureInfo fi : features) {
            String s = required ? fi.getExtraModulesRequiredText(): fi.getExtraModulesRecommendedText();
            if (nonNull(s) && !descriptionsList.contains(s)) {
                descriptionsList.add(s);
            }
        }
        return descriptionsList;
    } 
    
    /**
     * Generate download message from extra modules texts
     * @param extraModulesTexts
     * @return String Text to set in download label
     */
    protected String generateDownloadMessageFromExtraModulesTexts(List<String> extraModulesTexts) {
        StringBuilder sbDownload = new StringBuilder();
        if (!extraModulesTexts.isEmpty()) {
            sbDownload.append("<html><body>");
            for (int i = 0; i < extraModulesTexts.size(); i++) {
                sbDownload.append(extraModulesTexts.get(i));
                if (extraModulesTexts.size() > 1 && i < extraModulesTexts.size() - 1) {
                    sbDownload.append("<br>");
                }
            }
            sbDownload.append("</body></html>");
        }
        return sbDownload.toString();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        FeatureManager.logUI("ERGO_CLOSE");
    }

    public void setUpdateErrors(Collection<IOException> errors) {
        Collection<IOException> exceptions = new ArrayList<>(errors);
        exceptions.removeIf(e -> 
            // user might be offline, ignore, exception is already logged in FindComponentModules#findComponentModules
            // regular cluster activation does not require downloads anyway
            e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException
        );
        if (exceptions.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>"); // NOI18N
        for (IOException ex : exceptions) {
            sb.append("<br/>"); // NOI18N
            sb.append(ex.getLocalizedMessage());
        }
        sb.append("</html>"); // NOI18N
        String updateError = NbBundle.getMessage(ConfigurationPanel.class, "ERR_UpdateComponents", sb.toString());
        SwingUtilities.invokeLater(() -> {
            remove(errorLabel);
            progressPanel.removeAll();
            progressPanel.add(errorLabel);
            setError(updateError);
            progressPanel.revalidate();
            progressPanel.repaint();
        });
    }

    void setError(String msg) {
        assert SwingUtilities.isEventDispatchThread();
        errorLabel.setText(msg);
        if (msg != null && !msg.trim().isEmpty()) {
            errorLabel.setFocusable(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * - * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        errorLabel = BrokenProjectInfo.getErrorPane("dummy");
        infoLabel = new JLabel();
        activateButton = new JButton();
        progressPanel = new JPanel();
        downloadLabel = new JLabel();
        downloadButton = new JButton();
        selectionsPanel = new JPanel();

        errorLabel.setFocusable(false);

        Mnemonics.setLocalizedText(infoLabel, "dummy"); // NOI18N

        Mnemonics.setLocalizedText(activateButton, "dummy"); // NOI18N
        activateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                activateButtonActionPerformed(evt);
            }
        });

        progressPanel.setMinimumSize(new Dimension(0, 35));
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.PAGE_AXIS));

        Mnemonics.setLocalizedText(downloadLabel, NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.downloadLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(downloadButton, NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.downloadButton.text")); // NOI18N
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        selectionsPanel.setLayout(new BoxLayout(selectionsPanel, BoxLayout.PAGE_AXIS));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(progressPanel, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(errorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(downloadButton)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(activateButton))
                            .addComponent(downloadLabel)
                            .addComponent(infoLabel)
                            .addComponent(selectionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(errorLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(infoLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(downloadLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(selectionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(activateButton)
                    .addComponent(downloadButton))
                .addGap(19, 19, 19)
                .addComponent(progressPanel, GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void activateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_activateButtonActionPerformed
        FeatureManager.logUI("ERGO_DOWNLOAD");
        activateButton.setEnabled(false);
        downloadButton.setEnabled(false);
        selectionsPanel.setEnabled(false);
        Task task = FeatureManager.getInstance().create(() -> {
            ModulesInstaller.activateModules(false, progressMonitor, featureInfo, featureInstall, extrasFilter);
        });
        task.addTaskListener(onActivationFinished());
        task.schedule(0);
    }//GEN-LAST:event_activateButtonActionPerformed

    private void downloadButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        FeatureManager.logUI("ERGO_DOWNLOAD");
        activateButton.setEnabled(false);
        downloadButton.setEnabled(false);
        selectionsPanel.setEnabled(false);
        Task task = FeatureManager.getInstance().create(() -> {
            ModulesInstaller.activateModules(true, progressMonitor, featureInfo, Collections.emptyList(), extrasFilter);
        });
        task.addTaskListener(onActivationFinished());
        task.schedule(0);
    }//GEN-LAST:event_downloadButtonActionPerformed

    private TaskListener onActivationFinished() {
        return (task) -> {
            if (progressMonitor.error) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                ConfigurationPanel.this.removeAll();
                ConfigurationPanel.this.setLayout(new BorderLayout());
                try {
                    ConfigurationPanel.this.add(callable.call(), BorderLayout.CENTER);
                } catch (Exception ex) {
                    Exceptions.attachSeverity(ex, Level.INFO);
                    Exceptions.printStackTrace(ex);
                }
                ConfigurationPanel.this.invalidate();
                ConfigurationPanel.this.revalidate();
                ConfigurationPanel.this.repaint();
                if (featureInfo != null && !featureInfo.isEnabled()) {
                    String msg;
                    if (featureInfo.isPresent()) {
                        msg = NbBundle.getMessage(ConfigurationPanel.class, "MSG_EnableFailed");
                    } else {
                        msg = NbBundle.getMessage(ConfigurationPanel.class, "MSG_DownloadFailed");
                    }
                    progressMonitor.onError(msg);
                    return;
                }
                activateButton.setEnabled(true);
                progressPanel.removeAll();
                progressPanel.revalidate();
                progressPanel.repaint();
            });
        };
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton activateButton;
    private JButton downloadButton;
    private JLabel downloadLabel;
    private JEditorPane errorLabel;
    private JLabel infoLabel;
    private JPanel progressPanel;
    private JPanel selectionsPanel;
    // End of variables declaration//GEN-END:variables

    public void onPrepare(ProgressHandle progressHandle) {
        progressMonitor.updateProgress(progressHandle);
    }
    
    private final class ActivationProgressMonitor implements ProgressMonitor {

        boolean error = false;

        @Override
        public void onDownload(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        @Override
        public void onValidate(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        @Override
        public void onInstall(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        @Override
        public void onEnable(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        private void updateProgress(final ProgressHandle progressHandle) {
            final JLabel tmpMainLabel = ProgressHandleFactory.createMainLabelComponent(progressHandle);
            final JComponent tmpProgressPanel = ProgressHandleFactory.createProgressComponent(progressHandle);
            SwingUtilities.invokeLater(() -> {
                progressPanel.removeAll();
                progressPanel.add(tmpMainLabel);
                progressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                progressPanel.add(tmpProgressPanel);
                progressPanel.revalidate();
                progressPanel.repaint();
            });
        }

        @Override
        public void onError(final String message) {
            error = true;
            SwingUtilities.invokeLater(() -> {
                setError("<html>" + message + "</html>"); // NOI18N
                progressPanel.removeAll();
                progressPanel.add(errorLabel);
                downloadButton.setEnabled(true);
            });
        }
    }
}
