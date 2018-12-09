/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.installer.products.glassfish.mod.wizard.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.BrowserUtils;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiComboBox;
import org.netbeans.installer.utils.helper.swing.NbiDirectoryChooser;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationValidator;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxEditor;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxModel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelSwingUi;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelUi;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 */
public class GlassFishPanel extends DestinationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JdkLocationPanel jdkLocationPanel;

    private static String jdkSelectedInIDE;
    private static String jdkSelectedInGF;
        
    public GlassFishPanel() {
        jdkLocationPanel = new JdkLocationPanel();
        
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);
        
        setProperty(ERROR_IN_NETBEANS_INSTALLATION_FOLDER, DEFAULT_ERROR_IN_NETBEANS_INSTALLATION_FOLDER);

        setProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY,
                DEFAULT_JDK_LOCATION_LABEL_TEXT);
        setProperty(BROWSE_BUTTON_TEXT_PROPERTY,
                DEFAULT_BROWSE_BUTTON_TEXT);
        
        setProperty(WARNING_JDK_NOT_RECOMMENDED_VERSION,
                DEFAULT_WARNING_JDK_NOT_RECOMMENDED_VERSION);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new GlassFishPanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        jdkLocationPanel.setWizard(getWizard());
        
        if (getWizard().getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY) != null) {
            jdkLocationPanel.setProperty(
                    JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY,
                    getWizard().getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY));
        }
        
        jdkSelectedInIDE = null;
        List<Product> productsToInstall = Registry.getInstance().getProductsToInstall();
        for (Product product : productsToInstall) {
            if (product.getUid().equals("nb-base")) {
                jdkSelectedInIDE = product.getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY);
                break;
            }
        }
        jdkSelectedInGF = getWizard().getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY);
        if (jdkSelectedInGF != null) {
            getWizard().setProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY, jdkSelectedInGF);
        } else if (jdkSelectedInIDE != null) {
            getWizard().setProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY, jdkSelectedInIDE);
        }
        
        jdkLocationPanel.initialize();

        getWizard().setProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY, jdkSelectedInGF == null ? "" : jdkSelectedInGF); // NOI18N
    }

    public JdkLocationPanel getJdkLocationPanel() {
        return jdkLocationPanel;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class GlassFishPanelUi extends DestinationPanelUi {
        protected GlassFishPanel component;
                
        public GlassFishPanelUi(GlassFishPanel component) {
            super(component);
            
            this.component = component;
        }
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new GlassFishPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class GlassFishPanelSwingUi extends DestinationPanelSwingUi {
        protected GlassFishPanel panel;

        private NbiPanel containerPanel;
        
        private NbiLabel jdkLocationLabel;
        private NbiComboBox jdkLocationComboBox;
        private NbiButton browseButton;
        private NbiTextPane statusLabel;
        
        private NbiTextField jdkLocationField;
        
        private NbiDirectoryChooser fileChooser;
        
        public GlassFishPanelSwingUi(
                final GlassFishPanel panel,
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

                statusLabel.setText(StringUtils.format(
                        jdkLocationPanel.getProperty(JdkLocationPanel.ERROR_NOTHING_FOUND_PROPERTY),
                        minVersion.toJdkStyle(),
                        minVersion.toJdkStyle()));
            } else if (getNeedJava7Warning() == null) {
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
            
            
            super.initialize();
            
        }
        
        @Override
        protected void saveInput() {
            super.saveInput();
        }
        
        @Override
        protected String validateInput() {
            String errorMessage = super.validateInput();

            if (errorMessage != null) {
                return errorMessage;
            }

            panel.getJdkLocationPanel().getWizard().
                    setProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY, jdkSelectedInGF == null ? "" : jdkSelectedInGF); // NOI18N
            
            //#128991: Installation not recognized not empty dir for GF
            File f = FileUtils.eliminateRelativity(getDestinationField().getText().trim());
            if (FileUtils.exists(f)) {
                File [] list = f.listFiles();
                if (list!= null && list.length > 0) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_EMPTY_PROPERTY),
                            f.getAbsolutePath());
                }
            }   
            
            //#137248: Glassfish installation failed while using UNC paths
            if (SystemUtils.isWindows() && FileUtils.isUNCPath(f.getAbsolutePath())) {
                return StringUtils.format(
                        component.getProperty(ERROR_UNC_PATH_UNSUPPORTED_PROPERTY),
                        f.getAbsolutePath());
            }
            
            //#202619: org.netbeans.installer.utils.exceptions.UninstallationException: failed to stop the default domain
            File actualFolder = f.getAbsoluteFile();
            File netBeansInstallationLocation = null;
            
            List<Product> productsToInstall = Registry.getInstance().getProductsToInstall();
            for (Product product : productsToInstall) {
                if (product.getUid().equals("nb-base")) {
                    netBeansInstallationLocation = product.getInstallationLocation().getAbsoluteFile();
                    break;
                }
            }
            
            if (netBeansInstallationLocation != null) {
                do {                    
                    if (netBeansInstallationLocation.equals(actualFolder)) {
                        return StringUtils.format(component.getProperty(ERROR_IN_NETBEANS_INSTALLATION_FOLDER));
                    }
                    actualFolder = actualFolder.getParentFile();
                } while (actualFolder.getParentFile() != null);
            }
            
            if (getWarningMessage() == null && getNeedJava7Warning() != null) {
                if (! statusLabel.isVisible()) {
                    statusLabel.setText(getNeedJava7Warning());
                    statusLabel.setVisible(true);
                }
            } else {
                statusLabel.clearText();
                statusLabel.setVisible(false);
            }
            
            return null;
        }
        
        @Override
        protected String getWarningMessage() {
            String warningMessage  = panel.getJdkLocationPanel().validateLocation(
                        jdkLocationField.getText());
            return warningMessage;
        }
        
        private String getNeedJava7Warning() {
            String warningMessage = null;
            if (getWarningMessage() == null) {
                if (JavaUtils.criticalLowVersion.newerThan(
                        JavaUtils.getVersion(new File(jdkLocationField.getText())))) {
                    warningMessage = StringUtils.format(
                            panel.getProperty(WARNING_JDK_NOT_RECOMMENDED_VERSION), 
                            panel.jdkLocationPanel.getProperty(JdkLocationPanel.JAVA_DOWNLOAD_PAGE_PROPERTY));
                }
            }
            return warningMessage;
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
                        String location = ((LocationsComboBoxModel) model).getLocation();
                        jdkLocationField.setText(location);
                        if (jdkSelectedInIDE != null && ! jdkSelectedInIDE.equals(location)) {
                            jdkSelectedInGF = location;
                        } else if (jdkSelectedInIDE != null && jdkSelectedInIDE.equals(location)) {
                            jdkSelectedInGF = null;
                        }
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
            
            // statusLabel //////////////////////////////////////////////////////////
            statusLabel = new NbiTextPane();
            statusLabel.setContentType("text/html");
            statusLabel.addHyperlinkListener(BrowserUtils.createHyperlinkListener());
            
            
            // fileChooser //////////////////////////////////////////////////////////
            fileChooser = new NbiDirectoryChooser();
                        
            // this /////////////////////////////////////////////////////////////////
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
                    0, 0));                           // padx, pady - ???
            add(containerPanel, new GridBagConstraints(
                    0, 5,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
        }

        private void browseButtonPressed() {
            fileChooser.setSelectedFile(new File(jdkLocationField.getText()));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                jdkLocationComboBox.getModel().setSelectedItem(
                        fileChooser.getSelectedFile().getAbsolutePath());
            }
        }

    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.description"); // NOI18N

    public static final String JDK_LOCATION_LABEL_TEXT_PROPERTY =
            "jdk.location.label.text"; // NOI18N
    public static final String BROWSE_BUTTON_TEXT_PROPERTY =
            "browse.button.text"; // NOI18N
    public static final String WARNING_JDK_NOT_RECOMMENDED_VERSION =
            "jdk.not.recommended.version";

    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.destination.button.text"); // NOI18N

    public static final String DEFAULT_JDK_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.jdk.location.label.text"); // NOI18N
    public static final String DEFAULT_BROWSE_BUTTON_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.browse.button.text"); // NOI18N

    public static final String ERROR_UNC_PATH_UNSUPPORTED_PROPERTY =
            "error.unc.path.unsupported"; // NOI18N
     public static final String ERROR_IN_NETBEANS_INSTALLATION_FOLDER =
            "error.in.nb.installation.folder"; // NOI18N

     public static final String DEFAULT_ERROR_UNC_PATH_UNSUPPORTED =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.unc.path.unsupported"); // NOI18N
    public static final String DEFAULT_ERROR_IN_NETBEANS_INSTALLATION_FOLDER =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.in.nb.installation.folder"); // NOI18N
    public static final String DEFAULT_WARNING_JDK_NOT_RECOMMENDED_VERSION =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.warning.jdk.not.recommended.version"); // NOI18N
}
