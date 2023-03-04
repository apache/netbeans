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

package org.netbeans.core.options.keymap.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.core.options.keymap.api.ShortcutAction;

/**
 * The SPI class allowing to plug in implementations of shortcuts storage.
 * The instances of this class are registered in the global lookup
 * (Lookup.getDefault()).
 * 
 * The class can implement also the keyboard shortcuts profiles manipulation
 * (switching of the profiles, saving of the profiles etc.)
 * 
 * @author David Strupl
 */
public abstract class KeymapManager {

    /** My instance name */
    private String name;

    /**
     * Every instance is represented by an unique name.
     * @param name 
     */
    protected KeymapManager(String name) {
        this.name = name;
    }

    /**
     * This method should return the currently running shortcuts action
     * assignment.
     * @return 
     */
    public abstract Map<String, Set<ShortcutAction>> getActions();

    /**
     * Refreshes the current keymap model by reading the assignments stored
     * in the persistent storage.
     */
    public abstract void refreshActions();

    /**
     * Retrieves the action shortcuts assignments for given profile.
     * @param profileName The name of the profile to get shortcuts for.
     * @return Shortcuts for given profile.
     */
    public abstract Map<ShortcutAction, Set<String>> getKeymap(String profileName);

    /**
     * Retrieves the default action shortcuts assignments for given profile.
     * @param profileName The name of the profile to get shortcuts for.
     * @return Default shortcuts for given profile.
     */
    public abstract Map<ShortcutAction, Set<String>> getDefaultKeymap(String profileName);
    
    /**
     * Saves the given action shortcuts assignment under given profile name.
     * @param profileName 
     * @param actionToShortcuts 
     */
    public abstract void saveKeymap(String profileName,
            Map<ShortcutAction, Set<String>> actionToShortcuts);
    
    /**
     * Lists all profiles known to this KeymapManager.
     * @return the existing profile names.
     */
    public abstract List<String> getProfiles();

    public String getProfileDisplayName(String profileName) {
        return profileName;
    }
    
    /**
     * @return Currently active profile.
     */
    public abstract String getCurrentProfile();
    
    /**
     * Allows switching of the profiles.
     * @param profileName 
     */
    public abstract void setCurrentProfile(String profileName);
    
    /**
     * Deletes the given profile.
     * @param profileName 
     */
    public abstract void deleteProfile(String profileName);
 
    /**
     * The profile can be either default or custom.
     * @param profileName 
     * @return 
     */
    public abstract boolean isCustomProfile(String profileName);
    
    /**
     * @return this instance name (should be unique amongst all registered
     *      instances.
     */
    public final String getName() {
        return name;
    }
    
    /**
     * This mixin interface should be implemented by those KeymapManagers, which
     * support rollback of user settings. If a KeymapManager does not support a rollback,
     * the core keymap management will perform a profile revert by setting defaults
     * reported by the keymap manager back to it.
     * <p/>
     * Revert operation is a different from set-to-default-value in that the user
     * setting can be removed completely.
     */
    public interface WithRevert  {
        /**
         * Reverts action key bindings. The method should revert user
         * modifications written previously by saveKeymap() for the profile.
         * 
         * @param profile profile that should be changed
         * @param action actions to revert
         * @throws IOException if revert operation fails
         */
        public void revertActions(String profile, Collection<ShortcutAction> action) throws IOException;
        
        /**
         * Reverts profile to the default (shipped) state. The method only
         * applies to built-in profiles. User-defined profiles cannot be
         * reverted, just deleted.
         * 
         * @param profileName profile names to revert or delete
         * @throws IOException if revert fails
         */
        public void revertProfile(String profileName) throws IOException;
    }
}
