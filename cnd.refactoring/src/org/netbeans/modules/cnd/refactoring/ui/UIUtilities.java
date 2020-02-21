/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.refactoring.ui;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.cnd.refactoring.support.MemberInfo;


/** Class containing various utility methods and inner classes
 * useful when creating refactoring UI.
 *
 */
public final class UIUtilities {
    // not to be instantiated
    private UIUtilities() {
    }

    /** Returns the same string as passed in or " " if the passed string was an empty string.
     * This method is used as a workaround for issue #58302.
     * @param name Original table column name.
     * @return "Fixed" column name.
     */
    public static String getColumnName(String name) {
        return name == null || name.length() == 0 ? " " : name; // NOI18N
    }
    
    /** Initializes preferred (and eventually maximum) width of a table column based on
     * the size of its header and the estimated longest value.
     * @param table Table to adjust the column width for.
     * @param index Index of the column.
     * @param longValue Estimated long value for the column.
     * @param padding Number of pixes for padding.
     */
    public static void initColumnWidth(JTable table, int index, Object longValue, int padding) {
        TableColumn column = table.getColumnModel().getColumn(index);
        
        // get preferred size of the header
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = headerRenderer.getTableCellRendererComponent(
                table, column.getHeaderValue(), false, false, 0, 0);
        int width = comp.getPreferredSize().width;
        
        // get preferred size of the long value (remeber max of the pref. size for header and long value)
        comp = table.getDefaultRenderer(table.getModel().getColumnClass(index)).getTableCellRendererComponent(
                table, longValue, false, false, 0, index);
        width = Math.max(width, comp.getPreferredSize().width) + 2 * padding;
        
        // set preferred width of the column
        column.setPreferredWidth(width);
        // if the column contains boolean values, the preferred width
        // should also be its max width
        if (longValue instanceof Boolean) {
            column.setMaxWidth(width);
        }
    }

    /** Table cell renderer that renders Java elements (instances of NamedElement and its subtypes).
     * When rendering the elements it displays element's icon (if available) and display text.
     */
    public static class CsmElementTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i); 
            } else {
                setIcon(null);
            }
            return this;
        }
        
        
        /** Can be overriden to return alter the standard display text returned for elements.
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }
    
    /** Table cell renderer that renders Java elements (instances of NamedElement and its subtypes).
     * When rendering the elements it displays element's icon (if available) and display text.
     */
    public static class JavaElementListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, extractText(value), index, isSelected, cellHasFocus);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i); 
            }
            return this;
        }

        
        /** Can be overriden to return alter the standard display text returned for elements.
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }

    /** Table cell renderer for boolean values (a little more advanced that the
     * standard one). Enables hiding the combo box in case the value is <code>null</code>
     * rather than <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>
     * and disables the combo box for read-only cells to give a better visual feedback
     * that the cells cannot be edited.
     */
    public static class BooleanTableCellRenderer extends JCheckBox implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private final JLabel emptyLabel = new JLabel();

	public BooleanTableCellRenderer() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
            emptyLabel.setBorder(noFocusBorder);
            emptyLabel.setOpaque(true);
	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent result;
            if (value == null) {
                result = emptyLabel;
            } else {
                setSelected(((Boolean)value).booleanValue());
                setEnabled(table.getModel().isCellEditable(row, column));
                result = this;
            }

            result.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            result.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            result.setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder); // NOI18N
            
            return result;
        }
    }
}
