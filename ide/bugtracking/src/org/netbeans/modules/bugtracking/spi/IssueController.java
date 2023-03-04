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

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;

/**
 * Provides access to an Issue's UI.
 * 
 * <p>
 * Every Issue is expected to provide at least some visual component. 
 * Typically this would be an Issue editor making it possible to create and 
 * modify issues.
 * </p>
 * 
 * <p>
 * When viewing, creating or editing a new Issue, the UI is presented in an 
 * TopComponent in the editor area. Fire <code>PROP_CHANGED</code> to notify the Issue 
 * TopComponent that the UI state changed, {@link #isChanged()} will be called 
 * accordingly to determine if the IDE-s general SaveAction should be enabled. 
 * On save or TopComponent close are then the <code>saveChanges()</code> 
 * and <code>discardUnsavedChanges()</code> 
 * methods called accordingly.
 * </p>
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public interface IssueController {

    /**
     * Fired when the data presented in the Issue UI were changed by the user.
     */
    public static String PROP_CHANGED = "bugtracking.changed";
    
    /**
     * Returns a visual Issue component.
     * 
     * @return a visual component representing an Issue
     * @since 1.85
     */
    public JComponent getComponent();

    /**
     * Returns the help context associated with this controllers visual component.
     * 
     * @return a HelpCtx
     * @since 1.85
     */
    public HelpCtx getHelpCtx();

    /**
     * Called when the component returned by this controller was opened.
     * 
     * @since 1.85
     */
    public void opened();

    /**
     * Called when the component returned by this controller was closed.
     * 
     * @since 1.85
     */
    public void closed();

    /**
     * This method is called when the general IDE Save button is pressed or when 
     * Save was chosen on close of an Issue TopComponent.
     * 
     * @return <code>true</code> in case the save worked, otherwise <code>false</code>
     * 
     * @since 1.85
     */
    public boolean saveChanges();

    /**
     * This method is called when Discard was chosen on close of an Issue TopComponent.
     * 
     * @return <code>true</code> in case the discard worked, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean discardUnsavedChanges();

    /**
     * Determines whether the state of the UI has changed and is supposed to be saved.
     * 
     * @return <code>true</code> in case there are changes to be saved, otherwise <code>false</code>
     */
    public boolean isChanged();
    
    /**
     * Registers a PropertyChangeListener.
     * 
     * @param l a PropertyChangeListener
     * @since 1.85
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Unregisters a PropertyChangeListener.
     * 
     * @param l a PropertyChangeListener
     * @since 1.85
     */
    public void removePropertyChangeListener(PropertyChangeListener l);    
}
