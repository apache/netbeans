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

package org.netbeans.modules.java.ui;

import java.awt.Component;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.HtmlRenderer.Renderer;
import org.netbeans.api.java.source.UiUtils;

/** Contains implementation of various CellRenderers.
 *
 * @author Petr Hrebejk
 */
public final class Renderers {
    
    /** Creates a new instance of Renderers */
    private Renderers() {
    }
    
    public static TreeCellRenderer declarationTreeRenderer() {
        return new DeclarationTreeRenderer();
    }
    
    public static ListCellRenderer declarationListRenderer() {
        return new DeclarationTreeRenderer();
    }
        
    // Innerclasses ------------------------------------------------------------
    
    private static class DeclarationTreeRenderer implements TreeCellRenderer, ListCellRenderer {
        
        Renderer renderer;
        
        /** Creates a new instance of ClassesRenderer */
        private DeclarationTreeRenderer() {
            this.renderer = HtmlRenderer.createRenderer();        
        }
        
        // ListCellRenderer implementation -------------------------------------

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            if ( value instanceof DefaultMutableTreeNode ) {
                value = ((DefaultMutableTreeNode)value).getUserObject();
            }
            
            String name;
            String toolTip;
            Icon icon;
            if (value instanceof TypeElement) {
                TypeElement te = (TypeElement) value;
                name = getDisplayName(te);
                toolTip = getToolTipText(te);            
                icon = UiUtils.getElementIcon( te.getKind(), te.getModifiers() );
            }
            else {
                name = "??";
                toolTip = name = (value == null ? "NULL" : value.toString());
                icon = null;
            }
            
            JLabel comp = (JLabel)renderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            comp.setText( name );            
            comp.setToolTipText(toolTip);            
            if (icon != null) {                
                comp.setIcon(icon);
            }
//            if ( index % 2 > 0 ) {
//                comp.setBackground( list.getBackground().darker() ); // Too dark
//                comp.setOpaque( true );
//            }
            return comp;
            
        }
        
        // TreeCellRenderer implementation -------------------------------------
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {        
            
            if ( value instanceof DefaultMutableTreeNode ) {
                value = ((DefaultMutableTreeNode)value).getUserObject();
            }
            
            String name;
            String toolTip;
            Icon icon;
            if (value instanceof TypeElement) {
                TypeElement te = (TypeElement) value;
                name = getDisplayName(te);
                toolTip = getToolTipText(te);            
                icon = UiUtils.getElementIcon( te.getKind(), te.getModifiers() );
            }
            else {
                name = "???";
                toolTip = name = (value == null ? "NULL" : value.toString());
                icon = null;
            }
            
            JLabel comp = (JLabel)renderer.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );
            comp.setText( name );
            comp.setToolTipText(toolTip);
            if (icon != null) {
                comp.setIcon(icon);
            }
            return comp;
        }
        
        // Private methods -----------------------------------------------------

        private static String getDisplayName( TypeElement te ) {
            boolean deprecated = false;//XXX: removing isDeprecated from SourceUtils, use Elements.isDeprecated instead
            String simpleName = te.getSimpleName().toString();
            String qualifiedName = te.getQualifiedName().toString();
            int lastIndex = qualifiedName.length() - simpleName.length();
            lastIndex = lastIndex == 0 ? lastIndex : lastIndex - 1;
            return "<html><b>" + (deprecated ? "<s>" : "" ) + simpleName + (deprecated ? "</s></b>" : "</b>" ) + "<font color=\"#707070\"> (" + qualifiedName.substring( 0, lastIndex ) + ")</font></html>";            
        }

        private static String getToolTipText( TypeElement value ) {
            return value.getQualifiedName().toString();
        }       

               
    }
            
}
