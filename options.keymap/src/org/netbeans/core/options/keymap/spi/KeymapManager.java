/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
