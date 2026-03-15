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
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.swing.NbiRadioButton;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

public class NbJUnitLicensePanel extends WizardPanel {
    // Instance

    public NbJUnitLicensePanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        setProperty(LICENSE_DESCRIPTION_PROPERTY,
                DEFAULT_LICENSE_DESCRIPTION);
        setProperty(ACCEPT_RBUTTON_TEXT_PROPERTY,
                DEFAULT_ACCEPT_RBUTTON_TEXT);
        setProperty(DENY_RBUTTON_TEXT_PROPERTY,
                DEFAULT_DENY_RBUTTON_TEXT);
        setProperty(ERROR_CANNOT_GET_LOGIC_PROPERTY,
                DEFAULT_ERROR_CANNOT_GET_LOGIC);       
        setProperty(LICENSE_FORMAT_PROPERTY,
                DEFAULT_LICENSE_FORMAT);
    }
    
    @Override
    public boolean canExecuteForward() {                              
        if(System.getProperty(JUNIT_LICENSE_RESOURCE_PROPERTY)!= null) {
            List <Product> products = Registry.getInstance().getProductsToInstall();
            for (Product p : products) {
               if(p.getUid().equals(NB_JAVASE_UID)) {
                   return true;
               }
            }            
        }
        return  false;
    }
    
    @Override
    public boolean canExecuteBackward() {
        return canExecuteForward();
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new LicensesPanelUi(this);
        }
        
        return wizardUi;
    }
    
    // Inner Classes
    public static class LicensesPanelUi extends WizardPanelUi {
        protected NbJUnitLicensePanel component;
        
        public LicensesPanelUi(NbJUnitLicensePanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new LicensesPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class LicensesPanelSwingUi extends WizardPanelSwingUi {        
                
        private NbiTextPane licensePane;
        private NbiScrollPane licenseScrollPane;
        private NbiTextPane descriptionPane;
        
        private NbiRadioButton acceptRButton;
        private NbiRadioButton denyRButton;

        private Product javaseProduct;
        
        public LicensesPanelSwingUi(
                final NbJUnitLicensePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;           
            
            initComponents();
        }
        
        @Override
        public JComponent getDefaultFocusOwner() {
            return acceptRButton;
        }
        
        // protected
        @Override
        protected void initialize() {
            acceptRButton.setText(
                    component.getProperty(ACCEPT_RBUTTON_TEXT_PROPERTY));

            denyRButton.setText(
                    component.getProperty(DENY_RBUTTON_TEXT_PROPERTY));
            descriptionPane.setText(component.getProperty(LICENSE_DESCRIPTION_PROPERTY));
                       
            final StringBuilder text = new StringBuilder();
                                    
            if(System.getProperty(JUNIT_LICENSE_RESOURCE_PROPERTY)!=null) {
                
                final String licenseValue = SystemUtils.resolveString(
                        System.getProperty(JUNIT_LICENSE_RESOURCE_PROPERTY));
                final String license = SystemUtils.resolveString("$R{" + licenseValue + ";" + StringUtils.ENCODING_UTF8 + "}");
                final String format = component.getProperty(LICENSE_FORMAT_PROPERTY);
                if(license!=null) {
                    text.append(StringUtils.format(format, license));
                }
            } 
            if(System.getProperty(JUNIT_LICENSE_CONTENT_TYPE_PROPERTY)!=null) {
                licensePane.setContentType(System.getProperty(
                        JUNIT_LICENSE_CONTENT_TYPE_PROPERTY));
            }
            
            licensePane.setText(text);            
            licensePane.setCaretPosition(0);
            licensePane.requestFocus();

            licensePane.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    final int code = e.getKeyCode();
                    if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                        BoundedRangeModel brm = licenseScrollPane.getVerticalScrollBar().getModel();
                        brm.setValue(brm.getValue() + brm.getExtent());
                    } else if (code == KeyEvent.VK_Q) {
                        container.getCancelButton().doClick();
                    } else if (code == KeyEvent.VK_N ) {
                         if (denyRButton.isEnabled()) {
                            denyRButton.setSelected(true);
                            denyRButtonToggled();
                        }
                    } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_Y) {
                        if (acceptRButton.isEnabled()) {
                            acceptRButton.setSelected(true);
                            acceptRButtonToggled();
                        }
                    }
                }
            });

            if (System.getProperty(JUNIT_LICENSE_SCROLLING_REQUIRED) != null) {
                licenseScrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        JScrollBar vsb = licenseScrollPane.getVerticalScrollBar();
                        if (vsb.getValue() >= vsb.getMaximum() - vsb.getModel().getExtent()) {
                            acceptRButton.setEnabled(true);
                        }
                    }
                });
                acceptRButton.setEnabled(false);
            }          
            if(!acceptRButton.isSelected() && !denyRButton.isSelected()) {
                container.getNextButton().setEnabled(false);
            }
        }
        
        // private
        private void initComponents() {
            List <Product> products = Registry.getInstance().getProductsToInstall();
            for (Product p : products) {
               if(p.getUid().equals(NB_JAVASE_UID) ) {
                   javaseProduct = p;
               }
            }

            // licensePane
            licensePane = new NbiTextPane();
            licensePane.setOpaque(true);
            licensePane.setBackground(Color.WHITE);
            licensePane.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
            licensePane.setFocusable(true);

            //descriptionPane
            descriptionPane = new NbiTextPane();

            // licenseScrollPane
            licenseScrollPane = new NbiScrollPane(licensePane);

            ActionListener rbuttonListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {                   
                    if(event.getActionCommand().equals("accept")) {
                       acceptRButtonToggled();
                    } else
                    if (event.getActionCommand().equals("deny")) {
                       denyRButtonToggled();
                    }
                }
            };
            // acceptRButton
            acceptRButton = new NbiRadioButton();
            acceptRButton.setSelected(false);
            acceptRButton.setFocusable(true);
            acceptRButton.setActionCommand("accept");
            acceptRButton.addActionListener(rbuttonListener);

            // denyRButton
            denyRButton = new NbiRadioButton();
            denyRButton.setSelected(false);
            denyRButton.setActionCommand("deny");
            denyRButton.addActionListener(rbuttonListener);

            ButtonGroup rbuttonGroup = new ButtonGroup();
            rbuttonGroup.add(acceptRButton);
            rbuttonGroup.add(denyRButton);


            // this
            add(licenseScrollPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???

            add(descriptionPane, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(7, 11, 2, 11),        // padding
                    0, 0));
            add(acceptRButton, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(2, 11, 2, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(denyRButton, new GridBagConstraints(
                    0, 4,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(2, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void acceptRButtonToggled() {
            if (acceptRButton.isSelected()) {
                setJUnitAcceptedProperty(true);
                container.getNextButton().setEnabled(true);
            }
        }
        private void denyRButtonToggled() {
            if (denyRButton.isSelected()) {               
                setJUnitAcceptedProperty(false);
                container.getNextButton().setEnabled(true);
            }
        }
        private void setJUnitAcceptedProperty(boolean value) {            
            javaseProduct.setProperty(JUNIT_ACCEPTED_PROPERTY, Boolean.toString(value));
        }
    }
    
    // Constants
    public static final String ACCEPT_RBUTTON_TEXT_PROPERTY =
            "accept.rbutton.text"; // NOI18N
    public static final String DENY_RBUTTON_TEXT_PROPERTY =
            "deny.rbutton.text"; // NOI18N
    public static final String LICENSE_DESCRIPTION_PROPERTY =
            "license.description"; // NOI18N
    public static final String ERROR_CANNOT_GET_LOGIC_PROPERTY =
            "error.cannot.get.logic";//NOI18N
    public static final String JUNIT_LICENSE_RESOURCE_PROPERTY =
            "nbi.junit.license.resource";//NOI18N
    public static final String JUNIT_LICENSE_CONTENT_TYPE_PROPERTY =
            "nbi.junit.license.content.type";//NOI18N
    public static final String LICENSE_FORMAT_PROPERTY =
            "license.format";//NOI18N
    public static final String JUNIT_ACCEPTED_PROPERTY =
            "junit.accepted";//NOI18N
    private static final String JUNIT_LICENSE_SCROLLING_REQUIRED =
            "nbi.junit.license.scrolling.required";
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.description"); // NOI18N
    public static final String DEFAULT_LICENSE_FORMAT =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.license.format"); // NOI18N
    public static final String DEFAULT_APPEND_LICENSE_FORMAT =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.append.license.format"); // NOI18N
    public static final String DEFAULT_LICENSE_DESCRIPTION =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.license.description"); // NOI18N
    public static final String DEFAULT_ACCEPT_RBUTTON_TEXT =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.accept.rbutton.text"); // NOI18N
    public static final String DEFAULT_DENY_RBUTTON_TEXT =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.deny.rbutton.text"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_GET_LOGIC =
            ResourceUtils.getString(NbJUnitLicensePanel.class,
            "NLP.error.cannot.get.logic"); // NOI18N
    public static final String JUNIT_UID =
            "junit"; // NOI18N
    public static final String NB_JAVASE_UID =
            "nb-javase"; // NOI18N
}
