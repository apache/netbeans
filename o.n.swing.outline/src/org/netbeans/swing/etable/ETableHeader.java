/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.swing.etable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * The ETable header renderer.
 * Delegating to the original default renderer should assure L&F compliance.
 * The rendering logic moved from {@link ETableColumn}.ETableHeaderRenderer.
 * 
 * @author Martin Entlicher
 */
class ETableHeader extends JTableHeader {
    
    public ETableHeader() {
        super();
    }
    
    public ETableHeader(TableColumnModel cm) {
        super(cm);
    }

    @Override
    public void setDefaultRenderer(TableCellRenderer defaultRenderer) {
        super.setDefaultRenderer(new ETableHeaderRenderer(defaultRenderer));
    }
    
    /**
     * Special renderer painting sorting icons and also special icon
     * for the QuickFilter columns.
     */
    private class ETableHeaderRenderer extends DefaultTableCellRenderer implements TableCellRenderer, UIResource {
        
        private TableCellRenderer headerRendererUI;
        private Map<ETableColumn, TableCellRenderer> defaultColumnHeaderRenderers = new HashMap<ETableColumn, TableCellRenderer>();
        
        private ETableHeaderRenderer(TableCellRenderer headerRenderer) {
            this.headerRendererUI = headerRenderer;
            setName("TableHeader.renderer");    // NOI18N
        }

        @Override
        public void updateUI() {
            TableCellRenderer defaultTableHeaderRenderer = headerRendererUI;
            if (defaultTableHeaderRenderer instanceof JComponent) {
                ((JComponent) defaultTableHeaderRenderer).updateUI();
            } else {
                super.updateUI();
            }
        }
        
        /**
         * Get the table header renderer for the particular column.
         * If the column is {@link ETableColumn}, check createDefaultHeaderRenderer()
         * method. If it's not overridden, use the current header renderer.
         * If it is overridden, set the current header renderer to the base
         * column's renderer. If the overridden renderer delegates to the original,
         * it will get this header renderer ({@link #headerRendererUI}).
         * @param tc The column
         * @return the renderer
         */
        private TableCellRenderer getColumnHeaderRenderer(TableColumn tc) {
            if (tc instanceof ETableColumn) {
                ETableColumn eColumn = (ETableColumn) tc;
                TableCellRenderer columnHeaderRenderer;
                if (!defaultColumnHeaderRenderers.containsKey(eColumn)) {
                    TableCellRenderer tcr = eColumn.createDefaultHeaderRenderer();
                    if (tcr instanceof ETableColumn.ETableColumnHeaderRendererDelegate) {
                        defaultColumnHeaderRenderers.put(eColumn, null);
                        columnHeaderRenderer = null;
                    } else {
                        eColumn.setTableHeaderRendererDelegate(headerRendererUI);
                        columnHeaderRenderer = tcr;
                    }
                } else {
                    columnHeaderRenderer = defaultColumnHeaderRenderers.get(eColumn);
                }
                if (columnHeaderRenderer != null) {
                    return columnHeaderRenderer;
                } else {
                    return headerRendererUI;
                }
            } else {
                return headerRendererUI;
            }
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            TableColumn tcm = getColumnModel().getColumn(column);
            TableCellRenderer headerRenderer = getColumnHeaderRenderer(tcm);
            Component resUI = headerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (resUI instanceof JLabel) {
                JLabel label = (JLabel)resUI;

                String valueString = "";
                if (value != null) {
                    valueString = value.toString();
                }
                if (table instanceof ETable) {
                    ETable et = (ETable)table;
                    valueString = et.getColumnDisplayName(valueString);
                }
                Icon sortIcon = null;

                List<TableColumn> sortedColumns = ((ETableColumnModel) table.getColumnModel ()).getSortedColumns ();
                
                int sortRank = 0;
                boolean ascending = false;
                Icon customIcon = null;
                if (tcm instanceof ETableColumn) {
                    ETableColumn eColumn = (ETableColumn) tcm;
                    sortRank = eColumn.getSortRank();
                    ascending = eColumn.isAscending();
                    customIcon = eColumn.getCustomIcon();
                }

                if (sortRank != 0) {
                    if (sortedColumns.size () > 1) {
                        valueString = (valueString == null || valueString.isEmpty()) ?
                            Integer.toString(sortRank) :
                            sortRank+" "+valueString;
                    }
                    // don't use deriveFont() - see #49973 for details
                    label.setFont (new Font (label.getFont ().getName (), Font.BOLD, label.getFont ().getSize ()));

                    if (ascending) {
                        sortIcon = UIManager.getIcon("ETableHeader.ascendingIcon");
                        if (sortIcon == null) {
                            sortIcon = new SortUpIcon();
                        }
                    } else {
                        sortIcon = UIManager.getIcon("ETableHeader.descendingIcon");
                        if (sortIcon == null) {
                            sortIcon = new SortDownIcon();
                        }
                    }
                }
                label.setText(valueString);
                if (sortIcon == null) {
                    label.setIcon(customIcon);
                } else {
                    if (customIcon == null) {
                        label.setIcon(sortIcon);
                    } else {
                        label.setIcon(mergeIcons(customIcon, sortIcon, 16, 0, label));
                    }
                }
            }
            return resUI;
        }
    }
        
    /**
     * An icon pointing down. It is used if the LAF does not supply
     * special icon.
     */
    private static class SortDownIcon implements Icon {

        public SortDownIcon() {
        }

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawLine(x    , y + 2, x + 8, y + 2);
            g.drawLine(x    , y + 2, x + 4, y + 6);
            g.drawLine(x + 8, y + 2, x + 4, y + 6);
        }
    }

    /**
     * An icon pointing up. It is used if the LAF does not supply
     * special icon.
     */
    private static class SortUpIcon implements Icon {

        public SortUpIcon() {
        }

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawLine(x    , y + 6, x + 8, y + 6);
            g.drawLine(x    , y + 6, x + 4, y + 2);
            g.drawLine(x + 8, y + 6, x + 4, y + 2);
        }
    }

    /**
     * Utility method merging 2 icons.
     */
    private static Icon mergeIcons(Icon icon1, Icon icon2, int x, int y, Component c) {
        int w = 0, h = 0;
        if (icon1 != null) {
            w = icon1.getIconWidth();
            h = icon1.getIconHeight();
        }
        if (icon2 != null) {
            w = icon2.getIconWidth()  + x > w ? icon2.getIconWidth()   + x : w;
            h = icon2.getIconHeight() + y > h ? icon2.getIconHeight()  + y : h;
        }
        if (w < 1) w = 16;
        if (h < 1) h = 16;
        
        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment ().
                                          getDefaultScreenDevice ().getDefaultConfiguration ().
                                          getColorModel (java.awt.Transparency.BITMASK);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage (model,
             model.createCompatibleWritableRaster (w, h), model.isAlphaPremultiplied (), null);
        
        java.awt.Graphics g = buffImage.createGraphics ();
        if (icon1 != null) {
            icon1.paintIcon(c, g, 0, 0);
        }
        if (icon2 != null) {
            icon2.paintIcon(c, g, x, y);
        }
        g.dispose();
        
        return new ImageIcon(buffImage);
    }

}
