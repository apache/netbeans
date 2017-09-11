/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.windows.view.ui.popupswitcher;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.awt.HtmlRenderer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Table showing opened TopComponents in two columns. One column contains
 * editor windows, other column contains non-document windows. It may have additional
 * columns to show sub-components of currently selected item.
 * 
 * @since 2.46
 * @author S. Aubrecht
 */
class Table extends JTable {

    private static final Border rendererBorder =
            BorderFactory.createEmptyBorder(2, 5, 0, 5);

    private static final Icon NULL_ICON = new NullIcon(16);
    private Color foreground;
    private Color background;
    private Color selForeground;
    private Color selBackground;

    private static final int MAX_VISIBLE_ROWS = 20;
    private static final int MAX_TOP_COLUMN_WIDTH = 450;
    private static final int MAX_SUB_COLUMN_WIDTH = 225;

    /**
     * Flag indicating that the fixed row height has not yet been calculated -
     * this is for fontsize support
     */
    private boolean needCalcRowHeight = true;

    //Preferred size of the viewport area.
    private Dimension prefSize = null;

    private final boolean showIcons;

    //a wrapper for cell renderer, used when top-level item has sub-items
    private final JPanel topItemPanel = new JPanel();
    //shows a submenu-like arrow icon and the name of currently active sub-component
    private final JLabel rightArrowLabel = new JLabel();

    public Table( Model model ) {
        super( model );
        showIcons = model.hasIcons();
        init();
    }

    private void init() {
        setOpaque( false );
        getSelectionModel().clearSelection();
        getSelectionModel().setAnchorSelectionIndex(-1);
        getSelectionModel().setLeadSelectionIndex(-1);
        setAutoscrolls( false );
        setShowHorizontalLines(false);
        setShowVerticalLines( false);
        setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        setTableHeader( null );
        // Calc row height here so that TableModel can adjust number of columns.
        calcRowHeight(getOffscreenGraphics());

        //mouse click into the table performs the switching
        addMouseListener( new MouseAdapter() {
            @Override
            public void mousePressed( MouseEvent e ) {
                int row = rowAtPoint( e.getPoint() );
                int col = columnAtPoint( e.getPoint() );
                if( row >= 0 && col >= 0 ) {
                    if( select( row, col ) ) {
                        performSwitching();
                    }
                }
            }
        });

        //icon for top-level items with sub-items
        rightArrowLabel.setIcon( new ArrowIcon() );
        rightArrowLabel.setIconTextGap( 2 );
        rightArrowLabel.setHorizontalTextPosition( JLabel.LEFT );
        topItemPanel.setLayout( new BorderLayout(5, 0) );
        topItemPanel.add( rightArrowLabel, BorderLayout.EAST );
        topItemPanel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(Table.class, "ACD_OTHER_EDITORS") );
        topItemPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 2) );

        //adjust column widths to accomodate the widest item in each column
        for( int col=0; col<getColumnCount(); col++ ) {
            if( getSwitcherModel().isTopItemColumn( col ) )
                adjustColumnWidths( col );
        }

        //include the width of vertical scrollbar if there are too many rows
        int maxRowCount = getSwitcherModel().getMaxRowCount();
        if( maxRowCount > MAX_VISIBLE_ROWS && getRowCount() <= MAX_VISIBLE_ROWS ) {
            JScrollPane scroll = new JScrollPane();
            scroll.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
            int scrollWidth = scroll.getVerticalScrollBar().getPreferredSize().width;
            TableColumn tc = getColumnModel().getColumn( getColumnCount()-1 );
            tc.setMaxWidth( tc.getMaxWidth() + scrollWidth );
            tc.setPreferredWidth( tc.getPreferredWidth() + scrollWidth );
            tc.setWidth( tc.getWidth() + scrollWidth );
        }
    }

    private void adjustColumnWidths( int topColumn ) {
        TableColumnModel colModel = getColumnModel();
        int colWidth = 0;
        int subColWidth = -1;
        for( int row=0; row<getRowCount(); row++ ) {
            Item item = ( Item ) getValueAt( row, topColumn );
            Component ren = prepareRenderer( this.getCellRenderer( row, topColumn ), row, topColumn, item, true );
            int prefWidth = ren.getPreferredSize().width;
            colWidth = Math.max( colWidth, prefWidth );
            
            if( null != item && item.hasSubItems() && topColumn+1 < getColumnCount()
                    && !getSwitcherModel().isTopItemColumn( topColumn+1 ) ) {
                Item[] subItems = item.getActivatableSubItems();
                for( int i=0; i<subItems.length; i++ ) {
                    ren = prepareRenderer( this.getCellRenderer( 0, topColumn+1 ), 0, topColumn+1, subItems[i], true );
                    prefWidth = ren.getPreferredSize().width;
                    subColWidth = Math.max( subColWidth, prefWidth );
                }
            }
        }
        colWidth = Math.min( colWidth, MAX_TOP_COLUMN_WIDTH );
        TableColumn tc = colModel.getColumn( topColumn );
        tc.setPreferredWidth( colWidth );
        tc.setWidth( colWidth );
        tc.setMaxWidth( colWidth );

        if( subColWidth > 0 ) {
            subColWidth = Math.min( subColWidth, MAX_SUB_COLUMN_WIDTH );
            tc = colModel.getColumn( topColumn+1 );
            tc.setPreferredWidth( subColWidth );
            tc.setWidth( subColWidth );
            tc.setMaxWidth( subColWidth );
        }
    }

    @Override
    protected TableColumnModel createDefaultColumnModel() {
        return super.createDefaultColumnModel();
    }

    @Override
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();
    }

    void setInitialSelection( int hits, boolean forward ) {
        hits++;
        int direction = forward ? 1 : -1;
        int initialColumn = getSwitcherModel().getInitialColumn();
        int initialRow = forward ? 0 : getSwitcherModel().getRowCount( initialColumn )-1;
        while( initialRow > 0 && null == getValueAt( initialRow, initialColumn ) ) {
            initialRow--;
        }
        initialRow += hits * direction;
        initialRow = Math.max( 0, initialRow );
        initialRow = Math.min( initialRow, getSwitcherModel().getRowCount( initialColumn )-1 );
        
        select( initialRow, initialColumn );
    }


    /**
     * Changes selection to given table cell.
     * @param row
     * @param col
     * @return True if the selection has changed, false otherwise (e.g. the coordinates
     * point to an empty cell).
     */
    boolean select( int row, int col ) {
        if( null == getValueAt( row, col ) )
            return false;

        changeSelection( row, col, false, false );
        return true;
    }

    /**
     * Activates the currently selected item and closes the popup switcher window.
     */
    void performSwitching() {
        int selRow = getSelectedRow();
        int selCol = getSelectedColumn();
        if( selRow < 0 || selCol < 0 )
            return;
        Item selItem = ( Item ) getSwitcherModel().getValueAt( selRow, selCol );
        if( null != selItem ) {
            selItem.activate();
            KeyboardPopupSwitcher.hidePopup();
        }
    }

    @Override
    public void updateUI() {
        needCalcRowHeight = true;
        super.updateUI();
    }

    @Override
    public void setFont(Font f) {
        needCalcRowHeight = true;
        super.setFont(f);
    }

    @Override
    public Component prepareRenderer(
            TableCellRenderer renderer,
            int row,
            int column) {

        Item item = (Item) getSwitcherModel().getValueAt(row, column);

        boolean selected = row == getSelectedRow() &&
                column == getSelectedColumn() && item != null;

        return prepareRenderer( renderer, row, column, item, selected );
    }

    private Component prepareRenderer( TableCellRenderer renderer,
            int row, int column, Item item, boolean selected) {


        Component ren = renderer.getTableCellRendererComponent(this, item,
                selected, selected, row, column);

        if( null == item ) {
            // it's a filler space, we're done
            if( ren instanceof JLabel  ) {
                ((JLabel)ren).setOpaque( false );
                ((JLabel)ren).setIcon( null );
                ((JLabel)ren).getAccessibleContext().setAccessibleDescription(null);
            }
            return ren;
        }

        return configureRenderer( ren, item, selected );
    }

    private Component configureRenderer( Component ren, Item item, boolean selected ) {
        if (!(ren instanceof JLabel))
            return ren;

        JLabel lbl = ( JLabel ) ren;
        // #199007: Swing HTML renderer does a poor job of truncating long labels
        JLabel prototype = (JLabel) ren;
        lbl = HtmlRenderer.createLabel();
        if( lbl instanceof HtmlRenderer.Renderer ) {
            ((HtmlRenderer.Renderer)lbl).setRenderStyle( HtmlRenderer.STYLE_TRUNCATE );
        }
        lbl.setForeground(prototype.getForeground());
        lbl.setBackground(prototype.getBackground());
        lbl.setFont(prototype.getFont());
        // border, text will be overwritten below anyway
        ren = lbl;

        lbl.getAccessibleContext().setAccessibleDescription(null);

        Icon icon = null;
        if( item.isTopItem() ) {
            icon = item.getIcon();
            if( showIcons && (icon == null || icon.getIconWidth() == 0) ) {
                icon = NULL_ICON;
            }
        }

        lbl.setText(selected ? stripHtml( item.getDisplayName() ) : item.getDisplayName());
        lbl.setBorder(rendererBorder);
        lbl.setIcon(icon);
        if( null != icon && item.isTopItem() ) {
            lbl.setIconTextGap(26 - icon.getIconWidth());
        }
        lbl.setOpaque(true);

        if( item.isTopItem() && item.hasSubItems() 
                && (selected || item.isParentOf(getSelectedItem())) ) {
            ren = configureTopItemRenderer( lbl, item );
        }

        return ren;
    }

    private Component configureTopItemRenderer( JLabel ren, Item item ) {
        Item activeSubItem = item.getActiveSubItem();
        if( null != activeSubItem ) {
            rightArrowLabel.setText( truncateSubItemText( activeSubItem.getDisplayName() ) );
            rightArrowLabel.setForeground( ren.getForeground() );
            rightArrowLabel.setBackground( ren.getBackground() );
        } else {
            rightArrowLabel.setText( null );
        }
        topItemPanel.setBackground( ren.getBackground() );
        topItemPanel.removeAll();
        topItemPanel.add( ren, BorderLayout.CENTER );
        topItemPanel.add( rightArrowLabel, BorderLayout.EAST );
        return topItemPanel;
    }

    static String stripHtml( String htmlText ) {
        if( null == htmlText )
            return null;
        String res = htmlText.replaceAll( "<[^>]*>", "" ); // NOI18N // NOI18N
        res = res.replaceAll( "&nbsp;", " " ); // NOI18N // NOI18N
        res = res.trim();
        return res;
    }

    private static String truncateSubItemText( String text ) {
        text = stripHtml( text );
        StringBuilder sb = new StringBuilder( "[" + text + "]" ); //NOI18N  //NOI18N
        JLabel lbl = new JLabel( sb.toString() );
        while( lbl.getPreferredSize().width > 225 && sb.length() > 3 ) {
            if( sb.charAt( sb.length()-2 ) != PopupSwitcher.DOTS ) {
                sb.insert( sb.length()-1, PopupSwitcher.DOTS );
            }
            sb.deleteCharAt( sb.length()-3 );
            lbl.setText( sb.toString() );
        }
        return sb.toString();
    }

    void nextRow() {
        changeRow( 1 );
    }

    void previousRow() {
        changeRow( -1 );
    }

    private void changeRow( int step ) {
        int selCol = Math.max( getSelectedColumn(), 0 );
        int selRow = Math.max( getSelectedRow(), 0 );
        selRow += step;
        if( selRow < 0 ) {
            if( !changeColumn( step, false ) )
                changeColumn( 2*step, false );
            return;
        }
        if( selRow > getRowCount() || null == getValueAt( selRow, selCol ) ) {
            if( getSwitcherModel().isTopItemColumn( selCol ) ) {
                if( !getSwitcherModel().isTopItemColumn( selCol+step ) )
                    step *= 2;
                changeColumn( step, false );
            } else {
                if( !select( selRow, selCol-1 ) )
                    changeColumn( step, false );
            }
            return;
        }
        select( selRow, selCol );
    }

    void nextColumn() {
        if( !changeColumn( 1, true ) )
            changeColumn( 2, true );
    }

    void previousColumn() {
        if( !changeColumn( -1, true ) )
            changeColumn( -2, true );
    }

    private boolean changeColumn( int step, boolean keepRowSelection ) {
        int selCol = Math.max( getSelectedColumn(), 0 );
        selCol += step;
        if( selCol < 0 )
            selCol = getColumnCount()-1;
        if( selCol >= getColumnCount() )
            selCol = 0;
        int selRow = getSelectedRow();
        Model m = getSwitcherModel();
        if( !keepRowSelection ) {
            selRow = step > 0 ? 0 : getRowCount()-1;
        } else if( step == -1 && !m.isTopItemColumn( getSelectedColumn() ) ) {
            Item child = getSelectedItem();
            while( selRow > 0 ) {
                Item parent = (Item)getValueAt( selRow--, getSelectedColumn()-1 );
                if( null == parent || parent.isParentOf( child ) )
                    break;
            }
        }
        while( selRow > 0 && null == getValueAt( selRow, selCol ) )
            selRow--;
        return select( selRow, selCol );
    }

    private static class NullIcon implements Icon {
        private final int size;

        public NullIcon( int size ) {
            this.size = size;
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {}
    }

    @Override
    public Color getForeground() {
        if (foreground == null) {
            foreground = PopupSwitcher.getDefaultForeground();
        }
        return foreground != null ? foreground : super.getForeground();
    }

    @Override
    public Color getBackground() {
        if (background == null) {
            background = PopupSwitcher.getDefaultBackground();
        }
        return background != null ? background : super.getBackground();
    }

    @Override
    public Color getSelectionForeground() {
        if (selForeground == null) {
            selForeground = PopupSwitcher.getSelectionForeground();
        }
        return selForeground != null ? selForeground : super.getSelectionForeground();
    }

    @Override
    public Color getSelectionBackground() {
        if (selBackground == null) {
            selBackground = PopupSwitcher.getSelectionBackground();
        }
        return selBackground != null ? selBackground : super.getSelectionBackground();
    }

    /**
     * Calculate the height of rows based on the current font.  This is done
     * when the first paint occurs, to ensure that a valid Graphics object is
     * available.
     *
     * @since 1.25
     */
    private void calcRowHeight(Graphics g) {
        Font f = getFont();
        FontMetrics fm = g.getFontMetrics(f);
        // As icons are displayed use maximum from font and icon height
        int height = Math.max(fm.getHeight(), 16) + 4;
        needCalcRowHeight = false;
        setRowHeight(height);
    }

    private static SoftReference<BufferedImage> ctx = null;

    /**
     * Provides an offscreen graphics context so that widths based on character
     * size can be calculated correctly before the component is shown
     */
    private static Graphics2D getOffscreenGraphics() {
        BufferedImage result = null;
        // XXX multi-monitors w/ different resolution may have problems; Better
        // to call Toolkit to create a screen graphics
        if (ctx != null) {
            result = ctx.get();
        }
        if (result == null) {
            result = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            ctx = new SoftReference<BufferedImage>(result);
        }
        return (Graphics2D) result.getGraphics();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        if( needCalcRowHeight ) {
            calcRowHeight( getOffscreenGraphics() );
            prefSize = null;
        }
        if( null == prefSize ) {
            Dimension dim = new Dimension();
            for( int i=0; i<getColumnCount(); i++ ) {
                TableColumn tc = getColumnModel().getColumn( i );
                dim.width += tc.getPreferredWidth();
            }
            int rowCount = Math.min( MAX_VISIBLE_ROWS, getRowCount() );
            dim.height = rowCount*getRowHeight();
            Rectangle screen = Utilities.getUsableScreenBounds();
            dim.width = Math.min( dim.width, screen.width-100 );
            dim.height = Math.min( dim.height, screen.height-100 );
            prefSize = dim;
        }
        return prefSize;
    }

    private Model getSwitcherModel() {
        return (Model) getModel();
    }

    public Item getSelectedItem() {
        int row = getSelectedRow();
        int col = getSelectedColumn();
        if( row < 0 || col < 0 )
            return null;
        return (Item) getValueAt(row, col);
    }

    @Override
    public void paint(Graphics g) {
        if (needCalcRowHeight) {
            calcRowHeight(g);
        }
        super.paint(g);

        paintVerticalLine(g);
    }

    /**
     * Copied from BasicTableUI, we don't want to paint all grid lines, just
     * a single vertical line to visually separate document and view items.
     * @param g
     */
    private void paintVerticalLine( Graphics g ) {
        Rectangle clip = g.getClipBounds();

        Rectangle bounds = getBounds();
        // account for the fact that the graphics has already been translated
        // into the table's bounds
        bounds.x = bounds.y = 0;

	if (getRowCount() <= 0 || getColumnCount() <= 0 ||
                // this check prevents us from painting the entire table
                // when the clip doesn't intersect our bounds at all
                !bounds.intersects(clip)) {

	    return;
	}

        boolean ltr = getComponentOrientation().isLeftToRight();

	Point upperLeft = clip.getLocation();
        if (!ltr) {
            upperLeft.x++;
        }

	Point lowerRight = new Point(clip.x + clip.width - (ltr ? 1 : 0),
                                     clip.y + clip.height);

        int rMin = rowAtPoint(upperLeft);
        int rMax = rowAtPoint(lowerRight);
        // This should never happen (as long as our bounds intersect the clip,
        // which is why we bail above if that is the case).
        if (rMin == -1) {
	    rMin = 0;
        }
        // If the table does not have enough rows to fill the view we'll get -1.
        // (We could also get -1 if our bounds don't intersect the clip,
        // which is why we bail above if that is the case).
        // Replace this with the index of the last row.
        if (rMax == -1) {
	    rMax = getRowCount()-1;
        }

        int cMin = columnAtPoint(ltr ? upperLeft : lowerRight);
        int cMax = columnAtPoint(ltr ? lowerRight : upperLeft);
        // This should never happen.
        if (cMin == -1) {
	    cMin = 0;
        }
	// If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
	    cMax = getColumnCount()-1;
        }
        g.setColor(getGridColor());

	Rectangle minCell = getCellRect(rMin, cMin, true);
	Rectangle maxCell = getCellRect(rMax, cMax, true);
        Rectangle damagedArea = minCell.union( maxCell );

        TableColumnModel cm = getColumnModel();
        int tableHeight = damagedArea.y + damagedArea.height;
        int x;
        if (getComponentOrientation().isLeftToRight()) {
            x = damagedArea.x;
            for (int column = cMin; column <= cMax; column++) {
                int w = cm.getColumn(column).getWidth();
                x += w;
                if( getSwitcherModel().isTopItemColumn( column+1 ) && column > 0 )
                    g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
            }
        } else {
            x = damagedArea.x;
            for (int column = cMax; column >= cMin; column--) {
                int w = cm.getColumn(column).getWidth();
                x += w;
                if( getSwitcherModel().isTopItemColumn( column+1 ) && column > 0 )
                    g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
            }
        }
    }

    @Override
    public void changeSelection(final int rowIndex, final int columnIndex, boolean toggle, boolean extend) {
        if( null == getModel().getValueAt( rowIndex, columnIndex ) )
            return;
        super.changeSelection( rowIndex, columnIndex, false, false );
        getSwitcherModel().setCurrentSelection( rowIndex, columnIndex );
        Rectangle rect = getCellRect( rowIndex, columnIndex, true );
        Rectangle visible = new Rectangle();
        computeVisibleRect( visible );
        if( visible.width > 0 && visible.height > 0 && !visible.contains( rect ) )
            scrollRectToVisible( rect );
    }


    private static class ArrowIcon implements Icon {

        private static final int SIZE = 10;

        @Override
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            g.setColor( c.getForeground() );
            Polygon s = new Polygon();
            s.addPoint( x, y );
            s.addPoint( x+SIZE/2, y+SIZE/2 );
            s.addPoint( x, y+SIZE );
            s.addPoint( x, y );
            g.fillPolygon( s );
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }

    }
}
