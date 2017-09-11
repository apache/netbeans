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
