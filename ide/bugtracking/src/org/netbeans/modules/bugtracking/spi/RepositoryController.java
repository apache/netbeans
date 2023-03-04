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

package org.netbeans.modules.bugtracking.spi;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.util.HelpCtx;

/**
 * Provides access to an Repository UI.
 * <p>
 * Every Repository is expected to provide an UI to create or change its attributes. 
 * </p>
 * <p>
 * When creating or editing a new Repository instance, the UI is presented in a modal dialog 
 * with the necessary Save button to trigger the {@link #applyChanges()} method.
 * </p>
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public interface RepositoryController {

    /**
     * Returns a visual component representing the repository this controller is meant for.
     * 
     * @return a visual component representing a repository
     * @since 1.85
     */
    public JComponent getComponent();

    /**
     * Returns the help context associated with this controllers visual component.
     * @return a help context
     * @since 1.85
     */
    public HelpCtx getHelpCtx();

    /**
     * Determines whether the data in this controllers visual component are valid or not.
     * @return <code>true</code> in case the the data are valid, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean isValid();

    /**
     * Called when the Repository UI is about to be opened. 
     * Populate the controllers component.
     * @since 1.85
     */
    public void populate();
    
    /**
     * In case the controller isn't valid, then return an error message to be 
     * shown in Repository dialog.
     * 
     * @return error message
     * @since 1.85
     */
    public String getErrorMessage();
    
    /**
     * Is called when the "Save" button was pressed.
     */
    public void applyChanges(); 
    
    /**
     * Is called when the "Cancel" button was pressed.
     * @since 1.85
     */
    public void cancelChanges(); 

    /**
     * Registers a ChangeListener.
     * 
     * @param l a ChangeListener
     * @since 1.85
     */
    public void addChangeListener(ChangeListener l);

    /**
     * Unregisters a ChangeListener.
     * 
     * @param l a ChangeListener
     * @since 1.85
     */
    public void removeChangeListener(ChangeListener l);
    
}
