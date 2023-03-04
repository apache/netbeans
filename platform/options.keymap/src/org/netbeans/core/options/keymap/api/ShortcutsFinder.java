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

package org.netbeans.core.options.keymap.api;

import java.util.Set;

/**
 * Objects implementing this interface serve as providers for storing
 * the shortcuts definitions.
 * 
 * @author David Strupl, Svata Dedic
 */
public interface ShortcutsFinder {
    /**
     * Find a ShortcutAction for a given string representation of the shortcut.
     * @param shortcuts 
     * @return the shortcut
     */
    ShortcutAction findActionForShortcut(String shortcuts);
    
    /**
     * The actions can be assigned IDs when storing them. This method
     * finds a ShortcutAction instance by given id.
     * @param id 
     * @return the shortcut
     */
    
    ShortcutAction findActionForId(String id);
    /**
     * This method will show the shortcut selection dialog to the user.
     * @return The shortcut that the user has selected on <code>null</code>
     *      if the user has cancelled the dialog
     */
    String showShortcutsDialog();
    
    /**
     * Retrieve all the shortcuts assigned to a given action.
     * @param action 
     * @return all the shortcuts for the action.
     */
    String[] getShortcuts(ShortcutAction action);
    
    /**
     * Refreshes the model by reading the stored assignments.
     */
    void refreshActions();
    
    /**
     * Assigns the given shortcuts to the given action.
     * @param action 
     * @param shortcuts 
     * @deprecated use {@link copy} to get a Writer. Method will be removed in NB 7.4
     */
    @Deprecated
    void setShortcuts(ShortcutAction action, Set<String> shortcuts);
    
    /**
     * Applies the changes by storing them to the storage.
     * @deprecated use {@link copy} to get a Writer. Method will be removed in NB 7.4
     */
    @Deprecated
    void apply();
    
    /**
     * Creates a local copy of the global shared Finder. Finders cannot be chained
     * using localCopy, each copy starts from the persistent data state. The local copy
     * is not thread-safe. The finder will come preinitialized, if called on background,
     * can save AWT thread from freezing during action loading and construction.
     * 
     * @return local writable copy of the shortcuts
     */
    Writer localCopy();
    
    public interface Writer extends ShortcutsFinder {
        /**
         * Assigns the given shortcuts to the given action.
         * @param action 
         * @param shortcuts 
         */
        void setShortcuts(ShortcutAction action, Set<String> shortcuts);

        /**
         * Applies the changes by storing them to the storage.
         */
        void apply();
    }
}
