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
package org.mycompany.installer.wizard.components.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

public class WelcomePanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    private Registry bundledRegistry;
    private Registry defaultRegistry;

    public WelcomePanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);

        setProperty(WELCOME_TEXT_PROPERTY,
                DEFAULT_WELCOME_TEXT);

        setProperty(WELCOME_ALREADY_INSTALLED_TEXT_PROPERTY,
                DEFAULT_WELCOME_ALREADY_INSTALLED_TEXT);
        setProperty(WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT_PROPERTY,
                DEFAULT_WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT);

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

    Registry getBundledRegistry() {
        return bundledRegistry;
    }

    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WelcomePanelUi(this);
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

        // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        return bundledRegistry.getNodes().size() > 1;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WelcomePanelUi extends ErrorMessagePanelUi {

        protected WelcomePanel component;

        public WelcomePanelUi(WelcomePanel component) {
            super(component);

            this.component = component;
        }

        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WelcomePanelSwingUi(component, container);
            }

            return super.getSwingUi(container);
        }
    }

    public static class WelcomePanelSwingUi extends ErrorMessagePanelSwingUi {

        protected WelcomePanel panel;
        private NbiTextPane textPane;

        private NbiPanel leftImagePanel;
        ValidatingThread validatingThread;

        public WelcomePanelSwingUi(
                final WelcomePanel component,
                final SwingContainer container) {
            super(component, container);

            this.panel = component;

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

            textPane.setContentType("text/html");
            textPane.setText(StringUtils.format(panel.getProperty(WELCOME_TEXT_PROPERTY)));
            List<Product> toInstall = Registry.getInstance().getProductsToInstall();
            if(toInstall.isEmpty()) {
                List <Product> list = panel.getBundledRegistry().getProducts();
                if(list.size() == 1) {
                    if(SystemUtils.getCurrentPlatform().isCompatibleWith(list.get(0).getPlatforms())) {
                        File installationLocation = list.get(0).getInstallationLocation();
                        textPane.setText(
                            StringUtils.format(
                            panel.getProperty(WELCOME_ALREADY_INSTALLED_TEXT_PROPERTY),
                            list.get(0).getDisplayName(),
                            installationLocation.getAbsolutePath()));
                    } else {
                        textPane.setText(
                            StringUtils.format(
                            WELCOME_INCOMPATIBLE_PLATFORM_TEXT,
                            list.get(0).getDisplayName()));
                    }
                    container.getCancelButton().setVisible(false);
                    container.getNextButton().setText(panel.getProperty(
                            WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT_PROPERTY));
                }
            }            

            super.initialize();
        }

        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // textPane /////////////////////////////////////////////////////////////
            textPane = new NbiTextPane();

            leftImagePanel = new NbiPanel();
            int width = 0;
            int height = 0;
            final String topLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY));
            final String bottomLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY));

            int bottomAnchor = NbiPanel.ANCHOR_BOTTOM_LEFT;

            if (topLeftImage != null) {
                leftImagePanel.setBackgroundImage(topLeftImage, ANCHOR_TOP_LEFT);
                width = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconWidth();
                height += leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconHeight();
            }
            if (bottomLeftImage != null) {
                leftImagePanel.setBackgroundImage(bottomLeftImage, bottomAnchor);
                width = leftImagePanel.getBackgroundImage(bottomAnchor).getIconWidth();
                height += leftImagePanel.getBackgroundImage(bottomAnchor).getIconHeight();
            }

            leftImagePanel.setPreferredSize(new Dimension(width, height));
            leftImagePanel.setMaximumSize(new Dimension(width, height));
            leftImagePanel.setMinimumSize(new Dimension(width, 0));
            leftImagePanel.setSize(new Dimension(width, height));

            leftImagePanel.setOpaque(false);
            // this /////////////////////////////////////////////////////////////////
            int dy = 0;
            add(leftImagePanel, new GridBagConstraints(
                    0, 0, // x, y
                    1, 100, // width, height
                    0.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.WEST, // anchor
                    GridBagConstraints.VERTICAL, // fill
                    new Insets(0, 0, 0, 0), // padding
                    0, 0));                           // padx, pady - ???
            add(textPane, new GridBagConstraints(
                    1, dy++, // x, y
                    4, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(10, 11, 11, 11), // padding
                    0, 0));                           // padx, pady - ???
            
            NbiTextPane separatorPane = new NbiTextPane();

            separatorPane = new NbiTextPane();
            add(separatorPane, new GridBagConstraints(
                    3, dy, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 0, 0, 0), // padding
                    0, 0));                           // padx, pady - ???


            // move error label after the left welcome image
            Component errorLabel = getComponent(0);
            getLayout().removeLayoutComponent(errorLabel);
            add(errorLabel, new GridBagConstraints(
                    1, 99, // x, y
                    99, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(4, 11, 4, 0), // padding
                    0, 0));                            // ??? (padx, pady)


        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.title");
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.description"); // NOI18N
    public static final String WELCOME_TEXT_PROPERTY =
            "welcome.text"; // NOI18N
    public static final String WELCOME_ALREADY_INSTALLED_TEXT_PROPERTY =
            "welcome.already.installed.text"; // NOI18N
    public static final String WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT_PROPERTY =
            "welcome.already.installed.next.button.text";//NOI18N
    public static final String WELCOME_INCOMPATIBLE_PLATFORM_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.incompatible.platform.text");//NOI18N

    public static final String DEFAULT_WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.already.installed.next.button.text");//NOI18N
    
    public static final String DEFAULT_WELCOME_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.welcome.text"); // NOI18N

    public static final String DEFAULT_WELCOME_ALREADY_INSTALLED_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.already.installed.text"); // NOI18N

    public static final String WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.top.image";//NOI18N
    public static final String WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.bottom.image";//NOI18N
}
