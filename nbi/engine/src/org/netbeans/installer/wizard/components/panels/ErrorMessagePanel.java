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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public class ErrorMessagePanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ErrorMessagePanel() {
        setProperty(CANCEL_DIALOG_MESSAGE_PROPERTY, DEFAULT_CANCEL_DIALOG_MESSAGE_TEXT);
        setProperty(CANCEL_DIALOG_TITLE_PROPERTY, DEFAULT_CANCEL_DIALOG_TITLE_TEXT);
        setProperty(ERROR_FAILED_VERIFY_INPUT_PROPERTY,DEFAULT_ERROR_FAILED_VERIFY_INPUT_TEXT);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new ErrorMessagePanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ErrorMessagePanelUi extends WizardPanelUi {
        protected ErrorMessagePanel        component;
        
        public ErrorMessagePanelUi(ErrorMessagePanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new ErrorMessagePanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class ErrorMessagePanelSwingUi extends WizardPanelSwingUi {
        /////////////////////////////////////////////////////////////////////////////
        // Constants
        public static final String ERROR_ICON =
                "org/netbeans/installer/wizard/components/panels/error.png"; // NOI18N
        public static final String WARNING_ICON =
                "org/netbeans/installer/wizard/components/panels/warning.png"; // NOI18N
        public static final String INFO_ICON =
                "org/netbeans/installer/wizard/components/panels/info.png"; // NOI18N
        public static final String EMPTY_ICON =
                "org/netbeans/installer/wizard/components/panels/empty.png"; // NOI18N
        
        public static final Color ERROR_COLOR = 
                Color.BLACK;
        public static final Color WARNING_COLOR = 
                Color.BLACK;
        public static final Color INFO_COLOR = 
                Color.BLACK;
        public static final Color EMPTY_COLOR = 
                Color.BLACK;
        
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        protected ErrorMessagePanel component;
        
        private Icon errorIcon;
        private Icon warningIcon;
        private Icon infoIcon;
        private Icon emptyIcon;
        
        private Color errorColor;
        private Color warningColor;
        private Color infoColor;
        private Color emptyColor;
        
        private NbiLabel errorLabel;
        
        private ValidatingThread validatingThread;
        
        public ErrorMessagePanelSwingUi(
                final ErrorMessagePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            errorIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(ERROR_ICON));
            warningIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(WARNING_ICON));
            infoIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(INFO_ICON));
            emptyIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(EMPTY_ICON));
            
            errorColor = 
                    ERROR_COLOR;
            warningColor = 
                    WARNING_COLOR;
            infoColor = 
                    INFO_COLOR;
            emptyColor = 
                    EMPTY_COLOR;
            
            initComponents();
        }
        
        @Override
        public void evaluateBackButtonClick() {
            if (validatingThread != null) {
                validatingThread.finish();
                validatingThread = null;
            }
            
            super.evaluateBackButtonClick();
        }
        
        @Override
        public void evaluateNextButtonClick() {
            if (validatingThread != null) {
                validatingThread.pause();
            }
            
            final String errorMessage = validateInput();
            
            if (errorMessage == null) {
                saveInput();
                if(validatingThread != null) {
                    validatingThread.finish();
                    validatingThread = null;
                }
                component.getWizard().next();
            } else {
                ErrorManager.notifyError(errorMessage);
                if (validatingThread != null) {
                    validatingThread.play();
                }
            }
        }
        
        @Override
        public void evaluateCancelButtonClick() {
            if (validatingThread != null) {
                validatingThread.pause();
            }
            
            if (!UiUtils.showYesNoDialog(
                    component.getProperty(CANCEL_DIALOG_TITLE_PROPERTY),
                    component.getProperty(CANCEL_DIALOG_MESSAGE_PROPERTY))) {
                if (validatingThread != null) {
                    validatingThread.play();
                }
                return;
            }
            if (validatingThread != null) {
                validatingThread.finish();
                validatingThread = null;
            }
            component.getWizard().getFinishHandler().cancel();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            updateErrorMessage();
            
            if (validatingThread == null) {
                validatingThread = new ValidatingThread(this, component);
                validatingThread.start();
            } else {
                validatingThread.play();
            }
        }
        
        protected String getWarningMessage() {                 
            return null;
        }
        
        protected String getInformationalMessage() {
            return null;
        }
        
        protected final synchronized void updateErrorMessage() {
            String message;
            
            try {
                message = validateInput();
                if (message != null) {
                    errorLabel.setIcon(errorIcon);
                    errorLabel.setText(message);
                    errorLabel.setForeground(errorColor);
                    container.getNextButton().setEnabled(false);
                    
                    return;
                }
                
                message = getWarningMessage();
                if (message != null) {
                    errorLabel.setIcon(warningIcon);
                    errorLabel.setText(message);
                    errorLabel.setForeground(warningColor);
                    container.getNextButton().setEnabled(true);
                    
                    return;
                }
                
                message = getInformationalMessage();
                if (message != null) {
                    errorLabel.setIcon(infoIcon);
                    errorLabel.setText(message);
                    errorLabel.setForeground(infoColor);
                    container.getNextButton().setEnabled(true);
                    
                    return;
                }
                
                errorLabel.setIcon(emptyIcon);
                errorLabel.clearText();
                errorLabel.setForeground(emptyColor);
                container.getNextButton().setEnabled(true);
            } catch (Exception e) {
                // we have a good reason to catch Exception here, as most of the
                // code that is called is not under the engine's control
                // (validateInput() is component-specific) and we do not want to
                // propagate unexpected exceptions that could otherwise be handled
                // normally
                
                ErrorManager.notifyError(
                        component.getProperty(ERROR_FAILED_VERIFY_INPUT_PROPERTY), e);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // errorLabel ///////////////////////////////////////////////////////////
            errorLabel = new NbiLabel();

            // this /////////////////////////////////////////////////////////////////
            add(errorLabel, new GridBagConstraints(
                    0, 99,                             // x, y
                    99, 1,                             // width, height
                    1.0, 0.0,                          // weight-x, weight-y
                    GridBagConstraints.CENTER,         // anchor
                    GridBagConstraints.HORIZONTAL,     // fill
                    new Insets(11, 11, 11, 11),        // padding
                    0, 0));                            // ??? (padx, pady)
        }
        
        /////////////////////////////////////////////////////////////////////////////
        // Inner Classes
        public static class ValidatingThread extends NbiThread {
            /////////////////////////////////////////////////////////////////////////
            // Instance
            private ErrorMessagePanelSwingUi swingUi;
            private boolean paused;
            private boolean stopped;
            
            public ValidatingThread(final ErrorMessagePanelSwingUi swingUi, WizardPanel panel) {
                super();

                this.swingUi = swingUi;
                this.paused = false;
                this.stopped = false;
            }
            
            public void run() {
                while (true) {
                    if(stopped) {
                        break;
                    }
                    if (!paused) {
                        swingUi.updateErrorMessage();
                    }
                    if(stopped) {
                        break;
                    }
                    try {
                        sleep(VALIDATION_DELAY);
                    } catch (InterruptedException e) {
                        ErrorManager.notifyDebug("Interrupted", e);
                    }
                }
            }
            
            public void pause() {
                paused = true;
            }
            
            public void finish() {
                paused = true;
                stopped = true;
            }
            
            public void play() {
                paused = false;
                stopped = false;
            }
            
            /////////////////////////////////////////////////////////////////////////
            // Constants
            public static final long VALIDATION_DELAY = 2000;
        }
        
        public static class ValidatingDocumentListener implements DocumentListener {
            /////////////////////////////////////////////////////////////////////////
            // Instance
            private ErrorMessagePanelSwingUi swingUi;
            
            public ValidatingDocumentListener(ErrorMessagePanelSwingUi swingUi) {
                this.swingUi = swingUi;
            }
            
            public void insertUpdate(DocumentEvent event) {
                swingUi.updateErrorMessage();
            }
            
            public void removeUpdate(DocumentEvent event) {
                swingUi.updateErrorMessage();
            }
            
            public void changedUpdate(DocumentEvent event) {
                swingUi.updateErrorMessage();
            }
        }
    }
    
    public static final String CANCEL_DIALOG_MESSAGE_PROPERTY =     
            "cancel.dialog.message";//NOI18N
    public static final String CANCEL_DIALOG_TITLE_PROPERTY = 
            "cancel.dialog.title";//NOI18N
    public static final String ERROR_FAILED_VERIFY_INPUT_PROPERTY = 
            "error.failed.verify.input";//NOI18N
    
    
    public static final String DEFAULT_CANCEL_DIALOG_MESSAGE_TEXT = 
            ResourceUtils.getString(ErrorMessagePanel.class,
            "EMP.cancel.dialog.message");//NOI18N
    public static final String DEFAULT_CANCEL_DIALOG_TITLE_TEXT = 
            ResourceUtils.getString(ErrorMessagePanel.class,
            "EMP.cancel.dialog.title");//NOI18N
    public static final String DEFAULT_ERROR_FAILED_VERIFY_INPUT_TEXT =
            ResourceUtils.getString(ErrorMessagePanel.class,
            "EMP.error.failed.input.verify");//NOI18N
}
