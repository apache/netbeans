/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.products.weblogic.wizard.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.BrowserUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiComboBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiPasswordField;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationValidator;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxEditor;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxModel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelSwingUi;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelUi;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi.ValidatingDocumentListener;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.utils.helper.swing.NbiDirectoryChooser;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;

/**
 *
 
 */
public class WebLogicPanel extends DestinationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JdkLocationPanel jdkLocationPanel;
    //private static boolean allPortsOccupied;
        
    public WebLogicPanel() {
        jdkLocationPanel = new JdkLocationPanel();
        
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);      
        
        setProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY,
                DEFAULT_JDK_LOCATION_LABEL_TEXT);
        setProperty(DOMAIN_DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DOMAIN_DESTINATION_LABEL_TEXT);        
        setProperty(BROWSE_BUTTON_TEXT_PROPERTY,
                DEFAULT_BROWSE_BUTTON_TEXT);


        setProperty(DOMAINNAME_LABEL_TEXT_PROPERTY,
                DEFAULT_DOMAINNAME_LABEL_TEXT);                       
        setProperty(USERNAME_LABEL_TEXT_PROPERTY,
                DEFAULT_USERNAME_LABEL_TEXT);
        setProperty(PASSWORD_LABEL_TEXT_PROPERTY,
                DEFAULT_PASSWORD_LABEL_TEXT);
        setProperty(REPEAT_PASSWORD_LABEL_TEXT_PROPERTY,
                DEFAULT_REPEAT_PASSWORD_LABEL_TEXT);
        
        setProperty(ERROR_DOMAINNAME_NULL_PROPERTY,
                DEFAULT_ERROR_DOMAINNAME_NULL);        
        setProperty(ERROR_DOMAINNAME_WEBLOGIC_PROPERTY,
                DEFAULT_ERROR_DOMAINNAME_WEBLOGIC);
        setProperty(ERROR_USERNAME_NULL_PROPERTY,
                DEFAULT_ERROR_USERNAME_NULL);
        setProperty(ERROR_USERNAME_NOT_ALNUM_PROPERTY,
                DEFAULT_ERROR_USERNAME_NOT_ALNUM);
        setProperty(ERROR_PASSWORD_NULL_PROPERTY,
                DEFAULT_ERROR_PASSWORD_NULL);
        setProperty(ERROR_PASSWORD_TOO_SHORT_PROPERTY,
                DEFAULT_ERROR_PASSWORD_TOO_SHORT);
        setProperty(ERROR_PASSWORD_SPACES_PROPERTY,
                DEFAULT_ERROR_PASSWORD_SPACES);
        setProperty(ERROR_PASSWORDS_DO_NOT_MATCH_PROPERTY,
                DEFAULT_ERROR_PASSWORDS_DO_NOT_MATCH);
        setProperty(ERROR_PASSWORD_DO_NOT_CONTAIN_DIGIT_PROPERTY,
                DEFAULT_ERROR_PASSWORDS_DO_NOT_CONTAIN_DIGIT);        
         
        setProperty(ERROR_UNC_PATH_UNSUPPORTED_PROPERTY,
                DEFAULT_ERROR_UNC_PATH_UNSUPPORTED);
        setProperty(ERROR_BRACKETS_IN_NOT_SPACED_PATH_PROPERTY,
                DEFAULT_BRACKETS_IN_NOT_SPACED_PATH);
        setProperty(ERROR_DOMAIN_EXISTS_PROPERTY,
                DEFAULT_ERROR_DOMAIN_EXISTS);        
          
        
        setProperty(ERROR_ALL_PORTS_OCCUPIED_PROPERTY,
                DEFAULT_ERROR_ALL_PORTS_OCCUPIED);        
        setProperty(WARNING_PORT_IN_USE_PROPERTY,
                DEFAULT_WARNING_PORT_IN_USE);
        

        setProperty(DEFAULT_DOMAINNAME_PROPERTY,
                DEFAULT_DEFAULT_DOMAINNAME);                 
        setProperty(DEFAULT_USERNAME_PROPERTY,
                DEFAULT_DEFAULT_USERNAME);
        setProperty(DEFAULT_PASSWORD_PROPERTY,
                DEFAULT_DEFAULT_PASSWORD);   
        
       setProperty(ERROR_DOMAINDIR_NULL_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_NULL);
        setProperty(ERROR_DOMAINDIR_NOT_VALID_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_NOT_VALID);
        setProperty(ERROR_DOMAINDIR_CONTAINS_EXCLAMATION_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_CONTAINS_EXCLAMATION);
        setProperty(ERROR_DOMAINDIR_CONTAINS_SEMICOLON_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_CONTAINS_SEMICOLON);
        setProperty(ERROR_DOMAINDIR_CONTAINS_COLON_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_CONTAINS_COLON);
        setProperty(ERROR_DOMAINDIR_CONTAINS_AMPERSAND_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_CONTAINS_AMPERSAND);
        setProperty(ERROR_DOMAINDIR_CONTAINS_WRONG_CHAR_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_CONTAINS_WRONG_CHAR);
        setProperty(ERROR_DOMAINDIR_MATCHES_PROHIBITED_REGEXP,
                DEFAULT_ERROR_DOMAINDIR_MATCHES_PROHIBITIED_REGEXP);
        setProperty(ERROR_DOMAINDIR_CANNOT_CANONIZE_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_CANNOT_CANONIZE);
        setProperty(ERROR_DOMAINDIR_NOT_ABSOLUTE_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_NOT_ABSOLUTE);
        setProperty(ERROR_DOMAINDIR_NOT_DIRECTORY_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_NOT_DIRECTORY);
        setProperty(ERROR_DOMAINDIR_NOT_READABLE_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_NOT_READABLE);
        setProperty(ERROR_DOMAINDIR_NOT_WRITABLE_PROPERTY,
                DEFAULT_ERROR_DOMAINDIR_NOT_WRITABLE);
        setProperty(ERROR_DOMAIN_NOT_EMPTY_PROPERTY,
                DEFAULT_ERROR_DOMAIN_NOT_EMPTY);        
       /* setProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY,
                DEFAULT_ERROR_NOT_ENOUGH_SPACE); 
        setProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY,
                DEFAULT_ERROR_CANNOT_CHECK_SPACE);        */
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WebLogicPanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        jdkLocationPanel.setWizard(getWizard());
        
        jdkLocationPanel.setProperty(
                JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY,
                getWizard().getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY));
        jdkLocationPanel.setProperty(
                JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY,
                getWizard().getProperty(JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY));
        jdkLocationPanel.setProperty(
                JdkLocationPanel.VENDOR_JDK_ALLOWED_PROPERTY,
                getWizard().getProperty(JdkLocationPanel.VENDOR_JDK_ALLOWED_PROPERTY));
        
        if (getWizard().getProperty(JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY) != null) {
            jdkLocationPanel.setProperty(
                    JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY,
                    getWizard().getProperty(JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY));
        }
        
        jdkLocationPanel.initialize();

        //This makes it possible to perform silent installation with emptry state files 
        //that means that JDK_LOCATION_PROPERTY property is explicitely set to the first location
        //that fits the requirements
        //TODO: Investigate the prons&cons and side affects of moving
        //this code to the end of JdkLocationPanel.initialize() method        
        File jdkLocation = jdkLocationPanel.getSelectedLocation();        
        if(jdkLocation!=null && !jdkLocation.getPath().equals(StringUtils.EMPTY_STRING)) {
            jdkLocationPanel.setLocation(jdkLocation);
        }         
        
        String password = getWizard().getProperty(PASSWORD_PROPERTY);        
        
        if (password == null) {
            password = getProperty(DEFAULT_PASSWORD_PROPERTY);
        }
        getWizard().setProperty(PASSWORD_PROPERTY, password);
        
        String username = getWizard().getProperty(USERNAME_PROPERTY);
        if (username == null) {
            username = getProperty(DEFAULT_USERNAME_PROPERTY);
        }
        getWizard().setProperty(USERNAME_PROPERTY, username);               
        
        String domainname = getWizard().getProperty(DOMAINNAME_PROPERTY);
        if (domainname == null) {
            domainname = getProperty(DEFAULT_DOMAINNAME_PROPERTY);
        }
        getWizard().setProperty(DOMAINNAME_PROPERTY, domainname); 
        
        
        final Product product = (Product) getWizard().
                   getContext().
                   get(Product.class);           
        //installation location can be set using <uid>.installation.location system property
        // Such a simplified approach is useful for silent installation - 
        // we can almost get rid of state file.
        // Limitation is that if we have to install two products with the same uid 
        // but different versions then such a thing does now work correctly.        
        final String ilSysProp = product.getUid() + StringUtils.DOT +
                DOMAIN_INSTALLATION_SUBDIR_PROPERTY;
        final String il = System.getProperty(ilSysProp);
        final String ilSysPropDisabled = ilSysProp + ".initialization.disabled";

        if (il != null && !Boolean.getBoolean(ilSysPropDisabled)) {
            LogManager.log("... try to use domain installation subdir for " + product.getDisplayName() +
                    " from system property " + ilSysProp + " : " + il);
            product.setProperty(DOMAIN_INSTALLATION_SUBDIR_PROPERTY, new File(il).getAbsolutePath());
            System.setProperty(ilSysPropDisabled, Boolean.toString(true));
        }
        
        String domainDestination = product.getProperty(DOMAIN_INSTALLATION_SUBDIR_PROPERTY);

        if (domainDestination == null) {
            domainDestination = DEFAULT_DOMAIN_DESTINATION_SUBDIR;
        }
        
        domainDestination = resolvePath(domainDestination).getAbsolutePath();
        
         
        /*String domainDestination = getWizard().getProperty(DOMAIN_DESTINATION_PROPERTY);        
        if (domainDestination == null) {
            File domainDestinationFile = new File(product.getProperty(DEFAULT_DOMAIN_DESTINATION_PROPERTY));
            domainDestination = domainDestinationFile.getAbsolutePath();                                                                                   
        }*/
        getWizard().setProperty(DOMAIN_INSTALLATION_SUBDIR_PROPERTY, domainDestination);        
    }
    
    public JdkLocationPanel getJdkLocationPanel() {
        return jdkLocationPanel;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WebLogicPanelUi extends DestinationPanelUi {
        protected WebLogicPanel component;
                
        public WebLogicPanelUi(WebLogicPanel component) {
            super(component);
            
            this.component = component;
        }
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WebLogicPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class WebLogicPanelSwingUi extends DestinationPanelSwingUi {
        protected WebLogicPanel panel;
        
        private NbiPanel containerPanel;
        
        private NbiLabel jdkLocationLabel;
        private NbiComboBox jdkLocationComboBox;
        private NbiButton browseButton;
        private NbiTextPane statusLabel;                
        
        private NbiTextField jdkLocationField;
        
        private NbiDirectoryChooser fileChooser;
        
        private NbiLabel domainDestinationLabel;
        private NbiTextField domainDestinationField;
        private NbiButton domainBrowseButton;
        
        private NbiLabel domainnameLabel;
        private NbiTextField domainnameField;
        
        private NbiLabel usernameLabel;
        private NbiTextField usernameField;
        
        private NbiLabel passwordLabel;
        private NbiPasswordField passwordField;
        
        private NbiLabel repeatPasswordLabel;
        private NbiPasswordField repeatPasswordField;                
        private String domainDestinationSuffix;
        private boolean internalChange;
        
        public WebLogicPanelSwingUi(
                final WebLogicPanel panel,
                final SwingContainer container) {
            super(panel, container);
            
            this.panel = panel;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            jdkLocationLabel.setText(
                    panel.getProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY));
            
            final JdkLocationPanel jdkLocationPanel = panel.getJdkLocationPanel();
            
            if (jdkLocationPanel.getLocations().isEmpty()) {
                final Version minVersion = Version.getVersion(jdkLocationPanel.getProperty(
                        JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY));
                final Version maxVersion = Version.getVersion(jdkLocationPanel.getProperty(
                        JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY));
                statusLabel.setContentType("text/html");
                statusLabel.setText(StringUtils.format(
                        jdkLocationPanel.getProperty(JdkLocationPanel.ERROR_NOTHING_FOUND_PROPERTY),
                        minVersion.toJdkStyle(),
                        maxVersion.toJdkStyle(),
                        jdkLocationPanel.getProperty(JdkLocationPanel.JAVA_DOWNLOAD_PAGE_PROPERTY)));

                statusLabel.addHyperlinkListener(BrowserUtils.createHyperlinkListener());
            } else {
                statusLabel.clearText();
                statusLabel.setVisible(false);
            }

            final List<File> jdkLocations = jdkLocationPanel.getLocations();                        
            final List<String> jdkLabels = jdkLocationPanel.getLabels();
            
            final LocationsComboBoxModel model = new LocationsComboBoxModel(
                    jdkLocations,
                    jdkLabels);            
            
            ((LocationsComboBoxEditor) jdkLocationComboBox.getEditor()).setModel(
                    model);
            jdkLocationComboBox.setModel(
                    model);
            
            final File selectedLocation = jdkLocationPanel.getSelectedLocation();
            final int index = jdkLocations.indexOf(selectedLocation);
            String selectedItem;
            if(index != -1) {
                  selectedItem = jdkLabels.get(index);  
            } else {
                  selectedItem = selectedLocation.toString();
            }  
            model.setSelectedItem(selectedItem);                                                       
            browseButton.setText(
                    panel.getProperty(BROWSE_BUTTON_TEXT_PROPERTY));            
            domainDestinationLabel.setText(
                    component.getProperty(DOMAIN_DESTINATION_LABEL_TEXT_PROPERTY));
            domainBrowseButton.setText(
                    component.getProperty(BROWSE_BUTTON_TEXT_PROPERTY));
                                    
            domainDestinationField.setText(panel.getWizard().getProperty(DOMAIN_INSTALLATION_SUBDIR_PROPERTY));            
            
            domainnameLabel.setText(
                    panel.getProperty(DOMAINNAME_LABEL_TEXT_PROPERTY));            
            usernameLabel.setText(
                    panel.getProperty(USERNAME_LABEL_TEXT_PROPERTY));
            passwordLabel.setText(
                    panel.getProperty(PASSWORD_LABEL_TEXT_PROPERTY));
            repeatPasswordLabel.setText(
                    panel.getProperty(REPEAT_PASSWORD_LABEL_TEXT_PROPERTY));            
            domainnameField.setText(panel.getWizard().getProperty(DOMAINNAME_PROPERTY));
            usernameField.setText(panel.getWizard().getProperty(USERNAME_PROPERTY));
            passwordField.setText(panel.getWizard().getProperty(PASSWORD_PROPERTY));
            repeatPasswordField.setText(panel.getWizard().getProperty(PASSWORD_PROPERTY));
                        
            domainDestinationSuffix = null;
            super.initialize();
            initDomainDestinationSuffix();
        }
        
        @Override
        protected void saveInput() {
            super.saveInput();
            
            panel.getJdkLocationPanel().setLocation(
                    new File(jdkLocationField.getText()));
            panel.getWizard().setProperty(
                    DOMAIN_INSTALLATION_SUBDIR_PROPERTY,
                    domainDestinationField.getText());            
            panel.getWizard().setProperty(
                    DOMAINNAME_PROPERTY,
                    domainnameField.getText());                        
            panel.getWizard().setProperty(
                    USERNAME_PROPERTY,
                    usernameField.getText());
            panel.getWizard().setProperty(
                    PASSWORD_PROPERTY,
                    new String(passwordField.getPassword()));                                                                              
        }
        
        @Override
        protected String validateInput() {
            String errorMessage = super.validateInput();
            
            if (errorMessage == null) {
                errorMessage = panel.getJdkLocationPanel().validateLocation(
                        jdkLocationField.getText());
            }
            
            if (errorMessage != null) {
                return errorMessage;
            }
            ///validateInput for Domain destination field copy-pasted from destination panel////
            //TODO change errors to contain "domain". copy from DestinationPanle
            final String domaindest = domainDestinationField.getText().trim();            
            final Product product = (Product) component.
                    getWizard().
                    getContext().
                    get(Product.class);
            
            try {
                if (domaindest.equals(StringUtils.EMPTY_STRING)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAINDIR_NULL_PROPERTY),
                            domaindest);
                }
                
                File file = FileUtils.eliminateRelativity(domaindest);
                
                String filePath = file.getAbsolutePath();
                if (filePath.length() > 45) {
                    filePath = filePath.substring(0, 45) + "...";
                }
                
                if (!SystemUtils.isPathValid(file.getAbsolutePath())) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAINDIR_NOT_VALID_PROPERTY),
                            filePath);
                }                
                
                final String[] prohibitedParts = product.getLogic().getProhibitedInstallationPathParts();
                if (prohibitedParts != null) {
                    for (String s : prohibitedParts) {
                        if (s != null && s.length() > 0) {
                            String prop = null;
                            if (s.length() == 1) { // character
                                if (file.getAbsolutePath().contains(s)) {
                                    if (s.equals("!")) {
                                        prop = ERROR_DOMAINDIR_CONTAINS_EXCLAMATION_PROPERTY;
                                    } else if (s.equals(";")) {
                                        prop = ERROR_DOMAINDIR_CONTAINS_SEMICOLON_PROPERTY;
                                    } else if (s.equals(":")) {
                                        prop = ERROR_DOMAINDIR_CONTAINS_COLON_PROPERTY;
                                    } else if (s.equals("&")) {
                                        prop = ERROR_DOMAINDIR_CONTAINS_AMPERSAND_PROPERTY;
                                    } else {
                                        // no user-friendly description for all other chars at this moment 
                                        // can be easily extended later
                                        prop = ERROR_DOMAINDIR_CONTAINS_WRONG_CHAR_PROPERTY;
                                    }
                                }
                            } else {// check if path matches regexp..
                                if (file.getAbsolutePath().matches(s)) {
                                    prop = ERROR_DOMAINDIR_MATCHES_PROHIBITED_REGEXP;
                                }
                            }
                            if (prop != null) {
                                return StringUtils.format(
                                        component.getProperty(prop),
                                        filePath,
                                        s);
                            }
                        }
                    }
                }                
                
                if (!file.equals(file.getAbsoluteFile())) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAINDIR_NOT_ABSOLUTE_PROPERTY),
                            file.getPath());
                }
                
                try {
                    file = file.getCanonicalFile();
                } catch (IOException e) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAINDIR_CANNOT_CANONIZE_PROPERTY),
                            filePath);
                }
                
                filePath = file.getAbsolutePath();
                if (filePath.length() > 45) {
                    filePath = filePath.substring(0, 45) + "...";
                }
                
                if (file.exists() && !file.isDirectory()) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAINDIR_NOT_DIRECTORY_PROPERTY),
                            filePath);
                }
                
                if (!FileUtils.canRead(file)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAINDIR_NOT_READABLE_PROPERTY),
                            filePath);
                }
                
                if (!FileUtils.canWrite(file)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAINDIR_NOT_WRITABLE_PROPERTY),
                            filePath);
                }                                                
                //TODO check the required size
                if(!Boolean.getBoolean(SystemUtils.NO_SPACE_CHECK_PROPERTY)) {
                    final long requiredSize =
                            product.getRequiredDiskSpace() + REQUIRED_SPACE_ADDITION;
                    final long availableSize =
                            SystemUtils.getFreeSpace(file);
                    if (availableSize < requiredSize) {
                        return StringUtils.format(
                                component.getProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY),
                                filePath,
                                StringUtils.formatSize(requiredSize - availableSize));
                    }
                }
            } catch (InitializationException e) {
                ErrorManager.notifyError(component.getProperty(
                        ERROR_CANNOT_GET_LOGIC_PROPERTY), e);
            } catch (NativeException e) {
                ErrorManager.notifyError(component.getProperty(
                        ERROR_CANNOT_CHECK_SPACE_PROPERTY), e);
            }            
                                                                                  
            final String domainname = domainnameField.getText().trim();
            final String username = usernameField.getText();
            final String password = new String(passwordField.getPassword());
            final String password2 = new String(repeatPasswordField.getPassword());
            
            //TODO insert check for domainname not to contain strange simbols
            if ((domainname == null) || domainname.equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_DOMAINNAME_NULL_PROPERTY),
                        domainname);
            }
            if (domainname.equals("weblogic")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_DOMAINNAME_WEBLOGIC_PROPERTY),
                        domainname);
            }
            
            if ((username == null) || username.trim().equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_USERNAME_NULL_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (!username.matches("[0-9a-zA-Z]+")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_USERNAME_NOT_ALNUM_PROPERTY),
                        username,
                        password,
                        password2);
            }
            
            //TODO insert check fot 1 digit in password
            if ((password == null) || password.trim().equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORD_NULL_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (password.length() < 8) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORD_TOO_SHORT_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (!password.equals(password2)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORDS_DO_NOT_MATCH_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (!password.trim().equals(password)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORD_SPACES_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if(!password.matches("((?=.*\\d).{8,})")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORD_DO_NOT_CONTAIN_DIGIT_PROPERTY),
                        username,
                        password,
                        password2);                
            }
            
            /*if ((httpPort.equals("") || httpsPort.equals("") || adminPort.equals("")) && allPortsOccupied) {
                return panel.getProperty(ERROR_ALL_PORTS_OCCUPIED_PROPERTY);
            }
            
            if ((httpPort == null) || httpPort.equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_NULL_PROPERTY),
                        httpPort);
            }
            if (!httpPort.matches("(0|[1-9][0-9]*)")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_NOT_INTEGER_PROPERTY),
                        httpPort);
            }
            int port = new Integer(httpPort);
            if ((port < 0) || (port > 65535)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_NOT_IN_RANGE_PROPERTY),
                        httpPort);
            }
            if (!SystemUtils.isPortAvailable(port)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_OCCUPIED_PROPERTY),
                        httpPort);
            }
            
            if ((httpsPort == null) || httpsPort.equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_NULL_PROPERTY),
                        httpsPort);
            }
            if (!httpsPort.matches("(0|[1-9][0-9]*)")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_NOT_INTEGER_PROPERTY),
                        httpsPort);
            }
            port = new Integer(httpsPort);
            if ((port < 0) || (port > 65535)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_NOT_IN_RANGE_PROPERTY),
                        httpsPort);
            }
            if (!SystemUtils.isPortAvailable(port)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_OCCUPIED_PROPERTY),
                        httpsPort);
            }
            
            if ((adminPort == null) || adminPort.equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_NULL_PROPERTY),
                        adminPort);
            }
            if (!adminPort.matches("(0|[1-9][0-9]*)")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_NOT_INTEGER_PROPERTY),
                        adminPort);
            }
            port = new Integer(adminPort);
            if ((port < 0) || (port > 65535)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_NOT_IN_RANGE_PROPERTY),
                        adminPort);
            }
            if (!SystemUtils.isPortAvailable(port)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_OCCUPIED_PROPERTY),
                        adminPort);
            }
            
            if (httpPort.equals(httpsPort)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_EQUALS_HTTPS_PROPERTY),
                        httpPort,
                        httpsPort);
            }
            if (httpPort.equals(adminPort)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_EQUALS_ADMIN_PROPERTY),
                        httpPort,
                        adminPort);
            }
            if (httpsPort.equals(adminPort)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_EQUALS_ADMIN_PROPERTY),
                        httpsPort, adminPort);
            }
            */            
            
            File f = FileUtils.eliminateRelativity(getDestinationField().getText().trim());                            
            //#128991: Installation not recognized not empty dir for WL
            //File f = FileUtils.eliminateRelativity(getDestinationField().getText().trim());
            if(FileUtils.exists(f)) {
                File [] list = f.listFiles();
                if (list!= null && list.length > 0) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_EMPTY_PROPERTY),
                            f.getAbsolutePath());
                }
            }                       
            //#137248: Glassfish installation failed while using UNC paths
            if(SystemUtils.isWindows() && FileUtils.isUNCPath(f.getAbsolutePath())) {
                return StringUtils.format(
                        component.getProperty(ERROR_UNC_PATH_UNSUPPORTED_PROPERTY),
                        f.getAbsolutePath());
            }
            //#163233 Installer allow enter paths which can not be used for installation
            //#163426 Unable to install GlassFish V2.1 to C:\Program Files (x86)\glassfish-v2.1
            if(SystemUtils.isWindows() && 
                !f.getAbsolutePath().contains(StringUtils.SPACE) && 
                (f.getAbsolutePath().contains("(") || f.getAbsolutePath().contains(")"))) {
                return StringUtils.format(
                        component.getProperty(ERROR_BRACKETS_IN_NOT_SPACED_PATH_PROPERTY),
                        f.getAbsolutePath());

            }
            
           // check for config file is good but for uninstaller it is better when domain dir is empty
           // because of uninstall files list
           /*File configfile = new File(domainDestinationField.getText().trim(), 
                    domainnameField.getText().trim() + File.pathSeparator + DOMAIN_CONFIG_FILE);
            /if(FileUtils.exists(configfile)) {                
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAIN_EXISTS_PROPERTY),
                            f.getAbsolutePath());                                                              
            } */             
            f = FileUtils.eliminateRelativity(new File(domainDestinationField.getText().trim(), 
                    domainnameField.getText().trim()).getAbsolutePath());                            
            if(FileUtils.exists(f)) {
                File [] list = f.listFiles();
                if (list!= null && list.length > 0) {
                    return StringUtils.format(
                            component.getProperty(ERROR_DOMAIN_NOT_EMPTY_PROPERTY),
                            f.getAbsolutePath());
                }
            }   
          
            return null;
        }
        
        @Override
        protected String getWarningMessage() {
            // check whether the selected ports are already in use by any other
            // installed application server (SJSAS or GlassFish)
           
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // containerPanel ///////////////////////////////////////////////////////
            containerPanel = new NbiPanel();
            
            // selectedLocationField ////////////////////////////////////////////////
            jdkLocationField = new NbiTextField();
            jdkLocationField.getDocument().addDocumentListener(
                    new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
                
                @Override
                public void removeUpdate(DocumentEvent e) {
                    //updateErrorMessage();
                }
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
            });
            
            // jdkLocationComboBox //////////////////////////////////////////////////
            final LocationValidator validator = new LocationValidator() {
                @Override
                public void validate(String location) {
                    jdkLocationField.setText(location);
                }
            };
            
            jdkLocationComboBox = new NbiComboBox();
            jdkLocationComboBox.setEditable(true);
            jdkLocationComboBox.setEditor(new LocationsComboBoxEditor(validator));
            jdkLocationComboBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    final ComboBoxModel model = jdkLocationComboBox.getModel();
                    
                    if (model instanceof LocationsComboBoxModel) {
                        jdkLocationField.setText(
                                ((LocationsComboBoxModel) model).getLocation());
                    }
                }
            });
            
            // jdkLocationLabel /////////////////////////////////////////////////////
            jdkLocationLabel = new NbiLabel();
            jdkLocationLabel.setLabelFor(jdkLocationComboBox);
            
            // browseButton /////////////////////////////////////////////////////////
            browseButton = new NbiButton();
            browseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    browseButtonPressed();
                }
            });
            
            // domainDestinationField /////////////////////////////////////////////////////
            domainDestinationField = new NbiTextField();
            domainDestinationField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    domainDestinationFieldChanged();
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    domainDestinationFieldChanged();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    domainDestinationFieldChanged();
                }

                private void domainDestinationFieldChanged () {
                    if (!internalChange) {
                        initDomainDestinationSuffix();
                    }
                    updateErrorMessage();
                }
            });
            getDestinationField().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate (DocumentEvent e) {
                    updateDomainDestination();
                }

                @Override
                public void removeUpdate (DocumentEvent e) {
                    updateDomainDestination();
                }

                @Override
                public void changedUpdate (DocumentEvent e) {
                    updateDomainDestination();
                }

                private void updateDomainDestination () {
                    if (domainDestinationSuffix != null) {
                        boolean previousValue = internalChange;
                        try {
                            internalChange = true;
                            domainDestinationField.setText(getDestinationField().getText() + domainDestinationSuffix);
                        } finally {
                            internalChange = previousValue;
                        }
                    }
                }
            });
            
            // destinationLabel /////////////////////////////////////////////////////
            domainDestinationLabel = new NbiLabel();
            domainDestinationLabel.setLabelFor(domainDestinationField);
            
            // destinationButton ////////////////////////////////////////////////////
            domainBrowseButton = new NbiButton();
            domainBrowseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    domainBrowseButtonPressed();
                }
            });                                    
            
            // statusLabel //////////////////////////////////////////////////////////
            statusLabel = new NbiTextPane();            
            
            // fileChooser //////////////////////////////////////////////////////////
            fileChooser = new NbiDirectoryChooser();
                        
            final Dimension longFieldSize = new Dimension(
                    200,
                    new NbiTextField().getPreferredSize().height);
            final Dimension shortFieldSize = new Dimension(
                    80,
                    longFieldSize.height);
 
            // domainnameField ////////////////////////////////////////////////////////
            domainnameField = new NbiTextField();
            domainnameField.setPreferredSize(longFieldSize);
            domainnameField.setMinimumSize(longFieldSize);
            domainnameField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // domainnameLabel ////////////////////////////////////////////////////////
            domainnameLabel = new NbiLabel();
            domainnameLabel.setLabelFor(domainnameField);            
            
            // usernameField ////////////////////////////////////////////////////////
            usernameField = new NbiTextField();
            usernameField.setPreferredSize(longFieldSize);
            usernameField.setMinimumSize(longFieldSize);
            usernameField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // usernameLabel ////////////////////////////////////////////////////////
            usernameLabel = new NbiLabel();
            usernameLabel.setLabelFor(usernameField);
            
            // passwordField ////////////////////////////////////////////////////////
            passwordField = new NbiPasswordField();
            passwordField.setPreferredSize(longFieldSize);
            passwordField.setMinimumSize(longFieldSize);
            passwordField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // passwordLabel ////////////////////////////////////////////////////////
            passwordLabel = new NbiLabel();
            passwordLabel.setLabelFor(passwordField);
            
            // repeatPasswordField //////////////////////////////////////////////////
            repeatPasswordField = new NbiPasswordField();
            repeatPasswordField.setPreferredSize(longFieldSize);
            repeatPasswordField.setMinimumSize(longFieldSize);
            repeatPasswordField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // repeatPasswordLabel //////////////////////////////////////////////////
            repeatPasswordLabel = new NbiLabel();
            repeatPasswordLabel.setLabelFor(repeatPasswordField);
            
            // this /////////////////////////////////////////////////////////////////
           /*add(domainDestinationLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(domainDestinationField, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(domainBrowseButton, new GridBagConstraints(
                    1, 3,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 4, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???            
            */
            add(jdkLocationLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(jdkLocationComboBox, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(browseButton, new GridBagConstraints(
                    1, 3,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 4, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
             add(statusLabel, new GridBagConstraints(
                    0, 4,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));
           add(domainDestinationLabel, new GridBagConstraints(
                    0, 5,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(domainDestinationField, new GridBagConstraints(
                    0, 6,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(domainBrowseButton, new GridBagConstraints(
                    1, 6,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 4, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???            
             
            /* add(new NbiPanel(), new GridBagConstraints(
                    1, 50,                            // x, y
                    2, 1,                             // width, height
                    0.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???*/
 
            add(containerPanel, new GridBagConstraints(
                    0, 7,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            // containerPanel ///////////////////////////////////////////////////////

            containerPanel.add(domainnameLabel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(domainnameField, new GridBagConstraints(
                    1, 0,                             // x, y
                    2, 1,                             // width, height
                    0.5, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 6, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
                        
            containerPanel.add(usernameLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(usernameField, new GridBagConstraints(
                    1, 1,                             // x, y
                    2, 1,                             // width, height
                    0.5, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(passwordLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(passwordField, new GridBagConstraints(
                    1, 2,                             // x, y
                    2, 1,                             // width, height
                    0.5, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(repeatPasswordLabel, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(repeatPasswordField, new GridBagConstraints(
                    1, 3,                             // x, y
                    2, 1,                             // width, height
                    0.5, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            /*
            containerPanel.add(httpPortLabel, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(httpPortField, new GridBagConstraints(
                    1, 3,                             // x, y
                    1, 1,                             // width, height
                    0.1, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 6, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    2, 3,                             // x, y
                    1, 1,                             // width, height
                    0.4, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(httpsPortLabel, new GridBagConstraints(
                    0, 4,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(httpsPortField, new GridBagConstraints(
                    1, 4,                             // x, y
                    1, 1,                             // width, height
                    0.1, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
                          
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    2, 4,                             // x, y
                    1, 1,                             // width, height
                    0.4, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 4,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(adminPortLabel, new GridBagConstraints(
                    0, 5,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(adminPortField, new GridBagConstraints(
                    1, 5,                             // x, y
                    1, 1,                             // width, height
                    0.1, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
             
             
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    2, 5,                             // x, y
                    1, 1,                             // width, height
                    0.4, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 5,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
             
             */
        }
        
        private void browseButtonPressed() {
            fileChooser.setSelectedFile(new File(jdkLocationField.getText()));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                jdkLocationComboBox.getModel().setSelectedItem(
                        fileChooser.getSelectedFile().getAbsolutePath());
            }
        }

       private void domainBrowseButtonPressed(){
                       final Product product = (Product) component.
                    getWizard().
                    getContext().
                    get(Product.class);
            
            final File currentDestination = new File(domainDestinationField.getText());
            
            fileChooser.setSelectedFile(currentDestination);
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String newDestination =
                        fileChooser.getSelectedFile().getAbsolutePath();

                String suffix = currentDestination.getName();

                if(! new File(newDestination).equals(currentDestination)) {
                    newDestination = new File(
                            newDestination,
                            suffix).getAbsolutePath();
                }                                                
                domainDestinationField.setText(newDestination);
            }        
       }        

        private void initDomainDestinationSuffix () {
            if (domainDestinationField.getText().startsWith(getDestinationField().getText())) {
                domainDestinationSuffix = domainDestinationField.getText().substring(getDestinationField().getText().length());
            } else {
                domainDestinationSuffix = null;
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.description"); // NOI18N
    
    
    public static final String DOMAIN_INSTALLATION_SUBDIR_PROPERTY =
            "domain.installation.subdir"; // NOI18N    
    public static final String DOMAINNAME_PROPERTY =
            "domainname"; // NOI18N       
    public static final String USERNAME_PROPERTY =
            "username"; // NOI18N
    public static final String PASSWORD_PROPERTY =
            "password"; // NOI18N   
    
    public static final String JDK_LOCATION_LABEL_TEXT_PROPERTY =
            "jdk.location.label.text"; // NOI18N
    public static final String BROWSE_BUTTON_TEXT_PROPERTY =
            "browse.button.text"; // NOI18N
    public static final String DOMAINNAME_LABEL_TEXT_PROPERTY =
            "domainname.label.text"; // NOI18N   
    public static final String USERNAME_LABEL_TEXT_PROPERTY =
            "username.label.text"; // NOI18N
    public static final String PASSWORD_LABEL_TEXT_PROPERTY =
            "password.label.text"; // NOI18N
    public static final String REPEAT_PASSWORD_LABEL_TEXT_PROPERTY =
            "repeat.password.label.text"; // NOI18N  
    public static final String DOMAIN_DESTINATION_LABEL_TEXT_PROPERTY
            = "domain.destination.label.text"; // NOI18N
    
    
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.destination.button.text"); // NOI18N   
    
          
    public static final String DEFAULT_JDK_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.jdk.location.label.text"); // NOI18N
    public static final String DEFAULT_DOMAIN_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.domain.destination.label.text"); // NOI18N 
    public static final String DEFAULT_BROWSE_BUTTON_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.browse.button.text"); // NOI18N
    public static final String DEFAULT_DOMAINNAME_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.domainname.label.text"); // NOI18N    
    public static final String DEFAULT_USERNAME_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.username.label.text"); // NOI18N
    public static final String DEFAULT_PASSWORD_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.password.label.text"); // NOI18N
    public static final String DEFAULT_REPEAT_PASSWORD_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.repeat.password.label.text"); // NOI18N   
    public static final String DEFAULT_DEFAULTS_LABEL_TEXT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.defaults.label.text"); // NOI18N
  
    public static final String DEFAULT_DOMAINNAME_PROPERTY =
            "default.domainname"; // NOI18N    
    public static final String DEFAULT_USERNAME_PROPERTY =
            "default.username"; // NOI18N
    public static final String DEFAULT_PASSWORD_PROPERTY =
            "default.password"; // NOI18N   
    
    public static final String DEFAULT_DOMAIN_DESTINATION_SUBDIR =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.default.domain.destination.subdir"); // NOI18N   
    public static final String DEFAULT_DEFAULT_DOMAINNAME =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.default.domainname"); // NOI18N    
    public static final String DEFAULT_DEFAULT_USERNAME =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.default.username"); // NOI18N
    public static final String DEFAULT_DEFAULT_PASSWORD =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.default.password"); // NOI18N    

    
    public static final String ERROR_DOMAINNAME_NULL_PROPERTY =
            "error.domainname.null"; // NOI18N    
    public static final String ERROR_DOMAINNAME_WEBLOGIC_PROPERTY =
            "error.domainname.weblogic"; // NOI18N
    public static final String ERROR_USERNAME_NULL_PROPERTY =
            "error.username.null"; // NOI18N
    public static final String ERROR_USERNAME_NOT_ALNUM_PROPERTY =
            "error.username.not.alnum"; // NOI18N
    public static final String ERROR_PASSWORD_NULL_PROPERTY =
            "error.password.null"; // NOI18N
    public static final String ERROR_PASSWORD_TOO_SHORT_PROPERTY =
            "error.password.too.short"; // NOI18N
    public static final String ERROR_PASSWORD_SPACES_PROPERTY =
            "error.password.spaces"; // NOI18N
    public static final String ERROR_PASSWORDS_DO_NOT_MATCH_PROPERTY =
            "error.passwords.do.not.match"; // NOI18N
    public static final String ERROR_PASSWORD_DO_NOT_CONTAIN_DIGIT_PROPERTY =
            "error.password.do.not.contain.digit"; // NOI18N        
    public static final String ERROR_ALL_PORTS_OCCUPIED_PROPERTY =
            "error.all.ports.occupied"; // NOI18N   
    public static final String ERROR_UNC_PATH_UNSUPPORTED_PROPERTY =
            "error.unc.path.unsupported"; // NOI18N
    public static final String ERROR_BRACKETS_IN_NOT_SPACED_PATH_PROPERTY =
            "error.brackets.in.not.spaced.path";
    public static final String ERROR_DOMAIN_EXISTS_PROPERTY =
            "error.domain.exists";             

    public static final String WARNING_PORT_IN_USE_PROPERTY =
            "warning.port.in.use"; // NOI18N
    public static final String WARNING_ASADMIN_FILES_EXIST_PROPERTY =
            "warning.asadmin.files.exist"; // NOI18N
    
    public static final String DEFAULT_ERROR_DOMAINNAME_NULL =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domainname.null"); // NOI18N     
    public static final String DEFAULT_ERROR_DOMAINNAME_WEBLOGIC =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domainname.weblogic"); // NOI18N            
    public static final String DEFAULT_ERROR_USERNAME_NULL =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.username.null"); // NOI18N
    public static final String DEFAULT_ERROR_USERNAME_NOT_ALNUM =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.username.not.alnum"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORD_NULL =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.password.null"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORD_TOO_SHORT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.password.too.short"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORD_SPACES =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.password.spaces"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORDS_DO_NOT_CONTAIN_DIGIT =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.password.do.not.contain.digit"); // NOI18N    
    public static final String DEFAULT_ERROR_PASSWORDS_DO_NOT_MATCH =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.passwords.do.not.match"); // NOI18N  
    public static final String DEFAULT_ERROR_ALL_PORTS_OCCUPIED =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.all.ports.occupied"); // NOI18N    
    public static final String DEFAULT_ERROR_UNC_PATH_UNSUPPORTED =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.unc.path.unsupported"); // NOI18N
    public static final String DEFAULT_BRACKETS_IN_NOT_SPACED_PATH =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.parantheses.in.not.spaced.path"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAIN_EXISTS =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domain.exists"); // NOI18N         
            
    public static final String DEFAULT_WARNING_PORT_IN_USE =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.warning.port.in.use"); // NOI18N  
    
    public static final String ERROR_DOMAINDIR_NULL_PROPERTY =
            "error.domaindir.null"; // NOI18N
    public static final String ERROR_DOMAINDIR_NOT_VALID_PROPERTY =
            "error.domaindir.not.valid"; // NOI18N
    public static final String ERROR_DOMAINDIR_CONTAINS_EXCLAMATION_PROPERTY =
            "error.domaindir.contains.exclamation"; // NOI18N
    public static final String ERROR_DOMAINDIR_CONTAINS_SEMICOLON_PROPERTY =
            "error.domaindir.contains.semicolon"; // NOI18N
    public static final String ERROR_DOMAINDIR_CONTAINS_COLON_PROPERTY =
            "error.domaindir.contains.colon"; // NOI18N
    public static final String ERROR_DOMAINDIR_CONTAINS_AMPERSAND_PROPERTY =
            "error.domaindir.contains.ampersand"; // NOI18N
    public static final String ERROR_DOMAINDIR_CONTAINS_WRONG_CHAR_PROPERTY =
            "error.domaindir.contains.wrong.char"; // NOI18N
    public static final String ERROR_DOMAINDIR_MATCHES_PROHIBITED_REGEXP =
            "error.domaindir.matches.prohibited.regexp";//NOI18N
    public static final String ERROR_DOMAINDIR_NOT_ABSOLUTE_PROPERTY =
            "error.domaindir.not.absolute"; // NOI18N
    public static final String ERROR_DOMAINDIR_CANNOT_CANONIZE_PROPERTY =
            "error.domaindir.cannot.canonize"; // NOI18N
    public static final String ERROR_DOMAINDIR_NOT_DIRECTORY_PROPERTY =
            "error.domaindir.not.directory"; // NOI18N
    public static final String ERROR_DOMAINDIR_NOT_READABLE_PROPERTY =
            "error.domaindir.not.readable"; // NOI18N
    public static final String ERROR_DOMAINDIR_NOT_WRITABLE_PROPERTY =
            "error.domaindir.not.writable"; // NOI18N
    public static final String ERROR_DOMAIN_NOT_EMPTY_PROPERTY =
            "error.domain.not.empty"; // NOI18N  
    /*
    public static final String ERROR_NOT_ENOUGH_SPACE_PROPERTY =
            "error.not.enough.space"; // NOI18N   
    public static final String ERROR_CANNOT_CHECK_SPACE_PROPERTY =
            "error.cannot.check.space"; // NOI18N
    */
    public static final String DEFAULT_ERROR_DOMAINDIR_NULL =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.null"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_NOT_VALID =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.not.valid"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_CONTAINS_EXCLAMATION =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.contains.exclamation"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_CONTAINS_SEMICOLON =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.contains.semicolon"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_CONTAINS_COLON =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.contains.colon"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_CONTAINS_AMPERSAND =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.contains.ampersand"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_CONTAINS_WRONG_CHAR =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.contains.wrong.char"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_MATCHES_PROHIBITIED_REGEXP =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.matches.prohibited.regexp"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_NOT_ABSOLUTE =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.not.absolute"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_CANNOT_CANONIZE =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.cannot.canonize"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_NOT_DIRECTORY =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.not.directory"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_NOT_READABLE =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.not.readable"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAINDIR_NOT_WRITABLE =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domaindir.not.writable"); // NOI18N
    public static final String DEFAULT_ERROR_DOMAIN_NOT_EMPTY =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.domain.not.empty"); // NOI18N
/*
    public static final String DEFAULT_ERROR_DOMAINDIR_NOT_ENOUGH_SPACE =
            ResourceUtils.getString(WebLogicPanel.class,
            "WLP.error.not.enough.space"); // NOI18N  
    public static final String DEFAULT_ERROR_CANNOT_CHECK_SPACE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "WLP.error.cannot.check.space"); // NOI18N   
     * */
     
      
    private static final String DOMAIN_CONFIG_FILE = 
            "config/config.xml"; // NOI18N  
    
}
