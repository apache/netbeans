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
