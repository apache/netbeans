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

package org.netbeans.lib.profiler.ui.components.table;

import org.netbeans.lib.profiler.ui.UIUtils;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;


/** An abstract superclass for table cell renderers to be used throughout the profiler.
 * It implements alrenating background colors for odd and even rows, and simplifies the writing of
 * concrete renderers by masking Swing's ugly CellRenderer API (by forcing "this" to be returned as the renderer).
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
public abstract class EnhancedTableCellRenderer extends JPanel implements TableCellRendererPersistent {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected boolean supportsFocusBorder;
    private Border originalBorder;
    private Color darkerUnselectedBackground;
    private Color unselectedBackground;
    private Color unselectedForeground;
    private Insets originalBorderInsets;
    private int horizontalAlignment;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a default table cell renderer with LEADING horizontal alignment showing border when focused.
     *  Rendering of focused cell border is disabled by default, to enable it, use setSupportsFocusBorder(true).
     */
    public EnhancedTableCellRenderer() {
        setOpaque(true);
        supportsFocusBorder = false;
        horizontalAlignment = SwingConstants.LEADING;
        unselectedBackground = UIUtils.getProfilerResultsBackground();
        darkerUnselectedBackground = UIUtils.getDarker(unselectedBackground);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setBorder(Border border) {
        super.setBorder(border);
        originalBorder = border;

        if (originalBorder != null) {
            originalBorderInsets = originalBorder.getBorderInsets(this);
        }
    }

    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /** Sets whether or not the renderer supports drawing border around the focused cell */
    public void setSupportsFocusBorder(boolean supportsFocusBorder) {
        this.supportsFocusBorder = supportsFocusBorder;

        if ((supportsFocusBorder) && (originalBorder == null)) {
            setBorder(BorderFactory.createEmptyBorder());
        }
    }

    public boolean getSupportsFocusBorder() {
        return supportsFocusBorder;
    }

    // ----------------------------------------------------------------------------
    // Cell renderer functionality

    /**
     * Returns the default table cell renderer.
     *
     * @param table      the <code>JTable</code>
     * @param value      the value to assign to the cell at
     *                   <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus   true if cell has focus
     * @param row        the row of the cell to render
     * @param column     the column of the cell to render
     * @return the default table cell renderer
     */
    public final Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
        if (supportsFocusBorder) {
            if ((hasFocus) && (isSelected) && (originalBorder != null)) {
                Border focusBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIUtils.getDarkerLine(table
                                                                                                                             .getSelectionBackground(),
                                                                                                                             0.65f)),
                                                                        BorderFactory.createEmptyBorder(originalBorderInsets.top,
                                                                                                        originalBorderInsets.left
                                                                                                        - 1,
                                                                                                        originalBorderInsets.bottom,
                                                                                                        originalBorderInsets.right));
                super.setBorder(focusBorder);
            } else {
                super.setBorder(originalBorder);
            }
        }

        if (isSelected && table.isEnabled()) {
            setRowForeground(table.isFocusOwner() ? table.getSelectionForeground() : UIUtils.getUnfocusedSelectionForeground());
            setRowBackground(table.isFocusOwner() ? table.getSelectionBackground() : UIUtils.getUnfocusedSelectionBackground());
        } else if (!table.isEnabled()) {
            setRowForeground(UIManager.getColor("TextField.inactiveForeground")); // NOI18N
            setRowBackground(UIManager.getColor("TextField.inactiveBackground")); // NOI18N
        } else {
            if ((row & 0x1) == 0) { //even row
                setRowForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
                setRowBackground((darkerUnselectedBackground != null) ? darkerUnselectedBackground
                                                                      : UIUtils.getDarker(table.getBackground()));
            } else {
                setRowForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
                setRowBackground((unselectedBackground != null) ? unselectedBackground : table.getBackground());
            }
        }

        setState(table, value, isSelected, hasFocus, row, column);
        setValue(table, value, row, column);

        return this;
    }

    /**
     * Returns persistent (new) table cell renderer with provided parameters.
     *
     * @param table      the <code>JTable</code>
     * @param value      the value to assign to the cell at
     *                   <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus   true if cell has focus
     * @param row        the row of the cell to render
     * @param column     the column of the cell to render
     * @return the default table cell renderer
     */
    public abstract Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected,
                                                                      boolean hasFocus, int row, int column);

    // ----------------------------------------------------------------------------
    // Private impl
    public static Color getDarker(Color c) {
        return UIUtils.getSafeColor((int) (c.getRed() - 30), (int) (c.getGreen() - 30), (int) (c.getBlue() - 30));
    }

    /**
     * Called each time this renderer is to be used to render a specific row, with the color
     * to be used for painting background of this row. The default implementation sets the
     * background of the panel to this color, so this method does not need to be overridden unless
     * the subclass has any opaque components placed into the panel that should alternate their background
     * on each line and change on selected lines.
     *
     * @param c the color to be used for row background
     */
    protected void setRowBackground(Color c) {
        setBackground(c);
    }

    // ----------------------------------------------------------------------------
    // API for subclasses

    /**
     * Called each time this renderer is to be used to render a specific row, with the color
     * to be used for painting foreground of this row (based on table's getSelectionForeground).
     * The default implementation sets the foreground of the panel to this color.
     * Subclasses should override this to meaningfully change the rendering when the selection changes.
     *
     * @param c the color to be used for row foreground
     */
    protected void setRowForeground(Color c) {
        setForeground(c);
    }

    /**
     * Called each time this renderer is to be used to render a specific value on specified row/column.
     * Subclasses need to implement this method to render the value.
     *
     * @param table the table in which the rendering occurs
     * @param value the value to be rendered
     * @param row the row at which the value is located
     * @param column the column at which the value is located
     */
    protected abstract void setValue(JTable table, Object value, int row, int column);

    /**
     * Called each time this renderer is to be used to render a specific value on specified row/column.
     * Subclasses can override this method to set their states according to provided values.
     *
     * @param table      the <code>JTable</code>
     * @param value      the value to assign to the cell at
     *                   <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus   true if cell has focus
     * @param row        the row of the cell to render
     * @param column     the column of the cell to render
     */
    protected void setState(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    }
}
