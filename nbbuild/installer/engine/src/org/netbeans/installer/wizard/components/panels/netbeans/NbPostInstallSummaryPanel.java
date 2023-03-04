/**
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

package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.BrowserUtils;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiSeparator;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.containers.SwingFrameContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_INSTALL;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_UNINSTALL;

/**
 *
 */
public class NbPostInstallSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbPostInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY, 
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, 
                DEFAULT_DESCRIPTION);
        
        setProperty(MESSAGE_TEXT_SUCCESS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_SUCCESS);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS);
        setProperty(MESSAGE_TEXT_WARNINGS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_WARNINGS);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS);
        setProperty(MESSAGE_TEXT_ERRORS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_ERRORS);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS);
        
        setProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL);
        setProperty(MESSAGE_FILES_REMAINING_PROPERTY,
                DEFAULT_MESSAGE_FILES_REMAINING);
        
        setProperty(NEXT_BUTTON_TEXT_PROPERTY, 
                DEFAULT_NEXT_BUTTON_TEXT);
    }
    
    @Override
    public boolean isPointOfNoReturn() {
        return true;
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbPostInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbPostInstallSummaryPanelUi extends WizardPanelUi {
        protected NbPostInstallSummaryPanel component;
        
        public NbPostInstallSummaryPanelUi(NbPostInstallSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbPostInstallSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbPostInstallSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected NbPostInstallSummaryPanel component;
        
        private NbiTextPane messagePaneInstall;
        private NbiTextPane messagePaneUninstall;
        private NbiTextPane messagePaneRestart;
        private NbiTextPane messagePaneNetBeans;
        private NbiTextPane messagePaneRegistration;                                 
        private NbiCheckBox checkBoxRegistration;                                    
        private NbiPanel registrationPanel;
        private NbiPanel spacer;
        private NbiTextPane messagePaneMySQL;

        private NbiCheckBox metricsCheckbox;
        private NbiTextPane metricsInfo;
        private NbiTextPane metricsList;
        private NbiPanel metricsPanel;
        private NbiSeparator separator;
        
        public NbPostInstallSummaryPanelSwingUi(
                final NbPostInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initializeContainer() {
            super.initializeContainer();
            
            // set up the back button
            container.getBackButton().setVisible(false);
            container.getBackButton().setEnabled(false);
            
            // set up the next (or finish) button
            container.getNextButton().setVisible(true);
            container.getNextButton().setEnabled(true);
            
            container.getNextButton().setText(
                    component.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
            
            // set up the cancel button
            container.getCancelButton().setVisible(false);
            container.getCancelButton().setEnabled(false);            
        }
        
        @Override
        public void evaluateNextButtonClick() {
            container.getNextButton().setEnabled(false);            
            super.evaluateNextButtonClick();
        }
        
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            final List <Product>  successfulInstall = registry.getProducts(INSTALLED_SUCCESSFULLY);
            final List <Product>     warningInstall = registry.getProducts(INSTALLED_WITH_WARNINGS);
            final List <Product>       errorInstall = registry.getProducts(FAILED_TO_INSTALL);
            
            final List<Product> successfulUninstall = registry.getProducts(UNINSTALLED_SUCCESSFULLY);
            final List<Product>    warningUninstall = registry.getProducts(UNINSTALLED_WITH_WARNINGS);
            final List<Product>      errorUninstall = registry.getProducts(FAILED_TO_UNINSTALL);
            
            if (errorInstall.size() > 0) {
                messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY));
                messagePaneInstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_ERRORS_PROPERTY), 
                        LogManager.getLogFile(),
                        StringUtils.asString(errorInstall)));
            } else if (warningInstall.size() > 0) {
                messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY));
                messagePaneInstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_WARNINGS_PROPERTY),
                        LogManager.getLogFile(),
                        StringUtils.asString(warningInstall)));
            } else if (successfulInstall.size() > 0) {
                messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY));
                messagePaneInstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_SUCCESS_PROPERTY), 
                        LogManager.getLogFile(),
                        StringUtils.asString(successfulInstall)));
            } else {
                messagePaneInstall.setVisible(false);
            }

            if (errorUninstall.size() > 0) {
                messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY));
                messagePaneUninstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY),
                        LogManager.getLogFile(), 
                        StringUtils.asString(errorUninstall)));
            } else if (warningUninstall.size() > 0) {
                messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY));
                messagePaneUninstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY), 
                        LogManager.getLogFile(),
                        StringUtils.asString(warningUninstall)));
            } else if (successfulUninstall.size() > 0) {
                messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY));
                messagePaneUninstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY),
                        LogManager.getLogFile(),
                        StringUtils.asString(successfulUninstall)));
            } else {
                messagePaneUninstall.setVisible(false);
            }
            
            final List<Product> products = new LinkedList<Product>();
            
            products.addAll(successfulInstall);
            products.addAll(warningInstall);
            
            messagePaneMySQL.setContentType(DEFAULT_MESSAGE_MYSQL_CONTENT_TYPE);
            messagePaneMySQL.setText("");
            messagePaneMySQL.setVisible(false);
            for (Product product : products) {
                if (product.getUid().equals("mysql")) {
                    messagePaneMySQL.setText(
                            StringUtils.format(SystemUtils.isWindows() ? 
                                DEFAULT_MYSQL_MESSAGE_WINDOWS : 
                                DEFAULT_MYSQL_MESSAGE_UNIX,
                            product.getInstallationLocation()));
                    messagePaneMySQL.setVisible(true);
                    messagePaneMySQL.addHyperlinkListener(BrowserUtils.createHyperlinkListener());
                    break;
                }
            }
            
            messagePaneRestart.setContentType(DEFAULT_MESSAGE_RESTART_CONTENT_TYPE);
            messagePaneRestart.setText("");
            messagePaneRestart.setVisible(false);
            for (Product product : products) {
                if (product.getUid().equals("jdk")) {
                    final String restartRequired = product.getProperty(RESTART_IS_REQUIRED_PROPERTY);
                    LogManager.log("... restart is required property: " + restartRequired);
                    if(Boolean.parseBoolean(restartRequired)) {
                        messagePaneRestart.setText(DEFAULT_MESSAGE_RESTART_TEXT);
                        messagePaneRestart.setVisible(true);
                    }
                    break;
                }
            }
            
            messagePaneNetBeans.setContentType(DEFAULT_MESSAGE_NETBEANS_CONTENT_TYPE);
            messagePaneNetBeans.setText("");
            boolean nbInstalled = false;
            for (Product product: products) {
                if (product.getUid().equals("nb-base") || product.getUid().equals("nb-all")) {
                    String platformSpecificSummary;
                    if (SystemUtils.isWindows()) {
                        platformSpecificSummary = DEFAULT_MESSAGE_NETBEANS_TEXT_WINDOWS;
                    } else if (SystemUtils.isMacOS()) {
                        platformSpecificSummary = DEFAULT_MESSAGE_NETBEANS_TEXT_MACOSX;
                    } else {
                        platformSpecificSummary = DEFAULT_MESSAGE_NETBEANS_TEXT_UNIX;
                    }
                    String netbeansSummary = product.getProperty(NETBEANS_SUMMARY_MESSAGE_TEXT_PROPERTY);
                    if (netbeansSummary == null) {
                        netbeansSummary = "";
                    }
                    messagePaneNetBeans.setText(MessageFormat.format("{0}{1}", netbeansSummary, platformSpecificSummary)); // NOI18N
                    nbInstalled = true;
                    break;
                }
            }
            // initialize registration components
            List<Product> toRegister = new LinkedList<Product>();
            for (Product product : products) {
                final String uid = product.getUid();
                if (uid.equals("nb-base") || uid.equals("jdk") || uid.equals("glassfish") || uid.equals("sjsas") || uid.equals("nb-all")) {
                    toRegister.add(product);
                }
            }
            boolean registrationEnabled = 
                    nbInstalled            && // if NetBeans is among installed products
                    !toRegister.isEmpty()  && // if anything to register
                    !SystemUtils.isMacOS() && // no support on mac
                    Boolean.getBoolean(ALLOW_SERVICETAG_REGISTRATION_PROPERTY) && //system property is defined
                    BrowserUtils.isBrowseSupported();
            
            if (!registrationEnabled) {
                //separator.setVisible(false);
                //messagePaneRegistration.setVisible(false);
                //checkBoxRegistration.setVisible(false);
                //spacer.setVisible(false);
                registrationPanel.setVisible(false);
                checkBoxRegistration.setSelected(false);
                System.setProperty(ALLOW_SERVICETAG_REGISTRATION_PROPERTY, "" + false);
            } else {
                String productsString = StringUtils.EMPTY_STRING;
                for (Product product : toRegister) {
                    final String uid = product.getUid();
                    String name = StringUtils.EMPTY_STRING;
                    if (uid.equals("nb-base") || uid.equals("nb-all")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_NETBEANS;
                    } else if (uid.equals("jdk")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_JDK;
                    } else if (uid.equals("glassfish")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_GLASSFISH;
                    } else if (uid.equals("sjsas")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_APPSERVER;
                    }
                    if (productsString.equals(StringUtils.EMPTY_STRING)) {
                        productsString = name;
                    } else {
                        productsString = StringUtils.format(DEFAULT_MESSAGE_REGISTRATION_CONCAT, productsString, name);
                    }
                }
                messagePaneRegistration.setContentType(DEFAULT_MESSAGE_REGISTRATION_CONTENT_TYPE);
                messagePaneRegistration.setText(StringUtils.format(DEFAULT_MESSAGE_REGISTRATION_TEXT, productsString));
                checkBoxRegistration.setText(StringUtils.format(DEFAULT_MESSAGE_REGISTRATION_CHECKBOX, productsString));
                // be default - it is checked
                checkBoxRegistration.doClick();
                // do not show message about starting the IDE and plugin manager in case of registration 
                messagePaneNetBeans.setVisible(false);
            }

            /*if(nbInstalled) {                
                metricsList.setContentType(DEFAULT_MESSAGE_METRICS_LIST_CONTENT_TYPE);
                metricsList.setText(DEFAULT_MESSAGE_METRICS_LIST);
                metricsInfo.setContentType(DEFAULT_MESSAGE_METRICS_TEXT_CONTENT_TYPE);
                metricsInfo.setText(DEFAULT_MESSAGE_METRICS_TEXT);
                metricsCheckbox.setText(DEFAULT_MESSAGE_METRICS_CHECKBOX);
                metricsCheckbox.doClick();
            } else {*/
                metricsPanel.setVisible(false);
            //}
            products.clear();
            
            products.addAll(successfulUninstall);
            products.addAll(warningUninstall);
            
            final List<Product> notCompletelyRemoved = new LinkedList<Product>();
            for (Product product: products) {
                if (!FileUtils.isEmpty(product.getInstallationLocation())) {
                    notCompletelyRemoved.add(product);
                }
            }
            
            if (notCompletelyRemoved.size() > 0) {
                final String text = messagePaneUninstall.getText();
                messagePaneUninstall.setText(text + StringUtils.format(
                        panel.getProperty(MESSAGE_FILES_REMAINING_PROPERTY),
                        StringUtils.asString(notCompletelyRemoved)));
            }
        }
        
        private void initComponents() {
            // messagePaneInstall ///////////////////////////////////////////////////
            messagePaneInstall = new NbiTextPane();
            
            // messagePaneUninstall /////////////////////////////////////////////////
            messagePaneUninstall = new NbiTextPane();

            // messagePaneRestart ///////////////////////////////////////////////////
            messagePaneRestart = new NbiTextPane();
            
            // messagePaneNetBeans ///////////////////////////////////////////////////
            messagePaneNetBeans = new NbiTextPane();

            
            metricsPanel = new NbiPanel();
            
            metricsCheckbox = new NbiCheckBox();
            
            metricsList = new NbiTextPane();
            
            metricsInfo = new NbiTextPane();
            
            metricsCheckbox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.setProperty(ENABLE_NETBEANS_METRICS_PROPERTY,
                            "" + metricsCheckbox.isSelected());
                }
            });
            metricsInfo.addHyperlinkListener(BrowserUtils.createHyperlinkListener());
            
            metricsPanel.add(metricsCheckbox, new GridBagConstraints(
                    0, 0, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(0, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
           
            metricsPanel.add(metricsList, new GridBagConstraints(
                    0, 1, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(0, 7, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            metricsPanel.add(metricsInfo, new GridBagConstraints(
                    0, 2, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(0, 29, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            
            // messagePaneRegistration            
            messagePaneRegistration = new NbiTextPane();

            //checkBoxRegistration
            checkBoxRegistration = new NbiCheckBox();
            checkBoxRegistration.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.setProperty(ALLOW_SERVICETAG_REGISTRATION_PROPERTY,
                            "" + checkBoxRegistration.isSelected());
                }
            });
            separator = new NbiSeparator();
            
            registrationPanel = new NbiPanel();
            
            registrationPanel.add(separator, new GridBagConstraints(
                    0, 0, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(0, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            
            registrationPanel.add(checkBoxRegistration, new GridBagConstraints(
                    0, 1, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            registrationPanel.add(messagePaneRegistration, new GridBagConstraints(
                    0, 2, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(4, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???

            // spacer
            spacer = new NbiPanel();

            messagePaneMySQL = new NbiTextPane();
            // this /////////////////////////////////////////////////////////////////
            add(messagePaneInstall, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneUninstall, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneMySQL, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            
            add(messagePaneRestart, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???

            add(messagePaneNetBeans, new GridBagConstraints(
                    0, 4,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(11, 11, 0, 11),       // padding
                    0, 0));                           // padx, pady - ???
            
            add(metricsPanel, new GridBagConstraints(
                    0, 5,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(11, 0, 0, 0),       // padding
                    0, 0));              
            
            
            add(registrationPanel, new GridBagConstraints(
                    0, 6,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(11, 0, 0, 0),       // padding
                    0, 0));  
            
            add(spacer, new GridBagConstraints(
                    0, 7, // x, y
                    1, 1, // width, height
                    1.0, 10.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 0, 0, 0), // padding
                    0, 0));                           // padx, pady - ???
            
            if (container instanceof SwingFrameContainer) {
                final SwingFrameContainer sfc = (SwingFrameContainer) container;
                sfc.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent event) {
                        SwingUi currentUi = component.getWizardUi().getSwingUi(container);
                        if (currentUi != null) {
                            if (!container.getCancelButton().isEnabled() && // cancel button is disabled
                                    !container.getCancelButton().isVisible() && // no cancel button at this panel
                                    !container.getBackButton().isVisible() && // no back button at this panel
                                    container.getNextButton().isVisible() && // next button is visible
                                    container.getNextButton().isEnabled()) { // and enabled                                                                
                                currentUi.evaluateNextButtonClick();
                                sfc.removeWindowListener(this);
                            }
                        }
                    }
                });
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_TEXT_SUCCESS_PROPERTY =
            "message.text.success"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY =
            "message.content.type.success"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_PROPERTY =
            "message.text.warnings"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY =
            "message.content.type.warnings"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_PROPERTY =
            "message.text.errors"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY =
            "message.content.type.errors"; // NOI18N
    public static final String MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY =
            "message.text.success.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY =
            "message.content.type.success.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY =
            "message.text.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY =
            "message.content.type.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY =
            "message.text.errors.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY =
            "message.content.type.errors.uninstall"; // NOI18N
    public static final String MESSAGE_FILES_REMAINING_PROPERTY =
            "message.files.remaining"; // NOI18N
    public static final String RESTART_IS_REQUIRED_PROPERTY =
            "restart.required";//NOI18N
    public static final String NETBEANS_SUMMARY_MESSAGE_TEXT_PROPERTY =
            "netbeans.summary.message.text"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.errors.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.errors.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_FILES_REMAINING =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.files.remaining"); // NOI18N

    public static final String DEFAULT_MESSAGE_RESTART_TEXT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.restart.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_RESTART_CONTENT_TYPE =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.restart.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_WINDOWS = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.windows"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_UNIX = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.unix"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_MACOSX = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.macosx"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_CONTENT_TYPE = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_MYSQL_CONTENT_TYPE =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.mysql.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_TEXT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_NETBEANS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.netbeans"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_GLASSFISH =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.glassfish"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_APPSERVER =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.appserver"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_JDK =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.jdk"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_CONCAT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.concat");//NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_CHECKBOX =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.checkbox"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_CONTENT_TYPE =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.content.type");//NOI18N    
    public static final String DEFAULT_MESSAGE_METRICS_TEXT_CONTENT_TYPE =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.metrics.text.content.type");//NOI18N    
    public static final String DEFAULT_MESSAGE_METRICS_LIST_CONTENT_TYPE =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.metrics.list.content.type");//NOI18N    
    public static final String DEFAULT_MESSAGE_METRICS_TEXT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.metrics.text");//NOI18N    
    public static final String DEFAULT_MESSAGE_METRICS_LIST =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.metrics.list");//NOI18N    
    public static final String DEFAULT_MESSAGE_METRICS_CHECKBOX =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.metrics.checkbox");//NOI18N    
    public static final String ALLOW_SERVICETAG_REGISTRATION_PROPERTY =
            "servicetag.allow.register";
    public static final String ENABLE_NETBEANS_METRICS_PROPERTY =
            "enable.netbeans.metrics";
    public static final String DEFAULT_MYSQL_MESSAGE_WINDOWS = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,            
	    "NPoISP.message.using.mysql.windows");
    public static final String DEFAULT_MYSQL_MESSAGE_UNIX = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.using.mysql.unix");

    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            NbPostInstallSummaryPanel.class,
            "NPoISP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.description"); // NOI18N
    
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.next.button.text"); // NOI18N
}
