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

package org.netbeans.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.utils.InstallationLogDialog;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_INSTALL;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_UNINSTALL;

/**
 *
 * @author Kirill Sorokin
 */
public class PostCreateBundleSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public PostCreateBundleSummaryPanel() {
        setProperty(MESSAGE_SUCCESS_TEXT_PROPERTY, DEFAULT_MESSAGE_SUCCESS_TEXT);
        setProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE);
        setProperty(MESSAGE_ERRORS_TEXT_PROPERTY, DEFAULT_MESSAGE_ERRORS_TEXT);
        setProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_TEXT);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE);
        setProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_VIEW_LOG_BUTTON_TEXT);
        setProperty(SEND_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_SEND_LOG_BUTTON_TEXT);
        setProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY, DEFAULT_COMPONENTS_LIST_SEPARATOR);
        
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PostCreateBundleSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PostCreateBundleSummaryPanelUi extends WizardPanelUi {
        protected PostCreateBundleSummaryPanel        component;
        
        public PostCreateBundleSummaryPanelUi(PostCreateBundleSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PostCreateBundleSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class PostCreateBundleSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected PostCreateBundleSummaryPanel component;
        
        private NbiTextPane messagePane;
        
        private NbiLabel successfullyBundledComponentsLabel;
        private NbiTextPane successfullyBundledComponentsPane;
        private NbiLabel componentsFailedToBundleLabel;
        private NbiTextPane componentsFailedToBundlePane;
        
        private NbiButton viewLogButton;
        private NbiButton sendLogButton;
        
        private NbiPanel spacer;
        
        private InstallationLogDialog logDialog;
        
        public PostCreateBundleSummaryPanelSwingUi(
                final PostCreateBundleSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            
            // set up the back button
            container.getBackButton().setEnabled(false);
            
            // set up the next (or finish) button
            container.getNextButton().setText(
                    component.getProperty(FINISH_BUTTON_TEXT_PROPERTY));
            
            // set up the cancel button
            container.getCancelButton().setEnabled(false);
        }
        
        @Override
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            
            final boolean errorsEncountered =
                    registry.getProducts(FAILED_TO_INSTALL).size() > 0 &&
                    registry.getProducts(FAILED_TO_UNINSTALL).size() > 0;
            
            if (errorsEncountered) {
                messagePane.setContentType(component.getProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY));
                messagePane.setText(component.getProperty(MESSAGE_ERRORS_TEXT_PROPERTY));
            } else {
                messagePane.setContentType(component.getProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY));
                messagePane.setText(component.getProperty(MESSAGE_SUCCESS_TEXT_PROPERTY));
            }
            
            List<Product> components;
            
            components = registry.getProducts(INSTALLED_SUCCESSFULLY);
            if (components.size() > 0) {
                successfullyBundledComponentsLabel.setVisible(true);
                successfullyBundledComponentsPane.setVisible(true);
                
                successfullyBundledComponentsLabel.setText(component.getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY));
                successfullyBundledComponentsPane.setContentType(component.getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
                successfullyBundledComponentsPane.setText(StringUtils.format(component.getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY), StringUtils.asString(components, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                successfullyBundledComponentsLabel.setVisible(false);
                successfullyBundledComponentsPane.setVisible(false);
            }
            
            components = registry.getProducts(FAILED_TO_INSTALL);
            if (components.size() > 0) {
                componentsFailedToBundleLabel.setVisible(true);
                componentsFailedToBundlePane.setVisible(true);
                
                componentsFailedToBundleLabel.setText(component.getProperty(COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY));
                componentsFailedToBundlePane.setContentType(component.getProperty(COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY));
                componentsFailedToBundlePane.setText(StringUtils.format(component.getProperty(COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY), StringUtils.asString(components, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                componentsFailedToBundleLabel.setVisible(false);
                componentsFailedToBundlePane.setVisible(false);
            }
            
            viewLogButton.setText(component.getProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY));
            sendLogButton.setText(component.getProperty(SEND_LOG_BUTTON_TEXT_PROPERTY));
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // messagePane //////////////////////////////////////////////////////////
            messagePane = new NbiTextPane();
            
            // successfullyBundledComponentsPane ////////////////////////////////////
            successfullyBundledComponentsPane = new NbiTextPane();
            
            // successfullyBundledComponentsLabel ///////////////////////////////////
            successfullyBundledComponentsLabel = new NbiLabel();
            successfullyBundledComponentsLabel.setLabelFor(
                    successfullyBundledComponentsPane);
            
            // componentsFailedToBundlePane /////////////////////////////////////////
            componentsFailedToBundlePane = new NbiTextPane();
            
            // componentsFailedToBundleLabel ////////////////////////////////////////
            componentsFailedToBundleLabel = new NbiLabel();
            componentsFailedToBundleLabel.setLabelFor(componentsFailedToBundlePane);
            
            // viewLogButton ////////////////////////////////////////////////////////
            viewLogButton = new NbiButton();
            viewLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    viewLogButtonClicked();
                }
            });
            
            // sendLogButton ////////////////////////////////////////////////////////
            sendLogButton = new NbiButton();
            sendLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    sendLogButtonClicked();
                }
            });
            sendLogButton.setEnabled(false);
            
            // spacer ///////////////////////////////////////////////////////////////
            spacer = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(messagePane, new GridBagConstraints(
                    0, 0,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(successfullyBundledComponentsLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(successfullyBundledComponentsPane, new GridBagConstraints(
                    0, 2,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(componentsFailedToBundleLabel, new GridBagConstraints(
                    0, 3,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsFailedToBundlePane, new GridBagConstraints(
                    0, 4,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, 5,                             // x, y
                    2, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(viewLogButton, new GridBagConstraints(
                    0, 6,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(3, 11, 11, 0),         // padding
                    0, 0));                           // padx, pady - ???
            add(sendLogButton, new GridBagConstraints(
                    1, 6,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.WEST,          // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(3, 6, 11, 11),         // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void viewLogButtonClicked() {
            if (LogManager.getLogFile() != null) {
                if (logDialog == null) {
                    logDialog = new InstallationLogDialog();
                }
                logDialog.setVisible(true);
                logDialog.loadLogFile();
            } else {
                ErrorManager.notifyError("Log file is not available.");
            }
        }
        
        private void sendLogButtonClicked() {
            // does nothing
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_SUCCESS_TEXT_PROPERTY =
            "message.success.text"; // NOI18N
    public static final String MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY =
            "message.success.content.type"; // NOI18N
    public static final String MESSAGE_ERRORS_TEXT_PROPERTY =
            "message.errors.text"; // NOI18N
    public static final String MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY =
            "message.errors.content.type"; // NOI18N
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY =
            "successfully.bundled.components.label.text"; // NOI18N
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY =
            "successfully.bundled.components.text"; // NOI18N
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY =
            "successfully.bundled.components.content.type"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY =
            "components.failed.to.bundle.label.text"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY =
            "components.failed.to.bundle.text"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY =
            "components.failed.to.bundle.content.type"; // NOI18N
    public static final String VIEW_LOG_BUTTON_TEXT_PROPERTY =
            "view.log.button.text"; // NOI18N
    public static final String SEND_LOG_BUTTON_TEXT_PROPERTY =
            "send.log.button.text"; // NOI18N
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY =
            "components.list.separator"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_SUCCESS_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.message.success.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.message.success.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_ERRORS_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.message.errors.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.message.errors.content.type"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.successfully.bundled.components.label.text"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.successfully.bundled.components.text"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.successfully.bundled.components.content.type"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.components.failed.to.bundle.label.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.components.failed.to.bundle.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.components.failed.to.bundle.content.type"); // NOI18N
    public static final String DEFAULT_VIEW_LOG_BUTTON_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.view.log.button.text"); // NOI18N
    public static final String DEFAULT_SEND_LOG_BUTTON_TEXT =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.send.log.button.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.components.list.separator"); // NOI18N
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.dialog.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(PostCreateBundleSummaryPanel.class,
            "PoCBSP.dialog.description"); // NOI18N
}
