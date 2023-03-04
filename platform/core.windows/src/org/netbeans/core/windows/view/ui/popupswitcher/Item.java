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
package org.netbeans.core.windows.view.ui.popupswitcher;

import java.awt.Image;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;


/**
 * An element in popup window switcher table model.
 *
 * @see Model
 * @author S. Aubrecht
 * @since 2.46
 */
abstract class Item {

    private final String displayName;
    private final String description;
    private final Icon icon;
    private final boolean active;

    protected Item( String displayName, String description, Icon icon, boolean active ) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.active = active;
    }

    /**
     * Creates a new item from given TopComponent, it also creates sub-items
     * if the TopComponent has sub-components.
     * @param tc
     * @return
     */
    public static Item create( TopComponent tc ) {
        return new TopItem( tc );
    }

    public abstract void activate();

    @Override
    public final String toString() {
        return displayName;
    }

    public final String getDescription() {
        return description;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final Icon getIcon() {
        return icon;
    }

    /**
     * @return True if this Item has sub-items.
     */
    public abstract boolean hasSubItems();

    /**
     * @return Sub items of this Item, the sub-items do not include the active sub-item.
     */
    public abstract Item[] getActivatableSubItems();

    /**
     * @return An active sub-item or null.
     */
    public abstract Item getActiveSubItem();

    /**
     * @return True if given Item is an active one, applies mostly to sub-items only.
     */
    public final boolean isActive() {
        return active;
    }

    /**
     * @return True if this Item is top-level one, false if it is a child of some
     * other item.
     */
    public abstract boolean isTopItem();

    /**
     * @param subItem
     * @return True if the given subItem is the child of this Item.
     */
    public abstract boolean isParentOf( Item subItem );

    /**
     * Top-level Item that can have child SubItems.
     */
    private static class TopItem extends Item {

        private final TopComponent tc;
        private final Item[] subItems;
        private final Item activeSubItem;

        public TopItem( TopComponent tc ) {
            super( extractDisplayName( tc ), tc.getToolTipText(), extractIcon( tc ), false );
            this.tc = tc;
            Item[] subItems = null;
            Item activeSubItem = null;
            TopComponent.SubComponent[] subs = tc.getSubComponents();
            if( null != subs && subs.length > 0 ) {
                for( TopComponent.SubComponent sc : subs ) {
                    if( sc.isActive() ) {
                        activeSubItem = new SubItem( tc, sc );

                        break;
                    }
                }
                subItems = new Item[null == activeSubItem ? subs.length : subs.length-1];
                int index = 0;
                for( TopComponent.SubComponent sc : subs ) {
                    if( sc.isActive() )
                        continue;
                    subItems[index++] = new SubItem( tc, sc );
                }
            }
            this.subItems = subItems;
            this.activeSubItem = activeSubItem;
        }

        @Override
        public void activate() {
            tc.requestActive();
        }

        @Override
        public boolean hasSubItems() {
            return null != subItems;
        }

        @Override
        public Item[] getActivatableSubItems() {
            return subItems;
        }

        @Override
        public boolean isTopItem() {
            return true;
        }

        @Override
        public Item getActiveSubItem() {
            return activeSubItem;
        }

        @Override
        public boolean isParentOf( Item child ) {
            if( null != subItems ) {
                for( Item sub : subItems ) {
                    if( sub == child )
                        return true;
                }
            }
            return null != child && child == activeSubItem;
        }
    }

    /**
     * A child of some top-level Item.
     */
    private static class SubItem extends Item {

        private final TopComponent.SubComponent subComponent;
        private final TopComponent parent;

        public SubItem( TopComponent parent, TopComponent.SubComponent subComponent ) {
            super( subComponent.getDisplayName(), subComponent.getDescription(), null,
                    subComponent.isActive() );
            this.subComponent = subComponent;
            this.parent = parent;
        }

        @Override
        public void activate() {
            parent.requestActive();
            subComponent.activate();
        }

        @Override
        public boolean hasSubItems() {
            return false;
        }

        @Override
        public boolean isTopItem() {
            return false;
        }

        @Override
        public Item[] getActivatableSubItems() {
            return null;
        }

        @Override
        public Item getActiveSubItem() {
            return null;
        }

        @Override
        public boolean isParentOf( Item subItem ) {
            return false;
        }
    }

    private static String extractDisplayName( TopComponent tc ) {
        String name = tc.getShortName();
        if( name == null || name.isEmpty() ) {
            name = tc.getHtmlDisplayName();
        }
        if( name == null || name.isEmpty() ) {
            name = tc.getDisplayName();
        }
        if( name == null || name.isEmpty() ) {
            name = tc.getName();
        }
        return name;
    }

    private static Icon extractIcon( TopComponent tc ) {
        Image img = tc.getIcon();
        if( null != img )
            return ImageUtilities.image2Icon( img );
        return null;
    }
}
