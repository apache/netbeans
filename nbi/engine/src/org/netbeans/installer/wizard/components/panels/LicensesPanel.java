/*
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

package org.netbeans.installer.wizard.components.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

public class LicensesPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public LicensesPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(ACCEPT_CHECKBOX_TEXT_PROPERTY,
                DEFAULT_ACCEPT_CHECKBOX_TEXT);
        setProperty(ERROR_CANNOT_GET_LOGIC_PROPERTY,
                DEFAULT_ERROR_CANNOT_GET_LOGIC);
        setProperty(APPEND_LICENSE_FORMAT_PROPERTY, 
                DEFAULT_APPEND_LICENSE_FORMAT);
        setProperty(SINGLE_PRODUCT_LICENSE_FORMAT_PROPERTY,
                DEFAULT_SINGLE_PRODUCT_LICENSE_FORMAT);
        setProperty(OVERALL_LICENSE_FORMAT_PROPERTY, 
                DEFAULT_OVERALL_LICENSE_FORMAT);
    }
    
    @Override
    public boolean canExecuteForward() {
        List <Product> products = Registry.getInstance().getProductsToInstall();
        
        boolean doShowPanel = true;
        if (products.size() > 0) {
            doShowPanel = System.getProperty(OVERALL_LICENSE_RESOURCE_PROPERTY) != null;
            if (!doShowPanel) {
                for (Product p : products) {
                    if (p.isLogicDownloaded()) {
                        try {
                            if (p.getLogic().getLicense() != null) {
                                doShowPanel = true;
                            }
                        } catch (InitializationException e) {
                        }
                    } else {
                        doShowPanel = true;
                    }
                }
            }
        }
        return products.size()  > 0 && doShowPanel;
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
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class LicensesPanelUi extends WizardPanelUi {
        protected LicensesPanel component;
        
        public LicensesPanelUi(LicensesPanel component) {
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
        protected LicensesPanel component;
        
        private List<Product> acceptedProducts;
        
        private NbiTextPane licensePane;
        private NbiScrollPane licenseScrollPane;
        
        private NbiCheckBox acceptCheckBox;
        
        public LicensesPanelSwingUi(
                final LicensesPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            this.acceptedProducts = new LinkedList<Product>();
            
            initComponents();
        }
        
        @Override
        public JComponent getDefaultFocusOwner() {
            return acceptCheckBox;
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            acceptCheckBox.setText(
                    component.getProperty(ACCEPT_CHECKBOX_TEXT_PROPERTY));
            
            final List<Product> currentProducts =
                    Registry.getInstance().getProductsToInstall();
            
            final StringBuilder text = new StringBuilder();
            
            boolean everythingAccepted = true;
            
            if(System.getProperty(OVERALL_LICENSE_RESOURCE_PROPERTY)!=null) {
                if(acceptedProducts.size()==0) {
                    everythingAccepted = false;
                }
                final String licenseValue = SystemUtils.resolveString(
                        System.getProperty(OVERALL_LICENSE_RESOURCE_PROPERTY));
                final String license = SystemUtils.resolveString("$R{" + licenseValue + ";" + StringUtils.ENCODING_UTF8 + "}");
                final String format = component.getProperty(OVERALL_LICENSE_FORMAT_PROPERTY);
                if(license!=null) {
                    text.append(StringUtils.format(format, license));
                }
            } else {
                final String format = (currentProducts.size() == 1) ? 
                    component.getProperty(SINGLE_PRODUCT_LICENSE_FORMAT_PROPERTY) :
                    component.getProperty(APPEND_LICENSE_FORMAT_PROPERTY);
                
                for (Product product: currentProducts) {
                    if (!acceptedProducts.contains(product)) {
                        everythingAccepted = false;
                    }
                    try {
                        Text license = product.getLogic().getLicense();
                        if(license!=null && license.getText()!=null) {
                            text.append(
                                    StringUtils.format(format, 
                                    product.getDisplayName(),
                                    license.getText()));                            
                        }
                    } catch (InitializationException e) {
                        ErrorManager.notifyError(
                                component.getProperty(ERROR_CANNOT_GET_LOGIC_PROPERTY),
                                e);
                    }                    
                }
            }
            if(System.getProperty(OVERALL_LICENSE_CONTENT_TYPE_PROPERTY)!=null) {
                licensePane.setContentType(System.getProperty(
                        OVERALL_LICENSE_CONTENT_TYPE_PROPERTY));
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
                    } else if (code == KeyEvent.VK_N || code == KeyEvent.VK_Q) {
                        container.getCancelButton().doClick();
                    } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_Y) {
                        if (acceptCheckBox.isEnabled()) {
                            acceptCheckBox.setSelected(true);
                            acceptCheckBoxToggled();
                        }
                    }
                }
            });

            if (System.getProperty(WHOLE_LICENSE_SCROLLING_REQUIRED) != null) {
                licenseScrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        JScrollBar vsb = licenseScrollPane.getVerticalScrollBar();
                        if (vsb.getValue() >= vsb.getMaximum() - vsb.getModel().getExtent()) {
                            acceptCheckBox.setEnabled(true);
                        }
                    }
                });
                acceptCheckBox.setEnabled(false);
            }
            
            if (!everythingAccepted) {
                acceptCheckBox.setSelected(false);
            }
            acceptCheckBoxToggled();
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // licensePane //////////////////////////////////////////////////////////
            licensePane = new NbiTextPane();
            licensePane.setOpaque(true);
            licensePane.setBackground(Color.WHITE);
            licensePane.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
            licensePane.setFocusable(true);

            // licenseScrollPane ////////////////////////////////////////////////////
            licenseScrollPane = new NbiScrollPane(licensePane);
            
            // acceptCheckBox ///////////////////////////////////////////////////////
            acceptCheckBox = new NbiCheckBox();
            acceptCheckBox.setSelected(false);
            acceptCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    acceptCheckBoxToggled();
                }
            });
            
            // this /////////////////////////////////////////////////////////////////
            add(licenseScrollPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(acceptCheckBox, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(7, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void acceptCheckBoxToggled() {
            if (acceptCheckBox.isSelected()) {
                for (Product product: Registry.
                        getInstance().getProductsToInstall()) {
                    if (!acceptedProducts.contains(product)) {
                        acceptedProducts.add(product);
                    }
                }
                
                container.getNextButton().setEnabled(true);
            } else {
                container.getNextButton().setEnabled(false);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ACCEPT_CHECKBOX_TEXT_PROPERTY =
            "accept.checkbox.text"; // NOI18N
    public static final String ERROR_CANNOT_GET_LOGIC_PROPERTY =
            "error.cannot.get.logic";//NOI18N
    public static final String OVERALL_LICENSE_RESOURCE_PROPERTY =
            "nbi.overall.license.resource";//NOI18N
    public static final String OVERALL_LICENSE_CONTENT_TYPE_PROPERTY =
            "nbi.overall.license.content.type";//NOI18N
    public static final String APPEND_LICENSE_FORMAT_PROPERTY =
            "append.license.format";//NOI18N
    public static final String OVERALL_LICENSE_FORMAT_PROPERTY =
            "overall.license.format";//NOI18N
    public static final String SINGLE_PRODUCT_LICENSE_FORMAT_PROPERTY =
            "single.product.license.format";//NOI18N
    private static final String WHOLE_LICENSE_SCROLLING_REQUIRED =
            "nbi.whole.license.scrolling.required";
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.description"); // NOI18N
    public static final String DEFAULT_APPEND_LICENSE_FORMAT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.append.license.format"); // NOI18N
    public static final String DEFAULT_OVERALL_LICENSE_FORMAT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.overall.license.format"); // NOI18N
    public static final String DEFAULT_SINGLE_PRODUCT_LICENSE_FORMAT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.single.product.license.format"); // NOI18N
    public static final String DEFAULT_ACCEPT_CHECKBOX_TEXT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.accept.checkbox.text"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_GET_LOGIC =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.error.cannot.get.logic"); // NOI18N
}
