/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.commons.treelist;

import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Wrapper for node renderers. Defines appropriate foreground/background colors,
 * borders.
 *
 * @author S. Aubrecht
 */
final public  class ListRendererPanel extends JPanel {

    private final ListNode node;

    public ListRendererPanel(final ListNode node) {
        super(new BorderLayout());

        this.node = node;
        setOpaque(true);
    }

    public void configure( Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowHeight, int rowWidth ) {
        removeAll();
        int maxWidth = rowWidth - SelectionList.INSETS_LEFT - SelectionList.INSETS_RIGHT;
        JComponent inner = node.getComponent( foreground, background, isSelected, hasFocus, maxWidth > 0 ? maxWidth : 0);
        add( inner, BorderLayout.CENTER );

        setBackground( background );
        setForeground( foreground );

        Border border = null;
        if( hasFocus ) {
            if( isSelected ) {
                border = UIManager.getBorder( "List.focusSelectedCellHighlightBorder" ); // NOI18N
            }
            if( border == null ) {
                border = UIManager.getBorder( "List.focusCellHighlightBorder" ); // NOI18N
            }
        }
        if( null != border ) {
            border = BorderFactory.createCompoundBorder( border,
                    BorderFactory.createEmptyBorder( SelectionList.INSETS_TOP, SelectionList.INSETS_LEFT,
                    SelectionList.INSETS_BOTTOM, SelectionList.INSETS_RIGHT ) );
        } else {
            border = BorderFactory.createEmptyBorder( SelectionList.INSETS_TOP, SelectionList.INSETS_LEFT,
                    SelectionList.INSETS_BOTTOM, SelectionList.INSETS_RIGHT );
        }

        try {
            setBorder( border );
        } catch( NullPointerException npe ) {
            //workaround for 175940
            Logger.getLogger( ListRendererPanel.class.getName() ).log( Level.INFO, "Bug #175940", npe );
        }

        RendererPanel.configureAccessibility(this, false);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Component c = SwingUtilities.getDeepestComponentAt(this, event.getX(), event.getY());
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            String tooltip = jc.getToolTipText();
            if (null != tooltip) {
                return tooltip;
            }
        }
        return super.getToolTipText(event);
    }
}
