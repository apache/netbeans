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

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * Interface representing palette category.
 *
 * @author S. Aubrecht
 */
public interface Category {

    /**
     * Category's internal name (id).
     */
    String getName();

    /**
     * Category's display name - user editable.
     */
    String getDisplayName();

    /**
     * Short description for tooltips.
     */
    String getShortDescription();
    
    /**
     * Icon
     */
    Image getIcon(int type);
    
    /**
     * Actions for category's popup menu.
     */
    Action[] getActions();

    /**
     * Category items.
     */
    Item[] getItems();
    
    void addCategoryListener( CategoryListener listener );
    
    void removeCategoryListener( CategoryListener listener );
    
    Transferable getTransferable();
    
    Lookup getLookup();
    
    boolean dropItem( Transferable dropItem, int dndAction, Item target, boolean dropBefore );
    
    boolean dragOver( DropTargetDragEvent e );
}
