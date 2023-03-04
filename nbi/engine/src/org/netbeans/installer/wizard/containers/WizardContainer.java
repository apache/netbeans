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

package org.netbeans.installer.wizard.containers;

import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This interface represents the container for the UI of a {@link WizardComponent}.
 * Each {@link Wizard} "owns" an instance of this class and uses it to initialize 
 * the UI of its active component.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public interface WizardContainer {
    /**
     * Shows or hides the container. The behavior of this method is 
     * component-specific. A frame would probably map this method directly, while
     * a console-mode container could draw itself or clear the screen.
     * 
     * @param visible Whether to show the container - <code>true</code>, or hide 
     * it - <code>false</code>.
     */
    void setVisible(final boolean visible);
    
    /**
     * Updates the container with a new UI. This method is usually called by the 
     * wizard when the active component changes - the wizard wants to display its 
     * UI.
     * 
     * @param ui UI which needs to be shown.
     */
    void updateWizardUi(final WizardUi ui);
    
    /**
     * Opens(creates) the container. This method is usually called by the wizard upon 
     * container initialization
     *      
     */
    void open();    
    
    /**
     * Closes(destroyes) the container. This method is usually called by the wizard upon 
     * container closing
     *      
     */
    void close();
    
}
