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


package org.netbeans.spi.palette;

import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * <p>An interface implemented by palette clients to provide custom actions
 * for popup menus and actions for import of new items.</p>
 *
 * @author S. Aubrecht.
 */
public abstract class PaletteActions {

    /**
     * @return An array of action that will be used to construct buttons for import
     * of new palette item in palette manager window.
     *
     */
    public abstract Action[] getImportActions();
    
    /**
     * @return Custom actions to be added to the top of palette's default popup menu.
     */
    public abstract Action[] getCustomPaletteActions();
    
    /**
     * @param category Lookup representing palette's category.
     *
     * @return Custom actions to be added to the top of default popup menu for the given category.
     */
    public abstract Action[] getCustomCategoryActions( Lookup category );
    
    /**
     * @param item Lookup representing palette's item.
     *
     * @return Custom actions to be added to the top of the default popup menu for the given palette item.
     */
    public abstract Action[] getCustomItemActions( Lookup item );
    
    
    /**
     * @param item Lookup representing palette's item.
     *
     * @return An action to be invoked when user double-clicks the item in the
     * palette (e.g. insert item at editor's default location).
     * Return null to disable preferred action for this item.
     */
    public abstract Action getPreferredAction( Lookup item );
    
    /**
     * An action that will be invoked as part of the palette refresh logic,
     * for example when user chooses "Refresh" in palette's popup menu. Can be null.
     * The action properties (label, icon) are not displayed to the user, the Palette module
     * will provide its own.
     * @return Custom refresh action or null.
     * @since 1.9
     */
    public Action getRefreshAction() {
        return null;
    }
    
    /**
     * An action that resets the palette content to its default state. The action can be 
     * invoked by the user from palette's popup menu for from the Palette Manager window.
     * The action properties (label, icon) are not displayed to the user, the Palette module
     * provides its own.
     * @return Custom reset action or null to use the default one that removes all user's 
     * modifications to the XML layer files.
     * @since 1.11
     */
    public Action getResetAction() {
        return null;
    }
}
