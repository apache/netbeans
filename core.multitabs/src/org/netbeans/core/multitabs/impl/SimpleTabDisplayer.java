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
package org.netbeans.core.multitabs.impl;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
public class SimpleTabDisplayer extends AbstractTabDisplayer implements ListSelectionListener {

    private final TabTable table;

    public SimpleTabDisplayer( final TabDataModel tabModel, int tabsLocation ) {
        super( tabModel, tabsLocation );
        table = new TabTable( tabModel, tabsLocation );
        scrollPane.setViewportView( table );
        table.getSelectionModel().setSelectionInterval( 0, 0 );
        table.getSelectionModel().addListSelectionListener( this );
        table.getColumnModel().getSelectionModel().setSelectionInterval( 0, 0 );
        table.getColumnModel().getSelectionModel().addListSelectionListener( this );
        table.addMouseWheelListener( this );
        
        table.setBorder( TabTableUI.createTabBorder( table, tabsLocation ) );
    }

    @Override
    public Rectangle getTabBounds( int tabIndex ) {
        Rectangle res = table.getTabBounds( tabIndex );
        if( null != res )
            res = SwingUtilities.convertRectangle( table, res, this );
        return res;
    }

    @Override
    public TabData getTabAt( Point p ) {
        p = SwingUtilities.convertPoint( this, p, table );
        return table.getTabAt( p );
    }

    @Override
    public void valueChanged( ListSelectionEvent e ) {
        if( e.getValueIsAdjusting() )
            return;
        int newSelIndex = -1;
        int selRow = table.getSelectedRow();
        int selCol = table.getSelectedColumn();
        if( selRow >= 0 && selCol >= 0 ) {
            TabData td = ( TabData ) table.getValueAt( selRow, selCol );
            if( td != null ) {
                newSelIndex = tabModel.indexOf( td );
                Rectangle rect = table.getCellRect( selRow, selCol, true );
                table.scrollRectToVisible( rect );
                controller.setSelectedIndex( newSelIndex );
            }
        } else {
            SwingUtilities.invokeLater( new Runnable() {

                @Override
                public void run() {
                    int selIndex = controller.getSelectedIndex();
                    if( selIndex < 0 )
                        selIndex = 0;
                    setSelectedIndex( selIndex );
                }
            });
        }
    }

    @Override
    public void setSelectedIndex( int index ) {
        if( index >= getModel().size() ) {
            return;
        }
        TabTableModel model = ( TabTableModel ) table.getModel();
        int selRow = model.toRowIndex( index );
        int selCol = model.toColumnIndex( index );
        if( selCol >=0 && selRow >= 0 ) {
            table.getSelectionModel().setSelectionInterval( selRow, selRow );
            table.getColumnModel().getSelectionModel().setSelectionInterval( selCol, selCol );
            Rectangle rect = table.getCellRect( selRow, selCol, true );
            table.scrollRectToVisible( rect );
        }
    }

    @Override
    public void attach( final Controller controller ) {
        super.attach( controller );
        table.addMouseListener( controller );
        CloseButtonHandler closeHandler = new CloseButtonHandler( this, controller );
        table.addMouseListener( closeHandler );
        table.addMouseMotionListener( closeHandler );
    }

    @Override
    public int dropIndexOfPoint( Point location ) {
        int res = -1;
        location = SwingUtilities.convertPoint( this, location, table );
        TabData tab = table.getTabAt( location );
        if( null != tab ) {
            res = getModel().indexOf( tab );
            Rectangle rect = getTabBounds( res );
            rect = SwingUtilities.convertRectangle( this, rect, table );
            if( orientation == JTabbedPane.VERTICAL ) {
                if( location.y <= rect.y + rect.height/2 ) {
                    res = Math.max( 0, res );
                } else {
                    res++;
                }
            } else {
                if( location.x <= rect.x + rect.width/2 ) {
                    res = Math.max( 0, res );
                } else {
                    res++;
                }
            }
        }
        return res;
    }

    @Override
    public Rectangle dropIndication( TopComponent draggedTC, Point location ) {
        int tabIndex = dropIndexOfPoint( location );
        if( tabIndex < 0 )
            return null;
        if( tabIndex == getModel().size() )
            tabIndex--;
        Rectangle rect = getTabBounds( tabIndex );
        if( orientation == JTabbedPane.VERTICAL ) {
            if( location.y <= rect.y + rect.height/2 ) {
                rect.y -= rect.height / 2;
            } else {
                rect.y += rect.height / 2;
            }
        } else {
            if( location.x <= rect.x + rect.width/2 ) {
                rect.x -= rect.width / 2;
            } else {
                rect.x += rect.width / 2;
            }
        }
        return rect;
    }
}
