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

package org.netbeans.modules.payara.common.wizards;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.payara.common.ServerDetails;
import org.netbeans.modules.payara.spi.Utils;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author pblaha
 * @author Peter Williams
 */
public class AddServerLocationVisualPanel extends javax.swing.JPanel implements Retriever.Updater {

    public static enum DownloadState { AVAILABLE, DOWNLOADING, COMPLETED };

    public static final String DOWNLOAD_PREFIX = "https://www.payara.fish/"; // NOI18N
    
    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private Retriever retriever;
    private volatile DownloadState downloadState;
    private volatile String statusText;
    private ServerWizardIterator wizardIterator;

    public AddServerLocationVisualPanel(ServerWizardIterator swi) {
        this.wizardIterator = swi;
        initComponents();
        initUserComponents();
    }

    private void initUserComponents() {
        downloadButton.setEnabled(false);
        
        setName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "TITLE_ServerLocation"));
        
        hk2HomeTextField.setText(getPreviousValue());            
        hk2HomeTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                homeFolderChanged();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                homeFolderChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                homeFolderChanged();
            }                    
        });
        chooseServerComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    serverVersionChanged();
                }
            }
        });
        setDownloadState(DownloadState.AVAILABLE);
        updateMessageText("");
        // Set initial radio buttons status to local server.
        localDomainRadioButton.setSelected(true);
        remoteDomainRadioButton.setSelected(false);
        wizardIterator.setLocal(true);
    }
    
    private String getPreviousValue() {
        Preferences prefs = NbPreferences.forModule(wizardIterator.getClass());
        String prevValue = null;
        if (null != prefs) {
            prevValue = prefs.get("INSTALL_ROOT_KEY", null); // NOI18N
        }
        if (null == prevValue) {
            String installDir = System.getProperty("INSTALL_ROOT_PROPERTY"); // NOI18N
            if (null != installDir && !(installDir.trim().length() == 0)) {
                 return installDir;
            } else {
                return System.getProperty("user.home") + File.separatorChar + "Payara_Server"; // NOI18N
            }
        } else {
            return prevValue;            
        }        
    }
    
    public DownloadState getDownloadState() {
        return downloadState;
    }

    /**
     * 
     * @return 
     */
    public String getHk2HomeLocation() {
        return hk2HomeTextField.getText();
    }
    
    /**
     * 
     * @return
     */
    public String getStatusText() {
        return statusText;
    }
    
    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * Get local/remote server selection.
     * <p/>
     * @return Value of <code>true</code> when local button is selected
     *         and <code>false</code> when remote button is selected.
     */
    public boolean isLocal() {
        return localDomainRadioButton.isSelected();
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l ) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }
    
    private String browseHomeLocation() {
        String hk2Location = null;
        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(this, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseButton")); //NOI18N
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            hk2Location = chooser.getSelectedFile().getAbsolutePath();
        }
        return hk2Location;
    }
    
    private JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        String t = NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName");
        chooser.setDialogTitle(t); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(new DirFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(t); //NOI18N
        chooser.getAccessibleContext().setAccessibleName(t); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription(t); //NOI18N

        // set the current directory
        File currentLocation = new File(hk2HomeTextField.getText());
        File currentLocationParent = currentLocation.getParentFile();
        if(currentLocationParent != null && currentLocationParent.exists()) {
            chooser.setCurrentDirectory(currentLocationParent);
        }
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            chooser.setSelectedFile(currentLocation);
        } 
        
        return chooser;
    }   
    
    @Override
    public void removeNotify() {
        // !PW Is there a better place for this?  If the retriever is still running
        // the user must have hit cancel on the wizard, so tell the retriever thread
        // to shut down and clean up.
        if(retriever != null) {
            retriever.stopRetrieval();
        }
        super.removeNotify();
    }
    
    // ------------------------------------------------------------------------
    // Updater implementation
    // ------------------------------------------------------------------------
    @Override
    public void updateMessageText(final String msg) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                if (msg.trim().startsWith("<html>")) {
                    downloadStatusLabel.setText(msg);
                } else {
                    downloadStatusLabel.setText("<html>"+msg+"</html>");
                }
                fireChangeEvent();
            }
        });
    }
    
    @Override
    public void updateStatusText(final String status) {
        statusText = status;
        fireChangeEvent();
    }

    @Override
    public void clearCancelState() {
        setDownloadState(retriever.getDownloadState() == Retriever.STATUS_COMPLETE ? 
            DownloadState.COMPLETED : DownloadState.AVAILABLE);
        retriever = null;
    }
    
    // ------------------------------------------------------------------------
    private void updateButton() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                DownloadState state = AddServerLocationVisualPanel.this.downloadState;
                boolean licenseAccepted = agreeCheckBox.isSelected();
                File val = new File(hk2HomeTextField.getText().trim());
                boolean writableLoc = AddServerLocationPanel.canCreate(val) || Utils.canWrite(val);
                String buttonTextKey = 
                        state == DownloadState.DOWNLOADING ? "LBL_CancelDownload" : 
                        state == DownloadState.COMPLETED ? "LBL_DownloadComplete" : "LBL_DownloadNow";
                String buttonText = NbBundle.getMessage(AddServerLocationVisualPanel.class, buttonTextKey);
                downloadButton.setText(buttonText);
                downloadButton.setEnabled(state != DownloadState.COMPLETED && licenseAccepted && writableLoc);
            }
        });
    }
    
    private synchronized void setDownloadState(DownloadState state) {
        downloadState = state;
        updateButton();
    }
    
    private void homeFolderChanged() {
        updateMessageText("");
        if(downloadState == DownloadState.COMPLETED) {
            setDownloadState(DownloadState.AVAILABLE);
        } else {
            updateButton();
        }
    }
    
    private void serverVersionChanged() {
        agreeCheckBox.setSelected(false);
        agreeCheckBoxActionPerformed(null);
    }
       
    private static class DirFilter extends javax.swing.filechooser.FileFilter {
        DirFilter() {
        }
        
        @Override
        public boolean accept(File f) {
            if(!f.exists() || !f.canRead() || !f.isDirectory()) {
                return false;
            } else {
                return true;
            }
        }
        
        @Override
        public String getDescription() {
            return NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_DirType");
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        domainType = new javax.swing.ButtonGroup();
        hk2HomeLabel = new javax.swing.JLabel();
        hk2HomeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        agreeCheckBox = new javax.swing.JCheckBox();
        readlicenseButton = new javax.swing.JButton();
        downloadStatusLabel = new javax.swing.JLabel();
        localDomainRadioButton = new javax.swing.JRadioButton();
        remoteDomainRadioButton = new javax.swing.JRadioButton();
        chooseServerLabel = new javax.swing.JLabel();
        chooseServerComboBox = new javax.swing.JComboBox<ServerDetails>(wizardIterator.downloadableValues);

        hk2HomeLabel.setLabelFor(hk2HomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hk2HomeLabel, org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        downloadButton.setText("[download/cancel]"); // NOI18N
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        agreeCheckBox.setMargin(new java.awt.Insets(4, 0, 4, 4));
        agreeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agreeCheckBoxActionPerformed(evt);
            }
        });

        readlicenseButton.setText(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ReadLicenseText")); // NOI18N
        readlicenseButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        readlicenseButton.setBorderPainted(false);
        readlicenseButton.setContentAreaFilled(false);
        readlicenseButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        readlicenseButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        readlicenseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readlicenseButtonActionPerformed(evt);
            }
        });

        downloadStatusLabel.setText("[download status]"); // NOI18N
        downloadStatusLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        downloadStatusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        domainType.add(localDomainRadioButton);
        localDomainRadioButton.setText(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.localDomainRadioButton")); // NOI18N
        localDomainRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localDomainRadioButtonActionPerformed(evt);
            }
        });

        domainType.add(remoteDomainRadioButton);
        remoteDomainRadioButton.setText(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.remoteDomainRadioButton")); // NOI18N
        remoteDomainRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remoteDomainRadioButtonActionPerformed(evt);
            }
        });

        chooseServerLabel.setLabelFor(hk2HomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(chooseServerLabel, org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseOne")); // NOI18N

        chooseServerComboBox.setSelectedItem(wizardIterator.downloadableValues[0]);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(downloadStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(localDomainRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addComponent(remoteDomainRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hk2HomeTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hk2HomeLabel)
                            .addComponent(chooseServerLabel)
                            .addComponent(chooseServerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(downloadButton)
                        .addGap(18, 18, 18)
                        .addComponent(agreeCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(readlicenseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(hk2HomeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hk2HomeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(localDomainRadioButton)
                    .addComponent(remoteDomainRadioButton))
                .addGap(10, 10, 10)
                .addComponent(chooseServerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chooseServerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(agreeCheckBox)
                            .addComponent(downloadButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                        .addComponent(downloadStatusLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(readlicenseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        hk2HomeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.hk2HomeTextField.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        agreeCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.agreeCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        readlicenseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.readlicenseButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void readlicenseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readlicenseButtonActionPerformed
        try {
            ServerDetails chosenServerVersion = (ServerDetails) chooseServerComboBox.getSelectedItem();
            URLDisplayer.getDefault().showURL(
                    new URL(chosenServerVersion.getLicenseUrl())); //NOI18N
        } catch (Exception ex){
            Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
}//GEN-LAST:event_readlicenseButtonActionPerformed

private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        if(retriever == null) {
            ServerDetails selectedValue = wizardIterator.downloadableValues[0];
            if (wizardIterator.downloadableValues.length > 1) {
                selectedValue = (ServerDetails) chooseServerComboBox.getSelectedItem();
            }
            if (null != selectedValue) {
            updateStatusText("");  // NOI18N
            retriever = new Retriever(new File(hk2HomeTextField.getText()), 
                    selectedValue.getIndirectUrl(), DOWNLOAD_PREFIX,
                    selectedValue.getDirectUrl(),
                    this, "payara"); // NOI18N
            new Thread(retriever).start();
            setDownloadState(DownloadState.DOWNLOADING);
            }
        } else {
            retriever.stopRetrieval();
            setDownloadState(DownloadState.AVAILABLE);
        }
}//GEN-LAST:event_downloadButtonActionPerformed

private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String newLoc = browseHomeLocation();
        if(newLoc != null && newLoc.length() > 0) {
            hk2HomeTextField.setText(newLoc);
        }
}//GEN-LAST:event_browseButtonActionPerformed

private void agreeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agreeCheckBoxActionPerformed
        DownloadState state = downloadState;
        if(state == DownloadState.COMPLETED) {
            setDownloadState(DownloadState.AVAILABLE);
        } else {
            File val = new File(hk2HomeTextField.getText().trim());
            boolean writableLoc = AddServerLocationPanel.canCreate(val) || Utils.canWrite(val);
            downloadButton.setEnabled(agreeCheckBox.isSelected() && writableLoc);
        }
}//GEN-LAST:event_agreeCheckBoxActionPerformed

    private void remoteDomainRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remoteDomainRadioButtonActionPerformed
        wizardIterator.setLocal(!remoteDomainRadioButton.isSelected());
    }//GEN-LAST:event_remoteDomainRadioButtonActionPerformed

    private void localDomainRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localDomainRadioButtonActionPerformed
         wizardIterator.setLocal(localDomainRadioButton.isSelected());
    }//GEN-LAST:event_localDomainRadioButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox agreeCheckBox;
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox<ServerDetails> chooseServerComboBox;
    private javax.swing.JLabel chooseServerLabel;
    private javax.swing.ButtonGroup domainType;
    private javax.swing.JButton downloadButton;
    private javax.swing.JLabel downloadStatusLabel;
    private javax.swing.JLabel hk2HomeLabel;
    private javax.swing.JTextField hk2HomeTextField;
    private javax.swing.JRadioButton localDomainRadioButton;
    private javax.swing.JButton readlicenseButton;
    private javax.swing.JRadioButton remoteDomainRadioButton;
    // End of variables declaration//GEN-END:variables
    
}
