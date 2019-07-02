/**
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

package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.utils.BrowserUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.UninstallUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 */
public class NbPreInstallSummaryPanel extends ErrorMessagePanel {
    private boolean removeNBInstallationLocation = false;
    private boolean removeNBUserDir = false;
    private File userDir;
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbPreInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(INSTALLATION_FOLDER_PROPERTY,
                DEFAULT_INSTALLATION_FOLDER);
        setProperty(INSTALLATION_FOLDER_NETBEANS_PROPERTY,
                DEFAULT_INSTALLATION_FOLDER_NETBEANS);
        setProperty(UNINSTALL_LIST_LABEL_TEXT_PROPERTY,
                DEFAULT_UNINSTALL_LIST_LABEL_TEXT);
        setProperty(UNINSTALL_LIST_NETBEANS_LABEL_TEXT_PROPERTY,
                DEFAULT_UNINSTALL_LIST_NETBEANS_LABEL_TEXT);

        setProperty(INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);
        setProperty(NB_ADDONS_LOCATION_TEXT_PROPERTY,
                DEFAULT_NB_ADDONS_LOCATION_TEXT);
        setProperty(GF_ADDONS_LOCATION_TEXT_PROPERTY,
                DEFAULT_GF_ADDONS_LOCATION_TEXT);
        setProperty(AS_ADDONS_LOCATION_TEXT_PROPERTY,
                DEFAULT_AS_ADDONS_LOCATION_TEXT);
        setProperty(JUNIT_PRESENT_TEXT_PROPERTY,
                DEFAULT_JUNIT_PRESENT_TEXT_PROPERTY);
        
        setProperty(NEXT_BUTTON_TEXT_PROPERTY,
                DEFAULT_NEXT_BUTTON_TEXT);
        
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
        setProperty(REMOVE_NETBEANS_USERDIR_TEXT_PROPERTY,
                DEFAULT_REMOVE_NETBEANS_USERDIR_TEXT);
        setProperty(REMOVE_NETBEANS_INSTALLDIR_TEXT_PROPERTY,
                DEFAULT_REMOVE_NETBEANS_INSTALLDIR_TEXT);
        setProperty(REMOVE_NETBEANS_USERDIR_CHECKBOX_PROPERTY,
                DEFAULT_REMOVE_NETBEANS_USERDIR_CHECKBOX);
        setProperty(REMOVE_NETBEANS_USERDIR_LINK_PROPERTY,
                DEFAULT_REMOVE_NETBEANS_USERDIR_LINK);
        setProperty(REMOVE_NETBEANS_INSTALLDIR_CHECKBOX_PROPERTY,
                DEFAULT_REMOVE_NETBEANS_INSTALLDIR_CHECKBOX);
        setProperty(CHECK_FOR_UPDATES_TEXT_PROPERTY,
                DEFAULT_CHECK_FOR_UPDATES_TEXT);
        setProperty(CHECK_FOR_UPDATES_CHECKBOX_PROPERTY,
                DEFAULT_CHECK_FOR_UPDATES_CHECKBOX);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbPreInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public void initialize() {
        assert ! SwingUtilities.isEventDispatchThread() : "Cannot run initialize() in EQ!";
        final List<Product> toInstall =
                Registry.getInstance().getProductsToInstall();
        final List<Product> toUnInstall =
                Registry.getInstance().getProductsToUninstall();
        
        if (toInstall.size() > 0) {
            setProperty(NEXT_BUTTON_TEXT_PROPERTY, DEFAULT_NEXT_BUTTON_TEXT);
            setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        } else {
            setProperty(NEXT_BUTTON_TEXT_PROPERTY, DEFAULT_NEXT_BUTTON_TEXT_UNINSTALL);
            setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION_UNINSTALL);
        }
        
        for (Product product : toUnInstall) {
            if (product.getUid().equals(NB_BASE_UID)) {
                File installLocation = product.getInstallationLocation();
                try {
                    removeNBInstallationLocation = FileUtils.canWrite(installLocation) &&
                            areThereNewFiles(installLocation);
                    userDir = NetBeansUtils.getNetBeansUserDirFile(installLocation);
                    removeNBUserDir = (FileUtils.exists(userDir) && FileUtils.canWrite(userDir));
                }catch (IOException ioe) {
                    LogManager.log(ioe);
                }
            }
        }       
    }

    private boolean doRemoveNBInstallationLocation() {
        return removeNBInstallationLocation;
    }

    private boolean doRemoveNBUserDir() {
        return removeNBUserDir;
    }

    private boolean areThereNewFiles(final File installLocation) throws IOException {
        LogManager.log("areThereNewFiles:  location "  + installLocation);
        Set<File> installedFiles = new HashSet<File>();
        Set<File> existentFilesList = FileUtils.getRecursiveFileSet(installLocation);

        for (Product product : Registry.getInstance().getProductsToUninstall()) {
            LogManager.log("Taking product " + product.getUid());
            if(product.getUid().startsWith("nb-")) {
                // load the installed files list for this product
                try {
                    File installedFilesList = product.getInstalledFilesList();
                    if(installedFilesList.exists()) {
                        FilesList list = new FilesList().loadXmlGz(installedFilesList);
                        LogManager.log("loading files list for " + product.getUid());
                        installedFiles.addAll(list.toList());
                    }
                } catch (XMLException e) {
                    LogManager.log(ErrorLevel.WARNING,
                            "Error loading file list for " + product.getUid());
                    return false;
                }
            }
        }
        
        //add all updated files and downloaded plugins
        installedFiles.addAll(UninstallUtils.getFilesToDeteleAfterUninstallation());
                                      
        existentFilesList.removeAll(installedFiles);        
        
        //remove folders - there still might be some empty folders
        Iterator<File> eflIterator = existentFilesList.iterator();
        while (eflIterator.hasNext()) {
            if (eflIterator.next().isDirectory()) {
                eflIterator.remove();
            }
        }
        
        boolean result = !existentFilesList.isEmpty();
        LogManager.log(ErrorLevel.DEBUG, "installedFiles " + Arrays.toString(installedFiles.toArray()));
        LogManager.log(ErrorLevel.DEBUG, "existentFilesList after removal " + Arrays.toString(existentFilesList.toArray()));
        LogManager.log("areThereNewFiles returned " + result);
        return result;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbPreInstallSummaryPanelUi extends ErrorMessagePanelUi {
        @SuppressWarnings("FieldNameHidesFieldInSuperclass")
        protected NbPreInstallSummaryPanel component;
        
        public NbPreInstallSummaryPanelUi(NbPreInstallSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbPreInstallSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbPreInstallSummaryPanelSwingUi extends ErrorMessagePanelSwingUi {
        @SuppressWarnings("FieldNameHidesFieldInSuperclass")
        protected NbPreInstallSummaryPanel component;
        
        private NbiTextPane locationsPane;
        
        private NbiLabel uninstallListLabel;
        private NbiTextPane uninstallListPane;
        
        private NbiLabel installationSizeLabel;
        private NbiLabel installationSizeValue;
        
        private NbiLabel downloadSizeLabel;
        private NbiLabel downloadSizeValue;

        private NbiCheckBox removeUserdirCheckbox;
        private NbiCheckBox removeInstalldirCheckbox;
        private NbiTextPane removeUserdirPane;
        private NbiTextPane removeInstalldirPane;
        private NbiLabel foldersToRemove;
        private NbiPanel spacer;
        private NbiCheckBox checkForUpdatesCheckbox;
        private NbiTextPane checkForUpdatesPane;
        
        private List <Pair <Product, NbiCheckBox>> productCheckboxList;
        private int gridy = 0 ;
        
        
        public NbPreInstallSummaryPanelSwingUi(
                final NbPreInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            
            container.getNextButton().setText(
                    panel.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
        }
        
        @Override
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            
            final StringBuilder text = new StringBuilder();
            long installationSize = 0;
            long downloadSize = 0;
            
            final List<Product> dependentOnNb = new LinkedList<Product>();
            final List<Product> dependentOnGf = new LinkedList<Product>();
            final List<Product> dependentOnAs = new LinkedList<Product>();
            boolean nbBasePresent = false;
            
            for (Product product: registry.getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
                downloadSize += product.getDownloadSize();
                try {
                    if (product.getLogic().registerInSystem() || product.getUid().equals("jdk")
			    || product.getUid().equals("mysql") || product.getUid().equals("javafxsdk")) {
                        nbBasePresent = product.getUid().equals(NB_ALL_UID) ? true : nbBasePresent;
                    } else {
                        if (product.getUid().startsWith("nb-")) {
                            dependentOnNb.add(product);
                        } else {
                            if(product.getDependencyByUid("glassfish").size()>0) {
                                dependentOnGf.add(product);
                            } else if(product.getDependencyByUid("sjsas").size()>0) {
                                dependentOnAs.add(product);
                            }
                        }
                    }
                } catch (InitializationException e) {
                    ErrorManager.notifyError(
                            panel.getProperty(ERROR_LOGIC_ACCESS_PROPERTY),e);
                }
            }
            
            
            File nbLocation = null;
            Product base;
            // If there are several packs to be installed but Base is already installed
            // then search it and the corresponding record to text
            if (dependentOnNb.size() > 0 && !nbBasePresent) {
                for(Product product : dependentOnNb) {
                    List <Dependency> bases = product.getDependencyByUid(NB_BASE_UID);
                    if(!bases.isEmpty()) {
                        // dependency is already resolved at this point
                        base = Registry.getInstance().getProducts(bases.get(0)).get(0);

                        if(base!=null) {
                            nbLocation = base.getInstallationLocation();
                            try {
                                if(base.getLogic().wrapForMacOs() && SystemUtils.isMacOS()) {
                                    final File app = nbLocation.getParentFile().getParentFile().getParentFile();
                                    nbLocation = app;
                                }
                            } catch (InitializationException e){
                                LogManager.log(".. cannot get logic for " + base.getDisplayName() + " (" + base.getVersion() + ")", e);
                            } catch (NullPointerException e){
                                LogManager.log(".. cannot get app directory for " + nbLocation);
                            }
                            if(nbLocation!=null) {
                                text.append(StringUtils.LF);
                                text.append(StringUtils.format(
                                        panel.getProperty(INSTALLATION_FOLDER_NETBEANS_PROPERTY),
                                        base.getDisplayName()));
                                text.append(StringUtils.LF);
                                text.append("    ");
                                text.append(nbLocation);
                                text.append(StringUtils.LF);                                
                            }
                            break;
                        }
                    }
                }
            }
            
            // add top-level components like nb-base, glassfish, tomcat, jdk
            for (Product product: registry.getProductsToInstall()) {
                try {                                       
                    if (product.getLogic().registerInSystem() ||
                            product.getUid().equals("jdk") || 
                            product.getUid().equals("mysql") ||
                            product.getUid().equals("javafxsdk")) {
                        String property = panel.getProperty(
                                product.getUid().equals(NB_BASE_UID) ?
                                    INSTALLATION_FOLDER_NETBEANS_PROPERTY :
                                    INSTALLATION_FOLDER_PROPERTY);
                        text.append(StringUtils.LF);
                        text.append(StringUtils.format(property,
                                product.getDisplayName()));
                        text.append(StringUtils.LF);
                        text.append("    ").append(product.getInstallationLocation());
                        text.append(StringUtils.LF); 
                    }
                } catch (InitializationException e) {
                    ErrorManager.notifyError(
                            panel.getProperty(ERROR_LOGIC_ACCESS_PROPERTY),e);
                }
            }
            // if we could not find nb-base location (very rare case) just mention all the packs to be installed
            if(!nbBasePresent && nbLocation == null && dependentOnNb.size() > 0) {
                text.append(StringUtils.LF);
                text.append(StringUtils.format(
                        panel.getProperty(NB_ADDONS_LOCATION_TEXT_PROPERTY),
                        StringUtils.asString(dependentOnNb)));
                text.append(StringUtils.LF);                
            }
            // at the end add glassfish components record
            if (dependentOnGf.size() > 0) {
                text.append(StringUtils.LF);
                text.append(StringUtils.format(
                        panel.getProperty(GF_ADDONS_LOCATION_TEXT_PROPERTY),
                        StringUtils.asString(dependentOnGf)));
                text.append(StringUtils.LF);                
            }
            if (dependentOnAs.size() > 0) {
                text.append(StringUtils.LF);
                text.append(StringUtils.format(
                        panel.getProperty(AS_ADDONS_LOCATION_TEXT_PROPERTY),
                        StringUtils.asString(dependentOnAs)));
                text.append(StringUtils.LF);
            }
            locationsPane.setText(text);

            List <Product> toUninstallVisible = new ArrayList<Product>();
            Product nbProduct = null;
            for(Product p : registry.getProductsToUninstall()) {
                String uid = p.getUid();
                if(uid.equals(NB_BASE_UID)) {
                    nbProduct = p;
                } else if(!uid.startsWith("nb-")) {
                    nbProduct = null;
                    break;
                }
            }
            for(Product p : registry.getProductsToUninstall()) {
                if (! Boolean.FALSE.toString().equals(p.getProperty("show-in-wizard"))) {
                    toUninstallVisible.add(p);
                }
            }
            String uninstallLabelText;
            if(nbProduct!=null) {
                String nbName = nbProduct.getDisplayName();
                try {
                    nbName = nbProduct.getLogic().getSystemDisplayName();
                } catch (InitializationException e) {
                }
                uninstallLabelText = StringUtils.format(panel.getProperty(UNINSTALL_LIST_NETBEANS_LABEL_TEXT_PROPERTY), nbName);
            } else {
                uninstallLabelText = StringUtils.format(panel.getProperty(UNINSTALL_LIST_LABEL_TEXT_PROPERTY));
            }
            uninstallListLabel.setText(uninstallLabelText);

            uninstallListPane.setText(
                    StringUtils.asString(toUninstallVisible));
            
            installationSizeLabel.setText(
                    panel.getProperty(INSTALLATION_SIZE_PROPERTY));
            installationSizeValue.setText(StringUtils.formatSize(
                    installationSize));
            
            downloadSizeLabel.setText(
                    panel.getProperty(DOWNLOAD_SIZE_PROPERTY));
            downloadSizeValue.setText(StringUtils.formatSize(
                    downloadSize));
            
            if (registry.getProductsToInstall().isEmpty()) {
                locationsPane.setVisible(false);
                installationSizeLabel.setVisible(false);
                installationSizeValue.setVisible(false);
            } else {
                locationsPane.setVisible(true);
                installationSizeLabel.setVisible(true);
                installationSizeValue.setVisible(true);
            }
            
            if (registry.getProductsToUninstall().isEmpty()) {
                uninstallListLabel.setVisible(false);
                uninstallListPane.setVisible(false);
            } else {
                uninstallListLabel.setVisible(true);
                uninstallListPane.setVisible(true);
            }
            
            downloadSizeLabel.setVisible(false);
            downloadSizeValue.setVisible(false);
            for (RegistryNode remoteNode: registry.getNodes(RegistryType.REMOTE)) {
                if (remoteNode.isVisible()) {
                    downloadSizeLabel.setVisible(true);
                    downloadSizeValue.setVisible(true);
                }
            }

            if(Boolean.getBoolean(REMOVE_NETBEANS_USERDIR_PROPERTY)) {
                removeUserdirCheckbox.doClick();
            }

            if (nbBasePresent) {
                checkForUpdatesCheckbox.setSelected(true);
                System.setProperty(CHECK_FOR_UPDATES_CHECKBOX_PROPERTY, Boolean.TRUE.toString());
                
                checkForUpdatesCheckbox.setText(
                        panel.getProperty(CHECK_FOR_UPDATES_CHECKBOX_PROPERTY));
                checkForUpdatesCheckbox.setBorder(new EmptyBorder(0, 0, 0, 0));
                checkForUpdatesCheckbox.setVisible(true);

                checkForUpdatesPane.setVisible(true);
                checkForUpdatesPane.setContentType("text/html");
                //checkForUpdatesPane.addHyperlinkListener(BrowserUtils.createHyperlinkListener());

                checkForUpdatesPane.setText(
                        StringUtils.format(
                        panel.getProperty(CHECK_FOR_UPDATES_TEXT_PROPERTY)
                        ));
            } else {
                checkForUpdatesCheckbox.setVisible(false);
                checkForUpdatesPane.setVisible(false);
            }

            foldersToRemove.setText(ADDITIONAL_FOLDERS_TO_DELETE);
            foldersToRemove.setVisible(false);

            removeUserdirCheckbox.setVisible(false);
            removeUserdirPane.setVisible(false);
            
            if(Boolean.getBoolean(REMOVE_NETBEANS_INSTALLDIR_PROPERTY)) {
                removeInstalldirCheckbox.doClick();
            }  
            removeInstalldirCheckbox.setVisible(false);
            removeInstalldirPane.setVisible(false);

            for (Product product : Registry.getInstance().getProductsToUninstall()) {                
                if (product.getUid().equals(NB_BASE_UID)) {
                    File installLocation = product.getInstallationLocation();
                    if(component.doRemoveNBInstallationLocation()) {
                        foldersToRemove.setVisible(true);
                        removeInstalldirCheckbox.setText(
                                    StringUtils.format(
                                    panel.getProperty(REMOVE_NETBEANS_INSTALLDIR_CHECKBOX_PROPERTY),
                                    installLocation.getAbsolutePath()));
                        removeInstalldirCheckbox.setBorder(new EmptyBorder(0, 0, 0, 0));
                        removeInstalldirCheckbox.setVisible(true);

                        removeInstalldirPane.setVisible(true);
                        removeInstalldirPane.setContentType("text/html");
                        removeInstalldirPane.setText(
                                panel.getProperty(REMOVE_NETBEANS_INSTALLDIR_TEXT_PROPERTY));
                    }

                    if (component.doRemoveNBUserDir()) {
                        foldersToRemove.setVisible(true);
                        removeUserdirCheckbox.setText(
                                StringUtils.format(
                                panel.getProperty(REMOVE_NETBEANS_USERDIR_CHECKBOX_PROPERTY),
                                component.userDir.getAbsolutePath()));
                        removeUserdirCheckbox.setBorder(new EmptyBorder(0, 0, 0, 0));
                        removeUserdirCheckbox.setVisible(true);

                        removeUserdirPane.setVisible(true);
                        removeUserdirPane.setContentType("text/html");
                        removeUserdirPane.addHyperlinkListener(BrowserUtils.createHyperlinkListener());

                        String name = product.getDisplayName();
                        try {
                            name = product.getLogic().getSystemDisplayName();
                        } catch (InitializationException e) {
                        }
                        removeUserdirPane.setText(
                                StringUtils.format(
                                panel.getProperty(REMOVE_NETBEANS_USERDIR_TEXT_PROPERTY),
                                name, panel.getProperty(REMOVE_NETBEANS_USERDIR_LINK_PROPERTY)));

                    }
                    break;
                }
            }

            //if(productCheckboxList!=null) {
            //    for(Pair <Product, NbiCheckBox> pair : productCheckboxList) {
            //        pair.getSecond().doClick();
            //    }
            //}            
            super.initialize();
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
                    spaceMap.put(downloadDataDirRoot, new Long(downloadSize));
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
                            
                            // add space required for next product installation
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
                
                    for (File root: spaceMap.keySet()) {
                        try {
                            final long availableSpace =
                                    SystemUtils.getFreeSpace(root);
                            final long requiredSpace =
                                    spaceMap.get(root) + REQUIRED_SPACE_ADDITION;
                            
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
        
        private void addProductCheckBox(List<Product> products, List<String> locations) {
            for (final Product product : products) {
                if (product.getStatus() == Status.INSTALLED) {
                  for(String location: locations) {
                    if(new File(location).equals(product.getInstallationLocation())) {
                    final NbiCheckBox checkbox = new NbiCheckBox();
                    final Pair<Product, NbiCheckBox> pair = new Pair(product, checkbox);
                    productCheckboxList.add(pair);
                    checkbox.setText(pair.getFirst().getDisplayName());
                    checkbox.setBorder(new EmptyBorder(0, 0, 0, 0));
                    checkbox.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {                            
                            if (pair.getSecond().isSelected()) {
                                pair.getFirst().setStatus(Status.TO_BE_UNINSTALLED);
                            } else {
                                pair.getFirst().setStatus(Status.INSTALLED);
                            }
                        }
                    });

                    add(checkbox, new GridBagConstraints(
                            0, gridy++, // x, y
                            1, 1, // width, height
                            1.0, 0.0, // weight-x, weight-y
                            GridBagConstraints.PAGE_START, // anchor
                            GridBagConstraints.HORIZONTAL, // fill
                            new Insets(0, 20, 0, 11), // padding
                            0, 0));                           // padx, pady - ???
                    break;
                }
                  }
                }
            }
        }
        private List<String> getRegisteredGlassFishLocations(File nbLocation) throws IOException{
            //temporary solution
            List<String> result = new ArrayList<String>();
            for(String nbattrs : new String[] {"nb/config/GlassFishEE6WC/Instances/.nbattrs", "nb/config/GlassFishEE6/Instances/.nbattrs"}) {
            File f = new File(nbLocation, nbattrs);
            if (f.exists()) {
                try {
                    List<String> list = FileUtils.readStringList(f, "utf-8");
                    for(String s : list) {
                        String prefix = "<attr name=\"installfolder\" stringvalue=\"";
                        if(s.indexOf(prefix)!=-1) {
                            String url = s.substring(s.indexOf(prefix) + prefix.length());
                            url = url.substring(0, url.indexOf("\""));
                            LogManager.log("Adding URL : " + url);
                            result.add(url);
                        }
                    }
                } catch (IOException e) {
                    LogManager.log("Cannot read file " + f, e);
                }
            }
            }
            return result;
        }
        
        private List<String> getRegisteredTomcatLocations(File nbLocation) throws IOException {
            //temporary solution
            File f = new File(nbLocation, "nb/config/J2EE/InstalledServers/.nbattrs");
            List<String> result = new ArrayList<String>();
            if (f.exists()) {
                try {
                    List<String> list = FileUtils.readStringList(f, "utf-8");
                    for (String s : list) {
                        String prefix = "<attr name=\"url\" stringvalue=\"";
                        if (s.indexOf(prefix) != -1) {
                            String path = s.substring(s.indexOf(prefix) + prefix.length());
                            String url = path.substring(0, path.indexOf("\""));
                            String prefix2 = ":home=";
                            if(url.startsWith("tomcat") && url.contains(prefix2)) {
                                url = url.substring(url.indexOf(prefix2) + prefix2.length());
                                url = url.substring(0, url.indexOf(":base="));
                                LogManager.log("Adding URL : " + url);
                                result.add(url);
                            }
                        }
                    }
                } catch (IOException e) {
                    LogManager.log("Cannot read file " + f, e);
                }
            }
            return result;
        }
        private List<String> getRegisteredGlassFishV2Locations(File nbLocation) throws IOException{
            String s = NetBeansUtils.getJvmOption(nbLocation, GLASSFISH_JVM_OPTION_NAME);
            List<String> result = new ArrayList<String>();
            if (s!=null) {
                result.add(s);
            }
            return result;
        }
        private List<String> getRegisteredWebLogicLocations(File nbLocation) throws IOException {
            //temporary solution
            File f = new File(nbLocation, "nb/config/J2EE/InstalledServers/.nbattrs");
            List<String> result = new ArrayList<String>();
            if (f.exists()) {
                try {
                    List<String> list = FileUtils.readStringList(f, "utf-8");
                    for (String s : list) {
                        String prefix = "<attr name=\"serverRoot\" stringvalue=\"";
                        String prefix2 = "wlserver";
                        if (s.indexOf(prefix) != -1 && s.indexOf(prefix2) != -1) {
                            String path = s.substring(s.indexOf(prefix) + prefix.length());
                            String url = path.substring(0, path.lastIndexOf(prefix2));                            
                            LogManager.log("Adding URL : " + url);
                            result.add(url);                            
                        }
                    }
                } catch (IOException e) {
                    LogManager.log("Cannot read file " + f, e);
                }
            }
            return result;
        }        

        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            gridy = 0 ;
            productCheckboxList = new ArrayList <Pair <Product, NbiCheckBox>>();
            // locationsPane ////////////////////////////////////////////////////////
            locationsPane = new NbiTextPane();
            
            // uninstallListPane ////////////////////////////////////////////////////
            uninstallListPane = new NbiTextPane();
            
            // uninstallListLabel ///////////////////////////////////////////////////
            uninstallListLabel = new NbiLabel();
            uninstallListLabel.setLabelFor(uninstallListPane);
            
            // installationSizeValue ////////////////////////////////////////////////
            installationSizeValue = new NbiLabel();
            //installationSizeValue.setFocusable(true);
            
            // installationSizeLabel ////////////////////////////////////////////////
            installationSizeLabel = new NbiLabel();
            installationSizeLabel.setLabelFor(installationSizeValue);
            
            // downloadSizeValue ////////////////////////////////////////////////////
            downloadSizeValue = new NbiLabel();
            //downloadSizeValue.setFocusable(true);
            
            // downloadSizeLabel ////////////////////////////////////////////////////
            downloadSizeLabel = new NbiLabel();
            downloadSizeLabel.setLabelFor(downloadSizeValue);
            
            // spacer ///////////////////////////////////////////////////////////////
            spacer = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(locationsPane, new GridBagConstraints(
                    0, gridy++,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(uninstallListLabel, new GridBagConstraints(
                    0, gridy++,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(uninstallListPane, new GridBagConstraints(
                    0, gridy++,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???            
            int gridy0 = gridy;
            gridy++;
            for (Product product : Registry.getInstance().getProductsToUninstall()) {
                if (product.getUid().equals(NB_BASE_UID) || product.getUid().equals(NB_ALL_UID)) {
                    try {
                        File installLocation = product.getInstallationLocation();

                        List<String> gfLocations = getRegisteredGlassFishV2Locations(installLocation);
                        if (!gfLocations.isEmpty()) {
                            List<Product> glassfishesAppservers = Registry.getInstance().queryProducts(
                                    new OrFilter(
                                    new ProductFilter("glassfish",
                                    SystemUtils.getCurrentPlatform())));
                            addProductCheckBox(glassfishesAppservers, gfLocations);
                        }
                        List<String> gfModLocations = getRegisteredGlassFishLocations(installLocation);
                        if (!gfModLocations.isEmpty()) {
                            List<Product> glassfishservers = Registry.getInstance().queryProducts(
                                    new OrFilter(
                                    new ProductFilter("glassfish-mod",
                                    SystemUtils.getCurrentPlatform())));

                            addProductCheckBox(glassfishservers, gfModLocations);
                        }


                        List<String> tomcatLocations = getRegisteredTomcatLocations(installLocation);
                        if (!tomcatLocations.isEmpty()) {
                            addProductCheckBox(Registry.getInstance().getProducts("tomcat"), tomcatLocations);
                        } 
                        List<String> weblogicLocations = getRegisteredWebLogicLocations(installLocation);
                        if (!weblogicLocations.isEmpty()) {
                            addProductCheckBox(Registry.getInstance().getProducts("weblogic"), weblogicLocations);
                        }                        
                        addProductCheckBox(Registry.getInstance().getProducts("mysql"), null);

                        if (!productCheckboxList.isEmpty()) {
                            String productName;
                            try {
                                productName = product.getLogic().getSystemDisplayName();
                            } catch (InitializationException e) {
                                productName = product.getDisplayName();
                                LogManager.log(e);
                            }
                            NbiLabel runtimesToRemove = new NbiLabel();
                            runtimesToRemove.setText(StringUtils.format(ADDITIONAL_RUNTIMES_TO_DELETE,
                                    productName));
                            add(runtimesToRemove, new GridBagConstraints(
                                    0, gridy0, // x, y
                                    1, 1, // width, height
                                    1.0, 0.0, // weight-x, weight-y
                                    GridBagConstraints.PAGE_START, // anchor
                                    GridBagConstraints.HORIZONTAL, // fill
                                    new Insets(0, 11, 3, 11), // padding
                                    0, 0));                           // padx, pady - ???                            
                        }
                    } catch (IOException e) {
                        LogManager.log(e);
                    }
                }
            }

            foldersToRemove = new NbiLabel();            
            add(foldersToRemove, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 3, 11), // padding
                    0, 0));                           // padx, pady - ???


            removeInstalldirCheckbox = new NbiCheckBox();            
            add(removeInstalldirCheckbox, new GridBagConstraints(
                                    0, gridy++, // x, y
                                    1, 1, // width, height
                                    1.0, 0.0, // weight-x, weight-y
                                    GridBagConstraints.PAGE_START, // anchor
                                    GridBagConstraints.HORIZONTAL, // fill
                                    new Insets(0, 20, 0, 11), // padding
                                    0, 0));                           // padx, pady - ???

            removeInstalldirPane = new NbiTextPane();
            add(removeInstalldirPane, new GridBagConstraints(
                                    0, gridy++, // x, y
                                    1, 1, // width, height
                                    1.0, 0.0, // weight-x, weight-y
                                    GridBagConstraints.PAGE_START, // anchor
                                    GridBagConstraints.HORIZONTAL, // fill
                                    new Insets(0, 20, 0, 11), // padding
                                    0, 0));                           // padx, pady - ???

            removeInstalldirCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {                    
                    System.setProperty(REMOVE_NETBEANS_INSTALLDIR_PROPERTY,
                            "" + removeInstalldirCheckbox.isSelected());
                }
            });

            removeUserdirCheckbox = new NbiCheckBox();
            add(removeUserdirCheckbox, new GridBagConstraints(
                                    0, gridy++, // x, y
                                    1, 1, // width, height
                                    1.0, 0.0, // weight-x, weight-y
                                    GridBagConstraints.PAGE_START, // anchor
                                    GridBagConstraints.HORIZONTAL, // fill
                                    new Insets(11, 20, 0, 11), // padding
                                    0, 0));                           // padx, pady - ???


            removeUserdirPane = new NbiTextPane();
            add(removeUserdirPane, new GridBagConstraints(
                                    0, gridy++, // x, y
                                    1, 1, // width, height
                                    1.0, 0.0, // weight-x, weight-y
                                    GridBagConstraints.PAGE_START, // anchor
                                    GridBagConstraints.HORIZONTAL, // fill
                                    new Insets(0, 20, 0, 11), // padding
                                    0, 0));                           // padx, pady - ???


            removeUserdirCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.setProperty(REMOVE_NETBEANS_USERDIR_PROPERTY, 
                            "" + removeUserdirCheckbox.isSelected());
                }
            });

            checkForUpdatesCheckbox = new NbiCheckBox();
            add(checkForUpdatesCheckbox, new GridBagConstraints(
                                    0, gridy++, // x, y
                                    1, 1, // width, height
                                    1.0, 0.0, // weight-x, weight-y
                                    GridBagConstraints.PAGE_START, // anchor
                                    GridBagConstraints.HORIZONTAL, // fill
                                    new Insets(11, 20, 0, 11), // padding
                                    0, 0));                           // padx, pady - ???


            checkForUpdatesPane = new NbiTextPane();
            add(checkForUpdatesPane, new GridBagConstraints(
                                    0, gridy++, // x, y
                                    1, 1, // width, height
                                    1.0, 0.0, // weight-x, weight-y
                                    GridBagConstraints.PAGE_START, // anchor
                                    GridBagConstraints.HORIZONTAL, // fill
                                    new Insets(0, 20, 0, 11), // padding
                                    0, 0));                           // padx, pady - ???
            
            checkForUpdatesCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.setProperty(CHECK_FOR_UPDATES_CHECKBOX_PROPERTY, 
                            "" + checkForUpdatesCheckbox.isSelected());
                }
            });

            add(installationSizeLabel, new GridBagConstraints(
                    0, gridy++,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(22, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(installationSizeValue, new GridBagConstraints(
                    0, gridy++,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 22, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeLabel, new GridBagConstraints(
                    0, gridy++,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeValue, new GridBagConstraints(
                    0, gridy++,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 22, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, gridy + 10,                            // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
        }
        
        @Override
        public void evaluateNextButtonClick() {
            if (productCheckboxList != null) {
                for (Pair<Product, NbiCheckBox> pair : productCheckboxList) {
                    Product product = pair.getFirst();
                    if (product != null &&
                            product.getStatus() == Status.TO_BE_UNINSTALLED) {
                        product.setStatus(Status.INSTALLED);
                        List<Product> others = Registry.getInstance().getInavoidableDependents(product);
                        for (Product pr : others) {
                            pr.setStatus(Status.TO_BE_UNINSTALLED);
                        }
                        product.setStatus(Status.TO_BE_UNINSTALLED);
                    }
                }
            }
            super.evaluateNextButtonClick();
        }        
    }
    
/////////////////////////////////////////////////////////////////////////////////
// Constants

    public static final String INSTALLATION_FOLDER_PROPERTY =
            "installation.folder"; // NOI18N
    public static final String INSTALLATION_FOLDER_NETBEANS_PROPERTY =
            "installation.folder.netbeans"; // NOI18N
    public static final String UNINSTALL_LIST_LABEL_TEXT_PROPERTY =
            "uninstall.list.label.text"; // NOI18N
    public static final String UNINSTALL_LIST_NETBEANS_LABEL_TEXT_PROPERTY =
            "uninstall.list.netbeans.label.text"; // NOI18N

    public static final String INSTALLATION_SIZE_PROPERTY =
            "installation.size"; // NOI18N
    public static final String DOWNLOAD_SIZE_PROPERTY =
            "download.size"; // NOI18N
    public static final String NB_ADDONS_LOCATION_TEXT_PROPERTY =
            "addons.nb.install.location.text"; // NOI18N
    public static final String GF_ADDONS_LOCATION_TEXT_PROPERTY =
            "addons.gf.install.location.text"; // NOI18N
    public static final String AS_ADDONS_LOCATION_TEXT_PROPERTY =
            "addons.as.install.location.text"; // NOI18N
    public static final String JUNIT_PRESENT_TEXT_PROPERTY =
            "junit.present.text"; // NOI18N
    public static final String JUNIT_ACCEPTED_PROPERTY =
            "junit.accepted"; // NOI18N
    
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
    public static final String REMOVE_NETBEANS_USERDIR_PROPERTY =
            "remove.netbeans.userdir";
    public static final String REMOVE_NETBEANS_INSTALLDIR_PROPERTY =
            "remove.netbeans.installdir";
    public static final String REMOVE_NETBEANS_USERDIR_TEXT_PROPERTY =
            "remove.netbeans.userdir.text";
    public static final String REMOVE_NETBEANS_INSTALLDIR_TEXT_PROPERTY =
            "remove.netbeans.installdir.text";
    public static final String REMOVE_NETBEANS_USERDIR_LINK_PROPERTY =
            "remove.netbeans.userdir.link";
    public static final String REMOVE_NETBEANS_USERDIR_CHECKBOX_PROPERTY =
            "remove.netbeans.userdir.checkbox";
    public static final String REMOVE_NETBEANS_INSTALLDIR_CHECKBOX_PROPERTY =
            "remove.netbeans.installdir.checkbox";
    public static final String CHECK_FOR_UPDATES_PROPERTY =
            "check.for.updates";
    public static final String CHECK_FOR_UPDATES_TEXT_PROPERTY =
            "check.for.updates.text";
    public static final String CHECK_FOR_UPDATES_CHECKBOX_PROPERTY =
            "check.for.updates.checkbox";
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.description"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_UNINSTALL =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.description.uninstall"); // NOI18N
    
    public static final String DEFAULT_INSTALLATION_FOLDER =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.installation.folder"); // NOI18N
    public static final String DEFAULT_INSTALLATION_FOLDERS =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.installation.folders"); // NOI18N
    public static final String DEFAULT_INSTALLATION_FOLDER_NETBEANS =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.installation.folder.netbeans"); // NOI18N
    public static final String DEFAULT_UNINSTALL_LIST_LABEL_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.uninstall.list.label.text"); // NOI18N
    public static final String DEFAULT_UNINSTALL_LIST_NETBEANS_LABEL_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.uninstall.list.netbeans.label.text"); // NOI18N

    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.download.size"); // NOI18N
    public static final String DEFAULT_GF_ADDONS_LOCATION_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.addons.gf.install.location.text"); // NOI18N
    public static final String DEFAULT_AS_ADDONS_LOCATION_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.addons.as.install.location.text"); // NOI18N    
    public static final String DEFAULT_NB_ADDONS_LOCATION_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.addons.nb.install.location.text"); // NOI18N
    public static final String DEFAULT_JUNIT_PRESENT_TEXT_PROPERTY =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.junit.present.text"); // NOI18N
    
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.next.button.text"); // NOI18N
    public static final String DEFAULT_NEXT_BUTTON_TEXT_UNINSTALL =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.next.button.text.uninstall"); // NOI18N
    public static final String ADDITIONAL_RUNTIMES_TO_DELETE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.additional.runtimes.to.delete");//NOI18N
    public static final String ADDITIONAL_FOLDERS_TO_DELETE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.additional.folders.to.delete");//NOI18N
    public static final String DEFAULT_ERROR_NOT_ENOUGH_SPACE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.not.enough.space"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_CHECK_SPACE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.cannot.check.space");// NOI18N
    public static final String DEFAULT_ERROR_LOGIC_ACCESS =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.logic.access");// NOI18N
    public static final String DEFAULT_ERROR_FSROOTS =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.fsroots"); // NOI18N
    public static final String DEFAULT_ERROR_NON_EXISTENT_ROOT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.non.existent.root"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_WRITE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.cannot.write"); // NOI18N
    public static final String DEFAULT_REMOVE_NETBEANS_USERDIR_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.remove.netbeans.userdir.text"); // NOI18N
    public static final String DEFAULT_REMOVE_NETBEANS_INSTALLDIR_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.remove.netbeans.installdir.text"); // NOI18N
    public static final String DEFAULT_REMOVE_NETBEANS_USERDIR_LINK =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.remove.netbeans.userdir.link"); // NOI18N
    public static final String DEFAULT_REMOVE_NETBEANS_USERDIR_CHECKBOX =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.remove.netbeans.userdir.checkbox"); // NOI18N
    public static final String DEFAULT_REMOVE_NETBEANS_INSTALLDIR_CHECKBOX =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.remove.netbeans.installdir.checkbox"); // NOI18N
    public static final String DEFAULT_CHECK_FOR_UPDATES_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.check.for.updates.text"); // NOI18N
    public static final String DEFAULT_CHECK_FOR_UPDATES_CHECKBOX =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.check.for.updates.checkbox"); // NOI18N

    public static final String NB_BASE_UID =
            "nb-base";//NOI18N
    public static final String NB_ALL_UID = 
            "nb-all";//NOI18N
    public static final String NB_JAVASE_UID =
            "nb-javase";//NOI18N
    public static final String JUNIT_UID =
            "junit";//NOI18N
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
    public static final String GLASSFISH_JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N    
}
