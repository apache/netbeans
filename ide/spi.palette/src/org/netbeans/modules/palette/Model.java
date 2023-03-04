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


package org.netbeans.modules.palette;

import javax.swing.Action;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.*;

/**
 * An interface for palette contents.
 *
 * @author S. Aubrecht
 */
public interface Model {

    /**
     * Interested parties should listent to changes of this property to be
     * notified when the selected item has changed.
     */
    public static final String PROP_SELECTED_ITEM = "selectedItem";

    String getName();

    /**
     * @return Palette categories.
     */
    Category[] getCategories();
            
    /**
     * @return Actions for palette's popup menu.
     */
    Action[] getActions();
    
    void addModelListener( ModelListener listener );
    
    void removeModelListener( ModelListener listener );
    
    /**
     * @return The item currently selected in the palette or null if no item is selected.
     */
    Item getSelectedItem();
    
    /**
     * @return The category that owns the currently selected item.
     */
    Category getSelectedCategory();
    
    /**
     * Select new item and category.
     *
     * @param category New category to be selected or null.
     * @param item New item to be selected or null.
     */
    void setSelectedItem( Lookup category, Lookup item );
    
    /**
     * Ensure no item is selected.
     */
    void clearSelection();
    
    void refresh();
    
    void showCustomizer( PaletteController controller, Settings settings );
    
    Lookup getRoot();
    
    boolean moveCategory( Category source, Category target, boolean moveBefore );
    
    boolean canReorderCategories();
}
