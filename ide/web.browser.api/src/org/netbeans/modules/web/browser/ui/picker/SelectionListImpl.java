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
package org.netbeans.modules.web.browser.ui.picker;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

/**
 *
 * @author S. Aubrecht
 */
class SelectionListImpl extends JList<ListItem> {

    private int mouseOverRow = -1;

    SelectionListImpl() {
        setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        setBorder( BorderFactory.createEmptyBorder() );
        setOpaque( false );
        setBackground( new Color(0,0,0,0) );
        if( BrowserMenu.GTK ) {
            Color foreground = UIManager.getColor( "MenuItem.foreground"); //NOI18N
            if( null != foreground ) {
                setForeground( new Color(foreground.getRGB()) );
            }
        }
        setCellRenderer( new RendererImpl() );

        addFocusListener( new FocusAdapter() {

            @Override
            public void focusGained( FocusEvent e ) {
                if( getSelectedIndex() < 0 && isShowing() && getModel().getSize() > 0 ) {
                    setSelectedIndex( 0 );
                }
            }
        });

        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mouseEntered( MouseEvent e ) {
                mouseMoved( e );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                setMouseOver( -1 );
            }

            @Override
            public void mouseMoved( MouseEvent e ) {
                int row = locationToIndex( e.getPoint() );
                setMouseOver( row );
            }

            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getButton() == MouseEvent.BUTTON1 ) {
                    int row = locationToIndex( e.getPoint() );
                    if( row >= 0 && row == getSelectedIndex() ) {
                        e.consume();
                        clearSelection();
                        setSelectedIndex( row );
                    }
                }
            }
        };

        addMouseMotionListener( adapter );

        addMouseListener( adapter );
    }

    int getMouseOverRow() {
        return mouseOverRow;
    }

    private void setMouseOver( int newRow ) {
        int oldRow = mouseOverRow;
        mouseOverRow = newRow;
        repaintRow( oldRow );
        repaintRow( mouseOverRow );
    }

    private void repaintRow( int row ) {
        if( row >= 0 && row < getModel().getSize() ) {
            Rectangle rect = getCellBounds( row, row );
            if( null != rect )
                repaint( rect );
        }
    }
}
