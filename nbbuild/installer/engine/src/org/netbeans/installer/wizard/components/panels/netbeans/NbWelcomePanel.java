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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.AndFilter;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaFXUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiFrame;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelUi;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.containers.SwingFrameContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 */
public class NbWelcomePanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Registry bundledRegistry;
    private Registry defaultRegistry;
    
    private boolean registriesFiltered;
    private static BundleType type;
    private List <Product> lastChosenProducts;
    private String lastWarningMessage;
    private static String bundledproduct_name;

    public NbWelcomePanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY,
                DEFAULT_TEXT_PANE_CONTENT_TYPE);
        type = BundleType.getType(
                System.getProperty(WELCOME_PAGE_TYPE_PROPERTY));

        if(type.equals(BundleType.BUNDLEDPRODUCT)) {
            bundledproduct_name = System.getProperty(WELCOME_PAGE_BUNDLEDPRODUCT_NAME_PROPERTY);
        }
        
        setProperty(WELCOME_TEXT_HEADER_PROPERTY,                
                (type.equals(BundleType.JAVA_TOOLS) ?
                       DEFAULT_WELCOME_TEXT_HEADER_JTB :
                       (type.equals(BundleType.MYSQL) ?
                              DEFAULT_WELCOME_TEXT_HEADER_MYSQL :
                           (type.equals(BundleType.BUNDLEDPRODUCT)) ?
                                DEFAULT_WELCOME_TEXT_HEADER_BUNDLEDPRODUCT :
                                   DEFAULT_WELCOME_TEXT_HEADER )));
        
        setProperty(WELCOME_TEXT_DETAILS_PROPERTY,
                ResourceUtils.getString(NbWelcomePanel.class,
                WELCOME_TEXT_HEADER_APPENDING_PROPERTY + "." + type ));
                
        setProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_DIFFERENT_BUILD_INSTALLED_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_DIFFERENT_BUILD_INSTALLED_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE);
        setProperty(WELCOME_TEXT_OPENTAG_PROPERTY,
                DEFAULT_WELCOME_TEXT_OPENTAG);
        setProperty(WELCOME_TEXT_FOOTER_PROPERTY,
                DEFAULT_WELCOME_TEXT_FOOTER);
        setProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY,
                DEFAULT_CUSTOMIZE_BUTTON_TEXT);
        setProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY,
                DEFAULT_INSTALLATION_SIZE_LABEL_TEXT);
        
        setProperty(CUSTOMIZE_TITLE_PROPERTY,
                DEFAULT_CUSTOMIZE_TITLE);
        
        setProperty(MESSAGE_PROPERTY,
                DEFAULT_MESSAGE);
        setProperty(MESSAGE_INSTALL_PROPERTY,
                DEFAULT_MESSAGE_INSTALL);
        setProperty(MESSAGE_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_UNINSTALL);
        setProperty(COMPONENT_DESCRIPTION_TEXT_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_TEXT);
        setProperty(COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE);
        setProperty(SIZES_LABEL_TEXT_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT);
        setProperty(SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD);
        setProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);
        setProperty(OK_BUTTON_TEXT_PROPERTY,
                DEFAULT_OK_BUTTON_TEXT);
        setProperty(CANCEL_BUTTON_TEXT_PROPERTY,
                DEFAULT_CANCEL_BUTTON_TEXT);
        setProperty(DEFAULT_COMPONENT_DESCRIPTION_PROPERTY,
                DEFAULT_DEFAULT_COMPONENT_DESCRIPTION);
        
        setProperty(ERROR_NO_CHANGES_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES);
        setProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY);
        setProperty(ERROR_NO_RUNTIMES_INSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_RUNTIMES_INSTALL_ONLY);
        setProperty(ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY);
        setProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_INSTALL);
        setProperty(ERROR_CONFLICT_INSTALL_PROPERTY,
                DEFAULT_ERROR_CONFLICT_INSTALL);
        setProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_UNINSTALL);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT);
        setProperty(ERROR_EVERYTHING_IS_INSTALLED_PROPERTY,
                DEFAULT_ERROR_EVERYTHING_IS_INSTALLED);
        
        setProperty(WARNING_NO_COMPATIBLE_JDK_FOUND,
                DEFAULT_WARNING_NO_COMPATIBLE_JDK_FOUND);
        setProperty(WARNING_NO_COMPATIBLE_JAVA_FOUND,
                DEFAULT_WARNING_NO_COMPATIBLE_JAVA_FOUND);
        setProperty(WARNING_NO_COMPATIBLE_JDK_FOUND_DEPENDENT, 
                DEFAULT_WARNING_NO_COMPATIBLE_JDK_FOUND_DEPENDENT);
        
        // initialize the registries used on the panel - see the initialize() and
        // canExecute() method
        try {
            defaultRegistry = Registry.getInstance();
            bundledRegistry = new Registry();
            
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);
            if (bundledRegistryUri != null) {
                bundledRegistry.loadProductRegistry(bundledRegistryUri);
            } else {
                bundledRegistry.loadProductRegistry(
                        Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
            }
        } catch (InitializationException e) {
            ErrorManager.notifyError("Cannot load bundled registry", e);
        }
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbWelcomePanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public boolean canExecuteForward() {
        return canExecute();
    }
    
    @Override
    public boolean canExecuteBackward() {
        return canExecute();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        if (registriesFiltered) {
            return;
        }
        
        // we need to apply additional filters to the components tree - filter out
        // the components which are not present in the bundled registry; if the
        // bundled registry contains only one element - registry root, this means
        // that we're running without any bundle, hence not filtering is required;
        // additionally, we should not be suggesting to install tomcat by default,
        // thus we should correct it's initial status        
        Product bundledProductSkip = null;
        Product bundledProductToInstall = null;
        if (bundledRegistry.getNodes().size() > 1) {
            for (Product product: defaultRegistry.getProducts()) {
                if (bundledRegistry.getProduct(
                        product.getUid(),
                        product.getVersion()) == null) {
                    product.setVisible(false);
                    
                    if (product.getStatus() == Status.TO_BE_INSTALLED) {
                        product.setStatus(Status.NOT_INSTALLED);
                    }
                } else if (product.getUid().equals("tomcat") &&
                        (product.getStatus() == Status.TO_BE_INSTALLED)) {
                    boolean stateFileUsed = System.getProperty(
                            Registry.SOURCE_STATE_FILE_PATH_PROPERTY) != null;
                    boolean sysPropInstallLocation = System.getProperty(product.getUid() + 
				StringUtils.DOT + 
				Product.INSTALLATION_LOCATION_PROPERTY) == null;
                    if(!stateFileUsed && sysPropInstallLocation) {
                        product.setStatus(Status.NOT_INSTALLED);
                    }                               
                }  else if(type.equals(BundleType.BUNDLEDPRODUCT) && product.getUid().equals(bundledproduct_name)) { 
                    // current checking product in global registry is "bundledproduct_name", i.e jdk, javafxsdk, weblogic
                    if(product.getStatus() == Status.TO_BE_INSTALLED &&
                        ExecutionMode.getCurrentExecutionMode() == ExecutionMode.NORMAL) {
                        // check if bundledproduct is already installed (i.e from another installer)
                        if(bundledProductAlreadyInstalled(product)) {
                            product.setStatus(Status.NOT_INSTALLED);
                            product.setVisible(false);
                            setProperty(BUNDLEDPRODUCT_INSTALLED_TEXT_PROPERTY,
                                    StringUtils.format(DEFAULT_BUNDLEDPRODUCT_INSTALLED_TEXT, product.getDisplayName()));
                            bundledProductSkip = product;
                        } else {
                            // do not allow installation under non-admin user on windows
                            try {
                                if(doesBundledProductNeedPermissions(product) &&
                                        SystemUtils.isWindows() && !SystemUtils.isCurrentUserAdmin()) {
                                    product.setStatus(Status.NOT_INSTALLED);
                                    product.setVisible(false);
                                    setProperty(BUNDLEDPRODUCT_UNSUFFICIENT_PERMISSIONS_PROPERTY,
                                            StringUtils.format(DEFAULT_BUNDLEDPRODUCT_UNSUFFICIENT_PERMISSIONS_TEXT,product.getDisplayName()));
                                    bundledProductSkip = product;
                                }
                            } catch (NativeException e){
                                LogManager.log(e);
                            }
                        }
                    } else if(product.getStatus() == Status.INSTALLED && //have been installed by NBI installer
                            bundledRegistry.getProduct(
                            product.getUid(),product.getVersion())!=null){ //and it is the same product
                        setProperty(BUNDLEDPRODUCT_INSTALLED_TEXT_PROPERTY,
                                StringUtils.format(DEFAULT_BUNDLEDPRODUCT_INSTALLED_TEXT,
                                product.getDisplayName()));
                        bundledProductSkip = product;
                    } else if(product.getStatus() == Status.NOT_INSTALLED && //product filtered out by platform (for win 86/64)
                            bundledRegistry.getProduct(
                            product.getUid(),product.getVersion())!=null &&
                            !SystemUtils.getCurrentPlatform().isCompatibleWith(product.getPlatforms().get(0))) {
                        setProperty(BUNDLEDPRODUCT_NOT_COMPATIBLE_TEXT_PROPERTY,
                                StringUtils.format(DEFAULT_BUNDLEDPRODUCT_NOT_COMPATIBLE_TEXT_PROPERTY,
                                product.getDisplayName()));
                        bundledProductSkip = product;                                                    
                    }
                    bundledProductToInstall = (bundledProductSkip == null)? product : null;
                } else if (product.getUid().equals("javafxsdk")) {
                     if(product.getStatus() == Status.TO_BE_INSTALLED &&
                        ExecutionMode.getCurrentExecutionMode() == ExecutionMode.NORMAL &&
                        bundledProductAlreadyInstalled(product)) { // check if javafxsdk is already installed (i.e from another installer)
                            LogManager.log("... Changing javafxsdk status to NOT_INSTALLED");
                            product.setStatus(Status.NOT_INSTALLED);
                            product.setVisible(false);
                    }
                }
            }
        }
        LogManager.log("... bundledProductSkip = " + bundledProductSkip);
        LogManager.log("... bundledProductToInstall = " + bundledProductToInstall);

        if (bundledProductSkip != null) { //do not install bundledproduct
            final Product javafxsdk = getJavaFXSDKProduct();
            if(type.isJDKBundle() && javafxsdk != null) { //do not install javafxsdk as well
                // dont't install JavaFX if JDK is already installed or there is no permissions for its installation
                javafxsdk.setStatus(bundledProductSkip.getStatus());
                javafxsdk.setVisible(bundledProductSkip.isVisible());
                LogManager.log("... Skipping JavaFX installation");
            }

            if (defaultRegistry.getProductsToInstall().isEmpty()) {
                if (getProperty(BUNDLEDPRODUCT_INSTALLED_TEXT_PROPERTY) != null) {
                    setProperty(BUNDLEDPRODUCT_EVERYTHING_INSTALLED_TEXT_PROPERTY,
                            StringUtils.format(DEFAULT_BUNDLEDPRODUCT_EVERYTHING_INSTALLED_TEXT,
                            bundledProductSkip.getDisplayName()));
                } else if (getProperty(BUNDLEDPRODUCT_UNSUFFICIENT_PERMISSIONS_PROPERTY) != null) {
                    setProperty(BUNDLEDPRODUCT_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY,
                            StringUtils.format(DEFAULT_BUNDLEDPRODUCT_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT,
                            bundledProductSkip.getDisplayName()));
                } else if (getProperty(BUNDLEDPRODUCT_NOT_COMPATIBLE_TEXT_PROPERTY) != null) {
                        setProperty(BUNDLEDPRODUCT_NOT_COMPATIBLE_NETBEANS_INSTALLED_TEXT_PROPERTY,
                        StringUtils.format(DEFAULT_BUNDLEDPRODUCT_NOT_COMPATIBLE_NETBEANS_INSTALLED_TEXT_PROPERTY,
                            bundledProductSkip.getDisplayName()));
                }
            }
        }
            
        final List <Product> toInstall = defaultRegistry.getProductsToInstall();

        if(bundledProductToInstall != null) { // bundled product will be installed            
            boolean nbToInstall = false;
            for(Product productToInstall : toInstall) {
                if(productToInstall.getUid().equals("nb-base") || productToInstall.getUid().equals("nb-all")) {
                    nbToInstall = true;
                    break;
                }
            }
            if(!nbToInstall) {//install only bundledproduct, NetBeans is already installed
                LogManager.log("... NetBeans is already installed, install only bundled product");
                 setProperty(BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT_PROPERTY,
                        StringUtils.format(DEFAULT_BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT,
                        bundledProductToInstall.getDisplayName()));
            }
        }
        /*if(toInstall.size()==1 && toInstall.get(0).getUid().equals(bundledproduct_name)) { // install only bundledproduct
                setProperty(BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT_PROPERTY,
                        StringUtils.format(DEFAULT_BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT,
                        toInstall.get(0).getDisplayName()));
        }*/
        registriesFiltered = true;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        return bundledRegistry.getNodes().size() > 1;
    }
    
    // package access
    String getWarningMessage() {
        List<Product> list = Registry.getInstance().getProductsToInstall();
        if (lastChosenProducts != null) {            
            if (list.containsAll(lastChosenProducts) && 
                    list.size() == lastChosenProducts.size()) {
                return lastWarningMessage;
            }
        }
        lastChosenProducts = list;
        lastWarningMessage = null;
        
        List <Product> jdkDependentProducts = new ArrayList<Product> ();
        for (Product product : lastChosenProducts) {
            final List<Dependency> dependencies = product.getDependencyByUid("nb-all");
            if (dependencies.size() > 0 && product.getUid().startsWith("nb-")) {
                final List<Product> sources =
                        Registry.getInstance().getProducts(dependencies.get(0));
                if (sources.size() > 0) {
                    final Product nbProduct = sources.get(0);
                    if(nbProduct.getStatus() != Status.INSTALLED) {
                        continue;
                    }
                    List<Product> dependents = Registry.getInstance().getInavoidableDependents(nbProduct);
                    boolean requiresJDK = false;
                    for (Product pr : dependents) {
                        if (pr.getStatus().equals(Status.INSTALLED) && 
                                "false".equals(pr.getProperty(JdkLocationPanel.JRE_ALLOWED_PROPERTY))) {
                            // e.g. base and javase is installed and we install uml
                            requiresJDK = true;
                        }
                    }
                    requiresJDK = requiresJDK || "false".equals(product.getProperty(JdkLocationPanel.JRE_ALLOWED_PROPERTY));
                    if (requiresJDK) {
                        final File nbLocation = nbProduct.getInstallationLocation();
                        try {
                            final File javaHome = new File(NetBeansUtils.getJavaHome(nbLocation));
                            if (JavaUtils.isJavaHome(javaHome) && !JavaUtils.isJdk(javaHome)) {
                                jdkDependentProducts.add(product);
                                continue;
                            }
                        } catch (IOException e) {
                            LogManager.log(ErrorLevel.DEBUG, e);
                        }
                    }
                }
            }
        }
        if (jdkDependentProducts.size() > 0) {
            lastWarningMessage = StringUtils.format(
                    getProperty(WARNING_NO_COMPATIBLE_JDK_FOUND_DEPENDENT), 
                    StringUtils.asString(jdkDependentProducts));
            LogManager.log(lastWarningMessage);
            return lastWarningMessage;
        }
        
        boolean hasNestedJre = false;
        for (Product product : lastChosenProducts) {
            if (product.getUid().equals("jre-nested")) {
                hasNestedJre = true;
                break;
            }
        }
        
        for (Product product : lastChosenProducts) {
            for (WizardComponent c : product.getWizardComponents()) {
                if (c.getClass().getName().equals(SearchForJavaAction.class.getName())) {
                    for (WizardComponent wc : product.getWizardComponents()) {
                        try {
                            Method m = wc.getClass().getMethod("getJdkLocationPanel");
                            Wizard wizard = new Wizard(null, product.getWizardComponents(), -1, product, product.getClassLoader());
                            wc.setWizard(wizard);
                            wc.getWizard().getContext().put(product);
                            wc.initialize();
                            JdkLocationPanel jdkLocationPanel = (JdkLocationPanel) m.invoke(wc);
                            if (jdkLocationPanel.getSelectedLocation().equals(new File(StringUtils.EMPTY_STRING)) && !hasNestedJre) {
                                final String jreAllowed = jdkLocationPanel.getProperty(
                                        JdkLocationPanel.JRE_ALLOWED_PROPERTY);
                                lastWarningMessage = StringUtils.format(
                                        getProperty("false".equals(jreAllowed) ? 
                                            WARNING_NO_COMPATIBLE_JDK_FOUND : 
                                            WARNING_NO_COMPATIBLE_JAVA_FOUND));

                                LogManager.log(lastWarningMessage);
                                return lastWarningMessage;
                            }
                        } catch (NoSuchMethodException e) {
                        } catch (IllegalAccessException e) {
                        } catch (IllegalArgumentException e) {
                        } catch (InvocationTargetException e) {
                        }
                    }
                }
            }
        }
        return null;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbWelcomePanelUi extends ErrorMessagePanelUi {
        protected NbWelcomePanel component;
        
        public NbWelcomePanelUi(NbWelcomePanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbWelcomePanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbWelcomePanelSwingUi extends ErrorMessagePanelSwingUi {
        protected NbWelcomePanel panel;
        
        private NbiTextPane textPane;
        private NbiTextPane detailsTextPane;
        private NbiLabel detailsWarningIconLabel;
        private NbiTextPane textScrollPane;
        private NbiScrollPane scrollPane;
        private NbiButton customizeButton;
        private NbiLabel installationSizeLabel;
        
        private NbCustomizeSelectionDialog customizeDialog;
        private NbiPanel leftImagePanel;
        
        private List<RegistryNode> registryNodes;
        
        private boolean everythingIsInstalled;
        private boolean netBeansIsInstalled;
        
        ValidatingThread validatingThread;
        
        public NbWelcomePanelSwingUi(
                final NbWelcomePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.panel = component;
            
            registryNodes = new LinkedList<RegistryNode>();
            populateList(
                    registryNodes,
                    Registry.getInstance().getRegistryRoot());
            
            initComponents();
        }
        
        @Override
        public String getTitle() {
            return null; // the welcome page does not have a title
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            
            container.getBackButton().setVisible(false);
        }
      
        @Override
        protected void initialize() {              
            StringBuilder welcomeText = new StringBuilder();
            String header = StringUtils.format(panel.getProperty(WELCOME_TEXT_HEADER_PROPERTY));            
            
            if(type.equals(BundleType.BUNDLEDPRODUCT)) {
                for(Product product : panel.getBundledRegistry().getProducts(bundledproduct_name)) {
                    header = StringUtils.format(header, product.getDisplayName());
                    panel.setProperty(WELCOME_TEXT_DETAILS_PROPERTY,
                            StringUtils.format(panel.getProperty(WELCOME_TEXT_DETAILS_PROPERTY),
                            product.getDisplayName()));
                    break;
                }
            }            

            welcomeText.append(header);
            welcomeText.append(panel.getProperty(WELCOME_TEXT_FOOTER_PROPERTY));
            textPane.setContentType(
                    panel.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            textPane.setText(welcomeText);
            StringBuilder detailsText = new StringBuilder(
                    panel.getProperty(WELCOME_TEXT_OPENTAG_PROPERTY));
            boolean warningIcon = false;
            boolean installFXSDKMessage = false;
            if(panel.getProperty(BUNDLEDPRODUCT_EVERYTHING_INSTALLED_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(BUNDLEDPRODUCT_EVERYTHING_INSTALLED_TEXT_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(BUNDLEDPRODUCT_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(BUNDLEDPRODUCT_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(BUNDLEDPRODUCT_NOT_COMPATIBLE_NETBEANS_INSTALLED_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(BUNDLEDPRODUCT_NOT_COMPATIBLE_NETBEANS_INSTALLED_TEXT_PROPERTY));
                warningIcon = true;
            }  else if(panel.getProperty(BUNDLEDPRODUCT_INSTALLED_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(BUNDLEDPRODUCT_INSTALLED_TEXT_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(BUNDLEDPRODUCT_UNSUFFICIENT_PERMISSIONS_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(BUNDLEDPRODUCT_UNSUFFICIENT_PERMISSIONS_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(BUNDLEDPRODUCT_NOT_COMPATIBLE_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(BUNDLEDPRODUCT_NOT_COMPATIBLE_TEXT_PROPERTY));
                warningIcon = true;           
            } else if(panel.getProperty(BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT_PROPERTY));
                warningIcon = true;
                installFXSDKMessage = true;
            } else {
                detailsText.append(panel.getProperty(WELCOME_TEXT_DETAILS_PROPERTY));                
                installFXSDKMessage = true;                
            }

            //For JDK and JavaFX SDK bundle
            final Product javafxsdk = panel.getJavaFXSDKProduct();
            if(type.isJDKBundle() && javafxsdk != null && installFXSDKMessage) {
                if(javafxsdk.getStatus().equals(Status.TO_BE_INSTALLED)) { //install jdk && javafxsdk
                    LogManager.log("... JavaFX SDK will be installed");
                    detailsText.append(StringUtils.format(DEFAULT_ADDITIONAL_PRODUCT_TO_BE_INSTALLED_TEXT_PROPERTY,
                            javafxsdk.getDisplayName()));
                } else { //install jdk but do not install javafxsdk
                    LogManager.log("... JavaFX SDK is already installed; status is " + javafxsdk.getStatus());
                    detailsText.append(StringUtils.format(DEFAULT_ADDITIONAL_PRODUCT_ALREADY_INSTALLED_TEXT_PROPERTY,
                            javafxsdk.getDisplayName()));
                }
            }
            /////////////////////////////////
            detailsTextPane.setContentType(
                    panel.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            
            detailsTextPane.setText(detailsText.toString());
            if(warningIcon) {
                detailsWarningIconLabel.setIcon(new ImageIcon(
                        getClass().getClassLoader().getResource(WARNING_ICON)));
            } else {
                detailsWarningIconLabel.setVisible(false);                
            }
            

            everythingIsInstalled = true;
            netBeansIsInstalled = true;
            welcomeText = new StringBuilder(
                    panel.getProperty(WELCOME_TEXT_OPENTAG_PROPERTY));
            for (RegistryNode node: registryNodes) {
                if (node instanceof Product) {
                    final Product product = (Product) node;
                    final String productUid = product.getUid();
                    if (product.getUid().equals("glassfish")
                            || product.getUid().equals("glassfish-mod")
                            || product.getUid().equals("glassfish-mod-sun")
                            || product.getUid().equals("tomcat")
                            || product.getUid().equals("mysql")) {

                        List<Product> otherRuntimes = Registry.getInstance().queryProducts(
                                new ProductFilter(
                                product.getUid(),
                                null, //product.getVersion(),
                                product.getPlatforms()));

                        for (Product other : otherRuntimes) {
                            if (other.getStatus().equals(Status.INSTALLED)) {
                                if (product.getVersion().getMajor() == other.getVersion().getMajor()
                                        && product.getVersion().getMicro() == other.getVersion().getMicro()
                                        && product.getVersion().getMinor() == other.getVersion().getMinor()
                                        && ((product.getVersion().getUpdate() != other.getVersion().getUpdate())
                                        || (product.getVersion().getBuild() != other.getVersion().getBuild()))) {
                                    product.setStatus(Status.INSTALLED_DIFFERENT_BUILD);
                                }
                            }
                        }
                    }
                    if (product.getStatus() == Status.INSTALLED) {
                        if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.JAVA)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                    } else if (product.getStatus() == Status.INSTALLED_DIFFERENT_BUILD) {
                        if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.JAVA)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_PRODUCT_DIFFERENT_BUILD_INSTALLED_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                    } else if (product.getStatus() == Status.TO_BE_INSTALLED) {
                        if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.JAVA)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                        if(productUid.startsWith("nb-")) {
                            netBeansIsInstalled = false;
                        }
                        everythingIsInstalled = false;
                    } else if ((product.getStatus() == Status.NOT_INSTALLED)) {
                        if(productUid.startsWith("nb-")) {
                            netBeansIsInstalled = false;
                        }
                        everythingIsInstalled = false;
                    } else {
                        continue;
                    }
                } else if (node instanceof Group) {
                    final RegistryFilter filter = new AndFilter(
                            new ProductFilter(true),
                            new OrFilter(
                            new ProductFilter(Status.TO_BE_INSTALLED),
                            new ProductFilter(Status.INSTALLED)));
                    
                    if (node.hasChildren(filter)) {
                        if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.JAVA)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                    }
                }
            }                      
            
            welcomeText.append(panel.getProperty(WELCOME_TEXT_FOOTER_PROPERTY));
            
            textScrollPane.setContentType(
                    panel.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            textScrollPane.setText(welcomeText);
            textScrollPane.setCaretPosition(0);              
            customizeButton.setText(
                    panel.getProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY));                                                           
            
            scrollPane.getViewport().setMinimumSize(
                    textScrollPane.getPreferredScrollableViewportSize());
            updateSizes();                
            super.initialize();             
        }
        
        private void updateSizes() {
            long installationSize = 0;
            for (Product product: Registry.getInstance().getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
            }
            
            if (installationSize == 0) {
                installationSizeLabel.setText(StringUtils.EMPTY_STRING);
            } else {
                installationSizeLabel.setText(StringUtils.format(
                        panel.getProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY),
                        StringUtils.formatSize(installationSize)));
            }
        }

        @Override
        protected String getWarningMessage() { 
            String message = super.getWarningMessage();
            return (message!=null) ? message: panel.getWarningMessage();
        }
        
        @Override
        protected String validateInput() {
            if (everythingIsInstalled) {
                customizeButton.setEnabled(false);
                installationSizeLabel.setVisible(false);
                
                return panel.getProperty(ERROR_EVERYTHING_IS_INSTALLED_PROPERTY);
            } else {
                customizeButton.setEnabled(true);
                installationSizeLabel.setVisible(true);
            }
            
            final List<Product> products =
                    Registry.getInstance().getProductsToInstall();

            if (products.isEmpty()) {
                    // if  (!everythingIsInstalled) && (netBeansIsInstalled)
                    // => there are runtimes to install => show ERROR_NO_RUNTIMES_INSTALL_ONLY_PROPERTY
                return netBeansIsInstalled ?
                     panel.getProperty(ERROR_NO_RUNTIMES_INSTALL_ONLY_PROPERTY):
                     panel.getProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY);
            }
            
            String template = panel.getProperty(
                    ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY);
            for (Product product: products) {
                if (product.getRegistryType() == RegistryType.REMOTE) {
                    template = panel.getProperty(
                            ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY);
                    break;
                }
            }
            
            try {                
                if(!Boolean.getBoolean(SystemUtils.NO_SPACE_CHECK_PROPERTY)) {                     
                    final long availableSize = SystemUtils.getFreeSpace(
                            Installer.getInstance().getLocalDirectory());
                    
                    long requiredSize = 0;
                    for (Product product: products) {
                        requiredSize += product.getDownloadSize();
                    }
                    requiredSize += REQUIRED_SPACE_ADDITION;
                    
                    if (availableSize < requiredSize) {
                        return StringUtils.format(
                                template,
                                Installer.getInstance().getLocalDirectory(),
                                StringUtils.formatSize(requiredSize - availableSize));
                    }
                }
            } catch (NativeException e) {
                ErrorManager.notifyError(
                        "Cannot check the free disk space",
                        e);
            }
            
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // textPane /////////////////////////////////////////////////////////////
            textPane = new NbiTextPane();
            
            // textScrollPane /////////////////////////////////////////////////////////////
            textScrollPane = new NbiTextPane();
            textScrollPane.setOpaque(true);
            textScrollPane.setBackground(Color.WHITE);
            
            detailsTextPane = new NbiTextPane();
            detailsTextPane.setOpaque(true);
            detailsTextPane.setBackground(Color.WHITE);
            
            // scrollPane ////////////////////////////////////////////////////
            scrollPane = new NbiScrollPane(textScrollPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setViewportBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            scrollPane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            
            // customizeButton //////////////////////////////////////////////////////
            customizeButton = new NbiButton();
            customizeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    customizeButtonPressed();
                }
            });
            
            // installationSizeLabel ////////////////////////////////////////////////
            installationSizeLabel = new NbiLabel();
            
            
            leftImagePanel = new NbiPanel();
            int width = 0;
            int height = 0;
            final String topLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY));
            final String bottomLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY));
            final String backgroundImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_BACKGROUND_IMAGE_PROPERTY));

            /* For Sun's JDK branding
            int bottomAnchor = NbiPanel.ANCHOR_BOTTOM_LEFT;
            if(type.isJDKBundle() || type.equals(BundleType.JAVA_TOOLS)) {
                bottomAnchor = NbiPanel.ANCHOR_FULL;
            }*/

            if(backgroundImage!=null) {
                leftImagePanel.setBackgroundImage(backgroundImage, NbiPanel.ANCHOR_FULL);               
            }
            if(topLeftImage!=null) {
                leftImagePanel.setBackgroundImage(topLeftImage,NbiPanel.ANCHOR_TOP_LEFT);
                width   = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconWidth();
                height += leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconHeight();
            }
            if(bottomLeftImage!=null) {
                leftImagePanel.setBackgroundImage(bottomLeftImage, NbiPanel.ANCHOR_BOTTOM_LEFT);
                width   = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_BOTTOM_LEFT).getIconWidth();
                height += leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_BOTTOM_LEFT).getIconHeight();
            }
            if(backgroundImage != null) {
                width  = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_FULL).getIconWidth();
                height = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_FULL).getIconHeight();
            }
             
            leftImagePanel.setPreferredSize(new Dimension(width,height));
            leftImagePanel.setMaximumSize(new Dimension(width,height));
            leftImagePanel.setMinimumSize(new Dimension(width,0));
            leftImagePanel.setSize(new Dimension(width,height));
            
            leftImagePanel.setOpaque(false);
            // this /////////////////////////////////////////////////////////////////
            int dy = 0;
            add(leftImagePanel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 100,                           // width, height
                    0.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.WEST,     // anchor
                    GridBagConstraints.VERTICAL,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            add(textPane, new GridBagConstraints(
                    1, dy++,                             // x, y
                    4, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,        // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(10, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            detailsWarningIconLabel = new NbiLabel();
            add(detailsWarningIconLabel, new GridBagConstraints(
                    1, dy,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.NORTHWEST,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(2, 11, 0, 0),        // padding
                    0, 0));                           // padx, pady - ???
            add(detailsTextPane, new GridBagConstraints(
                    2, dy++,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.WEST,        // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(2, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            NbiTextPane separatorPane =  new NbiTextPane();
            BundleType type = BundleType.getType(
                    System.getProperty(WELCOME_PAGE_TYPE_PROPERTY));
            if(!type.equals(BundleType.JAVAEE) && !type.equals(BundleType.RUBY) &&
                    !type.equals(BundleType.JAVA_TOOLS) && !type.equals(BundleType.MYSQL)) {
                add(scrollPane, new GridBagConstraints(
                        1, dy++,                           // x, y
                        4, 1,                              // width, height
                        1.0, 10.0,                         // weight-x, weight-y
                        GridBagConstraints.LINE_START,     // anchor
                        GridBagConstraints.BOTH,           // fill
                        new Insets(0, 11, 0, 11),            // padding
                        0, 0));                            // padx, pady - ???
            }else {
                for (RegistryNode node: registryNodes) {
                    if (node instanceof Product) {
                        final Product product = (Product) node;
                        if(product.getUid().equals("glassfish") ||
                                product.getUid().equals("glassfish-mod") ||
                                product.getUid().equals("glassfish-mod-sun") ||
                                product.getUid().equals("tomcat") ||                             
				product.getUid().equals("mysql")) {
                            final NbiCheckBox chBox;
                            if (product.getStatus() == Status.INSTALLED) {
                                chBox = new NbiCheckBox();
                                chBox.setText( "<html>" +
                                        StringUtils.format(
                                        panel.getProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY),
                                        node.getDisplayName()));
                                chBox.setSelected(true);
                                chBox.setEnabled(false);
                            } else if (product.getStatus() == Status.INSTALLED_DIFFERENT_BUILD) {
                                chBox = new NbiCheckBox();
                                chBox.setText("<html>" +
                                        StringUtils.format(
                                        panel.getProperty(WELCOME_TEXT_PRODUCT_DIFFERENT_BUILD_INSTALLED_TEMPLATE_PROPERTY),
                                        node.getDisplayName()));
                                chBox.setSelected(false);
                                chBox.setEnabled(false);
                            } else if (product.getStatus() == Status.TO_BE_INSTALLED) {
                                chBox = new NbiCheckBox();
                                chBox.setText("<html>" +
                                        StringUtils.format(
                                        panel.getProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY),
                                        node.getDisplayName()));
                                chBox.setSelected(true);
                                chBox.setEnabled(true);
                            } else if (product.getStatus() == Status.NOT_INSTALLED) {
                                chBox = new NbiCheckBox();
                                chBox.setText("<html>" +
                                        StringUtils.format(
                                        panel.getProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY),
                                        node.getDisplayName()));
                                chBox.setSelected(false);
                                chBox.setEnabled(true);
                            } else {
                                chBox = null;
                            }
                            if(chBox != null) {
                                chBox.setOpaque(false);
                                
                                //chBox.setPreferredSize(new Dimension(chBox.getPreferredSize().width,
                                //        chBox.getPreferredSize().height-2));
                                chBox.setBorder(new EmptyBorder(0,0,0,0));
                                add(chBox,new GridBagConstraints(
                                        1, dy++,                             // x, y
                                        4, 1,                             // width, height
                                        1.0, 0.0,                         // weight-x, weight-y
                                        GridBagConstraints.LINE_START,        // anchor
                                        GridBagConstraints.HORIZONTAL,          // fill
                                        new Insets(0, 11, 0, 0),        // padding
                                        0, 0));
                                chBox.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        if(chBox.isSelected()) {
                                            product.setStatus(Status.TO_BE_INSTALLED);
                                        } else {
                                            product.setStatus(Status.NOT_INSTALLED);
                                        }
                                        updateErrorMessage();
                                        updateSizes();
                                    }
                                });
                                
                            }
                        }
                    }
                }
                add(separatorPane , new GridBagConstraints(
                        1, dy++,                             // x, y
                        4, 1,                                // width, height
                        1.0, 2.0,                            // weight-x, weight-y
                        GridBagConstraints.LINE_START,       // anchor
                        GridBagConstraints.BOTH,             // fill
                        new Insets(0, 0, 0, 0),              // padding
                        0, 0));                              // padx, pady - ???
            }
            add(customizeButton, new GridBagConstraints(
                    1, dy,                            // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(10, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            separatorPane =  new NbiTextPane();
            add(separatorPane , new GridBagConstraints(
                    3, dy,                            // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            add(installationSizeLabel, new GridBagConstraints(
                    4, dy,                            // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.EAST,          // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(10, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            
            // move error label after the left welcome image
            Component errorLabel = getComponent(0);
            getLayout().removeLayoutComponent(errorLabel);
            add(errorLabel, new GridBagConstraints(
                    1, 99,                             // x, y
                    99, 1,                             // width, height
                    1.0, 0.0,                          // weight-x, weight-y
                    GridBagConstraints.CENTER,         // anchor
                    GridBagConstraints.HORIZONTAL,     // fill
                    new Insets(4, 11, 4, 0),          // padding
                    0, 0));                            // ??? (padx, pady)
            
            // platform-specific tweak //////////////////////////////////////////////
            if (SystemUtils.isMacOS()) {
                customizeButton.setOpaque(false);
            }
            
            if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.JAVA) ||
                    type.equals(BundleType.JAVA_TOOLS)) {
                customizeButton.setVisible(true);
            } else {
                customizeButton.setVisible(false);
            }  
        }
        
        private void customizeButtonPressed() {
            if (customizeDialog == null) {
                final Runnable callback = new Runnable() {
                    public void run() {
                        initialize();
                    }
                };
                NbiFrame owner = null;
                if(container instanceof SwingFrameContainer) {
                    owner = (SwingFrameContainer) container;
                }
                customizeDialog = new NbCustomizeSelectionDialog(
                        owner,
                        panel,
                        callback,
                        registryNodes);
            }

            customizeDialog.setVisible(true);
            customizeDialog.requestFocus();
        }
        
        private void populateList(
                final List<RegistryNode> list,
                final RegistryNode parent) {
            final List<RegistryNode> groups = new LinkedList<RegistryNode>();
            for (RegistryNode node: parent.getChildren()) {
                if (!node.isVisible() || Boolean.FALSE.toString().equals(node.getProperty("show-in-wizard"))) {
                    continue;
                }
                
                if (node instanceof Product) {
                    if (!SystemUtils.getCurrentPlatform().isCompatibleWith(
                            ((Product) node).getPlatforms())) {
                        continue;
                    }
                    list.add(node);
                }
                
                if (node instanceof Group) {
                    if (node.hasChildren(new ProductFilter(true))) {
                        groups.add(node);
                    }
                }
            }
            
            for (RegistryNode node: groups) {
                list.add(node);
                if(!node.getUid().equals("nb-ide-group")) {
                    populateList(list, node);
                }
            }
        }
    }
    
    public Registry getBundledRegistry() {
        return bundledRegistry;
    }    
    
    
    public enum BundleType {
        JAVASE("javase"),
        JAVAEE("javaee"),
        JAVA("java"),
        JAVAME("javame"),
        RUBY("ruby"),
        HTML("html"),
        PHP("php"),
        CND("cnd"),
        JAVAFX("javafx"),
        CUSTOMIZE("customize"),        
        JAVA_TOOLS("java.tools"),
	MYSQL("mysql"),
        BUNDLEDPRODUCT("bundledproduct"),
        NBALL("nb-all");
        
        private String name;
        private BundleType(String s) {
            this.name = s;
        }
        public static BundleType getType(String s) {
            if(s!=null) {
                for(BundleType type : BundleType.values())
                    if(type.toString().equals(s)) {
                    return type;
                    }
            }
            return CUSTOMIZE;
        }
        @Override
        public String toString() {
            return name;
        }
        public boolean isJDKBundle() {
            return (name.equals("bundledproduct") && bundledproduct_name.contains("jdk"))
                    || name.endsWith(".jdk");
        }

        public boolean isWebLogicBundle() {
            return (name.equals("bundledproduct") && bundledproduct_name.contains("weblogic"));
        }
        public String getNetBeansBundleId() {
            if(isJDKBundle()) {
                return "NBJDK";
            } else if(this.equals(JAVA_TOOLS)) {
                return "NBEETOOLS";
            } else if(this.equals(MYSQL)) {
                return "NBMYSQL";
            } else if(isWebLogicBundle()) {
                return "NBWEBLOGIC";                
            } else if(this.equals(JAVAFX)) {
                return "NB";
            } else {
                return "NB";
            }
        }
    }

    private boolean bundledProductAlreadyInstalled(Product product) {
        if(product.getUid().equals("jdk")) {
            return JavaUtils.findJDKHome(product.getVersion())!= null;
        } else if(product.getUid().equals("javafxsdk")) {
            return JavaFXUtils.getJavaFXSDKInstallationPath(product.getPlatforms().get(0)) != null;
        } else if(product.getUid().equals("weblogic")) {
            return false; // research if it is possible to know if WL is already installed by standalone installer
        }        
        return false;
    }

    private boolean doesBundledProductNeedPermissions(Product product) {
         if(product.getUid().equals("weblogic")) {
            return true;
         } else if(product.getUid().equals("jdk")) {
            return true;  
         } 
         return false;
    }

    private Product getJavaFXSDKProduct() {
        final List<Product> products = defaultRegistry.getProducts("javafxsdk");
        Product javafxsdk = null;
        for(Product product : products) {
            if(bundledRegistry.getProduct(product.getUid(),product.getVersion()) != null) {
                javafxsdk = product;
		break;
            }
        }
        if(javafxsdk != null) {
            LogManager.log("... javafxsdk product is found: " + javafxsdk.getDisplayName() +
                    "  with the status: " + javafxsdk.getStatus());
        }
        return javafxsdk;
    }


    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.description"); // NOI18N
    
    public static final String TEXT_PANE_CONTENT_TYPE_PROPERTY =
            "text.pane.content.type"; // NOI18N
    public static final String WELCOME_TEXT_HEADER_PROPERTY =
            "welcome.text.header"; // NOI18N
    public static final String WELCOME_TEXT_DETAILS_PROPERTY =
            "welcome.text.details"; // NOI18N
    public static final String WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY =
            "welcome.text.group.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY =
            "welcome.text.product.installed.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_DIFFERENT_BUILD_INSTALLED_TEMPLATE_PROPERTY =
            "welcome.text.product.different.build.installed.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY =
            "welcome.text.product.not.installed.template"; // NOI18N
    public static final String WELCOME_TEXT_OPENTAG_PROPERTY =
            "welcome.text.opentag"; // NOI18N
    public static final String WELCOME_TEXT_FOOTER_PROPERTY =
            "welcome.text.footer"; // NOI18N
    public static final String CUSTOMIZE_BUTTON_TEXT_PROPERTY =
            "customize.button.text"; // NOI18N
    public static final String INSTALLATION_SIZE_LABEL_TEXT_PROPERTY =
            "installation.size.label.text"; // NOI18N    

    public static final String BUNDLEDPRODUCT_INSTALLED_TEXT_PROPERTY =
            "bundledproduct.already.installed.text";//NOI18N
    public static final String BUNDLEDPRODUCT_EVERYTHING_INSTALLED_TEXT_PROPERTY =
            "bundledproduct.everything.installed.text";//NOI18N
    public static final String BUNDLEDPRODUCT_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY =
            "bundledproduct.everything.installed.unsufficient.permissions.text";//NOI18N
    public static final String BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT_PROPERTY =
            "bundledproduct.netbeans.installed.text";//NOI18N
    public static final String BUNDLEDPRODUCT_UNSUFFICIENT_PERMISSIONS_PROPERTY =
            "bundledproduct.unsufficient.permissions";//NOI18N
    public static final String BUNDLEDPRODUCT_NOT_COMPATIBLE_TEXT_PROPERTY =
            "bundledproduct.not.installable.text";//NOI18N
    public static final String BUNDLEDPRODUCT_NOT_COMPATIBLE_NETBEANS_INSTALLED_TEXT_PROPERTY =
            "bundledproduct.not.installable.netbeans.installed.text";//NOI18N
    
    public static final String DEFAULT_TEXT_PANE_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.text.pane.content.type"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER_JTB =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header.jtb"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER_MYSQL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header.nbgfmysql"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER_BUNDLEDPRODUCT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header.nbbundledproduct"); // NOI18N


    public static final String WELCOME_TEXT_HEADER_APPENDING_PROPERTY =
            "NWP.welcome.text.header"; // NOI18N
    
    public static final String WELCOME_PAGE_TYPE_PROPERTY =
            "NWP.welcome.page.type";

    public static final String WELCOME_PAGE_BUNDLEDPRODUCT_NAME_PROPERTY =
            "NWP.welcome.page.bundledproduct.name";
    
    public static final String DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.group.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.installed.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_DIFFERENT_BUILD_INSTALLED_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.different.build.installed.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.not.installed.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_OPENTAG =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.opentag");
    public static final String DEFAULT_WELCOME_TEXT_FOOTER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.footer"); // NOI18N
    public static final String DEFAULT_CUSTOMIZE_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.button.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.installation.size.label.text"); // NOI18N      

    public static final String DEFAULT_BUNDLEDPRODUCT_INSTALLED_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.bundledproduct.installed.text"); // NOI18N
    public static final String DEFAULT_BUNDLEDPRODUCT_EVERYTHING_INSTALLED_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.bundledproduct.everything.installed.text"); // NOI18N
    public static final String DEFAULT_BUNDLEDPRODUCT_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.bundledproduct.everything.installed.admin.warning.text"); // NOI18N
    public static final String DEFAULT_BUNDLEDPRODUCT_NETBEANS_INSTALLED_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.bundledproduct.netbeans.installed.text");//NOI18N
    public static final String DEFAULT_BUNDLEDPRODUCT_UNSUFFICIENT_PERMISSIONS_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.bundledproduct.welcome.admin.warning.text");//NOI18N
    public static final String DEFAULT_BUNDLEDPRODUCT_NOT_COMPATIBLE_TEXT_PROPERTY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.bundledproduct.not.installable.text");//NOI18N
    public static final String DEFAULT_BUNDLEDPRODUCT_NOT_COMPATIBLE_NETBEANS_INSTALLED_TEXT_PROPERTY =
           ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.bundledproduct.not.installable.netbeans.installed.text");//NOI18N

    public static final String DEFAULT_ADDITIONAL_PRODUCT_TO_BE_INSTALLED_TEXT_PROPERTY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.additional.product.to.be.installed"); // NOI18N
    public static final String DEFAULT_ADDITIONAL_PRODUCT_ALREADY_INSTALLED_TEXT_PROPERTY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.additional.product.already.installed"); // NOI18N
    
    
    public static final String CUSTOMIZE_TITLE_PROPERTY =
            "customize.title"; // NOI18N
    public static final String MESSAGE_PROPERTY =
            "message"; // NOI18N
    public static final String MESSAGE_INSTALL_PROPERTY =
            "message.install"; // NOI18N
    public static final String MESSAGE_UNINSTALL_PROPERTY =
            "message.uninstall"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_TEXT_PROPERTY =
            "component.description.text"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY =
            "component.description.content.type"; // NOI18N
    public static final String SIZES_LABEL_TEXT_PROPERTY =
            "sizes.label.text"; // NOI18N
    public static final String SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY =
            "sizes.label.text.no.download"; // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_PROPERTY =
            "default.installation.size"; // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_PROPERTY =
            "default.download.size"; // NOI18N
    public static final String OK_BUTTON_TEXT_PROPERTY =
            "ok.button.text"; // NOI18N
    public static final String CANCEL_BUTTON_TEXT_PROPERTY =
            "cancel.button.text"; // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_PROPERTY =
            "default.component.description";
    public static final String WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.top.image";//NOI18N
    public static final String WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.bottom.image";//NOI18N
    public static final String WELCOME_PAGE_BACKGROUND_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.background.image";//NOI18N
    public static final String DEFAULT_CUSTOMIZE_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.title"); // NOI18N
    public static final String DEFAULT_MESSAGE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.both"); // NOI18N
    public static final String DEFAULT_MESSAGE_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.install"); // NOI18N
    public static final String DEFAULT_MESSAGE_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.uninstall"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.text"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.content.type"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text.no.download"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.download.size"); // NOI18N
    public static final String DEFAULT_OK_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.ok.button.text"); // NOI18N
    public static final String DEFAULT_CANCEL_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.cancel.button.text"); // NOI18N
    public static final String DEFAULT_DEFAULT_COMPONENT_DESCRIPTION =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.component.description"); // NOI18N
    
    public static final String ERROR_NO_CHANGES_PROPERTY =
            "error.no.changes.both"; // NOI18N
    public static final String ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY =
            "error.no.changes.install"; // NOI18N
    public static final String ERROR_NO_RUNTIMES_INSTALL_ONLY_PROPERTY =
            "error.no.runtimes.install"; // NOI18N
    public static final String ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY =
            "error.no.changes.uninstall"; // NOI18N
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY =
            "error.requirement.install"; // NOI18N
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY =
            "error.conflict.install"; // NOI18N
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY =
            "error.requirement.uninstall"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY =
            "error.not.enough.space.to.download"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY =
            "error.not.enough.space.to.extract"; // NOI18N
    public static final String ERROR_EVERYTHING_IS_INSTALLED_PROPERTY =
            "error.everything.is.installed"; // NOI18N
    public static final String WARNING_NO_COMPATIBLE_JDK_FOUND =
            "warning.no.compatible.jdk.found"; //NOI18N
    public static final String WARNING_NO_COMPATIBLE_JAVA_FOUND =
            "warning.no.compatible.java.found";//NOI18N
    public static final String WARNING_NO_COMPATIBLE_JDK_FOUND_DEPENDENT =
            "warning.no.compatible.jdk.found.dependent";//NOI18N
            
    public static final String DEFAULT_ERROR_NO_CHANGES =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.both"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.install"); // NOI18N
    public static final String DEFAULT_ERROR_NO_RUNTIMES_INSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.runtimes.install"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.install"); // NOI18N
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.conflict.install"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.not.enough.space.to.download"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.not.enough.space.to.extract"); // NOI18N
    public static final String DEFAULT_ERROR_EVERYTHING_IS_INSTALLED =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.everything.is.installed"); // NOI18N
    public static final String DEFAULT_WARNING_NO_COMPATIBLE_JDK_FOUND =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.warning.no.compatible.jdk.found"); // NOI18N
    public static final String DEFAULT_WARNING_NO_COMPATIBLE_JAVA_FOUND =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.warning.no.compatible.java.found"); // NOI18N
    public static final String DEFAULT_WARNING_NO_COMPATIBLE_JDK_FOUND_DEPENDENT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.warning.no.compatible.jdk.found.dependent"); // NOI18N
    
    
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
