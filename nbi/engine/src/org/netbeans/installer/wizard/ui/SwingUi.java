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

package org.netbeans.installer.wizard.ui;

import javax.swing.JComponent;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiPanel;

/**
 * This class represents the UI of a {@link WizardComponent} for the 
 * {@link UiMode#SWING} UI mode.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public abstract class SwingUi extends NbiPanel {
    /**
     * Returns the title of the component. The way the title is displayed is
     * dependent on the container. A frame could expose the title in the windows 
     * heading, for example.
     * 
     * @return Title of the component, or <code>null</code> if the component does 
     *      not have a title.
     */
    public abstract String getTitle();
    
    /**
     * Returns the description of the component. The way the description is 
     * displayed is dependent on the container.
     * 
     * @return Description of the component, or <code>null</code> if the component 
     *      does not have a description.
     */
    public abstract String getDescription();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Help</code> button. The expected behavior would 
     * be to display a help dialog which describes the required user input for the 
     * current component.
     */
    public abstract void evaluateHelpButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Back</code> button. The expected behavior would 
     * be to call the {@link Wizard#previous()} method.
     */
    public abstract void evaluateBackButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Next</code> button. The expected behavior would 
     * be to call the {@link Wizard#next()} method.
     */
    public abstract void evaluateNextButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Cancel</code> button. The expected behavior 
     * would be to cancel the wizard execution.
     */
    public abstract void evaluateCancelButtonClick();
    
    /**
     * Returns the button which should be activated when the user presses the 
     * <code>Enter</code> key.
     * 
     * @return Default handler for the <code>Enter</code> key.
     */
    public abstract NbiButton getDefaultEnterButton();
    
    /**
     * Returns the button which should be activated when the user presses the 
     * <code>Escape</code> key.
     * 
     * @return Default handler for the <code>Escape</code> key.
     */
    public abstract NbiButton getDefaultEscapeButton();
    
    /**
     * Returns the Swing component which should have focus when this UI is shown.
     * 
     * @return Default focus owner for this UI.
     */
    public abstract JComponent getDefaultFocusOwner();
}
