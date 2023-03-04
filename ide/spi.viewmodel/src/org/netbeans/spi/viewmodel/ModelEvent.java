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

package org.netbeans.spi.viewmodel;

import java.util.Arrays;
import java.util.EventObject;


/**
 * Encapsulates information describing changes to a model, and
 * used to notify model listeners of the change.
 *
 * @author   Jan Jancura
 * @since 1.4
 */
public class ModelEvent extends EventObject {

    private ModelEvent (Object source) {
        super (source);
    }

    /**
     * Used to notify that whole content of tree has been changed.
     *
     * @since 1.4
     */
    public static class TreeChanged extends ModelEvent {

        /**
         * Creates a new instance of TreeChanged event.
         *
         * @param source a source if event.
         *
         * @since 1.4
         */
        public TreeChanged (Object source) {
            super (source);
        }
    }
    
    /**
     * Used to notify that one cell in table has been changed.
     *
     * @since 1.4
     */
    public static class TableValueChanged extends ModelEvent {
        
        /**
         * The mask for value change.
         * @since 1.42
         */
        public static final int VALUE_MASK = 1;
        /**
         * The mask for HTML value change.
         * @since 1.42
         */
        public static final int HTML_VALUE_MASK = 2;
        /**
         * The mask for change of the read only state.
         * @since 1.42
         */
        public static final int IS_READ_ONLY_MASK = 4;
        
        private Object node;
        private String columnID;
        private int change;
        
        /**
         * Creates a new instance of TableValueChanged event.
         *
         * @param source a source if event.
         * @param node a changed node instance
         * @param columnID a changed column name
         *
         * @since 1.4
         */
        public TableValueChanged (
            Object source, 
            Object node,
            String columnID
        ) {
            this(source, node, columnID, 0xffffffff);
        }
        
        /**
         * Creates a new instance of TableValueChanged event.
         *
         * @param source a source if event.
         * @param node a changed node instance
         * @param columnID a changed column name
         * @param change one of the *_MASK constants or their aggregation.
         * @since 1.42
         */
        public TableValueChanged (
            Object source, 
            Object node,
            String columnID,
            int change
        ) {
            super (source);
            this.node = node;
            this.columnID = columnID;
            this.change = change;
        }
        
        /**
         * Returns changed node instance.
         *
         * @return changed node instance
         *
         * @since 1.4
         */
        public Object getNode () {
            return node;
        }
        
        /**
         * Returns changed column name.
         *
         * @return changed column name
         *
         * @since 1.4
         */
        public String getColumnID () {
            return columnID;
        }
        
        /**
         * Get the change mask.
         *
         * @return the change mask, one of the *_MASK constants or their aggregation.
         * @since 1.42
         */
        public int getChange() {
            return change;
        }
    }
    
    /**
     * Used to notify that one node has been changed (icon, displayName and 
     * children).
     *
     * @since 1.4
     */
    public static class NodeChanged extends ModelEvent {
        
        /**
         * The mask for display name change.
         * @since 1.6
         */
        public static final int DISPLAY_NAME_MASK = 1;
        /**
         * The mask for icon change.
         * @since 1.6
         */
        public static final int ICON_MASK = 2;
        /**
         * The mask for short description change.
         * @since 1.6
         */
        public static final int SHORT_DESCRIPTION_MASK = 4;
        /**
         * The mask for children change.
         * @since 1.6
         */
        public static final int CHILDREN_MASK = 8;
        /**
         * The mask for expansion change.
         * @since 1.15
         */
        public static final int EXPANSION_MASK = 16;
        
        private Object node;
        private int change;
        
        /**
         * Creates a new instance of NodeChanged event.
         *
         * @param source a source if event.
         * @param node a changed node instance
         *
         * @since 1.4
         */
        public NodeChanged (
            Object source, 
            Object node
        ) {
            this (source, node, 0xFFFFFFFF);
        }
        
        /**
         * Creates a new instance of NodeChanged event.
         *
         * @param source a source if event.
         * @param node a changed node instance.
         * @param change one of the *_MASK constant or their aggregation.
         *
         * @since 1.6
         */
        public NodeChanged(Object source, Object node, int change) {
            super (source);
            this.node = node;
            this.change = change;
        }
        
        /**
         * Returns changed node instance.
         *
         * @return changed node instance
         *
         * @since 1.4
         */
        public Object getNode () {
            return node;
        }
        
        /**
         * Get the change mask.
         *
         * @return the change mask, one of the *_MASK constant or their aggregation.
         * @since 1.6
         */
        public int getChange() {
            return change;
        }

        @Override
        public String toString() {
            return super.toString()+"(node = "+node+", change = "+getChangeString(change)+")";
        }

        private static String getChangeString(int change) {
            StringBuilder sb = new StringBuilder();
            if ((change & DISPLAY_NAME_MASK) != 0) {
                sb.append("DISPLAY_NAME, ");
            }
            if ((change & ICON_MASK) != 0) {
                sb.append("ICON, ");
            }
            if ((change & SHORT_DESCRIPTION_MASK) != 0) {
                sb.append("SHORT_DESCRIPTION, ");
            }
            if ((change & CHILDREN_MASK) != 0) {
                sb.append("CHILDREN, ");
            }
            if ((change & EXPANSION_MASK) != 0) {
                sb.append("EXPANSION, ");
            }
            if (sb.length() > 0) {
                sb.delete(sb.length() - 2, sb.length());
            }
            return sb.toString();
        }

    }

    /**
     * Event to change a selection in the tree table view.
     *
     * @since 1.19
     */
    public static class SelectionChanged extends ModelEvent {

        private Object[] nodes;
        
        /**
         * Creates a new instance of SelectionChanged event.
         *
         * @param source the source of the event.
         * @param nodes list of selected node instances. All nodes are deselected
         * when this list is empty.
         */
        public SelectionChanged(Object source, Object... nodes) {
            super (source);
            this.nodes = nodes;
        }

        /**
         * Returns selected node instances.
         *
         * @return selected node instances
         */
        public Object[] getNodes() {
            return nodes;
        }

        @Override
        public String toString() {
            return super.toString()+"(nodes = "+Arrays.toString(nodes)+")"; // NOI18N
        }

    }

}
