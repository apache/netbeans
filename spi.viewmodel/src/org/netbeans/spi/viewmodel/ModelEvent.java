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
