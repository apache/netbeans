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
public final  class ListRendererPanel extends JPanel {

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
