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

package org.netbeans.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class PreInstallSummaryPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public PreInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        
        setProperty(MESSAGE_TEXT_PROPERTY,
                DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY,
                DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(COMPONENTS_TO_INSTALL_LABEL_TEXT_PROPERTY,
                DEFAULT_COMPONENTS_TO_INSTALL_LABEL_TEXT);
        setProperty(COMPONENTS_TO_INSTALL_TEXT_PROPERTY,
                DEFAULT_COMPONENTS_TO_INSTALL_TEXT);
        setProperty(COMPONENTS_TO_INSTALL_CONTENT_TYPE_PROPERTY,
                DEFAULT_COMPONENTS_TO_INSTALL_CONTENT_TYPE);
        setProperty(COMPONENTS_TO_UNINSTALL_LABEL_TEXT_PROPERTY,
                DEFAULT_COMPONENTS_TO_UNINSTALL_LABEL_TEXT);
        setProperty(COMPONENTS_TO_UNINSTALL_TEXT_PROPERTY,
                DEFAULT_COMPONENTS_TO_UNINSTALL_TEXT);
        setProperty(COMPONENTS_TO_UNINSTALL_CONTENT_TYPE_PROPERTY,
                DEFAULT_COMPONENTS_TO_UNINSTALL_CONTENT_TYPE);
        setProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY,
                DEFAULT_COMPONENTS_LIST_SEPARATOR);
        setProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT);
        setProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY,
                DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT);
        setProperty(INSTALL_BUTTON_TEXT_PROPERTY,
                DEFAULT_INSTALL_BUTTON_TEXT);
        
        setProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY,
                DEFAULT_ERROR_NOT_ENOUGH_SPACE);
        setProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY,
                DEFAULT_ERROR_CANNOT_CHECK_SPACE);
        setProperty(ERROR_LOGIC_ACCESS_PROPERTY,
                DEFAULT_ERROR_LOGIC_ACCESS);
        setProperty(ERROR_FSROOTS_PROPERTY,
                DEFAULT_ERROR_FSROOTS);
        setProperty(ERROR_NON_EXISTENT_ROOT_PROPERTY,
                DEFAULT_ERROR_NON_EXISTENT_ROOT);
        setProperty(ERROR_CANNOT_WRITE_PROPERTY,
                DEFAULT_ERROR_CANNOT_WRITE);
    }
    
    @Override
    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToInstall().size() +
                Registry.getInstance().getProductsToUninstall().size() > 0;
    }
    
    @Override
    public boolean canExecuteBackward() {
        return Registry.getInstance().getProductsToInstall().size() +
                Registry.getInstance().getProductsToUninstall().size() > 0;
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PreInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PreInstallSummaryPanelUi extends ErrorMessagePanelUi {
        protected PreInstallSummaryPanel panel;
        
        public PreInstallSummaryPanelUi(PreInstallSummaryPanel panel) {
            super(panel);
            
            this.panel = panel;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PreInstallSummaryPanelSwingUi(panel, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class PreInstallSummaryPanelSwingUi extends ErrorMessagePanelSwingUi {
        protected PreInstallSummaryPanel panel;
        
        private NbiTextPane messagePane;
        private NbiLabel componentsToInstallLabel;
        private NbiTextPane componentsToInstallPane;
        private NbiLabel componentsToUninstallLabel;
        private NbiTextPane componentsToUninstallPane;
        private NbiLabel downloadSizeLabel;
        private NbiLabel requiredDiskSpaceLabel;
        
        private NbiPanel spacer;
        
        public PreInstallSummaryPanelSwingUi(
                final PreInstallSummaryPanel panel,
                final SwingContainer container) {
            super(panel, container);
            
            this.panel = panel;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            container.getNextButton().setText(
                    panel.getProperty(INSTALL_BUTTON_TEXT_PROPERTY));
        }
        
        @Override
        protected void initialize() {
            final String messageContentType = panel.getProperty(MESSAGE_CONTENT_TYPE_PROPERTY);
            messagePane.setContentType(messageContentType);
            
            final String messageText = panel.getProperty(MESSAGE_TEXT_PROPERTY);
            messagePane.setText(messageText);
            
            List<Product> componentsToInstall = Registry.getInstance().getProductsToInstall();
            List<Product> componentsToUninstall = Registry.getInstance().getProductsToUninstall();
            
            if (componentsToUninstall.size() > 0) {
                componentsToUninstallLabel.setVisible(true);
                componentsToUninstallPane.setVisible(true);
                
                final String componentsToUninstallLabelText = panel.getProperty(
                        COMPONENTS_TO_UNINSTALL_LABEL_TEXT_PROPERTY);
                componentsToUninstallLabel.setText(componentsToUninstallLabelText);
                
                final String componentsToUninstallContentType = panel.getProperty(
                        COMPONENTS_TO_UNINSTALL_CONTENT_TYPE_PROPERTY);
                componentsToUninstallPane.setContentType(componentsToUninstallContentType);
                
                final String componentsToUninstallText = StringUtils.format(
                        panel.getProperty(COMPONENTS_TO_UNINSTALL_TEXT_PROPERTY),
                        StringUtils.asString(componentsToUninstall, 
                        panel.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
                componentsToUninstallPane.setText(componentsToUninstallText);
            } else {
                componentsToUninstallLabel.setVisible(false);
                componentsToUninstallPane.setVisible(false);
            }
            
            if (componentsToInstall.size() > 0) {
                componentsToInstallLabel.setVisible(true);
                componentsToInstallPane.setVisible(true);
                downloadSizeLabel.setVisible(true);
                requiredDiskSpaceLabel.setVisible(true);
                
                final String componentsToInstallLabelText = panel.getProperty(
                        COMPONENTS_TO_INSTALL_LABEL_TEXT_PROPERTY);
                componentsToInstallLabel.setText(componentsToInstallLabelText);
                
                final String componentsToInstallContentType = panel.getProperty(
                        COMPONENTS_TO_INSTALL_CONTENT_TYPE_PROPERTY);
                componentsToInstallPane.setContentType(componentsToInstallContentType);
                
                final String componentsToInstallText = StringUtils.format(
                        panel.getProperty(COMPONENTS_TO_INSTALL_TEXT_PROPERTY),
                        StringUtils.asString(componentsToInstall, 
                        panel.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
                
                componentsToInstallPane.setText(componentsToInstallText);
                
                long downloadSize = 0;
                for (Product component: componentsToInstall) {
                    downloadSize += component.getDownloadSize();
                }
                
                long requiredDiskSpace = 0;
                for (Product component: componentsToInstall) {
                    requiredDiskSpace += component.getRequiredDiskSpace();
                }
                
                final String downloadSizeLabelText = StringUtils.format(
                        panel.getProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), 
                        StringUtils.formatSize(downloadSize));
                
                downloadSizeLabel.setText(downloadSizeLabelText);
                
                final String requiredDiskSpaceLabelText = StringUtils.format(
                        panel.getProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY), 
                        StringUtils.formatSize(requiredDiskSpace));
                
                requiredDiskSpaceLabel.setText(requiredDiskSpaceLabelText);
            } else {
                componentsToInstallLabel.setVisible(false);
                componentsToInstallPane.setVisible(false);
                downloadSizeLabel.setVisible(false);
                requiredDiskSpaceLabel.setVisible(false);
            }
        }
        
        @Override
        protected String validateInput() {
            try {
                if(!Boolean.getBoolean(SystemUtils.NO_SPACE_CHECK_PROPERTY)) {
                    final List<Product> toInstall = Registry.getInstance().getProductsToInstall();
                    final Map<File, Long> spaceMap = new HashMap<File, Long>();
                    final File downloadDataDir = Installer.getInstance().getLocalDirectory();
                    
                    // only roots for appropriate files
                    final String[] installFiles = new String[toInstall.size() + 1];                    
                    for (int i = 0; i < toInstall.size(); i++) {
                        installFiles[i] = toInstall.get(i).getInstallationLocation().getAbsolutePath();
                    }
                    installFiles[installFiles.length - 1] = downloadDataDir.getAbsolutePath();
                    final List<File> roots = SystemUtils.getFileSystemRoots(installFiles);
                    
                    LogManager.log("Roots : " + StringUtils.asString(roots));
                    
                    File downloadDataDirRoot = FileUtils.getRoot(downloadDataDir, roots);
                    long downloadSize = 0;
                    for (Product product: toInstall) {
                        downloadSize+=product.getDownloadSize();
                    }
                    // the critical check point - we download all the data
                    spaceMap.put(downloadDataDirRoot, downloadSize);                
                    long lastDataSize = 0;
                    
                    for (Product product: toInstall) {
                        final File installLocation = product.getInstallationLocation();                    
                        final File root = FileUtils.getRoot(installLocation, roots);
                        final long productSize = product.getRequiredDiskSpace();
                        
                        LogManager.log("    [" + root + "] <- " + installLocation);
                        
                        if ( root != null ) {                        
                            Long ddSize =  spaceMap.get(downloadDataDirRoot);
                            // remove space that was freed after the remove of previos product data
                            spaceMap.put(downloadDataDirRoot, 
                                    Long.valueOf(ddSize - lastDataSize));
                            
                            Long size = spaceMap.get(root);
                            size = Long.valueOf(
                                    (size != null ? size.longValue() : 0L) +
                                    productSize);                        
                            spaceMap.put(root, size);   
                            lastDataSize = product.getDownloadSize();
                        } else {
                            return StringUtils.format(
                                    panel.getProperty(ERROR_NON_EXISTENT_ROOT_PROPERTY),
                                    product, installLocation);
                        }
                    }
                
                    for (Map.Entry<File, Long> entry: spaceMap.entrySet()) {
                        File root = entry.getKey();

                        try {
                            final long availableSpace =
                                    SystemUtils.getFreeSpace(root);
                            final long requiredSpace = entry.getValue() + REQUIRED_SPACE_ADDITION;
                            
                            if (availableSpace < requiredSpace) {
                                return StringUtils.format(
                                        panel.getProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY),
                                        root,
                                        StringUtils.formatSize(requiredSpace - availableSpace));
                            }
                        } catch (NativeException e) {
                            ErrorManager.notifyError(
                                    panel.getProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY),
                                    e);
                        }
                    }
                }
                final List<Product> toUninstall =
                        Registry.getInstance().getProductsToUninstall();
                for (Product product: toUninstall) {
                    if (!FileUtils.canWrite(product.getInstallationLocation())) {
                        return StringUtils.format(
                                panel.getProperty(ERROR_CANNOT_WRITE_PROPERTY),
                                product,
                                product.getInstallationLocation());
                    }
                }
                
            } catch (IOException e) {
                ErrorManager.notifyError(
                        panel.getProperty(ERROR_FSROOTS_PROPERTY), e);
            }
            
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // messagePane //////////////////////////////////////////////////////////
            messagePane = new NbiTextPane();
            
            // componentsToUninstallPane ////////////////////////////////////////////
            componentsToUninstallPane = new NbiTextPane();
            
            // componentsToUninstallLabel ///////////////////////////////////////////
            componentsToUninstallLabel = new NbiLabel();
            componentsToUninstallLabel.setLabelFor(componentsToUninstallPane);
            
            // componentsToInstallPane //////////////////////////////////////////////
            componentsToInstallPane = new NbiTextPane();
            
            // componentsToInstallLabel /////////////////////////////////////////////
            componentsToInstallLabel = new NbiLabel();
            componentsToInstallLabel.setLabelFor(componentsToInstallPane);
            
            // downloadSizeLabel ////////////////////////////////////////////////////
            downloadSizeLabel = new NbiLabel();
            //downloadSizeLabel.setFocusable(true);
            
            // requiredDiskSpaceLabel ///////////////////////////////////////////////
            requiredDiskSpaceLabel = new NbiLabel();
            //requiredDiskSpaceLabel.setFocusable(true);
            
            // spacer ///////////////////////////////////////////////////////////////
            spacer = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(messagePane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsToUninstallLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsToUninstallPane, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(componentsToInstallLabel, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsToInstallPane, new GridBagConstraints(
                    0, 4,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeLabel, new GridBagConstraints(
                    0, 5,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(25, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(requiredDiskSpaceLabel, new GridBagConstraints(
                    0, 6,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, 7,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.title");
    
    public static final String MESSAGE_TEXT_PROPERTY =
            "message.text";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY =
            "message.content.type";
    public static final String COMPONENTS_TO_INSTALL_LABEL_TEXT_PROPERTY =
            "components.to.install.label.text";
    public static final String COMPONENTS_TO_INSTALL_TEXT_PROPERTY =
            "components.to.install.text";
    public static final String COMPONENTS_TO_INSTALL_CONTENT_TYPE_PROPERTY =
            "components.to.install.content.type";
    public static final String COMPONENTS_TO_UNINSTALL_LABEL_TEXT_PROPERTY =
            "components.to.uninstall.label.text";
    public static final String COMPONENTS_TO_UNINSTALL_TEXT_PROPERTY =
            "components.to.uninstall.text";
    public static final String COMPONENTS_TO_UNINSTALL_CONTENT_TYPE_PROPERTY =
            "components.to.uninstall.content.type";
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY =
            "components.list.separator";
    public static final String DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY =
            "download.size.label.text";
    public static final String REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY =
            "required.disk.space.label.text";
    public static final String INSTALL_BUTTON_TEXT_PROPERTY =
            "install.button.text";
    
    
    public static final String ERROR_NOT_ENOUGH_SPACE_PROPERTY =
            "error.not.enough.space"; // NOI18N
    public static final String ERROR_CANNOT_CHECK_SPACE_PROPERTY =
            "error.cannot.check.space"; // NOI18N
    public static final String ERROR_LOGIC_ACCESS_PROPERTY =
            "error.logic.access"; // NOI18N
    public static final String ERROR_FSROOTS_PROPERTY =
            "error.fsroots"; // NOI18N
    public static final String ERROR_NON_EXISTENT_ROOT_PROPERTY =
            "error.non.existent.root"; // NOI18N
    public static final String ERROR_CANNOT_WRITE_PROPERTY =
            "error.cannot.write"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.message.content.type");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_LABEL_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.components.to.install.label.text");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.components.to.install.text");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_CONTENT_TYPE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.components.to.install.content.type");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_LABEL_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.components.to.uninstall.label.text");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.components.to.uninstall.text");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_CONTENT_TYPE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.components.to.uninstall.content.type");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.components.list.separator");
    public static final String DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.download.size.label.text");
    public static final String DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.required.disk.space.label.text");
    public static final String DEFAULT_INSTALL_BUTTON_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.install.button.text");
    public static final String DEFAULT_ERROR_NOT_ENOUGH_SPACE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.not.enough.space"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_CHECK_SPACE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.cannot.check.space");// NOI18N
    public static final String DEFAULT_ERROR_LOGIC_ACCESS =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.logic.access");// NOI18N
    public static final String DEFAULT_ERROR_FSROOTS =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.fsroots"); // NOI18N
    public static final String DEFAULT_ERROR_NON_EXISTENT_ROOT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.non.existent.root"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_WRITE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.cannot.write"); // NOI18N
    
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
