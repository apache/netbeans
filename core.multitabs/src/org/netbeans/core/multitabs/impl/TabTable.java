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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class TabTable extends JTable {

    private final int  orientation;
    private final TabDataRenderer renderer = new TabDataRenderer();

    public TabTable( TabDataModel tabModel, int tabsLocation ) {
        this( TabTableModel.create(tabModel, tabsLocation),
                tabsLocation == JTabbedPane.TOP || tabsLocation == JTabbedPane.BOTTOM ? JTabbedPane.HORIZONTAL : JTabbedPane.VERTICAL );
    }

    protected TabTable( TabTableModel tableModel, int orientation ) {
        super( tableModel );
        this.orientation = orientation;
        getModel().addTableModelListener( new TableModelListener() {
            @Override
            public void tableChanged( TableModelEvent e ) {
                if( e.getFirstRow() != TableModelEvent.HEADER_ROW )
                    adjustColumnWidths();
            }
        });

        configure();
    }

    private void configure() {
        setRowSelectionAllowed( false );
        setColumnSelectionAllowed( false );
        setCellSelectionEnabled( true );
        setTableHeader( null );
        setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        getColumnModel().getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        setOpaque( false );
        setFocusable( false );
        ToolTipManager.sharedInstance().registerComponent( this );
        setDefaultRenderer( Object.class, renderer );
    }

    @Override
    protected void initializeLocalVars() {
        super.initializeLocalVars();
        setRowHeight( TabDataRenderer.getPreferredTableRowHeight() );
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return orientation == JTabbedPane.HORIZONTAL ? true : super.getScrollableTracksViewportHeight();
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return orientation == JTabbedPane.HORIZONTAL ? super.getScrollableTracksViewportHeight() : true;
    }

    @Override
    public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
        int res = 0;
        if( orientation == SwingConstants.VERTICAL ) {
            res = getRowHeight() / 2;
        } else {
            Point columnPoint = visibleRect.getLocation();
            columnPoint.x += 1;
            int col = columnAtPoint( columnPoint );
            if( col >= 0 ) {
                Rectangle rect = getCellRect( 0, col, true );
                res = rect.width / 2;
            } else {
                res = super.getScrollableUnitIncrement( visibleRect, orientation, direction );
            }
        }
        if( direction < 0 )
            res *= -1;
        return res;
    }

    @Override
    public String getToolTipText( MouseEvent event ) {
        int row = rowAtPoint( event.getPoint() );
        int col = columnAtPoint( event.getPoint() );
        if( row >= 0 && col >= 0 ) {
            if( isCloseButtonHighlighted( row, col ) ) {
                return NbBundle.getMessage( TabTable.class, "BtnClose_Tooltip" );
            }
            TabData td = ( TabData ) getValueAt( row, col );
            if( null != td ) {
                return td.getTooltip();
            }
        }
        return super.getToolTipText( event );
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();
        adjustColumnWidths();
    }

    protected void adjustColumnWidths() {
        TableColumnModel colModel = getColumnModel();
        for( int i=0; i<colModel.getColumnCount(); i++ ) {
            TableColumn tc = colModel.getColumn( i );
            int colWidth = 0;
            for( int row=0; row<getRowCount(); row++ ) {
                colWidth = Math.max( renderer.getPreferredWidth( getValueAt( row, i ) ), colWidth );
            }
            colWidth = Math.max( colWidth, 30 );
            colWidth += getIntercellSpacing().width;
            tc.setWidth( colWidth );
            tc.setMinWidth( colWidth );
            tc.setMaxWidth( colWidth );
            tc.setPreferredWidth( colWidth );
            tc.setResizable( false );
        }
    }

    TabData getTabAt( Point p ) {
        if( p.x < 0 || p.y < 0 )
            return null;
        int row = rowAtPoint( p );
        int col = columnAtPoint( p );
        if( row < 0 || col < 0 )
            return null;
        return (TabData)getValueAt( row, col );
    }

    private int closeButtonRow = -1;
    private int closeButtonCol = -1;

    void setCurrentCloseButtonCoords( int closeButtonRow, int closeButtonColumn ) {
        int oldRow = this.closeButtonRow;
        int oldCol = this.closeButtonCol;
        boolean change = closeButtonRow != this.closeButtonRow || closeButtonColumn != this.closeButtonCol;
        this.closeButtonCol = closeButtonColumn;
        this.closeButtonRow = closeButtonRow;

        if( change ) {
            if( oldRow >= 0 && oldCol >= 0 ) {
                Rectangle rect = getCellRect( oldRow, oldCol, true );
                if( null != rect )
                    repaint( rect );
            }
            if( this.closeButtonRow >= 0 && this.closeButtonCol >= 0 ) {
                Rectangle rect = getCellRect( this.closeButtonRow, this.closeButtonCol, true );
                if( null != rect )
                    repaint( rect );
            }
        }
    }

    boolean isCloseButtonHighlighted( int row, int col ) {
        return row == this.closeButtonRow && this.closeButtonCol == col;
    }

    Rectangle getTabBounds( int tabIndex ) {
        TabTableModel tabModel = ( TabTableModel ) getModel();
        int col = tabModel.toColumnIndex( tabIndex );
        int row = tabModel.toRowIndex( tabIndex );
        if( row >= 0 && col >= 0 ) {
            col = convertColumnIndexToView( col );
            row = convertRowIndexToView( row );
            return getCellRect( row, col, true );
        }
        return null;
    }

    @Override
    public void setUI( TableUI ui ) {
        super.setUI( new TabTableUI() );
    }
}
