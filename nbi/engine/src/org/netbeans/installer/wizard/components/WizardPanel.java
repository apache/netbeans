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

package org.netbeans.installer.wizard.components;

import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This class is a specialization of the {@link WizardComponent} which defines 
 * behavior specific to panels.
 * 
 * <p>
 * A panel is the most typical wizard component. It's behavioral capabilities are 
 * extremely limited and hence the only thing that a anel can do is to display a 
 * bunch of input fields requiing to enter some data. On the other hand a panel can 
 * be used to inform the user about an event and require him to perform some action 
 * before the wizard proceeds.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public abstract class WizardPanel extends WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * UI of the panel.
     */
    protected WizardUi wizardUi;
    
    /**
     * Creates a new instance of {@link WizardPanel}. This is the default 
     * <code>protected</code> constructor which must be called by the concrete
     * implementations.
     */
    protected WizardPanel() {
        // does nothing
    }
    
    /**
     * Executes the panel when it is read via a call to 
     * {@link org.netbeans.installer.wizard.Wizard#next()}. When the wizard is run 
     * in a GUI mode, no action is taken as all the functionality of a panel is 
     * contained in its UI. When the wizard is run in a silent mode, running this 
     * method automatically calls 
     * {@link org.netbeans.installer.wizard.Wizard#next()}.
     * 
     * @see WizardComponent#executeForward()
     */
    public final void executeForward() {
        // since silent mode does not assume any user interaction we just move 
        // forward
        if (UiMode.getCurrentUiMode() == UiMode.SILENT) {
            getWizard().next();
        }
    }
    
    /**
     * Executes the panel when it is read via a call to 
     * {@link org.netbeans.installer.wizard.Wizard#previous()}. When the wizard is 
     * run in a GUI mode, no action is taken as all the functionality of a panel is 
     * contained in its UI.
     * 
     * @see WizardComponent#executeForward()
     */
    public final void executeBackward() {
        // does nothing
    }
    
    /**
     * The default implementation of this method for {@link WizardPanel} has an 
     * empty body. Concrete implementations are expected to override this method
     * if they require any custom initialization.
     * 
     * @see WizardComponent#initialize()
     */
    public void initialize() {
        // does nothing
    }
    
    /**
     * {@inheritDoc}
     */
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WizardPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * Implementation of the {@link WizardUi} for {@link WizardPanel}.
     * 
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardPanelUi extends WizardComponentUi {
        /**
         * Current {@link WizardPanel} for this UI.
         */
        protected WizardPanel panel;
        
        /**
         * Creates a new instance of {@link WizardPanelUi}, initializing it with
         * the specified instance of {@link WizardPanel}.
         * 
         * @param panel Instance of {@link WizardPanel} which should be used 
         *      by this UI.
         */
        public WizardPanelUi(final WizardPanel panel) {
            super(panel);
            
            this.panel = panel;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public SwingUi getSwingUi(final SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WizardPanelSwingUi(panel, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    /**
     * Implementation of {@link SwingUi} for {@link WizardPanel}.
     * 
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardPanelSwingUi extends WizardComponentSwingUi {
        /**
         * Current {@link WizardPanel} for this UI.
         */
        protected WizardPanel panel;
        
        /**
         * Creates a new instance of {@link WizardPanelSwingUi}, initializing it 
         * with the specified instances of {@link WizardPanel} and 
         * {@link SwingContainer}.
         * 
         * @param panel Instance of {@link WizardPanel} which should be used 
         *      by this UI.
         * @param container Instance of {@link SwingContainer} which should be used 
         *      by this UI.
         */
        public WizardPanelSwingUi(
                final WizardPanel panel,
                final SwingContainer container) {
            super(panel, container);
            
            this.panel = panel;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void evaluateBackButtonClick() {
            if (validateInput() == null) {
                saveInput();
            }
            
            component.getWizard().previous();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void evaluateNextButtonClick() {
            String errorMessage = validateInput();
            
            if (errorMessage == null) {
                saveInput();
                component.getWizard().next();
            } else {
                ErrorManager.notifyError(errorMessage);
            }
        }
        
        // protected ////////////////////////////////////////////////////////////////
        /**
         * Saves the user input to the wizard's property container. This method does
         * not perform any additional validation of the input as it will be
         * validated prior to calling this method - in {@link #validateInput()}
         */
        protected void saveInput() {
            // does nothing
        }
        
        /**
         * Validates the user input. This method performs the validatation of the
         * data input by the user on this panel. It either passed the input data to
         * the component for validation, or takes the risk and validates the data
         * itself.
         *
         * @return Error message which describes what is incorrect with the user
         *      input, or <code>null</code> if the user input is correct.
         */
        protected String validateInput() {
            return null; // null means that everything is OK
        }
    }
}
