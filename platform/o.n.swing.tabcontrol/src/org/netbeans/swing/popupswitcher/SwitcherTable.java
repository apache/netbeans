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

package org.netbeans.swing.popupswitcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import org.openide.awt.HtmlRenderer;
import org.openide.util.Utilities;

/**
 * This class is used as a content for PopupSwitcher classes (see below). It
 * appropriately displays its contents (<code>SwitcherTableItem</code>s)
 * according to screen size, given position, used font, number of items, etc.
 * and inteligently consider number of rows and columns to be used.
 *
 * @see SwitcherTableItem
 *
 * @author mkrauskopf
 */
public class SwitcherTable extends JTable {

    private static final Border rendererBorder =
            BorderFactory.createEmptyBorder(2, 5, 0, 5);
    
    private Icon nullIcon = new NullIcon();
    private Color foreground;
    private Color background;
    private Color selForeground;
    private Color selBackground;
    
    /** Cached preferred size value */
    private Dimension prefSize;
    
    /** Current NetBeans LookAndFreel id */
    /**
     * Flag indicating that the fixed row height has not yet been calculated -
     * this is for fontsize support
     */
    private boolean needCalcRowHeight = true;
    
    private final boolean showIcons;
    
    /**
     * Creates a new instance of SwitcherTable. Created table will be as high
     * as possible. Height will be used during the number of row computing.
     */
    public SwitcherTable(SwitcherTableItem[] items) {
        this(items, 0);
    }
    
    /**
     * Creates a new instance of SwitcherTable. Height of created table will be
     * computed according to given y coordinate. Height will be used during the
     * number of row computing.
     */
    public SwitcherTable(SwitcherTableItem[] items, int y) {
        super();
        init();
        // get rid of the effect when popup seems to be higher that screen height
        int gap = (y == 0 ? 10 : 5);
        int height = Utilities.getUsableScreenBounds().height - y - gap;
        setModel(new SwitcherTableModel(items, getRowHeight(), height));
        getSelectionModel().clearSelection();
        getSelectionModel().setAnchorSelectionIndex(-1);
        getSelectionModel().setLeadSelectionIndex(-1);
        setAutoscrolls( false );
        boolean hasIcons = false;
        for( SwitcherTableItem i : items ) {
            if( i.getIcon() != null && i.getIcon().getIconWidth() > 0 ) {
                hasIcons = true;
                break;
            }
        }
        showIcons = hasIcons;
    }
    
    private void init() {
        Border b = UIManager.getBorder( "nb.popupswitcher.border" ); //NOI18N
        if( null == b )
            b = BorderFactory.createLineBorder(getForeground());
        setBorder(b);
        setShowHorizontalLines(false);
        // Calc row height here so that TableModel can adjust number of columns.
        calcRowHeight(getOffscreenGraphics());
    }

    /**
     * Show new set of switcher items in this table.
     * @param newItems
     * @param y
     *
     * @since 1.35
     */
    public void setSwitcherItems( SwitcherTableItem[] newItems, int y ) {
        int gap = (y == 0 ? 10 : 5);
        int height = Utilities.getUsableScreenBounds().height - y - gap;
        setModel(new SwitcherTableModel(newItems, getRowHeight(), height));
        prefSize = null;
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
    
    private static final boolean TABNAMES_HTML = Boolean.parseBoolean(System.getProperty("nb.tabnames.html", "true")); // #47290

    @Override
    public Component prepareRenderer(
            TableCellRenderer renderer,
            int row,
            int column) {
        
        SwitcherTableItem item
                = (SwitcherTableItem) getSwitcherTableModel().getValueAt(row, column);
        
        boolean selected = row == getSelectedRow() &&
                column == getSelectedColumn() && item != null;
        
        Component ren = renderer.getTableCellRendererComponent(this, item,
                selected, selected, row, column);
        JLabel lbl = null;
        if (ren instanceof JLabel) {
            // #199007: Swing HTML renderer does a poor job of truncating long labels
            JLabel prototype = (JLabel) ren;
            lbl = (JLabel) HtmlRenderer.createRenderer();
            if( lbl instanceof HtmlRenderer.Renderer ) {
                ((HtmlRenderer.Renderer)lbl).setRenderStyle( HtmlRenderer.STYLE_TRUNCATE );
            }
            lbl.setForeground(prototype.getForeground());
            lbl.setBackground(prototype.getBackground());
            lbl.setFont(prototype.getFont());
            // border, text will be overwritten below anyway
            ren = lbl;
        }
        
        if (item == null) {
            // it's a filler space, we're done
            if( null != lbl ) {
                lbl.setOpaque(false);
                lbl.setIcon(null);
            }
            return ren;
        }
        
        Icon icon = item.getIcon();
        if (icon == null || icon.getIconWidth() == 0 ) {
            icon = nullIcon;
        }
        boolean active = item.isActive();
        if( null != lbl ) {
            lbl.setText((selected || (active && !TABNAMES_HTML)) ? stripHtml( item.getHtmlName() ) : item.getHtmlName());
            lbl.setBorder(rendererBorder);
            if( showIcons ) {
                lbl.setIcon(icon);
                lbl.setIconTextGap(26 - icon.getIconWidth());
            }
        }
        
        if (active) {
            if (TABNAMES_HTML) {
                if( null != lbl )
                    lbl.setText(lbl.getText() + " ←"); // NOI18N
            } else if (Utilities.isWindows()) {
                ren.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize()));
            } else {
                // don't use deriveFont() - see #49973 for details
                ren.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
            }
        }

        if( null != lbl )
            lbl.setOpaque(true);
        
        return ren;
    }
    
    private String stripHtml( String htmlText ) {
        // XXX could be useful with TABNAMES_HTML on Win XP L&F (dark selection background)
        if( null == htmlText )
            return null;
        String res = htmlText.replaceAll( "<[^>]*>", "" ); // NOI18N // NOI18N
        res = res.replace( "&nbsp;", " " ); // NOI18N // NOI18N
        res = res.trim();
        return res;
    }

    private static class NullIcon implements Icon {
        @Override
        public int getIconWidth() { return 16; }
        @Override
        public int getIconHeight() { return 16; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {}
    }
    
    @Override
    public Color getForeground() {
        if (foreground == null) {
            foreground = UIManager.getColor( "nb.popupswitcher.foreground" ); //NOI18N
            if (foreground == null)
                foreground = UIManager.getColor("ComboBox.foreground"); //NOI18N
        }
        return foreground != null ? foreground : super.getForeground();
    }
    
    @Override
    public Color getBackground() {
        if (background == null) {
            background = UIManager.getColor( "nb.popupswitcher.background" ); //NOI18N
            if (background == null)
                background = UIManager.getColor("ComboBox.background"); //NOI18N
            if( null != background )
                background = new Color( background.getRGB() );
        }
        return background != null ? background : super.getBackground();
    }
    
    @Override
    public Color getSelectionForeground() {
        if (selForeground == null) {
            selForeground = UIManager.getColor( "nb.popupswitcher.selectionForeground" ); //NOI18N
            if (selForeground == null)
                selForeground = UIManager.getColor("ComboBox.selectionForeground"); //NOI18N
        }
        return selForeground != null ? selForeground : super.getSelectionForeground();
    }
    
    @Override
    public Color getSelectionBackground() {
        if (selBackground == null) {
            selBackground = UIManager.getColor( "nb.popupswitcher.selectionBackground" ); //NOI18N
            if (selBackground == null)
                selBackground = UIManager.getColor("ComboBox.selectionBackground"); //NOI18N
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
        int rowHeight = Math.max(fm.getHeight(), 16) + 4;
        needCalcRowHeight = false;
        setRowHeight(rowHeight);
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
    
    /**
     * Overridden to calculate a preferred size based on the current optimal
     * number of columns, and set up the preferred width for each column based
     * on the maximum width item & icon displayed in it
     */
    @Override
    public Dimension getPreferredSize() {
        if (prefSize == null) {
            int cols = getColumnCount();
            int rows = getRowCount();
            
            // Iterate all rows and find the widest cell of a whole table
            int columnWidth = 0;
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    TableCellRenderer ren = getCellRenderer(j,i);
                    Component c = prepareRenderer(ren, j, i);
                    // sometime adding of one pixel is needed to prevent "..." truncating
                    columnWidth = Math.max(
                            c.getPreferredSize().width + 1, columnWidth);
                }
            }
            columnWidth = Math.min(columnWidth, 250);
            // Set the same (maximum) widht to all columns
            for (int i = 0; i < cols; i++) {
                getColumnModel().getColumn(i).setPreferredWidth(columnWidth);
            }
            // Rows will be fixed height, so just multiply it out
            prefSize = new Dimension(columnWidth * cols, rows * getRowHeight());
        }
        return prefSize;
    }
    
    private SwitcherTableModel getSwitcherTableModel() {
        return (SwitcherTableModel) getModel();
    }
    
    public SwitcherTableItem getSelectedItem() {
        return (SwitcherTableItem) getValueAt(getSelectedRow(), getSelectedColumn());
    }
    
    @Override
    public void paint(Graphics g) {
        if (needCalcRowHeight) {
            calcRowHeight(g);
        }
        super.paint(g);
    }
    
    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        //#203125 - CTRL key is held down while showing the popup switcher which confuses
        //the mouse-originated selection changes 
        super.changeSelection( rowIndex, columnIndex, false, false );
    }
    
    /**
     * Returns the last valid row in the last collumn.
     *
     * @return index of last non-null value in the last collumn or -1 when all
     * values are null.
     */
    public int getLastValidRow() {
        int lastColIdx = getColumnCount() - 1;
        for (int i = getRowCount() - 1; i >= 0; i--) {
            if (getValueAt(i, lastColIdx) != null) {
                return i;
            }
        }
        return -1;
    }
}
